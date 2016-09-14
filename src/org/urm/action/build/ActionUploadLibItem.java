package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.engine.ServerAuthResource;
import org.urm.engine.meta.MetaProductBuildSettings;

public class ActionUploadLibItem extends ActionBase {

	String GROUPID;
	String FILE;
	String ARTEFACTID;
	String VERSION;
	String CLASSIFIER;
	
	public ActionUploadLibItem( ActionBase action , String stream , String GROUPID , String FILE , String ARTEFACTID , String VERSION , String CLASSIFIER ) {
		super( action , stream );
		this.GROUPID = GROUPID;
		this.FILE = FILE;
		this.ARTEFACTID = ARTEFACTID;
		this.VERSION = VERSION;
		this.CLASSIFIER = CLASSIFIER;
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		// set environment
		MetaProductBuildSettings build = getBuildSettings();
		String BUILD_JAVA_VERSION = build.CONFIG_MAVEN_JAVA_VERSION;
		String BUILD_MAVEN_VERSION = build.CONFIG_MAVEN_VERSION;

		shell.export( this , "JAVA_HOME" , meta.product.CONFIG_BUILDBASE_PATH + "/" + BUILD_JAVA_VERSION );
		shell.export( this , "PATH" , "$JAVA_HOME/bin:$PATH" );
		shell.export( this , "M2_HOME" , meta.product.CONFIG_BUILDBASE_PATH + "/" + BUILD_MAVEN_VERSION );
		shell.export( this , "M2" , "$M2_HOME/bin" );
		shell.export( this , "PATH" , "$M2:$PATH" );
		shell.export( this , "MAVEN_OPTS" , Common.getQuoted( "-XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );

		// upload
		if( GROUPID.isEmpty() || FILE.isEmpty() )
			exitUnexpectedState();
		
		if( !shell.checkFileExists( this , FILE ) )
		     exit1( _Error.MissingUploadFile1 , "unknown file " + FILE , FILE );
		
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
		ServerAuthResource res = getResource( build.CONFIG_NEXUS_RESOURCE );
		if( F_EXTENSION.equals( "pom" ) ) {
	        CMD = "mvn -e deploy:deploy-file --settings=$HOME/.m2/settings.branch.xml" + 
	        	" -DupdateReleaseInfo=true -DuniqueVersion=false -DrepositoryId=nexus" + 
	        	" -Durl=" + res.BASEURL + "/" + F_TARGETREP + " -Dpackaging=" + F_EXTENSION + 
	        	" -DgeneratePom=false -DgroupId=" + GROUPID + " -Dversion=" + F_VERSION + 
	        	" -DartifactId=" + F_ARTEFACTID + " " + F_CLASSIFIER + " -Dfile=" + FILE;
		}
		else {
			CMD = "mvn -e deploy:deploy-file --settings=$HOME/.m2/settings.branch.xml" +
				" -DupdateReleaseInfo=true -DuniqueVersion=false -DrepositoryId=nexus" +
				" -Durl=" + res.BASEURL + "/" + F_TARGETREP + " -Dpackaging=" + F_EXTENSION +
				" -DgeneratePom=true -DgroupId=" + GROUPID + " -Dversion=" + F_VERSION + 
				" -DartifactId=" + F_ARTEFACTID + " " + F_CLASSIFIER + " -Dfile=" + FILE;
		}
		
		shell.custom( this , CMD );
		return( true );
	}
	
}
