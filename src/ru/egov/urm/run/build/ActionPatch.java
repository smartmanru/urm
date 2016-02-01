package ru.egov.urm.run.build;


import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.LocalFolder;

public class ActionPatch extends ActionBase {

	public Builder builder;
	LocalFolder LOGDIR;
	
	public ActionPatch( ActionBase action , String stream , Builder builder , LocalFolder LOGDIR ) {
		super( action , stream );
		this.builder = builder;
		this.LOGDIR = LOGDIR;
	}

	@Override protected boolean executeSimple() throws Exception {
		LOGDIR.ensureExists( this );
		
		String logFile = LOGDIR.getFilePath( this , builder.project.PROJECT + "-build.log" );
		super.startRedirect( "PROJECT BUILD LOG:" , logFile );
		log( "ActionPatch: BUILDMODE=" + context.getBuildModeName() + ", CATEGORY=" + Common.getEnumLower( builder.project.CATEGORY ) + ", PROJECT=" + builder.project.PROJECT + 
				", REPOSITORY=" + builder.project.REPOSITORY + ", VCS=" + builder.project.getVCS( this ) + ", VCSPATH=" + builder.project.PATH + 
				", VCSREPO=" + builder.project.REPOSITORY + ", TAG=" + builder.TAG + ", VERSION=" + builder.APPVERSION + ", NEXUS_PATH=" + builder.getNexusPath( this , builder.project ) + ", BUILD_OPTIONS=" + builder.BUILD_OPTIONS );

		try {
			if( !executePatch() )
				super.setFailed();
			
			super.stopRedirect();
		}
		catch( Exception e ) {
			log( e );
			super.stopRedirect();
			throw e;
		}
		
		return( true );
	}
	
	private boolean executePatch() throws Exception {
		// checkout sources
		if( !builder.exportCode( this ) ) {
			log( "patch: checkout failed" );
			return( false );
		}

		// execute source preprocessing
		if( !builder.prepareSource( this ) ) {
			log( "patch: prepare source failed" );
			return( false );
		}

		// check source code
		if( options.OPT_CHECK ) {
			if( !builder.checkSourceCode( this ) ) {
				log( "patch: maven build skipped - source code invalid (" + builder.storage.buildFolder.folderPath + ". Exiting" );
				return( false );
			}
		}

		// build
		if( !builder.runBuild( this ) ) {
			log( "patch: build failed" );
			return( false );
		}

		// update build data
		if( !uploadBuildStatus() )
			return( false );
		
		// remove directory if build was successful
		builder.removeExportedCode( this );

		return( true );
	}
	
	private boolean uploadBuildStatus() throws Exception {
		String MODULE_PROJECT_NAME = builder.project.PROJECT;
		String MODULE_MSETTINGS="--settings=" + meta.product.CONFIG_MAVEN_CFGFILE;
		String UPLOAD_MAVEN_VERSION = meta.product.CONFIG_MAVEN_VERSION;

		session.export( this , "M2_HOME" , meta.product.CONFIG_BUILDBASE + "/" + UPLOAD_MAVEN_VERSION );
		session.export( this , "M2" , "$M2_HOME/bin" );
		session.export( this , "PATH" , "$M2:$PATH" );
		session.export( this , "MAVEN_OPTS" , Common.getQuoted( "-Xmx1g -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// upload versioninfo
		String FILENAME = builder.project.PROJECT + "-versioninfo.txt";
		LOGDIR.createFileFromString( this , FILENAME , builder.TAG );
		session.setTimeoutUnlimited( this );
		int status = session.customGetStatusNormal( this , "mvn deploy:deploy-file -B " +
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

		if( status != 0 ) {
			exit( "uploadBuildStatus: unable to register build status" );
			return( false );
		}
		
		return( true );
	}
}
