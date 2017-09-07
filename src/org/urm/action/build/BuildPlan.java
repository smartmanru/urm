package org.urm.action.build;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.conf.ActionGetConf;
import org.urm.action.database.ActionGetDB;
import org.urm.action.release.ActionGetCumulative;
import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsState;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.EngineSourceEvent;
import org.urm.engine.status.ObjectState.STATETYPE;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;

public class BuildPlan extends EngineEventsSource implements EngineEventsListener {
	
	List<BuildPlanSet> listSets;
	Map<String,BuildPlanSet> mapSets;
	public Dist dist;
	public BuildPlanSet selectSet;
	public RunError error;
	
	EngineEventsApp eventsApp;

	public static int EVENT_ITEMFINISHED = 1000;
	public static int EVENT_PLANFINISHED = 1001;
	
	private BuildPlan( Dist dist , EngineEvents events , String id ) {
		super( events , id );
		this.dist = dist;
		
		listSets = new LinkedList<BuildPlanSet>();
		mapSets = new HashMap<String,BuildPlanSet>();
		eventsApp = events.createApp( id );
	}
	
	@Override
	public EngineEventsState getState() {
		return( null );
	}
	
	@Override
	public void triggerEvent( EngineSourceEvent event ) {
		if( event.eventType == EngineEvents.EVENT_FINISHCHILDSTATE ) {
			ScopeState state = ( ScopeState )event.data;
			if( state.action instanceof ActionSetTagOnBuildBranch ) {
				if( state.type == STATETYPE.TypeTarget )
					addSetTagStatus( state.target.sourceProject , state.state );
			}
			else
			if( state.action instanceof ActionGetBinary ) {
				if( state.type == STATETYPE.TypeTarget )
					addGetBinaryStatus( state.target.sourceProject , state.state );
			}
			else
			if( state.action instanceof ActionGetDB ) {
				if( state.type == STATETYPE.TypeTarget )
					addGetDBNormalStatus( state.target.dbDelivery , state.state );
			}
			else
			if( state.action instanceof ActionGetConf ) {
				if( state.type == STATETYPE.TypeTarget )
					addGetConfStatus( state.target.confItem , state.state );
			}
			else
			if( state.action instanceof ActionGetCumulative ) {
				if( state.type == STATETYPE.TypeScope )
					addGetDBCumulativeStatus( state.state );
			}
			else
			if( state.action instanceof ActionBuild ) {
				if( state.type == STATETYPE.TypeTarget )
					addBuildStatus( state.target.sourceProject , state.state );
			}
		}
	}
	
	@Override
	public void triggerSubscriptionRemoved( EngineEventsSubscription sub ) {
	}
	
	public static BuildPlan create( ActionBase action , EngineEventsApp app , EngineEventsListener listener , Dist dist ) {
		EngineEvents events = action.engine.getEvents();
		BuildPlan plan = new BuildPlan( dist , events , "build-plan-" + action.ID );
		app.subscribe( plan , listener );
		return( plan );
	}
	
	public int getSetCount() {
		return( listSets.size() );
	}
	
	public void addSet( BuildPlanSet set ) {
		listSets.add( set );
		mapSets.put( set.name , set );
	}
	
	public BuildPlanSet findSet( String setName ) {
		return( mapSets.get( setName ) );
	}
	
	public void selectSet( String setName ) {
		if( setName.isEmpty() )
			selectSet = null;
		else
			selectSet = findSet( setName );
		
		for( BuildPlanSet set : listSets ) {
			for( BuildPlanItem item : set.listItems ) {
				if( selectSet != null && set != selectSet )
					item.setExecute( false );
			}
		}
	}

	public void clearRun() {
		error = null;
		for( BuildPlanSet set : listSets )
			set.clearRun();
	}
	
	public boolean hasExecute() {
		if( hasCompile() || hasConf() || hasDatabase() )
			return( true );
		return( false );
	}
	
	public boolean hasCompile() {
		if( isCompileAll() || isCompileSelected() )
			return( true );
		return( false );
	}
	
	public boolean isCompileAll() {
		if( selectSet != null )
			return( false );
			
		for( BuildPlanSet set : listSets ) {
			if( set.build && set.hasSelected() )
				return( true );
		}
		
		return( false );
	}
	
	public boolean isCompileSelected() {
		if( selectSet == null || selectSet.build == false )
			return( false );
		if( !selectSet.hasSelected() )
			return( false );
		return( true );
	}
	
