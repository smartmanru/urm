package org.urm.engine.status;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionCore;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.EventService;
import org.urm.engine.shell.Account;
import org.urm.meta.env.MetaEnvServerNode;

public class ScopeState extends ObjectState {

	public enum SCOPESTATE {
		New ,
		NotRun ,
		RunSuccess , 
		RunBeforeFail ,
		RunFail ,
		RunStopped
	};
	
	public enum FACTVALUE {
		PROCESSMODE ,
		PROCESSACTION ,
		BASEITEM ,
		VERSION ,
		BRANCHNAME ,
		TAGNAME
	};
	
	public ActionCore action;
	public SCOPESTATE state;

	public ActionScope scope;
	public ActionScopeSet set;
	public ActionScopeTarget target;
	public ActionScopeTargetItem item;
	public Account account;
	public ActionScopeTargetItem[] nodes;

	public List<ScopeStateFact> facts;
	
	public ScopeState( ScopeState parent , ActionCore action , ActionScope scope ) {
		super( STATETYPE.TypeScope , parent , scope );
		this.scope = scope;
		create( action );
	}

	public ScopeState( ScopeState parent , ActionScopeSet set ) {
		super( STATETYPE.TypeSet , parent , set );
		this.scope = set.scope;
		this.set = set;
		create( parent.action );
	}

	public ScopeState( ScopeState parent , Account account , ActionScopeTargetItem[] nodes ) {
		super( STATETYPE.TypeAccount , parent , account );
		this.scope = parent.scope;
		this.set = parent.set;
		this.account = account;
		this.nodes = nodes;
		create( parent.action );
	}

	public ScopeState( ScopeState parent , ActionScopeTarget target ) {
		super( STATETYPE.TypeScopeTarget , parent , target );
		this.scope = target.set.scope;
		this.set = target.set;
		this.target = target;
		create( parent.action );
	}

	public ScopeState( ScopeState parent , ActionScopeTargetItem item ) {
		super( STATETYPE.TypeScopeItem , parent , item );
		this.scope = item.target.set.scope;
		this.set = item.target.set;
		this.target = item.target;
		this.item = item;
		create( parent.action );
	}

	public ScopeState( ScopeState parent , MetaEnvServerNode node ) {
		super( STATETYPE.TypeServerNode , parent , node );
		this.scope = null;
		this.set = null;
		this.target = null;
		create( parent.action );
	}

	private void create( ActionCore action ) {
		this.action = action;
		this.state = SCOPESTATE.New;
		
		facts = new LinkedList<ScopeStateFact>(); 
		action.eventSource.startScopeItem( this );
		
		ActionCore notifyParent = action;
		notifyParent = notifyParent.parent;
		while( notifyParent != null ) {
			notifyParent.eventSource.startScopeItem( EventService.OWNER_ENGINE , EventService.EVENT_STARTCHILDSTATE , this );
			notifyParent = notifyParent.parent;
		}
	}
	
	public void setActionStatus( boolean status ) {
		setActionStatus( ( status )? SCOPESTATE.RunSuccess : SCOPESTATE.RunFail );
	}

	public void setActionNotRun() {
		setActionStatus( SCOPESTATE.NotRun );
	}
	
	public void setActionStatus( SCOPESTATE state ) {
		stopChilds();
		
		this.state = state;
		action.eventSource.finishScopeItem( this );
		
		ActionCore notifyParent = action;
		notifyParent = notifyParent.parent;
		while( notifyParent != null ) {
			notifyParent.eventSource.finishScopeItem( EventService.OWNER_ENGINE , EventService.EVENT_FINISHCHILDSTATE , this );
			notifyParent = notifyParent.parent;
		}
	}

	private void stopChilds() {
		for( ObjectState os : super.childs ) {
			ScopeState state = ( ScopeState )os;
			if( state.state == SCOPESTATE.New )
				state.setActionStatus( SCOPESTATE.RunStopped );
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
		if( type == STATETYPE.TypeScopeItem )
			return( ( ScopeState )parent );
		if( type == STATETYPE.TypeAccount )
			return( null );
		if( type == STATETYPE.TypeScopeTarget )
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
		if( type == STATETYPE.TypeScopeItem )
			return( ( ScopeState )parent.parent );
		if( type == STATETYPE.TypeAccount )
			return( ( ScopeState )parent );
		if( type == STATETYPE.TypeScopeTarget )
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

	public void addFact( Enum<?> factType ) {
		ScopeStateFact fact = new ScopeStateFact( this , factType , new FactValue[0] );
		addFact( fact );
	}
	
	public synchronized void addFact( Enum<?> factType , FACTVALUE type1 , String arg1 ) {
		ScopeStateFact fact = new ScopeStateFact( this , factType , new FactValue[] { new FactValue( type1 , arg1 ) } );
		addFact( fact );
	}
	
	public synchronized void addFact( Enum<?> factType , FACTVALUE type1 , String arg1 , 
			FACTVALUE type2 , String arg2 ) {
		ScopeStateFact fact = new ScopeStateFact( this , factType , new FactValue[] { new FactValue( type1 , arg1 ) ,
				new FactValue( type2 , arg2 ) } );
		addFact( fact );
	}
	
	public synchronized void addFact( Enum<?> factType , FACTVALUE type1 , String arg1 , 
			FACTVALUE type2 , String arg2 ,
			FACTVALUE type3 , String arg3 ) {
		ScopeStateFact fact = new ScopeStateFact( this , factType , new FactValue[] { new FactValue( type1 , arg1 ) ,
				new FactValue( type2 , arg2 ) ,
				new FactValue( type3 , arg3 ) } );
		addFact( fact );
	}
	
	public synchronized void addFact( Enum<?> factType , FACTVALUE type1 , String arg1 , 
			FACTVALUE type2 , String arg2 ,
			FACTVALUE type3 , String arg3 ,
			FACTVALUE type4 , String arg4 ) {
		ScopeStateFact fact = new ScopeStateFact( this , factType , new FactValue[] { new FactValue( type1 , arg1 ) ,
				new FactValue( type2 , arg2 ) ,
				new FactValue( type3 , arg3 ) ,
				new FactValue( type4 , arg4 ) } );
		addFact( fact );
	}
	
	public synchronized void addFact( Enum<?> factType , FACTVALUE type1 , String arg1 , 
			FACTVALUE type2 , String arg2 ,
			FACTVALUE type3 , String arg3 ,
			FACTVALUE type4 , String arg4 ,
			FACTVALUE type5 , String arg5 ) {
		ScopeStateFact fact = new ScopeStateFact( this , factType , new FactValue[] { new FactValue( type1 , arg1 ) ,
				new FactValue( type2 , arg2 ) ,
				new FactValue( type3 , arg3 ) ,
				new FactValue( type4 , arg4 ) ,
				new FactValue( type5 , arg5 ) } );
		addFact( fact );
	}

	public void addFact( ScopeStateFact fact ) {
		facts.add( fact );
		
		action.eventSource.customEvent( EventService.OWNER_ENGINE , EventService.EVENT_ADDFACT , fact );
		
		ActionCore notifyParent = action;
		notifyParent = notifyParent.parent;
		while( notifyParent != null ) {
			notifyParent.eventSource.customEvent( EventService.OWNER_ENGINE , EventService.EVENT_ADDCHILDFACT , fact );
			notifyParent = notifyParent.parent;
		}
	}
	
}
