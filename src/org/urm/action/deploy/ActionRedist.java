package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.storage.FileInfo;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStateInfo;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerLocation;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.Meta.VarCONTENTTYPE;
import org.urm.meta.product.Meta.VarDISTITEMORIGIN;

public class ActionRedist extends ActionBase {

	Dist dist;
	LocalFolder liveEnvFolder;

	public ActionRedist( ActionBase action , String stream , Dist dist , LocalFolder liveEnvFolder ) {
		super( action , stream );
		this.dist = dist;
		this.liveEnvFolder = liveEnvFolder;
	}

	@Override protected void runBefore( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		infoAction( "execute sg=" + set.sg.NAME + ", releasedir=" + dist.RELEASEDIR + ", servers={" + set.getScopeInfo( this ) + "} ..." );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		if( server.isDeployPossible() )
			executeServer( target );
		
		return( SCOPESTATE.RunSuccess );
	}

	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		MetaEnvServerLocation[] F_ENV_LOCATIONS_BINARY = new MetaEnvServerLocation[0];
		if( context.CTX_DEPLOYBINARY )
			F_ENV_LOCATIONS_BINARY = server.getLocations( this , true , false );
		
		MetaEnvServerLocation[] F_ENV_LOCATIONS_CONFIG = new MetaEnvServerLocation[0];
		LocalFolder liveServerFolder = null;
		if( liveEnvFolder != null & context.CTX_CONFDEPLOY ) {
			F_ENV_LOCATIONS_CONFIG = server.getLocations( this , false , true );
			liveServerFolder = liveEnvFolder.getSubFolder( this , server.NAME );
		}
		
		if( F_ENV_LOCATIONS_BINARY.length == 0 && F_ENV_LOCATIONS_CONFIG.length == 0 ) {
			debug( "server=" + server.NAME + " - no locations. Skipped." );
			return;
		}

		info( "============================================ execute server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );

		// iterate by nodes
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "execute server=" + server.NAME + " node=" + node.POS + " ..." );