	public boolean hasConf() {
		if( isConfAll() || isConfSelected() )
			return( true );
		return( false );
	}
	
	public boolean isConfAll() {
		if( selectSet != null )
			return( false );
			
		for( BuildPlanSet set : listSets ) {
			if( set.conf && set.hasSelected() )
				return( true );
		}
		
		return( false );
	}
	
	public boolean isConfSelected() {
		if( selectSet == null || selectSet.conf == false )
			return( false );
		if( !selectSet.hasSelected() )
			return( false );
		return( true );
	}
	
	public boolean hasDatabase() {
		if( isDatabaseAll() || isDatabaseSelected() )
			return( true );
		return( false );
	}
	
	public boolean isDatabaseAll() {
		if( selectSet != null )
			return( false );
			
		for( BuildPlanSet set : listSets ) {
			if( set.db && set.hasSelected() )
				return( true );
		}
		
		return( false );
	}
	
	public boolean isDatabaseSelected() {
		if( selectSet == null || selectSet.db == false )
			return( false );
		if( !selectSet.hasSelected() )
			return( false );
		return( true );
	}
	
	public boolean executeBuild( ActionBase action , CommandOptions options ) {
		boolean res = true;
		if( res && !executeCompileInternal( action , options ) )
			res = false;
		if( res && !executeConfInternal( action , options ) )
			res = false;
		if( res && !executeDatabaseInternal( action , options ) )
			res = false;
		finishPlan();
		return( res );
	}

	public boolean executeCompile( ActionBase action , CommandOptions options ) {
		boolean res = executeCompileInternal( action , options );
		finishPlan();
		return( res );
	}
	
