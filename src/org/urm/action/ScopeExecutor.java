package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.action.CommandContext;
import org.urm.engine.meta.MetaEnvDC;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.MetaSourceProject;
import org.urm.engine.meta.MetaSourceProjectSet;
import org.urm.engine.meta.Meta.VarCATEGORY;
import org.urm.engine.shell.Account;

public class ScopeExecutor {

	ActionBase action;
	CommandContext context;

	boolean runFailed = false;
	boolean exception = false;
	boolean runUniqueHosts = false;
	boolean runUniqueAccounts = false;
	
	public ScopeExecutor( ActionBase action ) {
		this.action = action;
		this.context = action.context;
	}

	private boolean checkFailed() {
		if( exception || action.progressFailed || runFailed )
			return( true );
		return( false );
	}

	private boolean getFinalStatus() {
		runFailed = checkFailed();
		if( runFailed )
			return( false );
		
		return( true );
	}

	private boolean finishExecutor( boolean res ) {
		if( res )
			res = getFinalStatus();
		action.engine.shellPool.releaseActionPool( action );
		return( res );
	}
	
	public boolean runAll( ActionScope scope ) {
		runFailed = false;
		exception = false;
		boolean res = runAllInternal( scope );
		return( finishExecutor( res ) );
	}
	
	public boolean runAll( ActionScopeSet set ) {
		runFailed = false;
		exception = false;
		boolean res = runTargetSetInternal( set );
		return( finishExecutor( res ) );
	}
	
