package org.urm.action.deploy;

import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.conf.ConfDiffSet;
import org.urm.action.database.DatabaseClient;
import org.urm.action.database.DatabaseRegistry;
import org.urm.action.database.DatabaseRegistryRelease;
import org.urm.action.database.DatabaseRegistryRelease.RELEASE_STATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistItemInfo;
import org.urm.engine.storage.FileInfo;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerLocation;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionVerifyDeploy extends ActionBase {

	Dist dist;
	LocalFolder tobeFolder;
	LocalFolder asisFolder;
	LocalFolder tobeConfigFolder;
	LocalFolder asisConfigFolder;
	LocalFolder tobeBinaryFolder;
	LocalFolder asisBinaryFolder;
	ActionConfigure configure;
	boolean verifyOk;

	static String MD5FILE = "md5sum.txt";
	
	public ActionVerifyDeploy( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		tobeFolder = artefactory.getWorkFolder( this , "tobe" );
		tobeConfigFolder = tobeFolder.getSubFolder( this , "config" );
		tobeConfigFolder.ensureExists( this );
		tobeBinaryFolder = tobeFolder.getSubFolder( this , "binary" );
		tobeBinaryFolder.ensureExists( this );
		
		if( dist.prod )
			configure = new ActionConfigure( this , null , tobeConfigFolder );
		else
			configure = new ActionConfigure( this , null , dist , tobeConfigFolder );
		configure.context.CTX_HIDDEN = true;
		if( !configure.runAll( scope ) )
			exit0( _Error.UnablePrepareConfiguration0 , "unable to prepare configuration files for comparison" );
		
		asisFolder = artefactory.getWorkFolder( this , "asis" );
		asisConfigFolder = asisFolder.getSubFolder( this , "config" );
		asisConfigFolder.ensureExists( this );
		asisBinaryFolder = asisFolder.getSubFolder( this , "binary" );
		asisBinaryFolder.ensureExists( this );
		verifyOk = true;
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		if( super.isFailed() )
			error( "errors checking environment" );
		else {
			if( verifyOk )
				info( "environment is exactly matched" );
			else
				error( "environment state differs from expected" );
		}
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		// ignore database and unreachable
		MetaEnvServer server = target.envServer;
		if( !server.isDeployPossible() ) {
			trace( "ignore due to server empty deployment" );
			return( SCOPESTATE.NotRun );
		}

		if( target.getItems( this ).size() == 0 ) {
			trace( "no nodes to verify. Skipped." );
			return( SCOPESTATE.NotRun );
		}

		executeServer( target );
		return( SCOPESTATE.RunSuccess );
	}
	
	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		
		info( "============================================ execute server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );

		if( server.isDatabase() )
			executeServerDatabase( server );
		else
			executeServerApp( target , server );
	}

	private void executeServerDatabase( MetaEnvServer server ) throws Exception {
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit1( _Error.UnableConnectServer1 , "unable to connect to server=" + server.NAME , server.NAME );

		boolean verifyServer = true; 
		for( String version : dist.release.getApplyVersions( this ) ) {
			DatabaseRegistry registry = DatabaseRegistry.getRegistry( this , client );
			DatabaseRegistryRelease release = registry.getReleaseInfo( this , version );
			
			if( release.state == RELEASE_STATE.FINISHED )
				info( version + ": release has been successfully applied to database" );
			else {
				verifyServer = false;
				if( release.state == RELEASE_STATE.UNKNOWN )
					info( version + ": nothing has been applied to database" );
				else
					info( version + ": release has not been completed, state=" + Common.getEnumLower( release.state ) );
			}
		}

		if( verifyServer )
			info( "server is exactly matched" );
		else {
			verifyOk = false;
			error( "server state differs from expected" );
		}
	}
	
	private void executeServerApp( ActionScopeTarget target , MetaEnvServer server ) throws Exception {
		MetaEnvServerLocation[] F_ENV_LOCATIONS_BINARY = new MetaEnvServerLocation[0];
		if( context.CTX_DEPLOYBINARY )
			F_ENV_LOCATIONS_BINARY = server.getLocations( this , true , false );
		
		MetaEnvServerLocation[] F_ENV_LOCATIONS_CONFIG = new MetaEnvServerLocation[0];
		if( context.CTX_CONFDEPLOY )
			F_ENV_LOCATIONS_CONFIG = server.getLocations( this , false , true );
		
		if( F_ENV_LOCATIONS_BINARY.length == 0 && F_ENV_LOCATIONS_CONFIG.length == 0 ) {
			debug( "server=" + server.NAME + " - no locations. Skipped." );
			return;
		}

		// iterate by nodes
		LocalFolder tobeConfigServerFolder = configure.getLiveFolder( server );
		LocalFolder asisConfigServerFolder = asisConfigFolder.getSubFolder( this , server.NAME );
		LocalFolder tobeBinaryServerFolder = tobeBinaryFolder.getSubFolder( this , server.NAME );
		LocalFolder asisBinaryServerFolder = asisBinaryFolder.getSubFolder( this , server.NAME );
		tobeConfigServerFolder.ensureExists( this );
		asisConfigServerFolder.ensureExists( this );

		boolean verifyServer = true;
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "execute server=" + server.NAME + " node=" + node.POS + " ..." );

			// verify configs to each node
			if( !executeNode( server , node , F_ENV_LOCATIONS_CONFIG , F_ENV_LOCATIONS_BINARY , tobeConfigServerFolder , asisConfigServerFolder , tobeBinaryServerFolder , asisBinaryServerFolder ) )
				verifyServer = false;
		}
		
		if( verifyServer )
			info( "server is exactly matched" );
		else {
			verifyOk = false;
			error( "server state differs from expected" );
		}
	}

	private boolean executeNode( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation[] confLocations , MetaEnvServerLocation[] binaryLocations , LocalFolder tobeConfigServerFolder , LocalFolder asisConfigServerFolder , LocalFolder tobeBinaryServerFolder , LocalFolder asisBinaryServerFolder ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		
		boolean verifyNode = true;
		
		// binaries
		MetaDistr distr = dist.meta.getDistr( this );
		info( "verify binaries ..." );
		for( MetaEnvServerLocation location : binaryLocations ) {
			String[] items = location.getNodeBinaryItems( this , node );
			for( String item : items ) {
				MetaDistrBinaryItem binaryItem = distr.getBinaryItem( this , item );
				if( !executeNodeBinary( server , node , location , binaryItem , tobeBinaryServerFolder , asisBinaryServerFolder ) )
					verifyNode = false;
			}
		}
	
		// configuration
		info( "verify configuration ..." );
		for( MetaEnvServerLocation location : confLocations ) {
			String[] items = location.getNodeConfItems( this , node );
			for( String item : items ) {
				MetaDistrConfItem confItem = distr.getConfItem( this , item );
				executeNodeConf( server , node , location , confItem , asisConfigServerFolder );
			}
		}
			
		// compare configuration tobe and as is
		if( confLocations.length > 0 ) {
			String nodePrefix = "node" + node.POS + "-";
			if( context.CTX_CHECK ) {
				if( !showConfDiffs( server , node , tobeConfigServerFolder , asisConfigServerFolder , nodePrefix ) )
					verifyNode = false;
			}
			else {
				if( !checkConfDiffs( server , node , tobeConfigServerFolder , asisConfigServerFolder , nodePrefix ) )
					verifyNode = false;
			}
		}
		
		if( !verifyNode )
			error( "node differs from distributive" );
		else
			debug( "node matched" );
		
		return( verifyNode );
	}
		
	private boolean showConfDiffs( MetaEnvServer server , MetaEnvServerNode node , LocalFolder tobeServerFolder , LocalFolder asisServerFolder , String nodePrefix ) throws Exception {
		boolean verifyNode = true;
		
		FileSet releaseSet = tobeServerFolder.getFileSet( this );
		FileSet prodSet = asisServerFolder.getFileSet( this );
		
		debug( "calculate diff between: " + tobeServerFolder.folderPath + " and " + asisServerFolder.folderPath + " (prefix=" + nodePrefix + ") ..." );
		ConfDiffSet diff = new ConfDiffSet( releaseSet , prodSet , nodePrefix , true );
		if( !dist.prod )
			diff.calculate( this , dist.release );
		else
			diff.calculate( this , null );
		
		if( diff.isDifferent( this ) ) {
			verifyNode = false;
			String diffFile = asisServerFolder.getFilePath( this , "confdiff.txt" );
			diff.save( this , diffFile );
			if( context.CTX_SHOWALL )
				error( "found configuration differences in node=" + node.POS + ", see " + diffFile );
			else {
				error( "found configuration differences in node=" + node.POS + ":" );
				info( "see " + diffFile );
			}
		}
		
		if( verifyNode )
			debug( "node configuration is matched" );
		
		return( verifyNode );
	}

	private boolean checkConfDiffs( MetaEnvServer server , MetaEnvServerNode node , LocalFolder tobeServerFolder , LocalFolder asisServerFolder , String nodePrefix ) throws Exception {
		boolean verifyNode = true;
		
		Map<String,String> tobeDirs = Common.copyListToMap( tobeServerFolder.getTopDirs( this ) ); 
		Map<String,String> asisDirs = Common.copyListToMap( asisServerFolder.getTopDirs( this ) );
		
		for( String dir : tobeDirs.keySet() ) {
			if( !dir.startsWith( nodePrefix ) )
				continue;

			if( !asisDirs.containsKey( dir ) ) {
				verifyNode = false;
				error( "not found expected dir, existing in live: " + dir );
				continue;
			}
			
			// match
			String file = Common.getPath( dir , MD5FILE );
			String asisMD5 = asisServerFolder.getFileContentAsString( this , file );
			asisMD5 = asisMD5.trim();
			
			LocalFolder confFolder = tobeServerFolder.getSubFolder( this , dir ); 
			String tobeMD5 = confFolder.getFilesMD5( this );
			
			if( !tobeMD5.equals( asisMD5 ) ) {
				verifyNode = false;
				debug( "asis MD5 hash: " + asisMD5 );
				debug( "tobe MD5 hash: " + tobeMD5 );
				error( "not matched component: " + Common.getPartAfterFirst( dir , nodePrefix ) );
			}
			else
				debug( "exactly matched component: " + Common.getPartAfterFirst( dir , nodePrefix ) );
		}
		
		for( String dir : asisDirs.keySet() ) {
			if( !dir.startsWith( nodePrefix ) )
				continue;

			if( !tobeDirs.containsKey( dir ) ) {
				verifyNode = false;
				error( "not found live dir, existing in expected: " + dir );
				continue;
			}
		}
		
		return( verifyNode );
	}
	
	private void executeNodeConf( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location , MetaDistrConfItem confItem , LocalFolder asisConfigServerFolder ) throws Exception {
		if( !dist.prod ) {
			if( dist.release.findConfComponent( this , confItem.KEY ) == null ) {
				trace( "ignore non-release conf item=" + confItem.KEY );
				return;
			}
		}
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , dist.meta );
		String name = sourceStorage.getConfItemLiveName( this , node , confItem );
		
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		LocalFolder asisConfFolder = asisConfigServerFolder.getSubFolder( this , name );
		asisConfFolder.ensureExists( this );
		
		if( context.CTX_CHECK ) {
			if( !redist.getConfigItem( this , asisConfFolder , confItem , location.DEPLOYPATH ) )
				ifexit( _Error.UnableGetConfigurationItem1 , "unable to get configuration item=" + confItem.KEY , new String[] { confItem.KEY } );
		}
		else {
			String asisMD5 = redist.getConfigItemMD5( this , confItem , location.DEPLOYPATH );
			if( asisMD5 == null )
				ifexit( _Error.UnableGetConfigurationItem1 , "unable to get configuration item=" + confItem.KEY , new String[] { confItem.KEY } );
			asisConfFolder.createFileFromString( this , MD5FILE , asisMD5 );
		}
	}

	private boolean executeNodeBinary( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location , MetaDistrBinaryItem binaryItem , LocalFolder tobeServerFolder , LocalFolder asisServerFolder ) throws Exception {
		if( binaryItem.isArchive( this ) )
			return( executeNodeArchive( server , node , location , binaryItem , tobeServerFolder , asisServerFolder ) );
		
		DistItemInfo distInfo = dist.getDistItemInfo( this , binaryItem , true );
		if( !distInfo.found ) {
			debug( "ignore non-release item=" + binaryItem.KEY );
			return( true );
		}
		
		String deployBaseName = location.getDeployName( this , binaryItem.KEY );
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		FileInfo runInfo = redist.getRuntimeItemInfo( this , binaryItem , location.DEPLOYPATH , deployBaseName );
		if( runInfo.md5value == null ) {
			error( "dist item=" + binaryItem.KEY + " is not found in location=" + location.DEPLOYPATH );
			return( false );
		}
		
		if( !runInfo.md5value.equals( distInfo.md5value ) ) {
			error( "dist item=" + binaryItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive (" +
				runInfo.md5value + " != " + distInfo.md5value + ")" );
			return( false );
		}
		
		String runtimeName = redist.getDeployVersionedName( this , location , binaryItem , deployBaseName , dist.release.RELEASEVER );
		if( !runInfo.deployFinalName.equals( runtimeName ) ) {
			info( "dist item=" + binaryItem.KEY + " is the same in location=" + location.DEPLOYPATH + 
					", but name differs from expected (" + 
					runInfo.deployFinalName + " != " + distInfo.fileName + ")" );
			return( true );
		}
		
		debug( "exactly matched item=" + binaryItem.KEY );
		return( true );
	}

	private boolean executeNodeArchive( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location , MetaDistrBinaryItem archiveItem , LocalFolder tobeServerFolder , LocalFolder asisServerFolder ) throws Exception {
		boolean getMD5 = ( context.CTX_CHECK )? false : true;
		DistItemInfo distInfo = dist.getDistItemInfo( this , archiveItem , getMD5 );
		if( !distInfo.found ) {
			debug( "ignore non-release item=" + archiveItem.KEY );
			return( true );
		}
		
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		if( !context.CTX_CHECK ) {
			FileInfo runInfo = redist.getRuntimeItemInfo( this , archiveItem , location.DEPLOYPATH , "" );
			if( runInfo.md5value == null ) {
				error( "dist item=" + archiveItem.KEY + " is not found in location=" + location.DEPLOYPATH );
				return( false );
			}
			
			if( !runInfo.md5value.equals( distInfo.md5value ) ) {
				error( "dist item=" + archiveItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive (" +
					runInfo.md5value + " != " + distInfo.md5value + ")" );
				return( false );
			}
		}
		else {
			// copy from runtime area and extract
			LocalFolder liveFolder = asisServerFolder.getSubFolder( this , "archive.live" );
			liveFolder.recreateThis( this );
			String fileName = "archive" + archiveItem.EXT;
			redist.saveTmpArchiveItem( this , location.DEPLOYPATH , archiveItem , fileName );
			redist.copyTmpFileToLocal( this , fileName , liveFolder );
			liveFolder.extractArchive( this , archiveItem.getArchiveType( this ), fileName , "" );
			liveFolder.removeFiles( this , fileName );
			
			// copy file from dist area and extract
			LocalFolder distFolder = tobeServerFolder.getSubFolder( this , "archive.dist" );
			distFolder.recreateThis( this );
			dist.copyDistFileToFolderRename( this , distFolder , distInfo.subPath , distInfo.fileName , fileName );
			distFolder.extractArchive( this , archiveItem.getArchiveType( this ), fileName , "" );
			distFolder.removeFiles( this , fileName );
			
			// compare using diff
			String name = "node" + node.POS + "-" + archiveItem.KEY + ".diff";
			String diffFile = asisServerFolder.getFilePath( this , name );
			
			boolean isdiff = false;
			if( isLocalWindows() ) {
				FileSet releaseSet = distFolder.getFileSet( this );
				FileSet prodSet = liveFolder.getFileSet( this );
				
				debug( "calculate diff between: " + distFolder.folderPath + " and " + liveFolder.folderPath + " ..." );
				ConfDiffSet diff = new ConfDiffSet( releaseSet , prodSet , "" , false );
				diff.calculate( this , null );
				
				if( diff.isDifferent( this ) ) {
					diff.save( this , diffFile );
					isdiff = true;
				}
			}
			else {
				int status = shell.customGetStatus( this , "diff -r " + liveFolder.folderPath + " " + distFolder.folderPath + " > " + diffFile );
				if( status != 0 )
					isdiff = true;
			}
			
			if( isdiff ) {
				if( context.CTX_SHOWALL )
					error( "dist item=" + archiveItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive (see " + diffFile + ")" );
				else {
					error( "dist item=" + archiveItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive:" );
					info( "see differences at " + diffFile );
				}
				return( false );
			}
		}
		
		debug( "exactly matched item=" + archiveItem.KEY );
		return( true );
	}
	
}
