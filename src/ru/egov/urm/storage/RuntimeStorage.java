package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerDeployment;
import ru.egov.urm.meta.MetaEnvServerLocation;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.meta.Metadata.VarDISTITEMTYPE;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.deploy.ServerDeployment;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;

public class RuntimeStorage extends ServerStorage {

	public RuntimeStorage( Artefactory artefactory , String type , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		super( artefactory , type , account , server , node );
	}

	public void restoreSysConfigs( ActionBase action , RedistStorage redist , LocalFolder srcFolder ) throws Exception {
		String msg = "restore server control configuratuion files";
		action.executeLogLive( action.getAccount( node ) , msg );
		if( action.context.SHOWONLY )
			return;
		
		String F_RUNTIMEDIR;
		String F_FILES;
		
		if( server.TYPE == VarSERVERTYPE.SERVICE ) {
			F_RUNTIMEDIR = "/etc/init.d";
			F_FILES = server.SERVICENAME;
		}
		else {
			F_RUNTIMEDIR = Common.getPath( server.ROOTPATH , server.BINPATH );
			F_FILES = "server.*.sh";
		}

		// prepare on local
		LocalFolder localDir = artefactory.getWorkFolder( action , "tmp" );
		localDir.ensureExists( action );
		String F_CONFIGTARFILE = "config.tgz";
		String tarFilePath = localDir.getFilePath( action , F_CONFIGTARFILE );
		srcFolder.createTarGzFromContent( action , tarFilePath , "*" , "" );
		
		// rollout to destination
		ShellExecutor shell = action.getShell( node );
		RemoteFolder remoteDir = redist.getRedistTmpFolder( action );
		shell.appendUploadLog( action , tarFilePath , remoteDir.folderPath );
		remoteDir.ensureExists( action );
		remoteDir.copyFileFromLocal( action , tarFilePath );

		RemoteFolder runtimeDir = new RemoteFolder( artefactory , action.getAccount( node ) , F_RUNTIMEDIR );
		String confFullPath = remoteDir.getFilePath( action , F_CONFIGTARFILE );
		shell.appendExecuteLog( action , "restore server system configuration (" + confFullPath + ")" + " to " + runtimeDir.folderPath );
		if( server.TYPE != VarSERVERTYPE.SERVICE )
			runtimeDir.removeFiles( action , F_FILES );
		runtimeDir.extractTarGz( action , confFullPath , "" );

		remoteDir.removeFiles( action , F_CONFIGTARFILE );
		localDir.removeFiles( action , F_CONFIGTARFILE );
	}

	public void restoreConfigItem( ActionBase action , RedistStorage redist , LocalFolder srcFolder , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String version ) throws Exception {
		String LOCATION = deployment.getDeployPath( action );
		String msg = "restore server configuratuion files item=" + confItem.KEY + ", location=" + LOCATION;
		action.executeLogLive( action.getAccount( node ) , msg );
		if( action.context.SHOWONLY )
			return;
		
		// prepare on local
		LocalFolder localDir = artefactory.getWorkFolder( action , "tmp" );
		localDir.ensureExists( action );
		String F_CONFIGTARFILE = "config.tgz";
		String tarFilePath = localDir.getFilePath( action , F_CONFIGTARFILE );
		srcFolder.createTarGzFromContent( action , tarFilePath , "*" , "" );
		
		// rollout to destination
		ShellExecutor shell = action.getShell( node );
		RemoteFolder remoteDir = redist.getRedistTmpFolder( action );
		shell.appendUploadLog( action , tarFilePath , remoteDir.folderPath );
		remoteDir.ensureExists( action );
		
		remoteDir.copyFileFromLocal( action , tarFilePath );
		String stagingPath = remoteDir.getFilePath( action , F_CONFIGTARFILE );
		
		RemoteFolder deployDir = getRuntimeLocationFolder( action , LOCATION );
		shell.appendExecuteLog( action , "restore server configuration file=" + stagingPath + " " + " to " + deployDir.folderPath );

		deployConfigItem( action , stagingPath , confItem , deployDir , true );
		
		// add to state
		deployment.getLocation( action );
		MetaEnvServerLocation location = deployment.getLocation( action );
		redist.restoreConfigFile( action , confItem , location , stagingPath , version );
	}
	
	private void deployConfigItem( ActionBase action , String stagingPath , MetaDistrConfItem confItem , RemoteFolder deployDir , boolean full ) throws Exception {
		if( confItem.CREATEDIR )
			deployDir.ensureExists( action );
		else {
			if( !deployDir.checkExists( action ) )
				action.exit( "deploy directory " + deployDir.folderPath + " does not exist" );
		}
			
		// delete old only if full deploy
		if( full ) {
			String includeFiles;
			String excludeFiles;
			if( action.options.OPT_HIDDEN ) {
				includeFiles = confItem.getLiveIncludeFiles( action );
				excludeFiles = confItem.getLiveExcludeFiles( action );
			}
			else {
				includeFiles = confItem.getTemplateIncludeFiles( action );
				excludeFiles = confItem.getTemplateExcludeFiles( action );
			}
			deployDir.removeFilesWithExclude( action , includeFiles , excludeFiles );
		}

		deployDir.extractTarGz( action , stagingPath , "" );
	}

