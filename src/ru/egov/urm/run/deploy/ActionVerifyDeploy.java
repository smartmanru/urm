package ru.egov.urm.run.deploy;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.conf.ConfDiffSet;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerLocation;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistItemInfo;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileInfo;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.SourceStorage;

public class ActionVerifyDeploy extends ActionBase {

	DistStorage dist;
	LocalFolder tobeFolder;
	LocalFolder asisFolder;
	ActionConfigure configure;
	boolean verifyOk;

	static String MD5FILE = "md5sum.txt";
	
	public ActionVerifyDeploy( ActionBase action , String stream , DistStorage dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		tobeFolder = artefactory.getWorkFolder( this , "tobe" );
		tobeFolder.ensureExists( this );
		
		if( dist.prod )
			configure = new ActionConfigure( this , null , tobeFolder );
		else
			configure = new ActionConfigure( this , null , dist , tobeFolder );
		configure.changeOptions();
		configure.options.OPT_HIDDEN = true;
		configure.runAll( scope );
		
		asisFolder = artefactory.getWorkFolder( this , "asis" );
		asisFolder.ensureExists( this );
		verifyOk = true;
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		// ignore database and unreachable
		MetaEnvServer server = target.envServer;
		if( server.TYPE == VarSERVERTYPE.DATABASE || 
			server.TYPE == VarSERVERTYPE.GENERIC_NOSSH ||
			server.TYPE == VarSERVERTYPE.UNKNOWN ) {
			trace( "ignore due to server type=" + Common.getEnumLower( server.TYPE ) );
			return( true );
		}

		if( target.getItems( this ).size() == 0 ) {
			trace( "no nodes to verify. Skipped." );
			return( true );
		}

		executeServer( target );
		return( true );
	}
	
	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		MetaEnvServerLocation[] F_ENV_LOCATIONS_BINARY = new MetaEnvServerLocation[0];
		if( options.OPT_DEPLOYBINARY )
			F_ENV_LOCATIONS_BINARY = server.getLocations( this , true , false );
		
		MetaEnvServerLocation[] F_ENV_LOCATIONS_CONFIG = new MetaEnvServerLocation[0];
		if( context.CONF_DEPLOY )
			F_ENV_LOCATIONS_CONFIG = server.getLocations( this , false , true );
		
		if( F_ENV_LOCATIONS_BINARY.length == 0 && F_ENV_LOCATIONS_CONFIG.length == 0 ) {
			debug( "server=$P_SERVER - no locations. Skipped." );
			return;
		}

		log( "============================================ execute server=" + server.NAME + ", type=" + Common.getEnumLower( server.TYPE ) + " ..." );

		// iterate by nodes
		LocalFolder tobeServerFolder = configure.getLiveFolder( server );
		LocalFolder asisServerFolder = asisFolder.getSubFolder( this , server.NAME );
		asisServerFolder.ensureExists( this );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			log( "execute server=" + server.NAME + " node=" + node.POS + " ..." );

