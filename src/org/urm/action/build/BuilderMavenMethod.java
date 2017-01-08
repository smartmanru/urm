package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class BuilderMavenMethod extends Builder {

	boolean MODULEOPTIONS_WAR = false;
	boolean MODULEOPTIONS_POMNEW = false;
	boolean MODULEOPTIONS_SETVERSION = false;
	boolean MODULEOPTIONS_REPLACESNAPSHOTS = false;
	boolean MODULEOPTIONS_COMPACT_STATIC = false;

	public BuilderMavenMethod( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		super( builder , project , storage , TAG , APPVERSION );
	}

	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		return( action.createDedicatedShell( "build" ) );
	}

	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		// handle module options

		action.info( "patchPrepareSource: prepare source code..." );

		LocalFolder CODEPATH = storage.buildFolder; 
		if( MODULEOPTIONS_POMNEW == true ) {
			action.info( "patchPrepareSource: prepare for new pom.xml..." );

			String cmd = "";
			cmd += "for x in $(find " + CODEPATH + " -name " + Common.getQuoted( "*.new" ) + "); do\n";
			cmd += "	FNAME_ORIGINAL=`echo $x | sed " + Common.getQuoted( "s/\\.new$//" ) + "`\n";
			cmd += "	rm $FNAME_ORIGINAL\n";
			cmd += "	cp $x $FNAME_ORIGINAL\n";
			cmd += "done";
			action.shell.customCheckErrorsDebug( action , cmd );
		}

		if( MODULEOPTIONS_SETVERSION == true )
			action.shell.customCheckErrorsDebug( action , CODEPATH.folderPath , "mvn versions:set -DnewVersion=" + APPVERSION );

		if( MODULEOPTIONS_REPLACESNAPSHOTS == true ) {
			action.info( "patchPrepareSource: replace snapshots..." );
			MetaProductBuildSettings build = action.getBuildSettings( project.meta );
			String NEXT_MAJORRELEASE = build.CONFIG_RELEASE_NEXTMAJOR;

			String cmd = "";
			cmd += "for fname in $(find " + CODEPATH + " -name pom.xml); do\n";
			cmd += "	echo patchPrepareSource: set $fname to " + NEXT_MAJORRELEASE + " from SNAPSHOT...\n";
			cmd += "	cat $fname | sed " + Common.getQuoted( "s/" + NEXT_MAJORRELEASE + "-SNAPSHOT/" + NEXT_MAJORRELEASE + "/g" ) + " > $fname-new\n";
			cmd += "	rm $fname\n";
			cmd += "	mv $fname-new $fname\n";
			cmd += "done";
			action.shell.customCheckErrorsDebug( action , cmd );
		}
		
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		// check pom version
		LocalFolder CODEPATH = storage.buildFolder; 
		Document file = action.readXmlFile( CODEPATH.getFilePath( action , "pom.xml" ) );
		String MAIN_POM_VER = ConfReader.xmlGetPathNodeText( file , "project/version" );

		// check if property
		if( MAIN_POM_VER.startsWith( "${" ) ) {
			Node node = ConfReader.xmlGetPathNode( file , "project/properties" );
			if( node == null )
				action.exit0( _Error.NoPropertiesInPom0 , "unable to find project/properties in pom.xml" );
			
			String VAR = MAIN_POM_VER.substring( 2 , MAIN_POM_VER.length() - 1 );
			MAIN_POM_VER = ConfReader.xmlGetPathNodeText( node , VAR );
		}

		if( !MAIN_POM_VER.equals( APPVERSION ) ) {
			action.error( "invalid pom.xml version: " + MAIN_POM_VER + ", expected " + APPVERSION + ". Exiting" );
			return( false );
		}
		
		return( true );
	}

	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// maven params
		MetaProductBuildSettings build = action.getBuildSettings( project.meta );

		String NEXUS_PATH = getNexusPath( action , project );
		String MODULE_ALT_REPO = "-DaltDeploymentRepository=nexus2::default::" + NEXUS_PATH;
		String MODULE_MSETTINGS = "--settings=" + build.CONFIG_MAVEN_CFGFILE;
		String MODULE_MAVEN_CMD = "deploy";
		String MODULE_ADDITIONAL_OPTIONS = super.getVarString( action , project.BUILDER_ADDOPTIONS );
		if( action.context.CTX_SHOWALL )
			MODULE_ADDITIONAL_OPTIONS += " -X";

		action.info( "build PATCHPATH=" + CODEPATH.folderPath + ", options=" + MODULE_ADDITIONAL_OPTIONS + ", cmd=" + MODULE_MAVEN_CMD + 
				" using maven to nexus path " + NEXUS_PATH + "..." );

		// set environment
		String BUILD_JAVA_HOME = builder.JAVA_JDKHOMEPATH;
		String BUILD_MAVEN_HOME = builder.MAVEN_HOMEPATH; 
		String MAVEN_CMD = "mvn -B " + MODULE_ADDITIONAL_OPTIONS + " clean " + 
				MODULE_MAVEN_CMD + " " + MODULE_ALT_REPO + " " + MODULE_MSETTINGS + " -Dmaven.test.skip=true";

		ShellExecutor session = action.shell;
		session.export( action , "JAVA_HOME" , session.getLocalPath( BUILD_JAVA_HOME ) );
		session.export( action , "PATH" , session.getLocalPath( session.getVariable( "JAVA_HOME" ) + "/bin" ) + session.getPathBreak() +
				session.getVariable( "PATH" ) );
		session.export( action , "M2_HOME" , session.getLocalPath( BUILD_MAVEN_HOME ) );
		session.export( action , "M2" , session.getLocalPath( session.getVariable( "M2_HOME" ) + "/bin" ) );
		session.export( action , "PATH" , session.getVariable( "M2" ) + session.getPathBreak() + session.getVariable( "PATH" ) );
		session.export( action , "MAVEN_OPTS" , Common.getQuoted( "-XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// execute maven
		action.info( "using maven:" );
		session.customCheckErrorsNormal( action , "mvn --version" );
		
		action.info( "execute: " + MAVEN_CMD );
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusNormal( action , CODEPATH.folderPath , MAVEN_CMD );
		action.setTimeout( timeout );

		if( status != 0 ) {
			action.error( "buildMaven: maven build failed" );
			return( false );
		}
					
		action.info( "buildMaven: maven build successfully finished" );
		return( true );
	}

	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	}

}
