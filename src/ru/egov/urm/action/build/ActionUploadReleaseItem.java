package ru.egov.urm.action.build;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.dist.Dist;

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
		session.export( this , "M2_HOME" , "/usr/local/apache-maven-" + meta.product.CONFIG_MAVEN_VERSION );
		session.export( this , "M2" , "$M2_HOME/bin; export PATH=" + Common.getQuoted( "$PATH:$M2" ) );
		session.export( this , "JAVA_HOME" , "/usr/java/" + meta.product.CONFIG_JAVA_VERSION );
		session.export( this , "PATH" , "$PATH:$JAVA_HOME/bin" );
	
		// get thirdparty information
		for( ActionScopeTargetItem scopeItem : items )
			uploadItem( scopeProject , release , scopeItem );
		return( true );
	}

	private void uploadItem( ActionScopeTarget scopeProject , Dist release , ActionScopeTargetItem scopeItem ) throws Exception {
	}
}
