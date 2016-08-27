package org.urm.server.action.build;

import java.util.List;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.dist.Dist;
import org.urm.server.meta.MetaProductBuildSettings;

public class ActionUploadReleaseItem extends ActionBase {

	Dist release;
	
	public ActionUploadReleaseItem( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		// load distr data for cross-product exports - thirdparty
		List<ActionScopeTargetItem> items = scopeProject.getItems( this );
		
		// set maven
		MetaProductBuildSettings build = getBuildSettings();
		shell.export( this , "M2_HOME" , "/usr/local/apache-maven-" + build.CONFIG_MAVEN_VERSION );
		shell.export( this , "M2" , "$M2_HOME/bin; export PATH=" + Common.getQuoted( "$PATH:$M2" ) );
		shell.export( this , "JAVA_HOME" , "/usr/java/" + build.CONFIG_JAVA_VERSION );
		shell.export( this , "PATH" , "$PATH:$JAVA_HOME/bin" );
	
		// get thirdparty information
		for( ActionScopeTargetItem scopeItem : items )
			uploadItem( scopeProject , release , scopeItem );
		return( true );
	}

	private void uploadItem( ActionScopeTarget scopeProject , Dist release , ActionScopeTargetItem scopeItem ) throws Exception {
	}
}
