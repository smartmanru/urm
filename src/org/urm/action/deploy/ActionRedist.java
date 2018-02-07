package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumItemOriginType;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileInfo;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStateInfo;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerLocation;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.Types.*;

public class ActionRedist extends ActionBase {

	Dist dist;
	LocalFolder liveEnvFolder;

	public ActionRedist( ActionBase action , String stream , Dist dist , LocalFolder liveEnvFolder ) {
		super( action , stream , "Redist to environment, release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.liveEnvFolder = liveEnvFolder;
	}

	@Override protected void runBefore( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		infoAction( "execute sg=" + set.sg.NAME + ", releasedir=" + dist.RELEASEDIR + ", servers={" + set.getScopeInfo( this ) + "} ..." );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
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

		info( "============================================ execute server=" + server.NAME + ", type=" + server.getServerTypeName() + " ..." );

		// iterate by nodes
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "execute server=" + server.NAME + " node=" + node.POS + " ..." );

			// deploy both binaries and configs to each node
			executeNode( server , node , F_ENV_LOCATIONS_BINARY , F_ENV_LOCATIONS_CONFIG , liveServerFolder );
		}

		MetaEnvServer staticServer = server.getStaticServer();
		if( context.CTX_DEPLOYBINARY && staticServer != null )
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
		HostAccount hostAccount = node.getHostAccount();
		info( "redist configuration to server=" + server.NAME + " node=" + node.POS + ", account=" + hostAccount.getFinalAccount() + " ..." );
		
		// by deployment location and conf component
		MetaDistr distr = server.meta.getDistr();
		for( MetaEnvServerLocation location : locations ) { 
			String[] items = location.getNodeConfItems( node );
			if( items.length == 0 ) {
				debug( "redist location=$F_LOCATIONFINAL no configuration to deploy, skipped." );
				continue;
			}
			
			for( String item : items ) {
				MetaDistrConfItem conf = distr.getConfItem( item );
				executeNodeConfigComp( server , node , location , conf , liveFolder );
			}
		}
	}
	
	private void executeNodeBinary( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation[] locations ) throws Exception {
		HostAccount hostAccount = node.getHostAccount();
		info( "redist binaries to server=" + server.NAME + " node=" + node.POS + ", account=" + hostAccount.getFinalAccount() + " ..." );
		
		// by deployment location
		for( MetaEnvServerLocation location : locations ) 
			executeNodeBinaryLocation( server , node , location );
	}

	private void executeNodeBinaryLocation( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location ) throws Exception {
		// get components by location
		String[] items = location.getNodeBinaryItems( node );
		if( items.length == 0 ) {
			debug( "redist location=$F_LOCATIONFINAL no binaries to deploy, skipped." );
			return;
		}

		// collect distribution items for all components
		if( !location.hasBinaryItems() ) {
			debug( "redist location=" + location.DEPLOYPATH + " no items to deploy, skipped." );
			return;
		}

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		EnumContentType C_REDIST_DIRTYPE = location.getContentType( true );

		info( "redist location=" + location.DEPLOYPATH + " deploytype=" + Common.getEnumLower( location.DEPLOYTYPE ) +
				" items=" + Common.getListSet( items ) + " contenttype=" + Common.getEnumLower( C_REDIST_DIRTYPE ) + " ..." );

		VersionInfo version = VersionInfo.getDistVersion( dist ); 
		redist.createLocation( this , version , location , C_REDIST_DIRTYPE );

		debug( "transfer items - " + Common.getListSet( items ) + " ..." );
		transferFileSet( server , node , redist , location , items );
	}

	private void transferFileSet( MetaEnvServer server , MetaEnvServerNode node , RedistStorage redist , MetaEnvServerLocation location , String[] items ) throws Exception {
		// ensure redist created
		EnumContentType CONTENTTYPE = location.getContentType( true );
		RedistStateInfo stateInfo = redist.getStateInfo( this , location.DEPLOYPATH , CONTENTTYPE );

		HostAccount hostAccount = node.getHostAccount();
		debug( hostAccount.getFinalAccount() + ": redist content=" + Common.getEnumLower( CONTENTTYPE ) + ": items - " + Common.getListSet( items ) + " ..." );
		MetaDistr distr = server.meta.getDistr();
		for( String key : items ) {
			MetaDistrBinaryItem binaryItem = distr.getBinaryItem( key );
			String deployBaseName = location.getDeployName( key );
			transferFile( server , node , redist , location , binaryItem , stateInfo , deployBaseName );
		}
	}

	private boolean transferFile( MetaEnvServer server , MetaEnvServerNode node , RedistStorage redist , MetaEnvServerLocation location , MetaDistrBinaryItem binaryItem , RedistStateInfo stateInfo , String deployBaseName ) throws Exception {
		if( !dist.checkIfReleaseItem( this , binaryItem ) ) {
			trace( "binary item=" + binaryItem.NAME + " is not in release. Skipped." );
			return( false );
		}
		
		if( binaryItem.ITEMORIGIN_TYPE == DBEnumItemOriginType.DERIVED ) {
			String fileName = dist.getBinaryDistItemFile( this , binaryItem.srcDistItem );
			if( fileName.isEmpty() ) {
				trace( "source of binary item=" + binaryItem.NAME + " is not found. Skipped." );
				return( false );
			}
			
			debug( "source of distributive item=" + binaryItem.NAME + " found in distributive, file=" + fileName );
			String fileExtracted = extractEmbeddedFile( binaryItem , fileName );
			VersionInfo version = VersionInfo.getDistVersion( dist ); 
			return( redist.copyReleaseFile( this , binaryItem , location , fileExtracted , deployBaseName , version , stateInfo ) );
		}
		else 
		if( binaryItem.ITEMORIGIN_TYPE == DBEnumItemOriginType.BUILD || binaryItem.ITEMORIGIN_TYPE == DBEnumItemOriginType.MANUAL || binaryItem.ITEMORIGIN_TYPE == DBEnumItemOriginType.DERIVED ) {
			String fileName = dist.getBinaryDistItemFile( this , binaryItem );
			if( fileName.isEmpty() ) {
				trace( "binary item=" + binaryItem.NAME + " is not found. Skipped." );
				return( false );
			}
	
			debug( "distributive item=" + binaryItem.NAME + " found in distributive, file=" + fileName );
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
		ReleaseTarget target = dist.release.findConfComponent( this , confItem.NAME );
		
		// not in release
		if( target == null ) {
			trace( "non-release component=" + confItem.NAME );
			return( false );
		}
		
		boolean F_PARTIAL = ( target.ALL )? false : true; 

		debug( "redist configuraton component=" + confItem.NAME + " (partial=" + F_PARTIAL + ") ..." );
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , server.meta );
		String name = sourceStorage.getConfItemLiveName( this , node , confItem );
		LocalFolder confFolder = liveFolder.getSubFolder( this , name );
		
		// not in distributive
		if( !confFolder.checkExists( this ) ) {
			trace( "missing release component=" + confItem.NAME );
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
		HostAccount hostAccount = node.getHostAccount();
		debug( hostAccount.getFinalAccount() + ": save backup ..." );
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		VersionInfo version = VersionInfo.getDistVersion( dist ); 
		ServerDeployment deployment = redist.getDeployment( this , version );
		
		// backup binary items
		for( EnumContentType content : EnumContentType.values() ) {
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
