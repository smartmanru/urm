package ru.egov.urm.run.deploy;

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
		LocalFolder tobeNodeFolder = tobeFolder.getSubFolder( this , server.NAME );
		LocalFolder asisNodeFolder = asisFolder.getSubFolder( this , server.NAME );
		tobeNodeFolder.ensureExists( this );
		asisNodeFolder.ensureExists( this );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			log( "execute server=" + server.NAME + " node=" + node.POS + " ..." );

			// verify configs to each node
			executeNode( server , node , F_ENV_LOCATIONS_CONFIG , F_ENV_LOCATIONS_BINARY , tobeNodeFolder , asisNodeFolder );
		}
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation[] confLocations , MetaEnvServerLocation[] binaryLocations , LocalFolder tobeNodeFolder , LocalFolder asisNodeFolder ) throws Exception {
		// binaries
		log( "verify binaries ..." );
		for( MetaEnvServerLocation location : binaryLocations )
			for( MetaDistrBinaryItem binaryItem : location.binaryItems.values() )
				executeNodeBinary( server , node , location , binaryItem );
	
		// configuration
		log( "verify configuration ..." );
		for( MetaEnvServerLocation location : confLocations )
			for( MetaDistrConfItem confItem : location.confItems.values() )
				executeNodeConf( server , node , location , confItem );
			
		// compare tobe and as is
		FileSet releaseSet = tobeNodeFolder.getFileSet( this );
		FileSet prodSet = asisNodeFolder.getFileSet( this );
		
		String nodePrefix = "node" + node.POS + "-";
		debug( "calculate diff between: " + releaseSet.dirPath + " and " + prodSet.dirPath + " (prefix=" + nodePrefix + ") ..." );
		ConfDiffSet diff = new ConfDiffSet( releaseSet , prodSet , nodePrefix );
		if( !dist.prod )
			diff.calculate( this , dist.info );
		else
			diff.calculate( this , null );
		
		if( diff.isDifferent( this ) ) {
			if( options.OPT_SHOWALL ) {
				String file = asisFolder.getFilePath( this , "diff.txt" );
				diff.save( this , file );
				log( "found configuration differences in node=" + node.INSTANCE + ", see " + file );
			}
			else
				log( "found configuration differences in node=" + node.INSTANCE );
		}
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
		LocalFolder asisConfFolder = asisFolder.getSubFolder( this , Common.getPath( server.NAME , name ) );
		asisConfFolder.ensureExists( this );
		
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		if( !redist.getConfigItem( this , asisConfFolder , confItem , location.DEPLOYPATH ) ) {
			if( !options.OPT_FORCE )
				exit( "unable to get configuration item=" + confItem.KEY );
			return;
		}

	}

	private void executeNodeBinary( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerLocation location , MetaDistrBinaryItem binaryItem ) throws Exception {
		DistItemInfo distInfo = dist.getDistItemInfo( this , binaryItem , true );
		if( !distInfo.found ) {
			debug( "ignore non-release item=" + binaryItem.KEY );
			return;
		}
		
		String deployName = location.getDeployName( this , binaryItem.KEY );
		RedistStorage storage = artefactory.getRedistStorage( this , server , node );
		FileInfo runInfo = storage.getItemInfo( this , binaryItem , location.DEPLOYPATH , deployName );
		if( runInfo.md5value == null ) {
			log( "dist item=" + binaryItem.KEY + " is not found in location=" + location.DEPLOYPATH );
			verifyOk = false;
			return;
		}
		
		if( !runInfo.md5value.equals( distInfo.md5value ) ) {
			log( "dist item=" + binaryItem.KEY + " in location=" + location.DEPLOYPATH + " differs from distributive (" +
				runInfo.md5value + " != " + distInfo.md5value + ")" );
			verifyOk = false;
			return;
		}
		
		if( !runInfo.finalName.equals( distInfo.fileName ) ) {
			log( "dist item=" + binaryItem.KEY + " in location=" + location.DEPLOYPATH + 
					" is the same, but name differs from expected (" + 
					runInfo.finalName + " != " + distInfo.fileName + ")" );
		}
		
		debug( "exactly matched item=" + binaryItem.KEY );
		return;
	}
	
}
