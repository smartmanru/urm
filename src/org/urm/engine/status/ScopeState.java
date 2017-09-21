package org.urm.engine.status;

import org.urm.action.ActionCore;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.shell.Account;

public class ScopeState extends ObjectState {

	public enum SCOPESTATE {
		New ,
		NotRun ,
		RunSuccess , 
		RunBeforeFail ,
		RunFail ,
		RunStopped
	};
	
	public ActionCore action;
	public SCOPESTATE state;

	public ActionScope scope;
	public ActionScopeSet set;
	public ActionScopeTarget target;
	public ActionScopeTargetItem item;
	public Account account;
	
	public ScopeState( ActionCore action , ActionScope scope ) {
		super( STATETYPE.TypeScope , null , scope );
		this.scope = scope;
		create( action );
	}

	public ScopeState( ScopeState parent , ActionScopeSet set ) {
		super( STATETYPE.TypeSet , parent , set );
		this.scope = set.scope;
		this.set = set;
		create( parent.action );
	}

	public ScopeState( ScopeState parent , Account account ) {
		super( STATETYPE.TypeAccount , parent , account );
		this.scope = parent.scope;
		this.set = parent.set;
		this.account = account;
		create( parent.action );
	}

	public ScopeState( ScopeState parent , ActionScopeTarget target ) {
		super( STATETYPE.TypeTarget , parent , target );
		this.scope = target.set.scope;
		this.set = target.set;
		this.target = target;
		create( parent.action );
	}

	public ScopeState( ScopeState parent , ActionScopeTargetItem item ) {
		super( STATETYPE.TypeItem , parent , item );
		this.scope = item.target.set.scope;
		this.set = item.target.set;
		this.target = item.target;
		this.item = item;
		create( parent.action );
	}

	private void create( ActionCore action ) {
		this.action = action;
		this.state = SCOPESTATE.New;
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
		
		ActionCore notifyParent = action;
		notifyParent = notifyParent.parent;
		while( notifyParent != null ) {
			notifyParent.eventSource.finishScopeItem( EngineEvents.EVENT_FINISHCHILDSTATE , this );
			notifyParent = notifyParent.parent;
		}
	}

	public void createItemScopeState( ActionScopeTargetItem item , SCOPESTATE state ) {
		ScopeState stateTarget = findTargetState( item.target );
		if( stateTarget == null )
			return;
		
		ScopeState stateItem = new ScopeState( stateTarget , item );
		stateItem.setActionStatus( state );
	}

	public ScopeState findTargetState( ActionScopeTarget target ) {
		if( type == STATETYPE.TypeItem )
			return( ( ScopeState )parent );
		if( type == STATETYPE.TypeAccount )
			return( null );
		if( type == STATETYPE.TypeTarget )
			return( this );
		ScopeState stateSet = findSetState( target.set );
		if( stateSet == null )
			return( null );
		for( ObjectState child : stateSet.childs ) {
			ScopeState childState = ( ScopeState )child; 
			if( childState.target == target )
				return( childState );
		}
		return( null );
	}

	public ScopeState findSetState( ActionScopeSet set ) {
		if( type == STATETYPE.TypeItem )
			return( ( ScopeState )parent.parent );
		if( type == STATETYPE.TypeAccount )
			return( ( ScopeState )parent );
		if( type == STATETYPE.TypeTarget )
			return( ( ScopeState )parent );
		if( type == STATETYPE.TypeSet )
			return( this );
		for( ObjectState child : childs ) {
			ScopeState childState = ( ScopeState )child;
			if( childState.set == set )
				return( childState );
		}
		return( null );
	}
	
}