			// verify configs to each node
			executeNode( server , node , F_ENV_LOCATIONS_CONFIG , F_ENV_LOCATIONS_BINARY , tobeServerFolder , asisServerFolder );
		}
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation[] confLocations , MetaEnvServerLocation[] binaryLocations , LocalFolder tobeServerFolder , LocalFolder asisServerFolder ) throws Exception {
		boolean verifyNode = true;
		
		// binaries
		log( "verify binaries ..." );
		for( MetaEnvServerLocation location : binaryLocations ) {
			for( MetaDistrBinaryItem binaryItem : location.binaryItems.values() ) {
				if( !executeNodeBinary( server , node , location , binaryItem , tobeServerFolder , asisServerFolder ) )
					verifyNode = false;
			}
		}
	
		// configuration
		log( "verify configuration ..." );
		for( MetaEnvServerLocation location : confLocations )
			for( MetaDistrConfItem confItem : location.confItems.values() )
				executeNodeConf( server , node , location , confItem );
			
		// compare configuration tobe and as is
		if( confLocations.length > 0 ) {
			String nodePrefix = "node" + node.POS + "-";
			if( options.OPT_CHECK ) {
				if( !showConfDiffs( server , node , tobeServerFolder , asisServerFolder , nodePrefix ) )
					verifyNode = false;
			}
			else {
				if( !checkConfDiffs( server , node , tobeServerFolder , asisServerFolder , nodePrefix ) )
					verifyNode = false;
			}
		}
		
		if( !verifyNode ) {
			log( "node differs from distributive" );
			verifyOk = false;
		}
		else
			debug( "node matched" );
	}
		
	private boolean showConfDiffs( MetaEnvServer server , MetaEnvServerNode node , LocalFolder tobeServerFolder , LocalFolder asisServerFolder , String nodePrefix ) throws Exception {
		boolean verifyNode = true;
		
		FileSet releaseSet = tobeServerFolder.getFileSet( this );
		FileSet prodSet = asisServerFolder.getFileSet( this );
		
		debug( "calculate diff between: " + tobeServerFolder.folderPath + " and " + asisServerFolder.folderPath + " (prefix=" + nodePrefix + ") ..." );
		ConfDiffSet diff = new ConfDiffSet( releaseSet , prodSet , nodePrefix );
		if( !dist.prod )
			diff.calculate( this , dist.info );
		else
			diff.calculate( this , null );
		
		if( diff.isDifferent( this ) ) {
			verifyNode = false;
			if( options.OPT_SHOWALL ) {
				String file = asisFolder.getFilePath( this , "diff.txt" );
				diff.save( this , file );
				log( "found configuration differences in node=" + node.POS + ", see " + file );
			}
			else
				log( "found configuration differences in node=" + node.POS );
		}
		
		if( verifyNode )
			log( "node configuration is matched" );
		
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
				log( "not found expected dir, existing in live: " + dir );
				continue;
			}
			
			// match
			String file = Common.getPath( dir , MD5FILE );
			String asisMD5 = asisServerFolder.getFileContentAsString( this , file );
			
			LocalFolder confFolder = tobeServerFolder.getSubFolder( this , dir ); 
			String tobeMD5 = confFolder.getFilesMD5( this );
			
			if( !tobeMD5.equals( asisMD5 ) ) {
				verifyNode = false;
				log( "not matched component: " + Common.getPartAfterFirst( dir , nodePrefix ) );
			}
			else
				debug( "exactly matched component: " + Common.getPartAfterFirst( dir , nodePrefix ) );
		}
		
		for( String dir : asisDirs.keySet() ) {
			if( !dir.startsWith( nodePrefix ) )
				continue;

			if( !tobeDirs.containsKey( dir ) ) {
				verifyNode = false;
				log( "not found live dir, existing in expected: " + dir );
				continue;
			}
		}
		
		return( verifyNode );
	}
	
	private void executeNodeConf( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location , MetaDistrConfItem confItem ) throws Exception {
		if( !dist.prod ) {
			if( dist.info.findConfComponent( this , confItem.KEY ) == null ) {
				trace( "ignore non-release conf item=" + confItem.KEY );
				return;
			}
		}
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		String name = sourceStorage.getConfItemLiveName( this , node , confItem );
		
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		LocalFolder asisConfFolder = asisFolder.getSubFolder( this , Common.getPath( server.NAME , name ) );
		asisConfFolder.ensureExists( this );
		
		if( options.OPT_CHECK ) {
			if( !redist.getConfigItem( this , asisConfFolder , confItem , location.DEPLOYPATH ) ) {
				if( !options.OPT_FORCE )
					exit( "unable to get configuration item=" + confItem.KEY );
			}
		}
		else {
			String asisMD5 = redist.getConfigItemMD5( this , confItem , location.DEPLOYPATH );
			if( asisMD5 == null ) {
				if( !options.OPT_FORCE )
					exit( "unable to get configuration item=" + confItem.KEY );
			}
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
			log( "dist item=" + binaryItem.KEY + " is not found in location=" + location.DEPLOYPATH );
			return( false );
		}
		
		if( !runInfo.md5value.equals( distInfo.md5value ) ) {
			log( "dist item=" + binaryItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive (" +
				runInfo.md5value + " != " + distInfo.md5value + ")" );
			return( false );
		}
		
		String redistFileName = redist.getDeployVersionedName( this , location , binaryItem , deployBaseName , dist.info.RELEASEVER );
		String runtimeName = redist.getRedistBinaryFileDeployName( this , redistFileName );
		if( !runInfo.finalName.equals( runtimeName ) ) {
			log( "dist item=" + binaryItem.KEY + " is the same in location=" + location.DEPLOYPATH + 
					", but name differs from expected (" + 
					runInfo.finalName + " != " + distInfo.fileName + ")" );
			return( true );
		}
		
		debug( "exactly matched item=" + binaryItem.KEY );
		return( true );
	}

	private boolean executeNodeArchive( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location , MetaDistrBinaryItem archiveItem , LocalFolder tobeServerFolder , LocalFolder asisServerFolder ) throws Exception {
		boolean getMD5 = ( options.OPT_CHECK )? false : true;
		DistItemInfo distInfo = dist.getDistItemInfo( this , archiveItem , getMD5 );
		if( !distInfo.found ) {
			debug( "ignore non-release item=" + archiveItem.KEY );
			return( true );
		}
		
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		if( !options.OPT_CHECK ) {
			FileInfo runInfo = redist.getRuntimeItemInfo( this , archiveItem , location.DEPLOYPATH , "" );
			if( runInfo.md5value == null ) {
				log( "dist item=" + archiveItem.KEY + " is not found in location=" + location.DEPLOYPATH );
				return( false );
			}
			
			if( !runInfo.md5value.equals( distInfo.md5value ) ) {
				log( "dist item=" + archiveItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive (" +
					runInfo.md5value + " != " + distInfo.md5value + ")" );
				return( false );
			}
		}
		else {
			// copy from runtime area and extract
			LocalFolder liveFolder = asisServerFolder.getSubFolder( this , "archive.live" );
			liveFolder.recreateThis( this );
			String fileName = "archive.tar.gz";
			redist.saveTmpArchiveItem( this , location.DEPLOYPATH , archiveItem , fileName );
			redist.copyTmpFileToLocal( this , fileName , liveFolder );
			liveFolder.extractTarGz( this , fileName , "" );
			liveFolder.removeFiles( this , fileName );
			
			// copy file from dist area and extract
			LocalFolder distFolder = tobeServerFolder.getSubFolder( this , "archive.dist" );
			distFolder.recreateThis( this );
			dist.copyDistFileToFolderRename( this , distFolder , distInfo.subPath , distInfo.fileName , fileName );
			distFolder.extractTarGz( this , fileName , "" );
			distFolder.removeFiles( this , fileName );
			
			// compare using diff
			String diffFile = asisServerFolder.getFilePath( this , "diff.txt" );
			int status = session.customGetStatus( this , "diff -r " + liveFolder.folderPath + " " + distFolder.folderPath + " > " + diffFile );
			if( status != 0 ) {
				log( "dist item=" + archiveItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive (see " + diffFile + ")" );
				return( false );
			}
		}
		
		debug( "exactly matched item=" + archiveItem.KEY );
		return( true );
	}
	
}
