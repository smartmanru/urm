package org.urm.action.build;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.ScopeState.SCOPETYPE;
import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.engine.ServerEvents;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerSourceEvent;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;

public class BuildPlan extends ServerEventsSource implements ServerEventsListener {
	
	List<BuildPlanSet> listSets;
	Map<String,BuildPlanSet> mapSets;
	public Dist dist;
	public BuildPlanSet selectSet;
	public RunError error;
	
	ServerEventsApp eventsApp;

	public static int EVENT_ITEMFINISHED = 1000;
	
	private BuildPlan( Dist dist , ServerEvents events , String id ) {
		super( events , id );
		this.dist = dist;
		
		listSets = new LinkedList<BuildPlanSet>();
		mapSets = new HashMap<String,BuildPlanSet>();
		eventsApp = events.createApp( id );
	}
	
	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
	@Override
	public void triggerEvent( ServerSourceEvent event ) {
		if( event.eventType == ServerEvents.EVENT_FINISHCHILDSTATE ) {
			ScopeState state = ( ScopeState )event.data;
			if( state.action instanceof ActionSetTagOnBuildBranch ) {
				if( state.type == SCOPETYPE.TypeTarget )
					addSetTagStatus( state.target.sourceProject , state.state );
			}
		}
	}
	
	@Override
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub ) {
	}
	
	public static BuildPlan create( ActionBase action , ServerEventsApp app , ServerEventsListener listener , Dist dist ) {
		ServerEvents events = action.engine.getEvents();
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
		if( !executeCompile( action , options ) )
			return( false );
		if( !executeConf( action , options ) )
			return( false );
		if( !executeDatabase( action , options ) )
			return( false );
		return( true );
	}
	
	public boolean executeCompile( ActionBase action , CommandOptions options ) {
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
	
	public BuildPlanItem getItem( MetaSourceProject sourceProject ) {
		BuildPlanSet set = getSet( sourceProject.set );
		for( BuildPlanItem item : set.listItems ) {
			if( item.target.sourceProject == sourceProject )
				return( item );
		}
		return( null );
	}
	
	private void addSetTagStatus( MetaSourceProject sourceProject , SCOPESTATE state ) {
		BuildPlanItem item = getItem( sourceProject );
		if( item == null )
			return;
		
		if( state != SCOPESTATE.RunSuccess ) {
			item.doneBuild = true;
			item.failedBuild = true;
			super.trigger( EVENT_ITEMFINISHED , item );
		}
	}
	
}
