package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.conf.ConfBuilder;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerLocation;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.meta.Metadata.VarDEPLOYTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RedistStateInfo;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.SourceStorage;

public class ActionRedist extends ActionBase {

	DistStorage dist;
	LocalFolder templateFolder;

	public ActionRedist( ActionBase action , String stream , DistStorage dist , LocalFolder templateFolder ) {
		super( action , stream );
		this.dist = dist;
		this.templateFolder = templateFolder;
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		logAction( "execute dc=" + meta.dc.NAME + ", releasedir=" + dist.RELEASEDIR + ", servers={" + scope.getScopeInfo( this ) + "} ..." );

		// if configuration deployment requested - validate environment data
		if( context.CTX_CONFDEPLOY ) {
			ActionConfCheck check = new ActionConfCheck( this , null );
			if( !check.runAll( scope ) ) {
				logAction( "configuration check failed: invalid environment data" );
				super.setFailed();
				return;
			}
		}
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		if( server.isDeployPossible( this ) )
			executeServer( target );
		
		return( true );
	}

	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		MetaEnvServerLocation[] F_ENV_LOCATIONS_BINARY = new MetaEnvServerLocation[0];
		if( context.CTX_DEPLOYBINARY )
			F_ENV_LOCATIONS_BINARY = server.getLocations( this , true , false );
		
		MetaEnvServerLocation[] F_ENV_LOCATIONS_CONFIG = new MetaEnvServerLocation[0];
		if( context.CTX_CONFDEPLOY )
			F_ENV_LOCATIONS_CONFIG = server.getLocations( this , false , true );
		
		if( F_ENV_LOCATIONS_BINARY.length == 0 && F_ENV_LOCATIONS_CONFIG.length == 0 ) {
			debug( "server=$P_SERVER - no locations. Skipped." );
			return;
		}

		log( "============================================ execute server=" + server.NAME + ", type=" + Common.getEnumLower( server.TYPE ) + " ..." );

		// export configuration templates
		LocalFolder liveFolder = prepareServerConfiguration( target , F_ENV_LOCATIONS_CONFIG );
		
		// cluster hot deploy - redist hotdeploy components to admin server only
		boolean F_CLUSTER_MODE = false;
		if( server.hotdeployServer != null && target.itemFull ) {
			F_CLUSTER_MODE = true;
			MetaEnvServer adminServer = server.hotdeployServer;
			MetaEnvServerNode adminNode = adminServer.getPrimaryNode( this );
			executeNode( server , adminServer , adminNode , F_CLUSTER_MODE , true , F_ENV_LOCATIONS_BINARY , F_ENV_LOCATIONS_CONFIG , liveFolder );
		}

		// iterate by nodes
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			log( "execute server=" + server.NAME + " node=" + node.POS + " ..." );

