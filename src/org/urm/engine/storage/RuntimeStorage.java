package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.action.deploy.ServerDeployment;
import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerLocation;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.Meta.VarARCHIVETYPE;
import org.urm.meta.product.Meta.VarCONTENTTYPE;
import org.urm.meta.product.Meta.VarDISTITEMTYPE;

public class RuntimeStorage extends ServerStorage {

	public RuntimeStorage( Artefactory artefactory , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		super( artefactory , account , server , node );
	}

	public void restoreSysConfigs( ActionBase action , RedistStorage redist , LocalFolder srcFolder ) throws Exception {
		String msg = "restore server control configuratuion files";
		action.executeLogLive( action.getNodeAccount( node ) , msg );
		if( !action.isExecute() )
			return;
		
		String F_RUNTIMEDIR = server.getSystemPath( action );
		String F_FILES = server.getSystemFiles( action );

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
		remoteDir.copyFileFromLocal( action , tarFilePath );

		Account nodeAccount = action.getNodeAccount( node );
		Account account = nodeAccount;
		if( server.isService() )
			account = account.getRootAccount( action );
		
		RemoteFolder runtimeDir = new RemoteFolder( account , F_RUNTIMEDIR );
		String confFullPath = remoteDir.getFilePath( action , F_CONFIGTARFILE );
		shell.appendExecuteLog( action , "restore server system configuration (" + confFullPath + ")" + " to " + runtimeDir.folderPath );
		
		if( !server.isService() )
			runtimeDir.removeFiles( action , F_FILES );
		
		runtimeDir.extractTar( action , confFullPath , "" );
		if( server.isLinux() ) {
			if( server.isService() ) {
				shell = action.getShell( account );
				shell.customCheckErrorsDebug( action , F_RUNTIMEDIR , "chown " + nodeAccount.USER + ": " + F_FILES + 
						"; chmod 744 " + F_FILES );
			}
			else
				shell.customCheckErrorsDebug( action , F_RUNTIMEDIR , "chmod 744 " + F_FILES );
		}

		remoteDir.removeFiles( action , F_CONFIGTARFILE );
		localDir.removeFiles( action , F_CONFIGTARFILE );
	}

	public void restoreConfigItem( ActionBase action , RedistStorage redist , LocalFolder srcFolder , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String version ) throws Exception {
		String LOCATION = deployment.getDeployPath( action );
		String msg = "restore server configuratuion files item=" + confItem.KEY + ", location=" + LOCATION;
		action.executeLogLive( action.getNodeAccount( node ) , msg );
		if( !action.isExecute() )
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
				action.exit1( _Error.MissingDeployDirectory1 , "deploy directory " + deployDir.folderPath + " does not exist" , deployDir.folderPath );
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
			if( Meta.isBinaryContent( action , content ) ) {
				if( !action.context.CTX_DEPLOYBINARY ) {
					action.trace( "ignore conf deploy content" );
					continue;
				}
			}
			else {
				if( !action.context.CTX_CONFDEPLOY ) {
					action.trace( "ignore conf deploy content" );
					return;
				}
			}
			
			for( String location : deployment.getLocations( action , content , rollout ) ) {
				RedistStateInfo info = new RedistStateInfo( meta );
				info.gather( action , node , content , super.getPathRedistLocation( action , RELEASEDIR , location , content , rollout ) );
				
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
		
		action.executeLogLive( action.getNodeAccount( node ) , msg );
		if( !action.isExecute() )
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
		// delete old
		String oldFile = deployFolder.findBinaryDistItemFile( action , redistFile.binaryItem , redistFile.deployBaseName );
		if( !oldFile.isEmpty() )
			deployFolder.removeFiles( action , oldFile );
		
		// deploy new
		deployFolder.ensureExists( action );
		deployFolder.copyFile( action , redistFolder , redistFile.getFileName( action ) , redistFile.deployFinalName );
		redist.changeStateItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , rollout );
	}
	
