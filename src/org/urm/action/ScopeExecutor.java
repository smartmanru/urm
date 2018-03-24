package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.AuthService;
import org.urm.engine.EventService;
import org.urm.engine.action.CommandContext;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.SourceEvent;
import org.urm.engine.shell.Account;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;

public class ScopeExecutor implements EngineEventsListener {

	enum AsyncType {
		ASYNC_RUNSIMPLE ,
		ASYNC_RUNSCOPE ,
		ASYNC_RUNSCOPESET ,
		ASYNC_RUNSCOPETARGET ,
		ASYNC_RUNSCOPESETTARGETS ,
		ASYNC_RUNCATEGORIES ,
		ASYNC_RUNUNIQUEHOSTS ,
		ASYNC_RUNUNIQUEACCOUNTS
	};
	
	ScopeState parentState;
	ActionBase action;
	boolean async;
	CommandContext context;

	boolean runUniqueHosts = false;
	boolean runUniqueAccounts = false;
	volatile boolean running;
	
	ActionEventsSource eventsSource;
	ScopeState stateFinal;

	AsyncType asyncType;
	ActionScope asyncScope;
	ActionScopeSet asyncScopeSet;
	ActionScopeTarget asyncScopeTarget;
	ActionScopeTarget[] asyncTargets;
	DBEnumScopeCategoryType[] asyncCategories;
	EngineEventsSubscription asyncSub;
	
	public ScopeExecutor( ScopeState parentState , ActionBase action , boolean async ) {
		this.parentState = parentState;
		this.action = action;
		this.async = async;
		this.context = action.context;
		this.eventsSource = action.eventSource;
		runUniqueHosts = false;
		runUniqueAccounts = false;
		running = false;
	}

	@Override
	public void triggerEvent( EngineEventsSubscription sub , SourceEvent event ) {
		if( event.isEngineEvent( EventService.EVENT_RUNASYNC ) )
			executeAsync();
	}
	
	public void stopExecution() {
		running = false;
	}
	
	public boolean runAsync() {
		EventService events = action.engine.getEvents();
		EngineEventsApp app = action.actionInit.getEventsApp();
		SourceEvent event = eventsSource.createCustomEvent( EventService.OWNER_ENGINE , EventService.EVENT_RUNASYNC , this );
		events.notifyListener( app , this , event );
		return( true );
	}
	
	public boolean executeAsync() {
		if( asyncType == AsyncType.ASYNC_RUNSIMPLE )
			return( runSimple() );
		if( asyncType == AsyncType.ASYNC_RUNSCOPE )
			return( runScope( asyncScope ) );
		if( asyncType == AsyncType.ASYNC_RUNSCOPESET )
			return( runScopeSet( asyncScopeSet ) );
		if( asyncType == AsyncType.ASYNC_RUNSCOPETARGET )
			return( runScopeTarget( asyncScopeTarget ) );
		if( asyncType == AsyncType.ASYNC_RUNSCOPESETTARGETS )
			return( runScopeSetTargets( asyncScopeSet , asyncTargets ) );
		if( asyncType == AsyncType.ASYNC_RUNCATEGORIES )
			return( runCategories( asyncScope , asyncCategories ) );
		if( asyncType == AsyncType.ASYNC_RUNUNIQUEHOSTS )
			return( runUniqueHosts( asyncScope ) );
		if( asyncType == AsyncType.ASYNC_RUNUNIQUEACCOUNTS )
			return( runUniqueAccounts( asyncScope ) );
		return( false );
	}
	