			// deploy both binaries and configs to each node
			executeNode( server , server , node , F_CLUSTER_MODE , false , F_ENV_LOCATIONS_BINARY , F_ENV_LOCATIONS_CONFIG , liveFolder );
		}

		if( context.CTX_DEPLOYBINARY && server.staticServer != null )
			exitNotImplemented();
	}

	private LocalFolder prepareServerConfiguration( ActionScopeTarget target, MetaEnvServerLocation[] locations ) throws Exception {
		MetaEnvServer server = target.envServer;
		if( !context.CTX_CONFDEPLOY )
			return( null );
		
		MetaDistrConfItem[] confItems = dist.getLocationConfItems( this , locations );
		if( confItems.length == 0 ) {
			trace( "no configuration in release found for server. Skipped" );
			return( null );
		}
		
		String PATH = "configuration";
		PATH = Common.getPath( PATH , server.NAME );
		LocalFolder confFolder = artefactory.getWorkFolder( this , PATH );
		LocalFolder templateFolder = confFolder.getSubFolder( this , "templates" );
		templateFolder.recreateThis( this );
		
		// copy from release templates
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		for( MetaDistrConfItem confItem : confItems ) {
			MetaReleaseTarget confTarget = dist.info.getConfComponent( this , confItem.KEY );
			if( !dist.copyDistConfToFolder( this , confTarget , templateFolder ) )
				debug( "missing release conf=" + confItem.KEY );
		}
		
		// configure 
		LocalFolder liveAllFolder = confFolder.getSubFolder( this , "live" );
		liveAllFolder.recreateThis( this );
		
		ConfBuilder builder = new ConfBuilder( this );
		for( ActionScopeTargetItem targetItem : target.getItems( this ) ) {
			MetaEnvServerNode node = targetItem.envServerNode;
			for( MetaDistrConfItem confItem : confItems ) {
				LocalFolder confTemplateFolder = templateFolder.getSubFolder( this , confItem.KEY );
				
				String name = sourceStorage.getConfItemLiveName( this , node , confItem );
				LocalFolder live = liveAllFolder.getSubFolder( this , name );
				confFolder.recreateThis( this );
				
				builder.configureComponent( confTemplateFolder , live , confItem , server , node );
			}
		}
		
		return( liveAllFolder );
	}
	
	private void executeNode( MetaEnvServer deployServer , MetaEnvServer server , MetaEnvServerNode node , boolean clusterMode , boolean admin , MetaEnvServerLocation[] F_ENV_LOCATIONS_BINARY , MetaEnvServerLocation[] F_ENV_LOCATIONS_CONFIG , LocalFolder liveFolder ) throws Exception {
		if( context.CTX_DEPLOYBINARY ) {
			if( F_ENV_LOCATIONS_BINARY.length == 0 ) {
				trace( "server=" + server.NAME + ", node=" + node.POS + " - ignore binary deploy due to no locations" );
				return;
			}
			
			executeNodeBinary( server , node , clusterMode , admin , F_ENV_LOCATIONS_BINARY );
		}
		else
			trace( "server=" + server.NAME + ", node=" + node.POS + " - ignore binary deploy due to options" );
		
		if( context.CTX_CONFDEPLOY ) {
			if( F_ENV_LOCATIONS_CONFIG.length == 0 ) {
				trace( "server=" + server.NAME + ", node=" + node.POS + " - ignore config deploy due to no locations" );
				return;
			}
			
			executeNodeConfig( server , node , clusterMode , admin , F_ENV_LOCATIONS_CONFIG , liveFolder );
		}
		
		if( context.CTX_BACKUP )
			executeNodeBackup( server , node );
	}

	private void executeNodeConfig( MetaEnvServer server , MetaEnvServerNode node , boolean clusterMode , boolean admin , MetaEnvServerLocation[] locations , LocalFolder liveFolder ) throws Exception {
		log( "redist configuration to server=" + server.NAME + " node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
		
		// by deployment location and conf component
		for( MetaEnvServerLocation location : locations ) 
			for( MetaDistrConfItem conf : location.confItems.values() ) 
				executeNodeConfigComp( server , node , clusterMode , admin , location , conf , liveFolder );
	}
	
	private void executeNodeBinary( MetaEnvServer server , MetaEnvServerNode node , boolean clusterMode , boolean admin , MetaEnvServerLocation[] locations ) throws Exception {
		log( "redist binaries to server=" + server.NAME + " node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
		
		// by deployment location
		for( MetaEnvServerLocation location : locations ) 
			executeNodeBinaryLocation( server , node , clusterMode , admin , location );
	}

	private VarCONTENTTYPE getDirType( boolean clusterMode , VarDEPLOYTYPE deploytype , boolean admin , VarCONTENTTYPE itemValue ) {
		// deploy to admin node only hotdeploy and in cluster mode
		if( ( clusterMode == false || deploytype != VarDEPLOYTYPE.HOTDEPLOY ) && admin == true )
			return( null );

		// in cluster mode deploy hotdeploy to admin node only, not to other nodes
		if( clusterMode == true && deploytype == VarDEPLOYTYPE.HOTDEPLOY && admin == false )
			return( null );

		// other case
		return( itemValue );
	}

	private void executeNodeBinaryLocation( MetaEnvServer server , MetaEnvServerNode node , boolean clusterMode , boolean admin , MetaEnvServerLocation location ) throws Exception {
		// get components by location
		if( location.binaryItems.size() == 0 ) {
			debug( "redist location=$F_LOCATIONFINAL no components to deploy, skipped." );
			return;
		}

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		VarCONTENTTYPE C_REDIST_DEFAULT = location.getContentType( this , true );
		VarCONTENTTYPE C_REDIST_DIRTYPE = getDirType( clusterMode , location.DEPLOYTYPE , admin , C_REDIST_DEFAULT );
		if( C_REDIST_DIRTYPE.equals( "none" ) ) {
			debug( "ignore deploy binary location=" + location.DEPLOYPATH + " clustermode=" + clusterMode + " node=" + node.POS + " deploytype=" + Common.getEnumLower( location.DEPLOYTYPE ) );
			return;
		}

		// collect distribution items for all components
		if( !location.hasBinaryItems( this ) ) {
			debug( "redist location=" + location.DEPLOYPATH + " no items to deploy, skipped." );
			return;
		}

		String items = Common.getSortedKeySet( location.binaryItems );
		log( "redist location=" + location.DEPLOYPATH + " deploytype=" + Common.getEnumLower( location.DEPLOYTYPE ) +
				" items=" + items + " contenttype=" + Common.getEnumLower( C_REDIST_DIRTYPE ) + " ..." );

		redist.createLocation( this , dist.RELEASEDIR , location , C_REDIST_DIRTYPE );

		debug( "transfer items - " + items + " ..." );
		transferFileSet( server , node , redist , location , items );
	}

	private void transferFileSet( MetaEnvServer server , MetaEnvServerNode node , RedistStorage redist , MetaEnvServerLocation location , String items ) throws Exception {
		// ensure redist created
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( this , true );
		redist.createLocation( this , dist.RELEASEDIR , location , CONTENTTYPE );
		RedistStateInfo stateInfo = redist.getStateInfo( this , location.DEPLOYPATH , CONTENTTYPE );

		debug( node.HOSTLOGIN + ": redist content=" + Common.getEnumLower( CONTENTTYPE ) + ": items - " + items + " ..." );
		for( String key : Common.getSortedKeys( location.binaryItems ) ) {
			MetaDistrBinaryItem binaryItem = location.binaryItems.get( key );
			String deployBaseName = location.getDeployName( this , key );
			transferFile( server , node , redist , location , binaryItem , stateInfo , deployBaseName );
		}
	}

	private boolean transferFile( MetaEnvServer server , MetaEnvServerNode node , RedistStorage redist , MetaEnvServerLocation location , MetaDistrBinaryItem binaryItem , RedistStateInfo stateInfo , String deployBaseName ) throws Exception {
		if( !dist.checkIfReleaseItem( this , binaryItem ) ) {
			trace( "binary item=" + binaryItem.KEY + " is not in release. Skipped." );
			return( false );
		}
		
		String fileName = dist.getBinaryDistItemFile( this , binaryItem );
		if( fileName.isEmpty() ) {
			trace( "binary item=" + binaryItem.KEY + " is not found. Skipped." );
			return( false );
		}

		debug( "distributive item=" + binaryItem.KEY + " found in distributive, file=" + fileName );
		redist.copyReleaseFile( this , binaryItem , dist , location , fileName , deployBaseName );
		return( true );
	}

	private boolean executeNodeConfigComp( MetaEnvServer server , MetaEnvServerNode node , boolean clusterMode , boolean admin , MetaEnvServerLocation location , MetaDistrConfItem confItem , LocalFolder liveFolder ) throws Exception {
		MetaReleaseTarget target = dist.info.findConfComponent( this , confItem.KEY );
		
		// not in release
		if( target == null ) {
			trace( "non-release component=" + confItem.KEY );
			return( false );
		}
		
		boolean F_PARTIAL = target.ALL; 

		debug( "redist configuraton component=" + confItem.KEY + " (partial=" + F_PARTIAL + ") ..." );
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		String name = sourceStorage.getConfItemLiveName( this , node , confItem );
		LocalFolder confFolder = liveFolder.getSubFolder( this , name );
		
		// not in distributive
		if( confFolder.checkExists( this ) ) {
			trace( "missing release component=" + confItem.KEY );
			return( false );
		}
		
		// archive
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		String fileBaseName = redist.getConfigArchiveName( this , confItem , target.ALL );
		String fileName = "node" + node.POS + "-" + fileBaseName;
		String filePath = liveFolder.getFilePath( this , fileName );
		confFolder.createTarGzFromContent( this , filePath , "*" , "" );
		
		redist.copyReleaseFile( this , confItem , dist , location , liveFolder , fileName , fileBaseName );
		return( true );
	}
	
	private void executeNodeBackup( MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		ServerDeployment deployment = redist.getDeployment( this , dist.RELEASEDIR );
		
		// backup binary items
		for( VarCONTENTTYPE content : VarCONTENTTYPE.values() ) {
			for( String location : deployment.getLocations( this , content , true ) ) {
				for( String file : deployment.getLocationFiles( this , content , true , location ) )
					redist.backupRedistItem( this , dist.RELEASEDIR  , content , location , file );
			}
		}
	}
	
}
