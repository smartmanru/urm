package ru.egov.urm.run.build;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.storage.BuildStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.vcs.ProjectVersionControl;

public class ActionPatch extends ActionBase {

	boolean MODULEOPTIONS_WAR = false;
	boolean MODULEOPTIONS_POMNEW = false;
	boolean MODULEOPTIONS_SETVERSION = false;
	boolean MODULEOPTIONS_REPLACESNAPSHOTS = false;
	boolean MODULEOPTIONS_COMPACT_STATIC = false;
	
	String NEXUS_PATH;
	
	LocalFolder OUTDIR;
	String TAG;
	String BUILD_OPTIONS;
	String APPVERSION;
	
	public ActionPatch( ActionBase action , String stream , LocalFolder OUTDIR , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( action , stream );
		this.OUTDIR = OUTDIR;
		this.TAG = TAG;
		this.BUILD_OPTIONS = BUILD_OPTIONS;
		this.APPVERSION = APPVERSION;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		LocalFolder BUILDDIR = OUTDIR.getSubFolder( this , scopeProject.set.NAME );
		BUILDDIR.ensureExists( this );
		
		NEXUS_PATH = meta.product.CONFIG_NEXUS_BASE + "/content/repositories/" + meta.product.CONFIG_NEXUS_REPO;
		
		// war build
		if( BUILD_OPTIONS.indexOf( 'w' ) >= 0 )
			MODULEOPTIONS_WAR = true;
		// replace original files with .new ones
		if( BUILD_OPTIONS.indexOf( 'n' ) >= 0 )
			MODULEOPTIONS_POMNEW = true;
		// add profile for war build
		if( BUILD_OPTIONS.indexOf( 's' ) >= 0 )
			MODULEOPTIONS_COMPACT_STATIC = true;
		// force set version
		if( BUILD_OPTIONS.indexOf( 'v' ) >= 0 )
			MODULEOPTIONS_SETVERSION = true;
		// clear all snapshots from release
		if( BUILD_OPTIONS.indexOf( 'r' ) >= 0 )
			MODULEOPTIONS_REPLACESNAPSHOTS = true;
		
		String logFile = BUILDDIR.getFilePath( this , scopeProject.sourceProject.PROJECT + "-build.log" );
		super.startRedirect( "PROJECT BUILD LOG:" , logFile );
		log( "ActionPatch: BUILDMODE=" + context.getBuildModeName() + ", CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", PROJECT=" + scopeProject.sourceProject.PROJECT + 
				", REPOSITORY=" + scopeProject.sourceProject.REPOSITORY + ", VCS=" + scopeProject.sourceProject.getVCS( this ) + ", VCSPATH=" + scopeProject.sourceProject.PATH + 
				", VCSREPO=" + scopeProject.sourceProject.REPOSITORY + ", TAG=" + TAG + ", VERSION=" + APPVERSION + ", NEXUS_PATH=" + NEXUS_PATH + ", BUILD_OPTIONS=" + BUILD_OPTIONS );

		try {
			if( !executePatch( scopeProject.sourceProject , TAG , APPVERSION ) )
				super.setFailed();
		}
		finally {
			super.stopRedirect();
		}
		
		return( true );
	}
	
	private boolean executePatch( MetaSourceProject sourceProject , String TAG , String APPVERSION ) throws Exception {
		BuildStorage storage = artefactory.getEmptyBuildStorage( this , sourceProject );
		LocalFolder PATCHPATH = storage.buildFolder;
		LocalFolder CODEPATH = PATCHPATH.getSubFolder( this , sourceProject.CODEPATH ); 

		// checkout sources
		if( !patchExport( PATCHPATH , sourceProject , TAG , APPVERSION ) ) {
			log( "patch: checkout failed" );
			return( false );
		}

		// execute source preprocessing
		if( !patchPrepareSource( CODEPATH , sourceProject , APPVERSION ) ) {
			log( "patch: prepare source failed" );
			return( false );
		}

		// check source code
		if( !patchCheckSourceCode( CODEPATH , sourceProject , APPVERSION ) ) {
			log( "patch: maven build skipped - source code invalid (" + PATCHPATH.folderPath + ". Exiting" );
			return( false );
		}

		// build
		if( !patchBuild( CODEPATH , sourceProject , TAG , APPVERSION ) ) {
			log( "patch: build failed" );
			return( false );
		}

		// remove directory if build was successful
		PATCHPATH.removeThis( this );
		return( true );
	}
	
	private boolean patchExport( LocalFolder PATCHPATH , MetaSourceProject project , String TAG , String APPVERSION ) throws Exception {
		// drop old
		PATCHPATH.removeThis( this );
	
		// checkout
		ProjectVersionControl vcs = new ProjectVersionControl( this ); 
		if( !vcs.export( PATCHPATH , project , "" , TAG , "" ) ) {
			log( "patchCheckout: having problem to export code" );
			return( false );
		}
		
		return( true );
	}