	public boolean runSimpleServer( SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( !auth.checkAccessServerAction( action , sa , readOnly ) ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", server operation)" );
			return( false );
		}
		
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSIMPLE;
			return( runAsync() );
		}
		
		return( runSimple() );
	}
	
	public boolean runSimpleProduct( String productName , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		AppProduct product = action.findProduct( productName );
		if( product == null ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", unknown product)" );
			return( false );
		}
		
		if( !auth.checkAccessProductAction( action , sa , product , readOnly ) ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", product operation)" );
			return( false );
		}
		
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSIMPLE;
			return( runAsync() );
		}
		
		return( runSimple() );
	}
	
	public boolean runSimpleEnv( MetaEnv env , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( !auth.checkAccessProductAction( action , sa , env , readOnly ) ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", environment operation)" );
			return( false );
		}
		
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSIMPLE;
			return( runAsync() );
		}
		
		return( runSimple() );
	}
	
	public boolean runProductBuild( String productName , SecurityAction sa , DBEnumBuildModeType mode , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		AppProduct product = action.findProduct( productName );
		if( product == null ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", unknown product)" );
			return( false );
		}
		
		if( !auth.checkAccessProductAction( action , sa , product , mode , readOnly ) ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", build operation)" );
			return( false );
		}
		
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSIMPLE;
			return( runAsync() );
		}
		
		return( runSimple() );
	}
	
	public boolean runAll( ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( env != null ) {
			if( !auth.checkAccessProductAction( action , sa , scope.meta , env , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", environment execute, scope)" );
				return( false );
			}
		}
		else {
			if( !auth.checkAccessProductAction( action , sa , scope.meta , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", product execute, scope)" );
				return( false );
			}
		}

		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSCOPE;
			asyncScope = scope;
			return( runAsync() );
		}
		
		return( runScope( scope ) );
	}
	
	public boolean runCategories( ActionScope scope , DBEnumScopeCategoryType[] categories , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( !auth.checkAccessProductAction( action , sa , scope.meta , readOnly ) ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", categories)" );
			return( false );
		}

		if( async ) {
			asyncType = AsyncType.ASYNC_RUNCATEGORIES;
			asyncScope = scope;
			asyncCategories = categories;
			return( runAsync() );
		}
		
		return( runCategories( scope , categories ) );
	}
	
	public boolean runAll( ActionScopeSet set , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( env != null ) {
			if( !auth.checkAccessProductAction( action , sa , set.scope.meta , env , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", environment execute, scope set)" );
				return( false );
			}
		}
		else {
			if( !auth.checkAccessProductAction( action , sa , set.scope.meta , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", product execute, scope set)" );
				return( false );
			}
		}
		
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSCOPESET;
			asyncScopeSet = set;
			return( runAsync() );
		}
		
		return( runScopeSet( set ) );
	}
	
	public boolean runTargetList( ActionScopeSet set , ActionScopeTarget[] targets , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( env != null ) {
			if( !auth.checkAccessProductAction( action , sa , set.scope.meta , env , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", environment execute, scope targets)" );
				return( false );
			}
		}
		else {
			if( !auth.checkAccessProductAction( action , sa , set.scope.meta , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", product execute, scope targets)" );
				return( false );
			}
		}

		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSCOPESETTARGETS;
			asyncScopeSet = set;
			asyncTargets = targets;
			return( runAsync() );
		}
		
		return( runScopeSetTargets( set , targets ) );
	}
	
	public boolean runSingleTarget( ActionScopeTarget item , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( env != null ) {
			if( !auth.checkAccessProductAction( action , sa , item.set.scope.meta , env , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", environment execute, scope target)" );
				return( false );
			}
		}
		else {
			if( !auth.checkAccessProductAction( action , sa , item.set.scope.meta , readOnly ) ) {
				accessDenied( "access denied (user=" + action.getUserName() + ", product execute, scope target)" );
				return( false );
			}
		}
		
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNSCOPETARGET;
			asyncScopeTarget = item;
			return( runAsync() );
		}
		
		return( runScopeTarget( item ) );
	}
	
	public boolean runEnvUniqueHosts( ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		AuthService auth = action.engine.getAuth();
		if( !auth.checkAccessProductAction( action , sa , scope.meta , env , readOnly ) ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", environment execute, hosts)" );
			return( false );
		}
			
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNUNIQUEHOSTS;
			asyncScope = scope;
			return( runAsync() );
		}
		
		return( runUniqueHosts( scope ) );
	}
	
	public boolean runEnvUniqueAccounts( ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		if( !action.context.CTX_HOSTUSER.equals( "default" ) ) {
			if( action.context.CTX_ROOTUSER || !action.context.CTX_HOSTUSER.isEmpty() )
				return( runEnvUniqueHosts( scope , env , sa , readOnly ) );
		}

		AuthService auth = action.engine.getAuth();
		if( !auth.checkAccessProductAction( action , sa , scope.meta , env , readOnly ) ) {
			accessDenied( "access denied (user=" + action.getUserName() + ", environment execute, acccounts)" );
			return( false );
		}
			
		if( async ) {
			asyncType = AsyncType.ASYNC_RUNUNIQUEACCOUNTS;
			asyncScope = scope;
			return( runAsync() );
		}
		
		return( runUniqueAccounts( scope ) );
	}

	// sync only
	public boolean runCustomTarget( ScopeState parentState , ActionScopeTarget target ) {
		try {
			ScopeState stateTarget = new ScopeState( parentState , target );
			SCOPESTATE ssTarget = runSingleTargetInternal( target , stateTarget );
			if( isRunDone( ssTarget ) ) {
				stateTarget.setActionStatus( ssTarget );
				if( !action.continueRun() )
					return( false );
			}
			else
				stateTarget.setActionNotRun();
		}
		catch( Throwable e ) {
			action.handle( e );
			return( false );
		}
		
		return( true );
	}
	
	// implementation
	private boolean runSimple() {
		ActionScope scope = new ActionScope( action );
		if( !startExecutor( scope ) )
			return( false );

		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.debug( action.NAME + ": run without scope" );
			action.runBefore( stateFinal );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}

		try {
			if( checkNeedRunAction( ss , action ) )
				ss = getActionStatus( ss , action , action.executeSimple( stateFinal ) );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateFinal );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}
	
	private boolean runScope( ActionScope scope ) {
		if( !startExecutor( scope ) )
			return( false );
		
		SCOPESTATE ss = runAllInternal( scope );
		return( finishExecutor( ss ) );
	}
	
	private boolean runScopeSet( ActionScopeSet set ) {
		if( !startExecutor( set.scope ) )
			return( false );
		
		ScopeState stateSet = new ScopeState( stateFinal , set );
		SCOPESTATE res = runTargetSetInternal( set , stateSet );
		stateSet.setActionStatus( res );
		return( finishExecutor( res ) );
	}

	private boolean runScopeTarget( ActionScopeTarget item ) {
		if( !startExecutor( item.set.scope ) )
			return( false );
			
		ScopeState stateSet = new ScopeState( stateFinal , item.set );
		
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.runBefore( stateSet );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
	
		try {
			if( checkNeedRunAction( ss , action ) ) {
				action.debug( action.NAME + ": run scope={" + item.set.NAME + "={" + item.NAME + "}}" );
				ss = runTargetListInternal( item.set , new ActionScopeTarget[] { item } , true , stateSet );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateSet );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		stateSet.setActionStatus( ss );
		return( finishExecutor( ss ) );
	}
	
	private boolean runScopeSetTargets( ActionScopeSet set , ActionScopeTarget[] targets ) {
		if( !startExecutor( set.scope ) )
			return( false );
		
		ScopeState stateSet = new ScopeState( stateFinal , set );
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.runBefore( stateSet );
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
				ss = runTargetListInternal( set , targets , true , stateSet );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateSet );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		stateSet.setActionStatus( ss );
		return( finishExecutor( ss ) );
	}

	private boolean runCategories( ActionScope scope , DBEnumScopeCategoryType[] categories ) {
		if( !startExecutor( scope ) )
			return( false );
		
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.debug( action.NAME + ": run scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
	
		try {
			if( checkNeedRunAction( ss , action ) )
				ss = getActionStatus( ss , action , runTargetCategoriesInternal( scope , categories ) );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss )  )
				action.runAfter( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}
	
	private boolean runUniqueHosts( ActionScope scope ) {
		if( !startExecutor( scope ) )
			return( false );
		
		SCOPESTATE ss = SCOPESTATE.New;
		DBEnumScopeCategoryType[] categories = new DBEnumScopeCategoryType[] { DBEnumScopeCategoryType.ENV };
		try {
			action.debug( action.NAME + ": run unique hosts of scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
	
		try {
			if( checkNeedRunAction( ss , action ) ) {
				runUniqueHosts = true;
				ss = runTargetCategoriesInternal( scope , categories );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}
	
	private boolean runUniqueAccounts( ActionScope scope ) {
		if( !startExecutor( scope ) )
			return( false );
		
		SCOPESTATE ss = SCOPESTATE.New;
		DBEnumScopeCategoryType[] categories = new DBEnumScopeCategoryType[] { DBEnumScopeCategoryType.ENV };
		try {
			action.debug( action.NAME + ": run unique accounts of scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
	
		try {
			if( checkNeedRunAction( ss , action ) ) {
				runUniqueAccounts = true;
				ss = runTargetCategoriesInternal( scope , categories );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( finishExecutor( ss ) );
	}
	
	private SCOPESTATE runTargetListInternal( ActionScopeSet set , ActionScopeTarget[] items , boolean runBefore , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			if( runUniqueHosts ) {
				Map<Account,ActionScopeTargetItem[]> map = new HashMap<Account,ActionScopeTargetItem[]>();
				Account[] hosts = set.getUniqueHosts( action , items , map );
				return( runHostListInternal( set , hosts , map , stateSet ) );
			}
			
			if( runUniqueAccounts ) {
				Map<Account,ActionScopeTargetItem[]> map = new HashMap<Account,ActionScopeTargetItem[]>();
				Account[] accounts = set.getUniqueAccounts( action , items , map );
				return( runAccountListInternal( set , accounts , map , stateSet ) );
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
				action.runBefore( stateSet , set , items );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
			
		try {
			if( checkNeedRunAction( ss , action ) && runBefore )
				ss = getActionStatus( ss , action , action.executeScopeSet( stateSet , set , items ) );

			if( checkNeedRunAction( ss , action ) && !isRunDone( ss ) ) {
				for( ActionScopeTarget target : getOrderedTargets( set , items ) ) {
					if( !running )
						break;
					
					ScopeState stateTarget = new ScopeState( stateSet , target );
					SCOPESTATE ssTarget = runSingleTargetInternal( target , stateTarget );
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
					action.runAfter( stateSet , set , items );
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
			action.runBefore( stateSet , set , items );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScopeSet( stateSet , set , items ) );
				if( !isRunDone( ss ) )
					ss = runTargetListInternal( set , set.targets.values().toArray( new ActionScopeTarget[0] ) , false , stateSet );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateSet , set , items );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runTargetCategoriesInternal( ActionScope scope , DBEnumScopeCategoryType[] categories ) {
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
					for( DBEnumScopeCategoryType CATEGORY : categories ) {
						if( !running )
							break;
						
						if( CATEGORY.checkCategoryProperty( set.CATEGORY ) )
							run = true;
					}
				}

				if( !run )
					continue;

				// execute set
				ScopeState stateSet = new ScopeState( stateFinal , set );
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
	
	private SCOPESTATE runAllInternal( ActionScope scope ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			String all = ( scope.isFull() )? " (all)" : "";
			action.debug( action.NAME + ": execute scope" + all + " ..." );
			action.runBefore( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScope( stateFinal , scope ) );
				if( !isRunDone( ss ) )
					ss = runTargetCategoriesInternal( scope , null );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateFinal , scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	private SCOPESTATE runSingleTargetInternal( ActionScopeTarget target , ScopeState stateTarget ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			String all = ( target.itemFull )? " (all)" : "";
			action.debug( action.NAME + ": execute target=" + target.NAME + all + " ..." );
			action.runBefore( stateTarget , target );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScopeTarget( stateTarget , target ) );
				if( !isRunDone( ss ) )
					ss = runTargetItemsInternal( target , stateTarget );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		try {
			if( isRunDone( ss ) )
				action.runAfter( stateTarget , target );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	private SCOPESTATE runTargetItemsInternal( ActionScopeTarget target , ScopeState stateTarget ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			List<ActionScopeTargetItem> items = target.getItems( action );
			if( items.isEmpty() ) {
				action.trace( "target=" + target.NAME + " is empty, not processed" );
				return( ss );
			}
			
			for( ActionScopeTargetItem item : items ) {
				if( !running )
					break;
				
				ScopeState stateItem = new ScopeState( stateTarget , item ); 
				SCOPESTATE ssItem = runSingleTargetItemInternal( target , item , stateItem );
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
	
	private SCOPESTATE runSingleTargetItemInternal( ActionScopeTarget target , ActionScopeTargetItem item , ScopeState stateItem ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			action.debug( action.NAME + ": run item=" + item.NAME );
			action.runBefore( stateItem , target , item );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunBeforeFail;
		}
		
		try {		
			if( checkNeedRunAction( ss , action ) ) {
				ss = getActionStatus( ss , action , action.executeScopeTargetItem( stateItem , target , item ) );
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
				action.runAfter( stateItem , target , item );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}
	
	private SCOPESTATE runHostListInternal( ActionScopeSet set , Account[] hosts , Map<Account,ActionScopeTargetItem[]> map , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			for( Account host : hosts ) {
				if( !running )
					break;
					
				ActionScopeTargetItem[] items = map.get( host );
				ScopeState stateAccount = new ScopeState( stateSet , host , items );
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
	
	private SCOPESTATE runSingleHostInternal( ActionScopeSet set , String host , int port , DBEnumOSType OSTYPE , ScopeState stateAccount ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			Datacenter dc = set.sg.getDatacenter();
			Account account = action.getSingleHostAccount( dc , host , port , OSTYPE );
			String serverNodes = set.sg.getServerNodesByHost( action , host );
			action.info( account.getPrintName() + ": serverNodes={" + serverNodes + "}" );
			
			ss = getActionStatus( ss , action , action.executeAccount( stateAccount , set , account ) );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	private SCOPESTATE runAccountListInternal( ActionScopeSet set , Account[] accounts , Map<Account,ActionScopeTargetItem[]> map , ScopeState stateSet ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			for( Account account : accounts ) {
				if( !running )
					break;
					
				ActionScopeTargetItem[] items = map.get( account );
				ScopeState stateAccount = new ScopeState( stateSet , account , items );
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
	
	private SCOPESTATE runSingleAccountInternal( ActionScopeSet set , Account account , ScopeState stateAccount ) {
		SCOPESTATE ss = SCOPESTATE.New;
		try {
			String serverNodes = set.sg.getServerNodesByAccount( action , account );
			action.info( account.getPrintName() + ": serverNodes={" + serverNodes + "}" );
			ss = getActionStatus( ss , action , action.executeAccount( stateAccount , set , account ) );
		}
		catch( Throwable e ) {
			action.handle( e );
			ss = SCOPESTATE.RunFail;
		}
		
		return( ss );
	}

	public boolean isRunDone( SCOPESTATE ss ) {
		if( !running )
			return( false );
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
	
	private ActionScopeSet[] getOrderedSets( ActionScope scope ) throws Exception {
		List<ActionScopeSet> list = new LinkedList<ActionScopeSet>();
		if( scope.meta != null ) {
			MetaSources sources = scope.meta.getSources(); 
			for( String sourceSetName : sources.getSetNames() ) {
				ActionScopeSet set = scope.findSet( action , DBEnumScopeCategoryType.PROJECT , sourceSetName );
				if( set != null )
					list.add( set );
			}
		}
			
		for( ActionScopeSet set : scope.getCategorySets( action ) )
			list.add( set );
		
		if( context.env != null ) {
			for( MetaEnvSegment envSet : context.env.getSegments() ) {
				ActionScopeSet set = scope.findSet( action , DBEnumScopeCategoryType.ENV , envSet.NAME );
				if( set != null )
					list.add( set );
			}
		}
		
		return( list.toArray( new ActionScopeSet[0] ) );
	}
	
	private List<ActionScopeTarget> getOrderedTargets( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		List<ActionScopeTarget> list = new LinkedList<ActionScopeTarget>();
		Map<String,ActionScopeTarget> map = new HashMap<String,ActionScopeTarget>();
		
		if( set.CATEGORY.isSource() ) {
			for( ActionScopeTarget target : targets )
				map.put( target.sourceProject.NAME , target );
			
			for( MetaSourceProject project : set.pset.getOrderedList() ) {
				ActionScopeTarget target = map.get( project.NAME );
				if( target != null )
					list.add( target );
			}
			
			return( list );
		}
				
		if( set.CATEGORY == DBEnumScopeCategoryType.ENV ) {
			for( ActionScopeTarget target : targets )
				map.put( target.envServer.NAME , target );

			for( MetaEnvServer server : set.sg.getServers() ) {
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
		if( !running )
			return( false );
		if( ss == SCOPESTATE.RunBeforeFail )
			return( false );
		if( !action.continueRun() )
			return( false );
		action.clearCall();
		return( true );
	}

	private SCOPESTATE getActionStatus( SCOPESTATE ss , ActionBase action , SCOPESTATE ssAction ) {
		if( !running ) {
			action.fail0( _Error.ActionStopped0 , "Action has been stopped" );
			return( SCOPESTATE.RunStopped );
		}
		if( action.isCallFailed() )
			return( SCOPESTATE.RunFail );
		if( ssAction == SCOPESTATE.RunFail )
			action.fail0( _Error.InternalError0 , "Internal error" );
		return( ssAction );
	}

	private boolean startExecutor( ActionScope scope ) {
		try {
			if( action.parent != null ) {
				if( !action.parent.startChild( action ) )
					return( false );
			}
				
			running = true;
			notifyStartAction( action );
			stateFinal = new ScopeState( parentState , action , scope );
			action.startExecutor( this , stateFinal );
			return( true );
		}
		catch( Throwable e ) {
			action.engine.log( "start action" , e );
		}
		
		return( false );
	}
	
	private boolean finishExecutor( SCOPESTATE ss ) {
		try {
			if( action.parent != null )
				action.parent.stopChild( action );
				
			action.engine.shellPool.releaseActionPool( action );
			stateFinal.setActionStatus( ss );
	
			boolean res = true;
			if( ss == SCOPESTATE.RunFail || ss == SCOPESTATE.RunBeforeFail )
				res = false;
			
			action.engine.blotter.stopAction( action , res );
			running = false;
			notifyFinishAction( action );
			return( res );
		}
		catch( Throwable e ) {
			action.engine.log( "stop action" , e );
			running = false;
			return( false );
		}
	}

	private void accessDenied( String msg ) {
		action.error( msg );
		action.fail0( _Error.AccessDenied0 , "Access denied" );
	}

	private void notifyStartAction( ActionCore action ) {
		action.eventSource.notifyCustomEvent( EventService.OWNER_ENGINE , EventService.EVENT_STARTACTION , action );
		
		ActionCore actionParent = action;
		while( actionParent.parent != null ) {
			actionParent = actionParent.parent;
			actionParent.eventSource.notifyCustomEvent( EventService.OWNER_ENGINE , EventService.EVENT_STARTCHILDACTION , action );
		}
	}

	private void notifyFinishAction( ActionBase action ) {
		action.eventSource.notifyCustomEvent( EventService.OWNER_ENGINE , EventService.EVENT_FINISHACTION , action );
		
		ActionCore actionParent = action;
		while( actionParent.parent != null ) {
			actionParent = actionParent.parent;
			actionParent.eventSource.notifyCustomEvent( EventService.OWNER_ENGINE , EventService.EVENT_FINISHCHILDACTION , action );
		}
	}
	
}