	private void deployRedistArchiveItem( ActionBase action , RedistStorage redist , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , RemoteFolder redistFolder , RemoteFolder deployFolder , FileInfo redistFile , boolean rollout ) throws Exception {
		String archiveFilePath = redistFolder.getFilePath( action , redistFile.getFileName( action ) );

		MetaDistrBinaryItem archiveItem = redistFile.binaryItem;
		VarARCHIVETYPE atype = archiveItem.getArchiveType( action );

		deployFolder.ensureExists( action );
		if( archiveItem.distItemType == VarDISTITEMTYPE.ARCHIVE_CHILD ) {
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
			deployFolder.extractArchive( action , atype , archiveFilePath , "" );
		}
		else
		if( archiveItem.distItemType == VarDISTITEMTYPE.ARCHIVE_DIRECT ) {
			// delete old
			deployFolder.removeFilesWithExclude( action , archiveItem.FILES , archiveItem.EXCLUDE );
			
			// deploy new
			deployFolder.extractArchive( action , atype , archiveFilePath , "" );
		}
		else
		if( archiveItem.distItemType == VarDISTITEMTYPE.ARCHIVE_SUBDIR ) {
			RemoteFolder deployTarFolder = deployFolder.getSubFolder( action , archiveItem.DEPLOYBASENAME );
			if( !deployTarFolder.checkExists( action ) ) {
				deployTarFolder.ensureExists( action );
				
				// delete old
				deployTarFolder.removeFilesWithExclude( action , archiveItem.FILES , archiveItem.EXCLUDE );
			}
			
			// deploy new
			deployTarFolder.extractArchive( action , atype , archiveFilePath , "" );
		}
		else
			action.exitUnexpectedState();
		
		redist.changeStateItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , rollout );
	}

	public void extractBaseArchiveSingleDir( ActionBase action , String archivePath , String archiveDir , String installPath , VarARCHIVETYPE archiveType ) throws Exception {
		ShellExecutor session = action.getShell( account );
		session.appendExecuteLog( action , "install/upgrade base from " + archivePath + " to " + installPath + " ..." );
		String installDir = Common.getDirName( installPath );
		String installName = Common.getBaseName( installPath );
		
		RemoteFolder rf = new RemoteFolder( account , installDir );
		rf.removeFolder( action , installName );
		
		if( archiveType == VarARCHIVETYPE.TAR || archiveType == VarARCHIVETYPE.TARGZ )
			rf.extractTarGzPart( action , archivePath , installName , archiveDir );
		else
		if( archiveType == VarARCHIVETYPE.ZIP )
			rf.unzipSingleFile( action , archivePath , archiveDir , installName );
		else
			action.exitUnexpectedState();
		
		if( !rf.checkFolderExists( action , installName ) )
			action.exit1( _Error.UnableExtractArchive1 , "unable to extract " + archiveDir + " from archive" , archiveDir );
	}

	public void createDirLink( ActionBase action , String link , String runtimePath ) throws Exception {
		if( account.isWindows() )
			action.exitUnexpectedState();
		
		ShellExecutor session = action.getShell( account );
		if( link.startsWith( "/" ) ) {
			session.customCheckErrorsDebug( action , "if [ -d " + link + " ]; then unlink " + link + 
					"; fi; ln -s " + runtimePath + " " + link );
		}
		else {
			String dir = Common.getDirName( runtimePath );
			session.customCheckErrorsDebug( action , dir , "if [ -d " + link + " ]; then unlink " + link + 
					"; fi; ln -s " + runtimePath + " " + link );
		}
	}

	public void installService( ActionBase action , String servicePath ) throws Exception {
		if( !server.isService() )
			action.exitUnexpectedState();
		
		if( server.osType == VarOSTYPE.LINUX ) {
			RemoteFolder runtimeDir = new RemoteFolder( action.getNodeAccount( node ) , servicePath );
			if( !runtimeDir.checkFileExists( action , "service" ) )
				action.exit1( _Error.MissingLiveServiceFile1 , "unable to find service file in " + runtimeDir.folderPath , runtimeDir.folderPath );

			String targetFile = "/etc/init.d/" + server.SYSNAME;
			runtimeDir.copyFile( action , "service" , targetFile );
			ShellExecutor session = action.getShell( account );
			session.custom( action , "chmod 744 " + targetFile );
		}
		else
			action.exitUnexpectedState();
	}

	public void createRootPath( ActionBase action ) throws Exception {
		RemoteFolder runtimeDir = new RemoteFolder( action.getNodeAccount( node ) , server.ROOTPATH );
		runtimeDir.ensureExists( action );
	}
	
	public void createBinPath( ActionBase action ) throws Exception {
		String path = Common.getPath( server.ROOTPATH , server.BINPATH );
		RemoteFolder runtimeDir = new RemoteFolder( action.getNodeAccount( node ) , path );
		runtimeDir.ensureExists( action );
	}
	
}
