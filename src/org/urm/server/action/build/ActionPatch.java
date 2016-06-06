package org.urm.server.action.build;


import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.LocalFolder;

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
		info( "ActionPatch: BUILDER=" + builder.BUILDER + ", BUILDMODE=" + context.getBuildModeName() + ", CATEGORY=" + Common.getEnumLower( builder.project.CATEGORY ) + ", PROJECT=" + builder.project.PROJECT + 
				", REPOSITORY=" + builder.project.REPOSITORY + ", VCS=" + builder.project.getVCS( this ) + ", VCSPATH=" + builder.project.PATH + 
				", VCSREPO=" + builder.project.REPOSITORY + ", TAG=" + builder.TAG + ", VERSION=" + builder.APPVERSION + ", NEXUS_PATH=" + builder.getNexusPath( this , builder.project ) );

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

		// update build data
		if( !uploadBuildStatus() )
			return( false );
		
		// remove directory if build was successful
		if( !context.CTX_SHOWALL )
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
		int timeout = setTimeoutUnlimited();
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
		setTimeout( timeout );

		if( status != 0 ) {
			exit( "uploadBuildStatus: unable to register build status" );
			return( false );
		}
		
		return( true );
	}
}
