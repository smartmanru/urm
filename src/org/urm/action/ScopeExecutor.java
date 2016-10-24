package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.action.CommandContext;
import org.urm.engine.shell.Account;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.Meta.VarCATEGORY;

public class ScopeExecutor {

	ActionBase action;
	CommandContext context;

	boolean runUniqueHosts = false;
	boolean runUniqueAccounts = false;
	
	ActionEventsSource eventsSource;
	ScopeState stateFinal;
	
	public ScopeExecutor( ActionBase action ) {
		this.action = action;
		this.context = action.context;
		this.eventsSource = action.eventSource;
	}

	public boolean runSimple() {
		startExecutor( null );

		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.debug( action.NAME + ": run without scope" );
			action.runBefore();
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}

		try {
			if( checkNeedRunAction( ss , action ) )
				ss = getActionStatus( ss , action , action.executeSimple() );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter();
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}

	public boolean runAll( ActionScope scope ) {
		startExecutor( scope );
		
		SCOPESTATE ss = runAllInternal( scope , stateFinal );
		
		return( finishExecutor( ss ) );
	}
	
	public boolean runCategories( ActionScope scope , VarCATEGORY[] categories ) {
		startExecutor( scope );
		
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.debug( action.NAME + ": run scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}

		try {
			if( checkNeedRunAction( ss , action ) )
				ss = getActionStatus( ss , action , runTargetCategoriesInternal( scope , categories , stateFinal ) );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss )  )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}
	
	public boolean runAll( ActionScopeSet set ) {
		startExecutor( set.scope );
		ScopeState stateSet = new ScopeState( stateFinal , set );
		
		SCOPESTATE res = runTargetSetInternal( set , stateSet );
		
		stateSet.setActionStatus( res );
		return( finishExecutor( res ) );
	}
	
	public boolean runTargetList( ActionScopeSet set , ActionScopeTarget[] targets ) {
		startExecutor( set.scope );
		ScopeState stateSet = new ScopeState( stateFinal , set );
		
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.runBefore();
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}

