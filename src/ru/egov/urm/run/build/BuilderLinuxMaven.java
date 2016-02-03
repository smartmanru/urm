package ru.egov.urm.run.build;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.BuildStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.vcs.ProjectVersionControl;

public class BuilderLinuxMaven extends Builder {

	public BuilderLinuxMaven( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( BUILDER , project , storage , TAG , BUILD_OPTIONS , APPVERSION );
	}

	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		return( action.context.pool.createDedicatedLocalShell( action , "build" ) );
	}

	@Override public boolean exportCode( ActionBase action ) throws Exception {
		// drop old
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	
		// checkout
		ProjectVersionControl vcs = new ProjectVersionControl( action ); 
		if( !vcs.export( CODEPATH , project , "" , TAG , "" ) ) {
			action.log( "patchCheckout: having problem to export code" );
			return( false );
		}
		
		return( true );
	}
	
	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		// handle module options

		action.log( "patchPrepareSource: prepare source code..." );

		LocalFolder CODEPATH = storage.buildFolder; 
		if( MODULEOPTIONS_POMNEW == true ) {
			action.log( "patchPrepareSource: prepare for new pom.xml..." );

			String cmd = "";
			cmd += "for x in $(find " + CODEPATH + " -name " + Common.getQuoted( "*.new" ) + "); do\n";
			cmd += "	FNAME_ORIGINAL=`echo $x | sed " + Common.getQuoted( "s/\\.new$//" ) + "`\n";
			cmd += "	rm $FNAME_ORIGINAL\n";
			cmd += "	cp $x $FNAME_ORIGINAL\n";
			cmd += "done";
			action.session.customCheckErrorsDebug( action , cmd );
		}

		if( MODULEOPTIONS_SETVERSION == true )
			action.session.customCheckErrorsDebug( action , "( cd " + CODEPATH + "; mvn versions:set -DnewVersion=" + APPVERSION + " )" );

		if( MODULEOPTIONS_REPLACESNAPSHOTS == true ) {
			action.log( "patchPrepareSource: replace snapshots..." );
			String NEXT_MAJORRELEASE = action.meta.product.CONFIG_NEXT_MAJORRELEASE;

			String cmd = "";
			cmd += "for fname in $(find " + CODEPATH + " -name pom.xml); do\n";
			cmd += "	echo patchPrepareSource: set $fname to " + NEXT_MAJORRELEASE + " from SNAPSHOT...\n";
			cmd += "	cat $fname | sed " + Common.getQuoted( "s/" + NEXT_MAJORRELEASE + "-SNAPSHOT/" + NEXT_MAJORRELEASE + "/g" ) + " > $fname-new\n";
			cmd += "	rm $fname\n";
			cmd += "	mv $fname-new $fname\n";
			cmd += "done";
			action.session.customCheckErrorsDebug( action , cmd );
		}
		
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		// check pom version
		LocalFolder CODEPATH = storage.buildFolder; 
		Document file = ConfReader.readXmlFile( action , CODEPATH.getFilePath( action , "pom.xml" ) );
		String MAIN_POM_VER = ConfReader.xmlGetPathNodeText( action , file , "project/version" );

		// check if property
		if( MAIN_POM_VER.startsWith( "${" ) ) {
			Node node = ConfReader.xmlGetPathNode( action , file , "project/properties" );
			if( node == null )
				action.exit( "unable to find project/properties in pom.xml " );
			
			String VAR = MAIN_POM_VER.substring( 2 , MAIN_POM_VER.length() - 1 );
			MAIN_POM_VER = ConfReader.xmlGetPathNodeText( action , node , VAR );
		}

		if( !MAIN_POM_VER.equals( APPVERSION ) ) {
			action.log( "invalid pom.xml version: " + MAIN_POM_VER + ", expected " + APPVERSION + ". Exiting" );
			return( false );
		}
		
		return( true );
	}

	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// maven params
		LocalFolder CODEPATH = storage.buildFolder; 
		String MODULE_MAVEN_PROFILES = action.meta.product.CONFIG_MAVEN_PROFILES;
		if( MODULEOPTIONS_COMPACT_STATIC == true ) {
			if( !MODULE_MAVEN_PROFILES.isEmpty() )
				MODULE_MAVEN_PROFILES += ",without-statics,without-jars";
			else
				MODULE_MAVEN_PROFILES = "without-statics,without-jars";
		}

		String NEXUS_PATH = getNexusPath( action , project );
		String MODULE_ALT_REPO = "-DaltDeploymentRepository=nexus2::default::" + NEXUS_PATH;
		String MODULE_MSETTINGS = "--settings=" + action.meta.product.CONFIG_MAVEN_CFGFILE;
		String MODULE_MAVEN_CMD = Common.getValueDefault( action.meta.product.CONFIG_MAVEN_CMD , "deploy" );
		String MAVEN_ADDITIONAL_OPTIONS = action.meta.product.CONFIG_MAVEN_ADDITIONAL_OPTIONS;
		if( action.context.CTX_SHOWALL )
			MAVEN_ADDITIONAL_OPTIONS += " -X";

		action.log( "build PATCHPATH=" + CODEPATH.folderPath + ", profile=" + MODULE_MAVEN_PROFILES + ", options=" + MAVEN_ADDITIONAL_OPTIONS + ", cmd=" + MODULE_MAVEN_CMD + 
				" using maven to nexus path " + NEXUS_PATH + "..." );

		// set environment
		String BUILD_JAVA_VERSION = project.getJavaVersion( action );
		String BUILD_MAVEN_VERSION = project.getBuilderVersion( action ); 
		String MAVEN_CMD = "mvn -B -P " + MODULE_MAVEN_PROFILES + " " + MAVEN_ADDITIONAL_OPTIONS + " clean " + 
				MODULE_MAVEN_CMD + " " + MODULE_ALT_REPO + " " + MODULE_MSETTINGS + " -Dmaven.test.skip=true";

		ShellExecutor session = action.session;
		session.export( action , "JAVA_HOME" , action.meta.product.CONFIG_BUILDBASE + "/" + BUILD_JAVA_VERSION );
		session.export( action , "PATH" , "$JAVA_HOME/bin:$PATH" );
		session.export( action , "M2_HOME" , action.meta.product.CONFIG_BUILDBASE + "/" + BUILD_MAVEN_VERSION );
		session.export( action , "M2" , "$M2_HOME/bin" );
		session.export( action , "PATH" , "$M2:$PATH" );
		session.export( action , "MAVEN_OPTS" , Common.getQuoted( "-XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// execute maven
		session.cd( action , CODEPATH.folderPath );
		action.log( "using maven:" );
		session.customCheckErrorsNormal( action , "which mvn" );
		session.customCheckErrorsNormal( action , "mvn --version" );
		
		action.log( "execute: " + MAVEN_CMD );
		session.setTimeoutUnlimited( action );
		int status = session.customGetStatusNormal( action , MAVEN_CMD );

		if( status != 0 ) {
			action.log( "buildMaven: maven build failed" );
			return( false );
		}
					
		action.log( "buildMaven: maven build successfully finished" );
		return( true );
	}

	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	}

}
