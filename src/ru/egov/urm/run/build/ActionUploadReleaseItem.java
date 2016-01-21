package ru.egov.urm.run.build;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;

public class ActionUploadReleaseItem extends ActionBase {

	DistStorage release;
	
	public ActionUploadReleaseItem( ActionBase action , String stream , DistStorage release ) {
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

	private void uploadItem( ActionScopeTarget scopeProject , DistStorage release , ActionScopeTargetItem scopeItem ) throws Exception {
	}
}
