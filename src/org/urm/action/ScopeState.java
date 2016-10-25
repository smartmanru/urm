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
		action.eventSource.finishScopeItem( this );
	}

	public void createItemScopeState( ActionScopeTargetItem item , SCOPESTATE state ) {
		ScopeState stateTarget = findTargetState( item.target );
		if( stateTarget == null )
			return;
		
		ScopeState stateItem = new ScopeState( stateTarget , item );
		stateItem.setActionStatus( state );
	}

	public ScopeState findTargetState( ActionScopeTarget target ) {
		if( type == SCOPETYPE.TypeItem )
			return( parent );
		if( type == SCOPETYPE.TypeAccount )
			return( null );
		if( type == SCOPETYPE.TypeTarget )
			return( this );
		ScopeState stateSet = findSetState( target.set );
		if( stateSet == null )
			return( null );
		for( ScopeState child : stateSet.childs ) {
			if( child.target == target )
				return( child );
		}
		return( null );
	}

	public ScopeState findSetState( ActionScopeSet set ) {
		if( type == SCOPETYPE.TypeItem )
			return( parent.parent );
		if( type == SCOPETYPE.TypeAccount )
			return( parent );
		if( type == SCOPETYPE.TypeTarget )
			return( parent );
		if( type == SCOPETYPE.TypeSet )
			return( this );
		for( ScopeState child : childs ) {
			if( child.set == set )
				return( child );
		}
		return( null );
	}
	
}
