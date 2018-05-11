package org.urm.action.build;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.MetaProductBuildSettings;

public class ActionUploadReleaseItem extends ActionBase {

	Dist release;
	
	public ActionUploadReleaseItem( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		// load distr data for cross-product exports - thirdparty
		List<ActionScopeTargetItem> items = scopeProject.getItems( this );
		
		// set maven
		MetaProductBuildSettings build = getBuildSettings( scopeProject.meta );
		shell.export( this , "M2_HOME" , "/usr/local/apache-maven-" + build.CONFIG_MAVEN_VERSION );
		shell.export( this , "M2" , "$M2_HOME/bin; export PATH=" + Common.getQuoted( "$PATH:$M2" ) );
		shell.export( this , "JAVA_HOME" , "/usr/java/" + build.CONFIG_MAVEN_JAVA_VERSION );
		shell.export( this , "PATH" , "$PATH:$JAVA_HOME/bin" );
	
		// get thirdparty information
		for( ActionScopeTargetItem scopeItem : items )
			uploadItem( scopeProject , release , scopeItem );
		return( SCOPESTATE.RunSuccess );
	}

	private void uploadItem( ActionScopeTarget scopeProject , Dist release , ActionScopeTargetItem scopeItem ) throws Exception {
	}
}