	public boolean runSimple() {
		runFailed = false;
		exception = false;
		boolean runDone = false;
		try {
			action.debug( action.NAME + ": run without scope" );
			action.runBefore();
			if( action.continueRun() ) {
				runDone = true;
				if( !action.executeSimple() )
					runDone = false;
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}

		try {
			if( runDone )
				action.runAfter();
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		return( finishExecutor( true ) );
	}

	public boolean runSingleTarget( ActionScopeTarget item ) {
		runFailed = false;
		exception = false;
		boolean runDone = false;
		try {
			action.runBefore();
			if( action.continueRun() ) {
				action.debug( action.NAME + ": run scope={" + item.set.NAME + "={" + item.NAME + "}}" );
				runDone = true;
				if( !runTargetListInternal( item.CATEGORY , item.set , new ActionScopeTarget[] { item } , true ) )
					runDone = false;
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter();
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		return( finishExecutor( true ) );
	}
	
	public boolean runTargetList( ActionScopeSet set , ActionScopeTarget[] targets ) {
		runFailed = false;
		exception = false;
		boolean runDone = false;
		try {
			action.runBefore();
			if( action.continueRun() ) {
				String list = "";
				for( ActionScopeTarget target : targets )
					list = Common.addItemToUniqueSpacedList( list , target.NAME );
				action.debug( action.NAME + ": run scope={" + set.NAME + "={" + list + "}}" );
				runDone = true;
				if( !runTargetListInternal( set.CATEGORY , set , targets , true ) )
					runDone = false;
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter();
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		return( finishExecutor( true ) );
	}
	
	public boolean runCategories( ActionScope scope , VarCATEGORY[] categories ) {
		runFailed = false;
		exception = false;
		boolean runDone = false;
		try {
			action.debug( action.NAME + ": run scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( scope );
			if( action.continueRun() ) {
				runDone = true;
				if( !runTargetCategoriesInternal( scope , categories ) )
					runDone = false;
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		return( finishExecutor( true ) );
	}
	
	public boolean runEnvUniqueHosts( ActionScope scope ) {
		runFailed = false;
		exception = false;
		boolean runDone = false;
		try {
			VarCATEGORY[] categories = new VarCATEGORY[] { VarCATEGORY.ENV };
			action.debug( action.NAME + ": run unique hosts of scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( scope );
			if( action.continueRun() ) {
				runDone = true;
				
				runUniqueHosts = true;
				if( !runTargetCategoriesInternal( scope , categories ) )
					runDone = false;
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		return( finishExecutor( true ) );
	}
	
	public boolean runEnvUniqueAccounts( ActionScope scope ) {
		if( !action.context.CTX_HOSTUSER.equals( "default" ) ) {
			if( action.context.CTX_ROOTUSER || !action.context.CTX_HOSTUSER.isEmpty() )
				return( runEnvUniqueHosts( scope ) );
		}
		
		runFailed = false;
		exception = false;
		boolean runDone = false;
		try {
			VarCATEGORY[] categories = new VarCATEGORY[] { VarCATEGORY.ENV };
			action.debug( action.NAME + ": run unique accounts of scope={" + scope.getScopeInfo( action , categories ) + "}" );
			action.runBefore( scope );
			if( action.continueRun() ) {
				runDone = true;
			
				runUniqueAccounts = true;
				if( !runTargetCategoriesInternal( scope , categories ) )
					runDone = false;
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		return( finishExecutor( true ) );
	}
	
	// implementation
	private boolean runSingleTargetItemInternal( VarCATEGORY CATEGORY , ActionScopeTarget target , ActionScopeTargetItem item ) {
		boolean runDone = false;
		try {
			action.debug( action.NAME + ": run item=" + item.NAME );
			action.runBefore( target , item );
			if( action.continueRun() ) {
				runDone = true;
				if( !action.executeScopeTargetItem( target , item ) ) {
					runDone = false;
					action.trace( "target=" + target.NAME + ", item=" + item.NAME + " is not processed" );
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}

		try {
			if( runDone )
				action.runAfter( target , item );
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}
	
	private boolean runTargetItemsInternal( VarCATEGORY CATEGORY , ActionScopeTarget target ) {
		boolean runDone = false;
		try {
			List<ActionScopeTargetItem> items = target.getItems( action );
			if( items.isEmpty() ) {
				action.trace( "target=" + target.NAME + " is empty, not processed" );
				return( false );
			}
			
			for( ActionScopeTargetItem item : items ) {
				if( runSingleTargetItemInternal( CATEGORY , target , item ) ) {
					runDone = true;
					if( !action.continueRun() )
						break;
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}
	
	private boolean runSingleTargetInternal( VarCATEGORY CATEGORY , ActionScopeTarget target ) {
		boolean runDone = false;
		try {
			String all = ( target.itemFull )? " (all)" : "";
			action.debug( action.NAME + ": execute target=" + target.NAME + all + " ..." );
			action.runBefore( target );
			if( action.continueRun() ) {
				runDone = true;
			
				if( !action.executeScopeTarget( target ) )
					runDone = false;
				
				if( !runDone ) {
					runDone = true;
					if( !runTargetItemsInternal( CATEGORY , target ) )
						runDone = false;
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter( target );
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}

	private boolean runSingleHostInternal( ActionScopeSet set , String host , int port , VarOSTYPE OSTYPE ) {
		boolean runDone = false;
		try {
			Account account = action.getSingleHostAccount( host , port , OSTYPE );
			String serverNodes = set.dc.getServerNodesByHost( action , host );
			action.info( account.getPrintName() + ": serverNodes={" + serverNodes + "}" );
			
			runDone = true;
			if( !action.executeAccount( set , account ) )
				runDone = false;
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}

	private boolean runSingleAccountInternal( ActionScopeSet set , Account account ) {
		boolean runDone = false;
		try {
			String serverNodes = set.dc.getServerNodesByAccount( action , account );
			action.info( account.getPrintName() + ": serverNodes={" + serverNodes + "}" );
			runDone = true;
			if( !action.executeAccount( set , account ) )
				runDone = false;
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}

	private boolean runHostListInternal( ActionScopeSet set , Account[] hosts ) {
		boolean runDone = false;
		try {
			for( Account host : hosts ) {
				if( runSingleHostInternal( set , host.HOST , host.PORT , host.osType ) ) {
					runDone = true;
					if( !action.continueRun() )
						break;
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}
	
	private boolean runAccountListInternal( ActionScopeSet set , Account[] accounts ) {
		boolean runDone = false;
		try {
			for( Account account : accounts ) {
				if( runSingleAccountInternal( set , account ) ) {
					runDone = true;
					if( !action.continueRun() )
						break;
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}
	
	private boolean runTargetListInternal( VarCATEGORY CATEGORY , ActionScopeSet set , ActionScopeTarget[] items , boolean runBeforeAfter ) {
		boolean runDone = false;
		try {
			if( runUniqueHosts ) {
				Account[] hosts = set.getUniqueHosts( action , items );
				return( runHostListInternal( set , hosts ) );
			}
			
			if( runUniqueAccounts ) {
				Account[] accounts = set.getUniqueAccounts( action , items );
				return( runAccountListInternal( set , accounts ) );
			}
			
			// execute list as is
			if( runBeforeAfter ) {
				action.runBefore( set , items );
			
				if( action.continueRun() ) {
					runDone = true;
					if( !action.executeScopeSet( set , items ) )
						runDone = false;
				}
			}

			if( !runDone ) {
				for( ActionScopeTarget target : getOrderedTargets( set , items ) ) {
					if( runSingleTargetInternal( CATEGORY , target ) ) {
						runDone = true;
						if( !action.continueRun() )
							break;
					}
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runBeforeAfter ) {
				if( runDone )
					action.runAfter( set , items );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}

	private boolean runTargetSetInternal( ActionScopeSet set ) {
		boolean runDone = false;
		ActionScopeTarget[] items = null;
		try {
			String all = ( set.setFull )? " (all)" : "";
			action.debug( action.NAME + ": execute scope set=" + set.NAME + all + " ..." );
			
			items = set.getTargets( action ).values().toArray( new ActionScopeTarget[0] ); 
			action.runBefore( set , items );
			if( action.continueRun() ) {
				runDone = true;

				if( !action.executeScopeSet( set , items ) )
					runDone = false;
				
				if( !runDone ) {
					runDone = true;
					if( !runTargetListInternal( set.CATEGORY , set , set.targets.values().toArray( new ActionScopeTarget[0] ) , false ) )
						runDone = false;
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter( set , items );
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}
	
	private boolean runTargetCategoriesInternal( ActionScope scope , VarCATEGORY[] categories ) {
		boolean runDone = false;
		try {
			if( scope.isEmpty( action , categories ) ) {
				action.debug( action.NAME + ": nothing to execute" );
				return( true );
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
				if( runTargetSetInternal( set ) ) {
					runDone = true;
					if( !action.continueRun() )
						break;
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}
	
	private boolean runAllInternal( ActionScope scope ) {
		boolean runDone = false;
		try {
			String all = ( scope.scopeFull )? " (all)" : "";
			action.debug( action.NAME + ": execute scope" + all + " ..." );
			action.runBefore( scope );
			if( action.continueRun() ) {
				runDone = true;
			
				if( !action.executeScope( scope ) )
					runDone = false;
				
				if( !runDone ) {
					runDone = true;
					if( !runTargetCategoriesInternal( scope , null ) )
						runDone = false;
				}
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		try {
			if( runDone )
				action.runAfter( scope );
		}
		catch( Throwable e ) {
			action.handle( e );
			exception = true;
		}
		
		runFailed = checkFailed();
		return( runDone );
	}

	private List<ActionScopeSet> getOrderedSets( ActionScope scope ) throws Exception {
		List<ActionScopeSet> list = new LinkedList<ActionScopeSet>();
		if( action.meta.sources != null ) {
			for( MetaSourceProjectSet sourceSet : action.meta.sources.getSetList( action ) ) {
				ActionScopeSet set = scope.findSet( action , sourceSet.CATEGORY , sourceSet.NAME );
				if( set != null )
					list.add( set );
			}
		}
		
		for( ActionScopeSet set : scope.getCategorySets( action ) )
			list.add( set );
		
		if( context.env != null ) {
			for( MetaEnvDC envSet : context.env.getOriginalDCList( action ) ) {
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
		
		if( action.meta.isSourceCategory( set.CATEGORY ) ) {
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

			for( MetaEnvServer server : set.dc.getOriginalServerList( action ) ) {
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
	
}