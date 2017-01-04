package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductBuildSettings;

public class ActionUploadLibItem extends ActionBase {

	Meta meta;
	String GROUPID;
	String FILE;
	String ARTEFACTID;
	String VERSION;
	String CLASSIFIER;
	
	public ActionUploadLibItem( ActionBase action , String stream , Meta meta , String GROUPID , String FILE , String ARTEFACTID , String VERSION , String CLASSIFIER ) {
		super( action , stream );
		
		this.meta = meta;
		this.GROUPID = GROUPID;
		this.FILE = FILE;
		this.ARTEFACTID = ARTEFACTID;
		this.VERSION = VERSION;
		this.CLASSIFIER = CLASSIFIER;
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		if( !scopeProject.sourceProject.isPrebuiltNexus() )
			super.exitUnexpectedState();
		
		// set environment
		ServerBuilders builders = super.getBuilders();
		MetaProductBuildSettings build = getBuildSettings( meta );
		String BUILD_MSETTINGS="--settings=" + shell.getLocalPath( build.CONFIG_MAVEN_CFGFILE );
		String BUILD_JAVA_HOME = shell.getLocalPath( builders.JAVA_HOMEPATH );
		String BUILD_MAVEN_HOME = shell.getLocalPath( builders.MAVEN_HOMEPATH );
		String BUILD_FILE = shell.getLocalPath( FILE );

		shell.export( this , "JAVA_HOME" , BUILD_JAVA_HOME );
		shell.export( this , "M2_HOME" , BUILD_MAVEN_HOME );
		shell.export( this , "M2" , shell.getLocalPath( shell.getVariable( "M2_HOME" ) + "/bin" ) );
		shell.export( this , "PATH" , shell.getLocalPath( shell.getVariable( "JAVA_HOME" ) + "/bin" ) + shell.getPathBreak() +
				shell.getVariable( "M2" ) + shell.getPathBreak() +
				shell.getVariable( "PATH" ) );
		shell.export( this , "MAVEN_OPTS" , Common.getQuoted( "-XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// upload
		if( GROUPID.isEmpty() || FILE.isEmpty() )
			exitUnexpectedState();
		
		if( !shell.checkFileExists( this , FILE ) )
		     exit1( _Error.MissingUploadFile1 , "unknown file " + BUILD_FILE , BUILD_FILE );
		
		// extract extension
		String F_BASENAME = Common.getBaseName( FILE );
		String F_EXTENSION = Common.getExtension( F_BASENAME );
		String F_NOEXTENSION = Common.getBaseNameNoExtension( F_BASENAME );
		String F_TARGETREP = "content/repositories/thirdparty";

		String F_ARTEFACTID;
		String F_VERSION;
		if( ARTEFACTID.isEmpty() ) {
	        // extract artefact ID and version from file
	        F_ARTEFACTID = F_NOEXTENSION.replaceAll( "-[0-9.]*$" , "" );
	        F_VERSION = Common.getPartAfterLast( F_NOEXTENSION , F_ARTEFACTID + "-" );
		}
		else {
	        if( VERSION.isEmpty() ) {
                // extract version from file
                F_ARTEFACTID = ARTEFACTID;
                F_VERSION = Common.getPartAfterLast( F_NOEXTENSION , F_ARTEFACTID + "-" );
	        }
	        else {
                F_ARTEFACTID = ARTEFACTID;
                F_VERSION = VERSION;
	        }
	    }

		String F_CLASSIFIER = "";
		if( !CLASSIFIER.isEmpty() )
			F_CLASSIFIER = "-Dclassifier=" + CLASSIFIER;

		String CMD;
		ServerAuthResource res = getResource( scopeProject.sourceProject.RESOURCE );
		if( F_EXTENSION.equals( "pom" ) ) {
	        CMD = "mvn -e deploy:deploy-file " + BUILD_MSETTINGS + 
	        	" -DupdateReleaseInfo=true -DuniqueVersion=false -DrepositoryId=nexus" + 
	        	" -Durl=" + res.BASEURL + "/" + F_TARGETREP + " -Dpackaging=" + F_EXTENSION + 
	        	" -DgeneratePom=false -DgroupId=" + GROUPID + " -Dversion=" + F_VERSION + 
	        	" -DartifactId=" + F_ARTEFACTID + " " + F_CLASSIFIER + " -Dfile=" + BUILD_FILE;
		}
		else {
			CMD = "mvn -e deploy:deploy-file " + BUILD_MSETTINGS +
				" -DupdateReleaseInfo=true -DuniqueVersion=false -DrepositoryId=nexus" +
				" -Durl=" + res.BASEURL + "/" + F_TARGETREP + " -Dpackaging=" + F_EXTENSION +
				" -DgeneratePom=true -DgroupId=" + GROUPID + " -Dversion=" + F_VERSION + 
				" -DartifactId=" + F_ARTEFACTID + " " + F_CLASSIFIER + " -Dfile=" + BUILD_FILE;
		}
		
		shell.custom( this , CMD );
		return( SCOPESTATE.RunSuccess );
	}
	
}