	private boolean executeCompileInternal( ActionBase action , CommandOptions options ) {
		String[] args = null;
		boolean run = true;
		
		// compile and get build results
		if( hasCompile() ) {
			run = false;
			if( isCompileAll() ) {
				args = new String[] { dist.RELEASEDIR , "all" };
				run = true;
			}
			else
			if( isCompileSelected() ) {
				BuildPlanSet set = selectSet;
				String[] selected = set.getSelected();
				
				args = new String[ 2 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				args[ 1 ] = set.set.set.NAME;
				for( int k = 0; k < selected.length; k++ )
					args[ 2 + k ] = Common.getPartAfterFirst( selected[ k ] , "::" );
				run = true;
			}
			
			if( run ) {
				error = action.runNotifyMethod( eventsApp , this , dist.meta , null , null , ReleaseCommandMeta.NAME , ReleaseCommandMeta.METHOD_BUILD , args , options );
				if( error != null )
					return( false );
			}
		}
		return( true );
	}

	public boolean executeConf( ActionBase action , CommandOptions options ) {
		boolean res = executeConfInternal( action , options );
		finishPlan();
		return( res );
	}
	
	private boolean executeConfInternal( ActionBase action , CommandOptions options ) {
		String[] args = null;
		boolean run = true;
			
		if( hasConf() ) {
			run = false;
			if( isConfAll() ) {
				args = new String[] { dist.RELEASEDIR , "config" , "all" };
				run = true;
			}
			if( isDatabaseSelected() ) {
				BuildPlanSet set = selectSet;
				String[] selected = set.getSelected();
				
				args = new String[ 2 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				args[ 1 ] = "config";
				run = true;
			}
			
			if( run ) {
				error = action.runNotifyMethod( eventsApp , this , dist.meta , null , null , ReleaseCommandMeta.NAME , ReleaseCommandMeta.METHOD_GETDIST , args , options );
				if( error != null )
					return( false );
			}
		}
		return( true );
	}

	public boolean executeDatabase( ActionBase action , CommandOptions options ) {
		boolean res = executeDatabaseInternal( action , options );
		finishPlan();
		return( res );
	}

	private void finishPlan() {
		for( BuildPlanSet set : listSets ) {
			for( BuildPlanItem item : set.listItems ) {
				if( selectSet == null || set == selectSet )
					item.setNotRun();
			}
		}
		super.notify( EVENT_PLANFINISHED , null );
	}
	
	private boolean executeDatabaseInternal( ActionBase action , CommandOptions options ) {
		String[] args = null;
		boolean run = true;
		
		if( hasDatabase() ) {
			run = false;
			if( isDatabaseAll() ) {
				args = new String[] { dist.RELEASEDIR , "db" , "all" };
				run = true;
			}
			if( isDatabaseSelected() ) {
				BuildPlanSet set = selectSet;
				String[] selected = set.getSelected();
				
				args = new String[ 2 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				args[ 1 ] = "db";
				run = true;
			}
			
			if( run ) {
				error = action.runNotifyMethod( eventsApp , this , dist.meta , null , null , ReleaseCommandMeta.NAME , ReleaseCommandMeta.METHOD_GETDIST , args , options );
				if( error != null )
					return( false );
			}
		}
		
		return( true );
	}
	
	public BuildPlanSet getSet( MetaSourceProjectSet sourceSet ) {
		for( BuildPlanSet set : listSets ) {
			if( set.build && set.set.set == sourceSet )
				return( set );
		}
		return( null );
	}
	
	public BuildPlanSet getConfSet() {
		for( BuildPlanSet set : listSets ) {
			if( set.conf )
				return( set );
		}
		return( null );
	}
	
	public BuildPlanSet getDatabaseSet() {
		for( BuildPlanSet set : listSets ) {
			if( set.db )
				return( set );
		}
		return( null );
	}
	
	public BuildPlanItem getItem( MetaSourceProject sourceProject ) {
		BuildPlanSet set = getSet( sourceProject.set );
		for( BuildPlanItem item : set.listItems ) {
			if( item.target.sourceProject == sourceProject )
				return( item );
		}
		return( null );
	}
	
	public BuildPlanItem getItem( MetaDistrConfItem confItem ) {
		BuildPlanSet set = getConfSet();
		for( BuildPlanItem item : set.listItems ) {
			if( item.target.distConfItem == confItem )
				return( item );
		}
		return( null );
	}
	
	public BuildPlanItem getItem( MetaDistrDelivery delivery , String dbVersion ) {
		BuildPlanSet set = getDatabaseSet();
		for( BuildPlanItem item : set.listItems ) {
			if( item.target.distDatabaseDelivery == delivery && item.dbVersion.equals( dbVersion ) )
				return( item );
		}
		return( null );
	}
	
	private void addSetTagStatus( MetaSourceProject sourceProject , SCOPESTATE state ) {
		BuildPlanItem item = getItem( sourceProject );
		if( item == null )
			return;
		
		if( state != SCOPESTATE.RunSuccess ) {
			item.setBuildDone( false );
			super.notify( EVENT_ITEMFINISHED , item );
		}
	}
	
	private void addGetBinaryStatus( MetaSourceProject sourceProject , SCOPESTATE state ) {
		BuildPlanItem item = getItem( sourceProject );
		if( item == null )
			return;
		
		boolean success = ( state == SCOPESTATE.RunSuccess )? true : false;
		item.setGetDone( success );
		super.notify( EVENT_ITEMFINISHED , item );
	}
	
	private void addGetConfStatus( MetaDistrConfItem confItem , SCOPESTATE state ) {
		BuildPlanItem item = getItem( confItem );
		if( item == null )
			return;
		
		boolean success = ( state == SCOPESTATE.RunSuccess )? true : false;
		item.setGetDone( success );
		super.notify( EVENT_ITEMFINISHED , item );
	}
	
	private void addGetDBNormalStatus( MetaDistrDelivery delivery , SCOPESTATE state ) {
		BuildPlanItem item = getItem( delivery , dist.release.RELEASEVER );
		if( item == null )
			return;
		
		boolean success = ( state == SCOPESTATE.RunSuccess )? true : false;
		item.setGetDone( success );
		super.notify( EVENT_ITEMFINISHED , item );
	}
	
	private void addGetDBCumulativeStatus( SCOPESTATE state ) {
		for( ReleaseDelivery delivery : dist.release.getDeliveries() ) {
			for( String version : dist.release.getCumulativeVersions() ) {
				BuildPlanItem item = getItem( delivery.distDelivery , version );
				if( item != null ) {
					boolean success = ( state == SCOPESTATE.RunSuccess )? true : false;
					item.setGetDone( success );
					super.notify( EVENT_ITEMFINISHED , item );
				}
			}
		}
	}
	
	private void addBuildStatus( MetaSourceProject sourceProject , SCOPESTATE state ) {
		BuildPlanItem item = getItem( sourceProject );
		if( item == null )
			return;
		
		boolean success = ( state == SCOPESTATE.RunSuccess )? true : false;
		item.setBuildDone( success );
		super.notify( EVENT_ITEMFINISHED , item );
	}
	
}
