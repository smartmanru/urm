package org.urm.action.build;


import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProjectItem;

public class ActionPatch extends ActionBase {

	public Builder builder;
	LocalFolder LOGDIR;
	
	public ActionPatch( ActionBase action , String stream , Builder builder , LocalFolder LOGDIR ) {
		super( action , stream );
		this.builder = builder;
		this.LOGDIR = LOGDIR;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		builder.createShell( this );
		if( !executePatch() )
			return( SCOPESTATE.RunFail );
		
		if( !executeGetArtefacts() )
			return( SCOPESTATE.RunFail );
		
		// update build data
		if( builder.builder.builders.registerBuild ) {
			if( !uploadBuildStatus() )
				return( SCOPESTATE.RunFail );
		}
		
		// remove directory if build was successful
		if( !context.CTX_SHOWALL )
			builder.removeExportedCode( this );

		return( SCOPESTATE.RunSuccess );
	}
	
	private boolean executePatch() throws Exception {
		// checkout sources
		if( !builder.exportCode( this ) ) {
			error( "patch: checkout failed" );
			return( false );
		}

		// execute source preprocessing
		if( !builder.prepareSource( this ) ) {
			error( "patch: prepare source failed" );
			return( false );
		}

		// check source code
		if( context.CTX_CHECK ) {
			if( !builder.checkSourceCode( this ) ) {
				error( "patch: source code invalid (" + builder.storage.buildFolder.folderPath + ". Exiting" );
				return( false );
			}
		}

		// build
		if( !builder.runBuild( this ) ) {
			error( "patch: build failed" );
			return( false );
		}

		return( true );
	}
	
	private boolean executeGetArtefacts() throws Exception {
		for( MetaSourceProjectItem item : builder.project.getItems() ) {
			if( builder.builder.isTargetLocal() || item.isTargetLocal() ) {
				if( !executeGetProjectArtefact( item ) )
					return( false );
			}
		}
		return( true );
	}

	private boolean executeGetProjectArtefact( MetaSourceProjectItem item ) throws Exception {
		LocalFolder downloadFolder = artefactory.getArtefactFolder( this , item.meta );
		LocalFolder codeFolder = builder.CODEPATH;
		
		if( item.isSourceDirectory() ) {
			LocalFolder codeDirFolder = codeFolder.getSubFolder( this , item.ITEMPATH );
			LocalFolder downloadDirFolder = downloadFolder.getSubFolder( this , item.ITEMBASENAME );
			if( !shell.checkDirExists( this , codeDirFolder.folderPath ) ) {
				String dir = shell.getLocalPath( codeDirFolder.folderPath );
				super.fail1( _Error.MissingProjectItemDirectory1 , "Missing project item directory: " + dir , dir );
				return( false );
			}
			
			shell.recreateDir( this , downloadDirFolder.folderPath );
			shell.copyDirContent( this , codeDirFolder.folderPath , downloadDirFolder.folderPath );
			return( true );
		}
		
		if( item.isSourceBasic() ) {
			LocalFolder codeDirFolder = codeFolder.getSubFolder( this , item.ITEMPATH );
			String file = item.ITEMBASENAME + item.ITEMEXTENSION;
			return( copyFile( codeDirFolder , downloadFolder , file ) );
		}
			
		if( item.isSourceStaticWar() ) {
			LocalFolder codeDirFolder = codeFolder.getSubFolder( this , item.ITEMPATH );
			String file = item.ITEMBASENAME + item.ITEMEXTENSION;
			if( !copyFile( codeDirFolder , downloadFolder , file ) )
				return( false );
			file = item.ITEMBASENAME + item.ITEMSTATICEXTENSION;
			if( !copyFile( codeDirFolder , downloadFolder , file ) )
				return( false );
			return( true );
		}

		super.exitUnexpectedState();
		return( false );
	}

	private boolean copyFile( LocalFolder codeDirFolder , LocalFolder downloadFolder , String file ) throws Exception {
		if( !shell.checkFileExists( this , codeDirFolder.folderPath , file ) ) {
			super.fail1( _Error.MissingProjectItemFile1 , "Missing project item file: " + file , file );
			return( false );
		}
		
		shell.copyFile( this , codeDirFolder.getFilePath( this , file ) , downloadFolder.getFilePath( this , file ) );
		return( true );
	}
	
	private boolean uploadBuildStatus() throws Exception {
		MetaProductBuildSettings build = getBuildSettings( builder.project.meta );
		String MODULE_PROJECT_NAME = builder.project.NAME;
		String MODULE_MSETTINGS="--settings=" + build.CONFIG_MAVEN_CFGFILE;
		String UPLOAD_MAVEN_VERSION = build.CONFIG_MAVEN_VERSION;

		MetaProductSettings product = builder.project.meta.getProductSettings( this );
		shell.export( this , "M2_HOME" , product.CONFIG_BUILDBASE_PATH + "/" + UPLOAD_MAVEN_VERSION );
		shell.export( this , "M2" , "$M2_HOME/bin" );
		shell.export( this , "PATH" , "$M2:$PATH" );
		shell.export( this , "MAVEN_OPTS" , Common.getQuoted( "-Xmx1g -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// upload versioninfo
		String FILENAME = builder.project.NAME + "-versioninfo.txt";
		LOGDIR.createFileFromString( this , FILENAME , builder.TAG );
		int timeout = setTimeoutUnlimited();
		int status = shell.customGetStatusNormal( this , "mvn deploy:deploy-file -B " +
			MODULE_MSETTINGS + " " +
			"-Durl=" + builder.getNexusPath( this , builder.project ) + " " +
			"-DuniqueVersion=false " +
			"-Dversion=" + builder.APPVERSION + " " +
			"-DgroupId=release " +
			"-DartifactId=" + MODULE_PROJECT_NAME + " " +
			"-Dfile=" + LOGDIR.getFilePath( this , FILENAME ) + " " +
			"-Dpackaging=txt " +
			"-Dclassifier=version " +
			"-DgeneratePom=true " +
			"-DrepositoryId=nexus2" );
		setTimeout( timeout );

		if( status != 0 ) {
			exit0( _Error.UnableRegisterBuildStatus0 , "uploadBuildStatus: unable to register build status" );
			return( false );
		}
		
		return( true );
	}
}