	private boolean patchPrepareSource( LocalFolder PATCHPATH , MetaSourceProject project , String APPVERSION ) throws Exception {
		// handle module options

		log( "patchPrepareSource: prepare source code..." );

		if( MODULEOPTIONS_POMNEW == true ) {
			log( "patchPrepareSource: prepare for new pom.xml..." );

			String cmd = "";
			cmd += "for x in $(find " + PATCHPATH + " -name " + Common.getQuoted( "*.new" ) + "); do\n";
			cmd += "	FNAME_ORIGINAL=`echo $x | sed " + Common.getQuoted( "s/\\.new$//" ) + "`\n";
			cmd += "	rm $FNAME_ORIGINAL\n";
			cmd += "	cp $x $FNAME_ORIGINAL\n";
			cmd += "done";
			session.customCheckErrorsDebug( this , cmd );
		}

		if( MODULEOPTIONS_SETVERSION == true )
			session.customCheckErrorsDebug( this , "( cd " + PATCHPATH + "; mvn versions:set -DnewVersion=" + APPVERSION + " )" );

		if( MODULEOPTIONS_REPLACESNAPSHOTS == true ) {
			log( "patchPrepareSource: replace snapshots..." );
			String NEXT_MAJORRELEASE = meta.product.CONFIG_NEXT_MAJORRELEASE;

			String cmd = "";
			cmd += "for fname in $(find " + PATCHPATH + " -name pom.xml); do\n";
			cmd += "	echo patchPrepareSource: set $fname to " + NEXT_MAJORRELEASE + " from SNAPSHOT...\n";
			cmd += "	cat $fname | sed " + Common.getQuoted( "s/" + NEXT_MAJORRELEASE + "-SNAPSHOT/" + NEXT_MAJORRELEASE + "/g" ) + " > $fname-new\n";
			cmd += "	rm $fname\n";
			cmd += "	mv $fname-new $fname\n";
			cmd += "done";
			session.customCheckErrorsDebug( this , cmd );
		}
		
		return( true );
	}

	private boolean patchCheckSourceCode( LocalFolder PATCHFOLDER , MetaSourceProject project , String APPVERSION ) throws Exception {
		String BUILDER = project.getBuilder( this ); 
		if( !BUILDER.equals( "maven" ) )
			return( true );
		
		// check pom version
		Document file = ConfReader.readXmlFile( this , PATCHFOLDER.getFilePath( this , "pom.xml" ) );
		String MAIN_POM_VER = ConfReader.xmlGetPathNodeText( this , file , "project/version" );

		// check if property
		if( MAIN_POM_VER.startsWith( "${" ) ) {
			Node node = ConfReader.xmlGetPathNode( this , file , "project/properties" );
			if( node == null )
				exit( "unable to find project/properties in pom.xml " );
			
			String VAR = MAIN_POM_VER.substring( 2 , MAIN_POM_VER.length() - 1 );
			MAIN_POM_VER = ConfReader.getRequiredPropertyValue( this , node , VAR );
		}

		if( !MAIN_POM_VER.equals( APPVERSION ) ) {
			log( "invalid pom.xml version: " + MAIN_POM_VER + ", expected " + APPVERSION + ". Exiting" );
			return( false );
		}
		
		return( true );
	}

	private boolean patchBuild( LocalFolder PATCHFOLDER , MetaSourceProject project , String TAG , String APPVERSION ) throws Exception {
		String BUILDER = project.getBuilder( this );

		// build
		if( BUILDER.equals( "maven" ) ) {
			if( !buildMaven( PATCHFOLDER , project , APPVERSION ) )
				return( false );
		}
		else if( BUILDER.equals( "gradle" ) ) {
			if( !buildGradle( PATCHFOLDER , project , APPVERSION ) )
				return( false );
		}
		else {
			log( "unknown builder=" + BUILDER );
			return( false );
		}

		if( !uploadBuildStatus( PATCHFOLDER , project , TAG , APPVERSION ) )
			return( false );
		
		return( true );
	}
	
