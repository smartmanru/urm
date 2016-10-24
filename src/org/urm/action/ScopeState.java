package org.urm.action;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.shell.Account;

public class ScopeState {

	public enum SCOPETYPE {
		TypeScope ,
		TypeSet ,
		TypeTarget ,
		TypeItem ,
		TypeAccount
	};
	
	public enum SCOPESTATE {
		New ,
		NotRun ,
		RunSuccess , 
		RunBeforeFail ,
		RunFail
	};
	
	ActionCore action;
	ScopeState parent;

	public SCOPETYPE type;	
	public ActionScope scope;
	public ActionScopeSet set;
	public ActionScopeTarget target;
	public ActionScopeTargetItem item;
	public Account account;
	public SCOPESTATE state;
	
	List<ScopeState> childs;
	
	public ScopeState( ActionCore action , ActionScope scope ) {
		this.type = SCOPETYPE.TypeScope;
		this.scope = scope;
		create( action , null );
	}

	public ScopeState( ScopeState parent , ActionScopeSet set ) {
		this.type = SCOPETYPE.TypeSet;
		this.scope = set.scope;
		this.set = set;
		create( parent.action , parent );
	}

	public ScopeState( ScopeState parent , Account account ) {
		this.type = SCOPETYPE.TypeAccount;
		this.scope = parent.scope;
		this.set = parent.set;
		this.account = account;
		create( parent.action , parent );
	}

	public ScopeState( ScopeState parent , ActionScopeTarget target ) {
		this.type = SCOPETYPE.TypeTarget;
		this.scope = target.set.scope;
		this.set = target.set;
		this.target = target;
		create( parent.action , parent );
	}

	public ScopeState( ScopeState parent , ActionScopeTargetItem item ) {
		this.type = SCOPETYPE.TypeItem;
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
	
	public void setActionStatus( boolean status ) {
		setActionStatus( ( status )? SCOPESTATE.RunSuccess : SCOPESTATE.RunFail );
	}

	public void setActionNotRun() {
		setActionStatus( SCOPESTATE.NotRun );
	}
	
	public void setActionStatus( SCOPESTATE state ) {
		this.state = state;
		action.eventSource.finishState( this );
	}

}