			// deploy both binaries and configs to each node
			executeNode( server , node , F_ENV_LOCATIONS_BINARY , F_ENV_LOCATIONS_CONFIG , liveServerFolder );
		}

		if( context.CTX_DEPLOYBINARY && server.staticServer != null )
			exitNotImplemented();
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation[] F_ENV_LOCATIONS_BINARY , MetaEnvServerLocation[] F_ENV_LOCATIONS_CONFIG , LocalFolder liveFolder ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		
		if( context.CTX_DEPLOYBINARY ) {
			if( F_ENV_LOCATIONS_BINARY.length == 0 )
				trace( "server=" + server.NAME + ", node=" + node.POS + " - ignore binary deploy due to no locations" );
			else
				executeNodeBinary( server , node , F_ENV_LOCATIONS_BINARY );
		}
		else
			trace( "server=" + server.NAME + ", node=" + node.POS + " - ignore binary deploy due to options" );
		
		if( liveFolder != null && context.CTX_CONFDEPLOY ) {
			if( F_ENV_LOCATIONS_CONFIG.length == 0 )
				trace( "server=" + server.NAME + ", node=" + node.POS + " - ignore config deploy due to no locations" );
			else
				executeNodeConfig( server , node , F_ENV_LOCATIONS_CONFIG , liveFolder );
		}

		if( context.CTX_BACKUP )
			executeNodeBackup( server , node );
	}

	private void executeNodeConfig( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation[] locations , LocalFolder liveFolder ) throws Exception {
		info( "redist configuration to server=" + server.NAME + " node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
		
		// by deployment location and conf component
		MetaDistr distr = server.meta.getDistr( this );
		for( MetaEnvServerLocation location : locations ) { 
			String[] items = location.getNodeConfItems( this , node );
			if( items.length == 0 ) {
				debug( "redist location=$F_LOCATIONFINAL no configuration to deploy, skipped." );
				continue;
			}
			
			for( String item : items ) {
				MetaDistrConfItem conf = distr.getConfItem( this , item );
				executeNodeConfigComp( server , node , location , conf , liveFolder );
			}
		}
	}
	
	private void executeNodeBinary( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation[] locations ) throws Exception {
		info( "redist binaries to server=" + server.NAME + " node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
		
		// by deployment location
		for( MetaEnvServerLocation location : locations ) 
			executeNodeBinaryLocation( server , node , location );
	}

	private void executeNodeBinaryLocation( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location ) throws Exception {
		// get components by location
		String[] items = location.getNodeBinaryItems( this , node );
		if( items.length == 0 ) {
			debug( "redist location=$F_LOCATIONFINAL no binaries to deploy, skipped." );
			return;
		}

		// collect distribution items for all components
		if( !location.hasBinaryItems( this ) ) {
			debug( "redist location=" + location.DEPLOYPATH + " no items to deploy, skipped." );
			return;
		}

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		VarCONTENTTYPE C_REDIST_DIRTYPE = location.getContentType( this , true );

		info( "redist location=" + location.DEPLOYPATH + " deploytype=" + Common.getEnumLower( location.DEPLOYTYPE ) +
				" items=" + Common.getListSet( items ) + " contenttype=" + Common.getEnumLower( C_REDIST_DIRTYPE ) + " ..." );

		VersionInfo version = VersionInfo.getDistVersion( this , dist ); 
		redist.createLocation( this , version , location , C_REDIST_DIRTYPE );

		debug( "transfer items - " + Common.getListSet( items ) + " ..." );
		transferFileSet( server , node , redist , location , items );
	}

	private void transferFileSet( MetaEnvServer server , MetaEnvServerNode node , RedistStorage redist , MetaEnvServerLocation location , String[] items ) throws Exception {
		// ensure redist created
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( this , true );
		RedistStateInfo stateInfo = redist.getStateInfo( this , location.DEPLOYPATH , CONTENTTYPE );

		debug( node.HOSTLOGIN + ": redist content=" + Common.getEnumLower( CONTENTTYPE ) + ": items - " + Common.getListSet( items ) + " ..." );
		MetaDistr distr = server.meta.getDistr( this );
		for( String key : items ) {
			MetaDistrBinaryItem binaryItem = distr.getBinaryItem( this , key );
			String deployBaseName = location.getDeployName( this , key );
			transferFile( server , node , redist , location , binaryItem , stateInfo , deployBaseName );
		}
	}

	private boolean transferFile( MetaEnvServer server , MetaEnvServerNode node , RedistStorage redist , MetaEnvServerLocation location , MetaDistrBinaryItem binaryItem , RedistStateInfo stateInfo , String deployBaseName ) throws Exception {
		if( !dist.checkIfReleaseItem( this , binaryItem ) ) {
			trace( "binary item=" + binaryItem.KEY + " is not in release. Skipped." );
			return( false );
		}
		
		if( binaryItem.distItemOrigin == VarDISTITEMORIGIN.DISTITEM ) {
			String fileName = dist.getBinaryDistItemFile( this , binaryItem.srcDistItem );
			if( fileName.isEmpty() ) {
				trace( "source of binary item=" + binaryItem.KEY + " is not found. Skipped." );
				return( false );
			}
			
			debug( "source of distributive item=" + binaryItem.KEY + " found in distributive, file=" + fileName );
			String fileExtracted = extractEmbeddedFile( binaryItem , fileName );
			VersionInfo version = VersionInfo.getDistVersion( this , dist ); 
			return( redist.copyReleaseFile( this , binaryItem , location , fileExtracted , deployBaseName , version , stateInfo ) );
		}
		else if( binaryItem.distItemOrigin == VarDISTITEMORIGIN.BUILD || binaryItem.distItemOrigin == VarDISTITEMORIGIN.MANUAL ) {
			String fileName = dist.getBinaryDistItemFile( this , binaryItem );
			if( fileName.isEmpty() ) {
				trace( "binary item=" + binaryItem.KEY + " is not found. Skipped." );
				return( false );
			}
	
			debug( "distributive item=" + binaryItem.KEY + " found in distributive, file=" + fileName );
			return( redist.copyReleaseFile( this , binaryItem , dist , location , fileName , deployBaseName , stateInfo ) );
		}
		else
			exitUnexpectedState();
		
		return( false );
	}

	private String extractEmbeddedFile( MetaDistrBinaryItem binaryItem , String fileName ) throws Exception {
		LocalFolder tmpFolder = artefactory.getWorkFolder( this , "extract" );
		tmpFolder.recreateThis( this );
		
		String fileExtracted = dist.extractEmbeddedItemToFolder( this , tmpFolder , binaryItem , fileName );
		String pathExtracted = tmpFolder.getFilePath( this , fileExtracted );
		debug( "file extracted path=" + pathExtracted );
		return( pathExtracted );
	}
	
	private boolean executeNodeConfigComp( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location , MetaDistrConfItem confItem , LocalFolder liveFolder ) throws Exception {
		ReleaseTarget target = dist.release.findConfComponent( this , confItem.KEY );
		
		// not in release
		if( target == null ) {
			trace( "non-release component=" + confItem.KEY );
			return( false );
		}
		
		boolean F_PARTIAL = ( target.ALL )? false : true; 

		debug( "redist configuraton component=" + confItem.KEY + " (partial=" + F_PARTIAL + ") ..." );
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , server.meta );
		String name = sourceStorage.getConfItemLiveName( this , node , confItem );
		LocalFolder confFolder = liveFolder.getSubFolder( this , name );
		
		// not in distributive
		if( !confFolder.checkExists( this ) ) {
			trace( "missing release component=" + confItem.KEY );
			return( false );
		}
		
		// archive
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		String fileBaseName = FileInfo.getFileName( this , confItem );
		String fileName = "node" + node.POS + "-" + fileBaseName;
		String filePath = liveFolder.getFilePath( this , fileName );
		confFolder.createTarFromContent( this , filePath , "*" , "" );
		
		redist.copyReleaseFile( this , confItem , dist , location , liveFolder , fileName , F_PARTIAL );
		return( true );
	}
	
	private void executeNodeBackup( MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		debug( node.HOSTLOGIN + ": save backup ..." );
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		VersionInfo version = VersionInfo.getDistVersion( this , dist ); 
		ServerDeployment deployment = redist.getDeployment( this , version );
		
		// backup binary items
		for( VarCONTENTTYPE content : VarCONTENTTYPE.values() ) {
			for( String location : deployment.getLocations( this , content , true ) ) {
				RedistStateInfo rinfo = new RedistStateInfo( server.meta );
				RedistStateInfo sinfo = new RedistStateInfo( server.meta );
				String RELEASEDIR = redist.getPathRedistLocation( this , version , location , content , true );
				rinfo.gather( this , node , content , RELEASEDIR );
				String STATEDIR = redist.getPathStateLocation( this , location , content );
				sinfo.gather( this , node , content , STATEDIR );
				
				for( String key : rinfo.getKeys( this ) )
					redist.backupRedistItem( this , version  , content , location , rinfo.getVerData( this , key ) , sinfo.findVerData( this , key ) );
			}
		}
	}
	
}