	private boolean buildMaven( LocalFolder PATCHFOLDER , MetaSourceProject project , String APPVERSION ) throws Exception {
		// maven params
		String MODULE_MAVEN_PROFILES = meta.product.CONFIG_MAVEN_PROFILES;
		if( MODULEOPTIONS_COMPACT_STATIC == true ) {
			if( !MODULE_MAVEN_PROFILES.isEmpty() )
				MODULE_MAVEN_PROFILES += ",without-statics,without-jars";
			else
				MODULE_MAVEN_PROFILES = "without-statics,without-jars";
		}

		String MODULE_ALT_REPO = "-DaltDeploymentRepository=nexus2::default::" + NEXUS_PATH;
		String MODULE_MSETTINGS = "--settings=" + meta.product.CONFIG_MAVEN_CFGFILE;
		String MODULE_MAVEN_CMD = Common.getValueDefault( meta.product.CONFIG_MAVEN_CMD , "deploy" );
		String MAVEN_ADDITIONAL_OPTIONS = meta.product.CONFIG_MAVEN_ADDITIONAL_OPTIONS;

		log( "build PATCHPATH=" + PATCHFOLDER.folderPath + ", profile=" + MODULE_MAVEN_PROFILES + ", options=" + MAVEN_ADDITIONAL_OPTIONS + ", cmd=" + MODULE_MAVEN_CMD + 
				" using maven to nexus path " + NEXUS_PATH + "..." );

		// set environment
		String BUILD_JAVA_VERSION = project.getJavaVersion( this );
		String BUILD_MAVEN_VERSION = project.getBuilderVersion( this ); 
		String MAVEN_CMD = "mvn -B -P " + MODULE_MAVEN_PROFILES + " " + MAVEN_ADDITIONAL_OPTIONS + " clean " + 
				MODULE_MAVEN_CMD + " " + MODULE_ALT_REPO + " " + MODULE_MSETTINGS + " -Dmaven.test.skip=true";

		session.export( this , "JAVA_HOME" , meta.product.CONFIG_BUILDBASE + "/" + BUILD_JAVA_VERSION );
		session.export( this , "PATH" , "$JAVA_HOME/bin:$PATH" );
		session.export( this , "M2_HOME" , meta.product.CONFIG_BUILDBASE + "/" + BUILD_MAVEN_VERSION );
		session.export( this , "M2" , "$M2_HOME/bin" );
		session.export( this , "PATH" , "$M2:$PATH" );
		session.export( this , "MAVEN_OPTS" , Common.getQuoted( "-Xmx1g -XX:MaxPermSize=300m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// execute maven
		session.cd( this , PATCHFOLDER.folderPath );
		log( "using maven:" );
		session.customCheckErrorsNormal( this , "which mvn" );
		session.customCheckErrorsNormal( this , "mvn --version" );
		
		log( "execute: " + MAVEN_CMD );
		int status = session.customGetStatusNormal( this , MAVEN_CMD );

		if( status != 0 ) {
			log( "buildMaven: maven build failed" );
			return( false );
		}
					
		return( true );
	}

	private boolean buildGradle( LocalFolder PATCHFOLDER , MetaSourceProject project , String APPVERSION ) throws Exception {
		// set java and gradle environment
		String BUILD_JAVA_VERSION = project.getJavaVersion( this );
		String BUILD_GRADLE_VERSION = project.getBuilderVersion( this ); 

		session.export( this , "JAVA_HOME" , meta.product.CONFIG_BUILDBASE + "/" + BUILD_JAVA_VERSION );
		session.export( this , "GR_HOME" , meta.product.CONFIG_BUILDBASE + "/" + BUILD_GRADLE_VERSION );
		session.export( this , "GR" , "$GR_HOME/bin" );
		session.export( this , "PATH" , "$GR:$JAVA_HOME/bin:$PATH" );

		String GRADLE_CMD = "gradle clean war publish -Dmaven.settings=" + meta.product.CONFIG_MAVEN_CFGFILE;

		// execute gradle
		session.cd( this , PATCHFOLDER.folderPath );
		log( "using gradle:" );
		session.customCheckErrorsNormal( this , "which gradle" );
		session.customCheckErrorsNormal( this , "gradle --version" );
		
		log( "execute: " + GRADLE_CMD );
		int status = session.customGetStatusNormal( this , GRADLE_CMD );

		if( status != 0 ) {
			log( "buildGradle: gradle build failed" );
			return( false );
		}
		
		return( true );
	}

	private boolean uploadBuildStatus( LocalFolder PATCHFOLDER , MetaSourceProject project , String TAG , String VERSION ) throws Exception {
		String MODULE_PROJECT_NAME = project.PROJECT;
		String MODULE_MSETTINGS="--settings=" + meta.product.CONFIG_MAVEN_CFGFILE;
		String UPLOAD_MAVEN_VERSION = meta.product.CONFIG_MAVEN_VERSION;

		session.export( this , "M2_HOME" , meta.product.CONFIG_BUILDBASE + "/" + UPLOAD_MAVEN_VERSION );
		session.export( this , "M2" , "$M2_HOME/bin" );
		session.export( this , "PATH" , "$M2:$PATH" );
		session.export( this , "MAVEN_OPTS" , Common.getQuoted( "-Xmx1g -XX:MaxPermSize=300m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// upload versioninfo
		PATCHFOLDER.createFileFromString( this , "versioninfo.txt" , TAG );
		int status = session.customGetStatusNormal( this , "mvn deploy:deploy-file -B " +
			MODULE_MSETTINGS + " " +
			"-Durl=" + NEXUS_PATH + " " +
			"-DuniqueVersion=false " +
			"-Dversion=" + VERSION + " " +
			"-DgroupId=release " +
			"-DartifactId=" + MODULE_PROJECT_NAME + " " +
			"-Dfile=versioninfo.txt " +
			"-Dpackaging=txt " +
			"-Dclassifier=version " +
			"-DgeneratePom=true " +
			"-DrepositoryId=nexus2" );

		if( status != 0 ) {
			exit( "uploadBuildStatus: unable to register build status" );
			return( false );
		}
		
		PATCHFOLDER.removeFiles( this , "versioninfo.txt" );
		return( true );
	}
}