	public void rollout( ActionBase action , String RELEASEDIR , ServerDeployment deployment ) throws Exception {
		deploy( action , RELEASEDIR , deployment , true );
	}
	
	public void rollback( ActionBase action , String RELEASEDIR , ServerDeployment deployment ) throws Exception {
		deploy( action , RELEASEDIR , deployment , false );
	}
	
	private void deploy( ActionBase action , String RELEASEDIR , ServerDeployment deployment , boolean rollout ) throws Exception {
		for( VarCONTENTTYPE content : VarCONTENTTYPE.values() ) {
			for( String location : deployment.getLocations( action , content , rollout ) ) {
				for( String file : deployment.getLocationFiles( action , content , rollout , location ) )
					deployRedistItem( action , RELEASEDIR  , content , location , file , rollout );
			}
		}
	}
	
	public void deployRedistItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , String file , boolean rollout ) throws Exception {
		String mode = ( rollout )? "rollout" : "rollback";
		String msg = "deploy redist item mode=" + mode + ", release=" + RELEASEDIR + ", content=" + 
				Common.getEnumLower( CONTENTTYPE ) + ", location=" + LOCATION + ", file=" + file;
		
		action.executeLogLive( action.getAccount( node ) , msg );
		if( action.context.SHOWONLY )
			return;

		RemoteFolder redistFolder = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , rollout );
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		
		RedistFileType rft = getRedistFileType( action , file );
		if( rft == RedistFileType.CONFCOMP )
			deployRedistConfItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFolder , deployFolder , file , rollout );
		else
		if( rft == RedistFileType.BINARY )
			deployRedistBinaryItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFolder , deployFolder , file , rollout );
		else
		if( rft == RedistFileType.ARCHIVE )
			deployRedistArchiveItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFolder , deployFolder , file , rollout );
		else
			action.exitUnexpectedState();

		// copy to state folder
		RedistStorage redist = artefactory.getRedistStorage( action , server , node ); 
		redist.changeStateItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , file , rollout );
		
		action.debug( "deploy done location=" + LOCATION + ", file=" + file );
	}

	private void deployRedistConfItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , RemoteFolder redistFolder , RemoteFolder deployFolder , String file , boolean rollout ) throws Exception {
		MetaDistrConfItem confItem = getRedistFileConfComp( action , file );
		boolean full = getRedistFileConfFull( action , file );
		String stagingPath = redistFolder.getFilePath( action , file );
		
		deployConfigItem( action , stagingPath , confItem , deployFolder , full );
	}
	
	private void deployRedistBinaryItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , RemoteFolder redistFolder , RemoteFolder deployFolder , String file , boolean rollout ) throws Exception {
		MetaDistrBinaryItem binaryItem = getRedistFileBinaryItem( action , file );
		String deployName = getRedistBinaryFileDeployName( action , file );
		
		// delete old
		String oldFile = deployFolder.findBinaryDistItemFile( action , binaryItem );
		if( !oldFile.isEmpty() )
			deployFolder.removeFiles( action , oldFile );
		
		// deploy new
		deployFolder.copyFile( action , redistFolder , file , deployName );
	}
	
	private void deployRedistArchiveItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , RemoteFolder redistFolder , RemoteFolder deployFolder , String file , boolean rollout ) throws Exception {
		MetaDistrBinaryItem archiveItem = getRedistFileArchiveItem( action , file );

		String tarFilePath = redistFolder.getFilePath( action , file );

		if( archiveItem.EXT.equals( ".tar.gz" ) == false && 
			archiveItem.EXT.equals( ".tgz" ) == false )
			action.exitNotImplemented();

		deployFolder.ensureExists( action );
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_CHILD ) {
			// delete old
			if( !deployFolder.checkFolderExists( action , archiveItem.DEPLOYBASENAME ) ) {
				String content = "";
				String exclude = "";
				String prefix = archiveItem.DEPLOYBASENAME + "/";
				
				for( String s : Common.splitSpaced( archiveItem.FILES ) )
					content = Common.addItemToUniqueSpacedList( content , prefix + s );
				for( String s : Common.splitSpaced( archiveItem.EXCLUDE ) )
					exclude = Common.addItemToUniqueSpacedList( exclude , prefix + s );
				
				deployFolder.removeFilesWithExclude( action , content , exclude );
			}
			
			// deploy new
			deployFolder.extractTarGz( action , tarFilePath , "" );
		}
		else
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT ) {
			// delete old
			deployFolder.removeFilesWithExclude( action , archiveItem.FILES , archiveItem.EXCLUDE );
			
			// deploy new
			deployFolder.extractTarGz( action , tarFilePath , "" );
		}
		else
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR ) {
			RemoteFolder deployTarFolder = deployFolder.getSubFolder( action , archiveItem.DEPLOYBASENAME );
			if( !deployTarFolder.checkExists( action ) ) {
				deployTarFolder.ensureExists( action );
				
				// delete old
				deployTarFolder.removeFilesWithExclude( action , archiveItem.FILES , archiveItem.EXCLUDE );
			}
			
			// deploy new
			deployTarFolder.extractTarGz( action , tarFilePath , "" );
		}
		else
			action.exitUnexpectedState();
	}
	
}
