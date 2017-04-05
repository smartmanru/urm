package org.urm.action.main;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.action.CommandOptions;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.action.CommandMethod;
import org.urm.meta.product.Meta;

public class ActionMethod extends ActionBase {

	public Meta meta;
	public CommandExecutor methodExecutor;
	public CommandOptions methodOptions;
	
	public ActionMethod( ActionBase action , String stream , Meta meta , CommandExecutor methodExecutor , CommandOptions methodOptions ) {
		super( action , stream , "execute method=" + methodOptions.command + "::" + methodOptions.method );
		this.meta = meta;
		this.methodExecutor = methodExecutor;
		this.methodOptions = methodOptions;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		context.setOptions( this , meta , methodOptions );
		CommandMethod executorMethod = methodExecutor.getAction( methodOptions.method );
		if( methodExecutor.runExecutor( this , executorMethod ) )
			return( SCOPESTATE.RunSuccess );
		return( SCOPESTATE.RunFail );
	}
	
}
