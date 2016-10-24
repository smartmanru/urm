package org.urm.action;

import java.util.LinkedList;
import java.util.List;

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
	
	List<ScopeState> childs;
	
	public ScopeState( ActionCore action , ActionScope scope ) {
		this.scope = scope;
		create( action , null );
	}

	public ScopeState( ScopeState parent , ActionScopeSet set ) {
		this.scope = set.scope;
		this.set = set;
		create( parent.action , parent );
	}

	public ScopeState( ScopeState parent , Account account ) {
		this.scope = parent.scope;
		this.set = parent.set;
		this.account = account;
		create( parent.action , parent );
	}

	public ScopeState( ScopeState parent , ActionScopeTarget target ) {
		this.scope = target.set.scope;
		this.set = target.set;
		this.target = target;
		create( parent.action , parent );
	}

	public ScopeState( ScopeState parent , ActionScopeTargetItem item ) {
		this.scope = item.target.set.scope;
		this.set = item.target.set;
		this.target = item.target;
		this.item = item;
		create( parent.action , parent );
	}

	private void create( ActionCore action , ScopeState parent ) {
		this.action = action;
		this.parent = parent;
		this.state = SCOPESTATE.New;
		childs = new LinkedList<ScopeState>();
		if( parent != null )
			parent.childs.add( this );
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