		try {
			if( checkNeedRunAction( ss , action ) ) {
				String list = "";
				for( ActionScopeTarget target : targets )
					list = Common.addItemToUniqueSpacedList( list , target.NAME );
				
				action.debug( action.NAME + ": run scope={" + set.NAME + "={" + list + "}}" );
				ss = runTargetListInternal( set.CATEGORY , set , targets , true , stateSet );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter();
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		stateSet.setActionStatus( ss );
		return( finishExecutor( ss ) );
	}
	
	public boolean runSingleTarget( ActionScopeTarget item ) {
		startExecutor( item.set.scope );
		ScopeState stateSet = new ScopeState( stateFinal , item.set );
		
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.runBefore();
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}

		try {
			if( checkNeedRunAction( ss , action ) ) {
				action.debug( action.NAME + ": run scope={" + item.set.NAME + "={" + item.NAME + "}}" );
				ss = runTargetListInternal( item.CATEGORY , item.set , new ActionScopeTarget[] { item } , true , stateSet );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter();
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		stateSet.setActionStatus( ss );
		return( finishExecutor( ss ) );
	}
	
	public boolean runEnvUniqueHosts( ActionScope scope ) {
		startExecutor( scope );
		
		SCOPESTATE ss = SCOPESTATE.New;
		VarCATEGORY[] categories = new VarCATEGORY[] { VarCATEGORY.ENV };
		try {
			action.debug( action.NAME + ": run unique hosts of scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}

		try {
			if( checkNeedRunAction( ss , action ) ) {
				runUniqueHosts = true;
				ss = runTargetCategoriesInternal( scope , categories , stateFinal );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}
	
	public boolean runEnvUniqueAccounts( ActionScope scope ) {
		if( !action.context.CTX_HOSTUSER.equals( "default" ) ) {
			if( action.context.CTX_ROOTUSER || !action.context.CTX_HOSTUSER.isEmpty() )
				return( runEnvUniqueHosts( scope ) );
		}

		startExecutor( scope );
		
		SCOPESTATE ss = SCOPESTATE.New;
		VarCATEGORY[] categories = new VarCATEGORY[] { VarCATEGORY.ENV };
		try {
			action.debug( action.NAME + ": run unique accounts of scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}

		try {
			if( checkNeedRunAction( ss , action ) ) {
				runUniqueAccounts = true;
				ss = runTargetCategoriesInternal( scope , categories , stateFinal );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}
	
	// implementation
	private SCOPESTATE runTargetListInternal( VarCATEGORY CATEGORY , ActionScopeSet set , ActionScopeTarget[] items , boolean runBefore , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			if( runUniqueHosts ) {
				Account[] hosts = set.getUniqueHosts( action , items );
				return( runHostListInternal( set , hosts , stateSet ) );
			}
			
			if( runUniqueAccounts ) {
				Account[] accounts = set.getUniqueAccounts( action , items );
				return( runAccountListInternal( set , accounts , stateSet ) );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
			return( ss );
		}

		try {
			// execute list as is
			if( runBefore )
				action.runBefore( set , items );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
			
		try {
			if( checkNeedRunAction( ss , action ) && runBefore )
				ss = getActionStatus( ss , action , action.executeScopeSet( set , items ) );

			if( checkNeedRunAction( ss , action ) && !isRunDone( ss ) ) {
				for( ActionScopeTarget target : getOrderedTargets( set , items ) ) {
					ScopeState stateTarget = new ScopeState( stateSet , target );
					SCOPESTATE ssTarget = runSingleTargetInternal( CATEGORY , target , stateTarget );
					ss = addChildState( ss , ssTarget );
					
					if( isRunDone( ssTarget ) ) {
						stateTarget.setActionStatus( ssTarget );
						if( !action.continueRun() )
							break;
					}
					else
						stateTarget.setActionNotRun();
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( runBefore ) {
				if( isRunDone( ss ) )
					action.runAfter( set , items );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	private SCOPESTATE runTargetSetInternal( ActionScopeSet set , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		ActionScopeTarget[] items = null;
		try {
			String all = ( set.setFull )? " (all)" : "";
			action.debug( action.NAME + ": execute scope set=" + set.NAME + all + " ..." );
			
			items = set.getTargets( action ).values().toArray( new ActionScopeTarget[0] ); 
			action.runBefore( set , items );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScopeSet( set , items ) );
				if( !isRunDone( ss ) )
					ss = runTargetListInternal( set.CATEGORY , set , set.targets.values().toArray( new ActionScopeTarget[0] ) , false , stateSet );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( set , items );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runTargetCategoriesInternal( ActionScope scope , VarCATEGORY[] categories , ScopeState stateScope ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			if( scope.isEmpty( action , categories ) ) {
				action.debug( action.NAME + ": nothing to execute" );
				return( ss );
			}

			for( ActionScopeSet set : getOrderedSets( scope ) ) {
				boolean run = true;
				if( categories != null ) {
					run = false;
					for( VarCATEGORY CATEGORY : categories ) {
						if( set.CATEGORY == CATEGORY )
							run = true;
					}
				}

				if( !run )
					continue;

				// execute set
				ScopeState stateSet = new ScopeState( stateScope , set );
				SCOPESTATE ssSet = runTargetSetInternal( set , stateSet );
				ss = addChildState( ss , ssSet );
				
				if( isRunDone( ssSet ) ) {
					stateSet.setActionStatus( ssSet );
					if( !action.continueRun() )
						break;
				}
				else
					stateSet.setActionNotRun();
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runAllInternal( ActionScope scope , ScopeState stateScope ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			String all = ( scope.scopeFull )? " (all)" : "";
			action.debug( action.NAME + ": execute scope" + all + " ..." );
			action.runBefore( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScope( scope ) );
				if( !isRunDone( ss ) )
					ss = runTargetCategoriesInternal( scope , null , stateScope );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	private SCOPESTATE runSingleTargetInternal( VarCATEGORY CATEGORY , ActionScopeTarget target , ScopeState stateTarget ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			String all = ( target.itemFull )? " (all)" : "";
			action.debug( action.NAME + ": execute target=" + target.NAME + all + " ..." );
			action.runBefore( target );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScopeTarget( target ) );
				if( !isRunDone( ss ) )
					ss = runTargetItemsInternal( CATEGORY , target , stateTarget );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( target );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	private SCOPESTATE runTargetItemsInternal( VarCATEGORY CATEGORY , ActionScopeTarget target , ScopeState stateTarget ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			List<ActionScopeTargetItem> items = target.getItems( action );
			if( items.isEmpty() ) {
				action.trace( "target=" + target.NAME + " is empty, not processed" );
				return( ss );
			}
			
			for( ActionScopeTargetItem item : items ) {
				ScopeState stateItem = new ScopeState( stateTarget , item ); 
				SCOPESTATE ssItem = runSingleTargetItemInternal( CATEGORY , target , item , stateItem );
				ss = addChildState( ss , ssItem );
				
				if( isRunDone( ssItem ) ) {
					stateItem.setActionStatus( ssItem );
					if( !action.continueRun() )
						break;
				}
				else
					stateItem.setActionNotRun();
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runSingleTargetItemInternal( VarCATEGORY CATEGORY , ActionScopeTarget target , ActionScopeTargetItem item , ScopeState stateItem ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.debug( action.NAME + ": run item=" + item.NAME );
			action.runBefore( target , item );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {		
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScopeTargetItem( target , item ) );
				if( ss == SCOPESTATE.NotRun )
					action.trace( "target=" + target.NAME + ", item=" + item.NAME + " is not processed" );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}

		try {
			if( isRunDone( ss ) )
				action.runAfter( target , item );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runHostListInternal( ActionScopeSet set , Account[] hosts , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			for( Account host : hosts ) {
				ScopeState stateAccount = new ScopeState( stateSet , host );
				SCOPESTATE ssAccount = runSingleHostInternal( set , host.HOST , host.PORT , host.osType , stateAccount );
				ss = addChildState( ss , ssAccount );
				
				if( isRunDone( ssAccount ) ) {
					stateAccount.setActionStatus( ssAccount );
					if( !action.continueRun() )
						break;
				}
				else
					stateAccount.setActionNotRun();
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runSingleHostInternal( ActionScopeSet set , String host , int port , VarOSTYPE OSTYPE , ScopeState stateAccount ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			Account account = action.getSingleHostAccount( host , port , OSTYPE );
			String serverNodes = set.dc.getServerNodesByHost( action , host );
			action.info( account.getPrintName() + ": serverNodes={" + serverNodes + "}" );
			
			ss = getActionStatus( ss , action , action.executeAccount( set , account ) );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	private SCOPESTATE runAccountListInternal( ActionScopeSet set , Account[] accounts , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			for( Account account : accounts ) {
				ScopeState stateAccount = new ScopeState( stateSet , account );
				SCOPESTATE ssAccount = runSingleAccountInternal( set , account , stateAccount );
				ss = addChildState( ss , ssAccount );
				
				if( isRunDone( ssAccount ) ) {
					stateAccount.setActionStatus( ssAccount );
					if( !action.continueRun() )
						break;
				}
				else
					stateAccount.setActionNotRun();
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runSingleAccountInternal( ActionScopeSet set , Account account , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			String serverNodes = set.dc.getServerNodesByAccount( action , account );
			action.info( account.getPrintName() + ": serverNodes={" + serverNodes + "}" );
			ss = getActionStatus( ss , action , action.executeAccount( set , account ) );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	public boolean isRunDone( SCOPESTATE ss ) {
		if( ss == SCOPESTATE.RunFail || ss == SCOPESTATE.RunSuccess )
			return( true );
		return( false );
	}

	public SCOPESTATE addChildState( SCOPESTATE ss , SCOPESTATE ssChild ) {
		if( ss == SCOPESTATE.New )
			return( ssChild );
		if( ssChild == SCOPESTATE.New )
			return( ss );
		
		if( ss == SCOPESTATE.NotRun )
			return( ssChild );
		if( ssChild == SCOPESTATE.NotRun )
			return( ss );
		
		if( ss == SCOPESTATE.RunSuccess && ssChild == SCOPESTATE.RunSuccess )
			return( SCOPESTATE.RunSuccess );
		
		return( SCOPESTATE.RunFail );
	}
	
	private List<ActionScopeSet> getOrderedSets( ActionScope scope ) throws Exception {
		List<ActionScopeSet> list = new LinkedList<ActionScopeSet>();
		if( scope.meta != null ) {
			MetaSource sources = scope.meta.getSources( action ); 
			for( MetaSourceProjectSet sourceSet : sources.getSetList( action ) ) {
				ActionScopeSet set = scope.findSet( action , sourceSet.CATEGORY , sourceSet.NAME );
				if( set != null )
					list.add( set );
			}
		}
			
		for( ActionScopeSet set : scope.getCategorySets( action ) )
			list.add( set );
		
		if( context.env != null ) {
			for( MetaEnvDC envSet : context.env.getOriginalDCList() ) {
				ActionScopeSet set = scope.findSet( action , VarCATEGORY.ENV , envSet.NAME );
				if( set != null )
					list.add( set );
			}
		}
		
		return( list );
	}
	
	private List<ActionScopeTarget> getOrderedTargets( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		List<ActionScopeTarget> list = new LinkedList<ActionScopeTarget>();
		Map<String,ActionScopeTarget> map = new HashMap<String,ActionScopeTarget>();
		
		if( Meta.isSourceCategory( set.CATEGORY ) ) {
			for( ActionScopeTarget target : targets )
				map.put( target.sourceProject.PROJECT , target );
			
			for( MetaSourceProject project : set.pset.getOriginalList( action ) ) {
				ActionScopeTarget target = map.get( project.PROJECT );
				if( target != null )
					list.add( target );
			}
			
			return( list );
		}
				
		if( set.CATEGORY == VarCATEGORY.ENV ) {
			for( ActionScopeTarget target : targets )
				map.put( target.envServer.NAME , target );

			for( MetaEnvServer server : set.dc.getOriginalServerList() ) {
				ActionScopeTarget target = map.get( server.NAME );
				if( target != null )
					list.add( target );
			}
			
			return( list );
		}

		// run in alphabetic sequence
		for( ActionScopeTarget target : targets )
			map.put( target.NAME , target );

		for( String key : Common.getSortedKeys( map ) )
			list.add( map.get( key ) );
		
		return( list );
	}

	private boolean checkNeedRunAction( SCOPESTATE ss , ActionBase action ) {
		if( ss == SCOPESTATE.RunBeforeFail )
			return( false );
		if( action.isFailed() )
			return( false );
		action.clearCall();
		return( true );
	}

	private SCOPESTATE getActionStatus( SCOPESTATE ss , ActionBase action , SCOPESTATE ssAction ) {
		if( action.isCallFailed() )
			return( SCOPESTATE.RunFail );
		return( ssAction );
	}

	private void startExecutor( ActionScope scope ) {
		stateFinal = new ScopeState( action , scope );
		action.eventSource.setState( stateFinal );
	}
	
	private boolean finishExecutor( SCOPESTATE ss ) {
		action.engine.shellPool.releaseActionPool( action );
		stateFinal.setActionStatus( ss );
		
		if( ss == SCOPESTATE.RunFail || ss == SCOPESTATE.RunBeforeFail )
			return( false );
		return( true );
	}
	
}
