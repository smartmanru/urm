package org.urm.action;

import org.urm.engine.shell.Account;

public class ScopeState {

	public enum SCOPESTATE {
		New ,
		NotRun ,
		RunSuccess , 
		RunBeforeFail ,
		RunFail
	}
	
	ActionCore action;
	ScopeState parent;
	
	ActionScope scope;
	ActionScopeSet set;
	ActionScopeTarget target;
	ActionScopeTargetItem item;
	Account account;
	SCOPESTATE state;
	
	public ScopeState( ActionCore action , ActionScope scope ) {
		this.action = action;
		this.parent = null;
		this.scope = scope;
		this.state = SCOPESTATE.New;
	}

	public ScopeState( ScopeState parent , ActionScopeSet set ) {
		this.action = parent.action;
		this.parent = parent;
		this.scope = set.scope;
		this.set = set;
		this.state = SCOPESTATE.New;
	}

	public ScopeState( ScopeState parent , Account account ) {
		this.action = parent.action;
		this.parent = parent;
		this.scope = parent.scope;
		this.set = parent.set;
		this.account = account;
		this.state = SCOPESTATE.New;
	}

	public ScopeState( ScopeState parent , ActionScopeTarget target ) {
		this.action = parent.action;
		this.parent = parent;
		this.scope = target.set.scope;
		this.set = target.set;
		this.target = target;
		this.state = SCOPESTATE.New;
	}

	public ScopeState( ScopeState parent , ActionScopeTargetItem item ) {
		this.action = parent.action;
		this.parent = parent;
		this.scope = item.target.set.scope;
		this.set = item.target.set;
		this.target = item.target;
		this.item = item;
		this.state = SCOPESTATE.New;
	}

	public void setActionStatus( SCOPESTATE state ) {
		this.state = state;
	}
	
	public void setActionStatus( boolean status ) {
		this.state = ( status )? SCOPESTATE.RunSuccess : SCOPESTATE.RunFail;
	}

	public void setActionNotRun() {
		this.state = SCOPESTATE.NotRun;
	}
	
}
