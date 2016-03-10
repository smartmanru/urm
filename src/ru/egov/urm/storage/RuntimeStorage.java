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
		if( action.context.CTX_SHOWONLY )
			return;
		
		String F_RUNTIMEDIR;
		String F_FILES;
		
		if( server.isService( action ) ) {
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
		String F_CONFIGTARFILE = "config.tar";
		String tarFilePath = localDir.getFilePath( action , F_CONFIGTARFILE );
		srcFolder.createTarFromContent( action , tarFilePath , "*" , "" );
		
		// rollout to destination
		ShellExecutor shell = action.getShell( node );
		RemoteFolder remoteDir = redist.getRedistTmpFolder( action );
		shell.appendUploadLog( action , tarFilePath , remoteDir.folderPath );
		remoteDir.ensureExists( action );
		remoteDir.copyFileFromLocal( action , tarFilePath );

		RemoteFolder runtimeDir = new RemoteFolder( artefactory , action.getAccount( node ) , F_RUNTIMEDIR );
		String confFullPath = remoteDir.getFilePath( action , F_CONFIGTARFILE );
		shell.appendExecuteLog( action , "restore server system configuration (" + confFullPath + ")" + " to " + runtimeDir.folderPath );
		if( !server.isService( action ) )
			runtimeDir.removeFiles( action , F_FILES );
		runtimeDir.extractTar( action , confFullPath , "" );

		remoteDir.removeFiles( action , F_CONFIGTARFILE );
		localDir.removeFiles( action , F_CONFIGTARFILE );
	}

	public void restoreConfigItem( ActionBase action , RedistStorage redist , LocalFolder srcFolder , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String version ) throws Exception {
		String LOCATION = deployment.getDeployPath( action );
		String msg = "restore server configuratuion files item=" + confItem.KEY + ", location=" + LOCATION;
		action.executeLogLive( action.getAccount( node ) , msg );
		if( action.context.CTX_SHOWONLY )
			return;
		
		// prepare on local
		LocalFolder localDir = artefactory.getWorkFolder( action , "tmp" );
		localDir.ensureExists( action );
		String F_CONFIGTARFILE = "config.tar";
		String tarFilePath = localDir.getFilePath( action , F_CONFIGTARFILE );
		srcFolder.createTarFromContent( action , tarFilePath , "*" , "" );
		
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
			if( action.context.CTX_HIDDEN ) {
				includeFiles = confItem.getLiveIncludeFiles( action );
				excludeFiles = confItem.getLiveExcludeFiles( action );
			}
			else {
				includeFiles = confItem.getTemplateIncludeFiles( action );
				excludeFiles = confItem.getTemplateExcludeFiles( action );
			}
			deployDir.removeFilesWithExclude( action , includeFiles , excludeFiles );
		}

		deployDir.extractTar( action , stagingPath , "" );
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
				RedistStateInfo info = new RedistStateInfo();
				info.gather( action , node , content , super.getRedistFolderByContent( action , content , rollout ) );
				
				for( String key : info.getKeys( action ) ) {
					FileInfo redistFile = info.getVerData( action , key );
					deployRedistItem( action , RELEASEDIR  , content , location , redistFile , rollout );
				}
			}
		}
	}
	
	public void deployRedistItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , boolean rollout ) throws Exception {
		String mode = ( rollout )? "rollout" : "rollback";
		String msg = "deploy redist item mode=" + mode + ", release=" + RELEASEDIR + ", content=" + 
				Common.getEnumLower( CONTENTTYPE ) + ", location=" + LOCATION + ", file=" + redistFile.getFileName( action );
		
		action.executeLogLive( action.getAccount( node ) , msg );
		if( action.context.CTX_SHOWONLY )
			return;

		RemoteFolder redistFolder = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , rollout );
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		RedistStorage redist = artefactory.getRedistStorage( action , server , node ); 
		
		if( redistFile.confItem != null )
			deployRedistConfItem( action , redist , RELEASEDIR , CONTENTTYPE , LOCATION , redistFolder , deployFolder , redistFile , rollout );
		else
		if( redistFile.binaryItem != null ) {
			if( redistFile.binaryItem.isArchive( action ) )
				deployRedistArchiveItem( action , redist , RELEASEDIR , CONTENTTYPE , LOCATION , redistFolder , deployFolder , redistFile , rollout );
			else
				deployRedistBinaryItem( action , redist , RELEASEDIR , CONTENTTYPE , LOCATION , redistFolder , deployFolder , redistFile , rollout );
		}
		else
			action.exitUnexpectedState();

		action.debug( "deploy done location=" + LOCATION + ", file=" + redistFile.getFileName( action ) );
	}

	private void deployRedistConfItem( ActionBase action , RedistStorage redist , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , RemoteFolder redistFolder , RemoteFolder deployFolder , FileInfo redistFile , boolean rollout ) throws Exception {
		boolean full = ( redistFile.partial )? false : true;
		String stagingPath = redistFolder.getFilePath( action , redistFile.getFileName( action ) );
		
		deployConfigItem( action , stagingPath , redistFile.confItem , deployFolder , full );
		redist.changeStateItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , rollout );
	}
	
	private void deployRedistBinaryItem( ActionBase action , RedistStorage redist , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , RemoteFolder redistFolder , RemoteFolder deployFolder , FileInfo redistFile , boolean rollout ) throws Exception {
		String deployName = redistFile.finalName;
		
		// delete old
		String oldFile = deployFolder.findBinaryDistItemFile( action , redistFile.binaryItem , deployName );
		if( !oldFile.isEmpty() )
			deployFolder.removeFiles( action , oldFile );
		
		// deploy new
		deployFolder.copyFile( action , redistFolder , redistFile.getFileName( action ) , deployName );
		redist.changeStateItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , rollout );
	}
	
	private void deployRedistArchiveItem( ActionBase action , RedistStorage redist , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , RemoteFolder redistFolder , RemoteFolder deployFolder , FileInfo redistFile , boolean rollout ) throws Exception {
		String tarFilePath = redistFolder.getFilePath( action , redistFile.getFileName( action ) );

		MetaDistrBinaryItem archiveItem = redistFile.binaryItem;
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
		
		redist.changeStateItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , rollout );
	}
	
}
