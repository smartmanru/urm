package org.urm.action.codebase;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.EngineBuilders;

public class ActionUploadReleaseItem extends ActionBase {

	Dist release;
	
	public ActionUploadReleaseItem( ActionBase action , String stream , Dist release ) {
		super( action , stream , "Upload items from distributive, release=" + release.RELEASEDIR );
		this.release = release;
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		// load distr data for cross-product exports - thirdparty
		List<ActionScopeTargetItem> items = scopeProject.getItems( this );
		
		// set maven
		EngineBuilders builders = super.getServerBuilders();
		String BUILD_JAVA_HOME = shell.getLocalPath( builders.JAVA_HOMEPATH );
		String BUILD_MAVEN_HOME = shell.getLocalPath( builders.MAVEN_HOMEPATH );

		shell.export( this , "JAVA_HOME" , BUILD_JAVA_HOME );
		shell.export( this , "M2_HOME" , BUILD_MAVEN_HOME );
		shell.export( this , "M2" , shell.getLocalPath( shell.getVariable( "M2_HOME" ) + "/bin" ) );
		shell.export( this , "PATH" , shell.getLocalPath( shell.getVariable( "JAVA_HOME" ) + "/bin" ) + shell.getPathBreak() +
				shell.getVariable( "M2" ) + shell.getPathBreak() +
				shell.getVariable( "PATH" ) );
		shell.export( this , "MAVEN_OPTS" , Common.getQuoted( "-XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled" ) );
	
		// get thirdparty information
		for( ActionScopeTargetItem scopeItem : items )
			uploadItem( scopeProject , release , scopeItem );
		return( SCOPESTATE.RunSuccess );
	}

	private void uploadItem( ActionScopeTarget scopeProject , Dist release , ActionScopeTargetItem scopeItem ) throws Exception {
	}
}
