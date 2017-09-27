package org.urm.action.deploy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.database.ActionApplyAutomatic;
import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.engine.dist.Dist;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsState;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.SourceEvent;
import org.urm.engine.status.ObjectState.STATETYPE;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;

public class DeployPlan extends EngineEventsSource implements EngineEventsListener {
	
	public static int METHOD_REDIST = 1;
	public static int METHOD_DEPLOYDIST = 2;
	
	public static int EVENT_ITEMFINISHED = 1000;
	public static int EVENT_REDISTFINISHED = 1001;
	public static int EVENT_DEPLOYFINISHED = 1002;
	
	public List<DeployPlanSegment> listSg;
	Map<String,DeployPlanSegment> mapSg;
	
	public Dist dist;
	public MetaEnv env;
	public DeployPlanSegment selectSg;
	public DeployPlanSet selectSet;
	public RunError error; 

	boolean redist;
	boolean deploy;
	
	EngineEventsApp eventsApp;

	private DeployPlan( Dist dist , MetaEnv env , boolean redist , boolean deploy , EngineEvents events , String id ) {
		super( events , id );
		this.dist = dist;
		this.env = env;
		this.redist = redist;
		this.deploy = deploy;
		
		listSg = new LinkedList<DeployPlanSegment>();
		mapSg = new HashMap<String,DeployPlanSegment>();
		eventsApp = events.createApp( id );
	}
	
	@Override
	public EngineEventsState getState() {
		return( null );
	}
	
	@Override
	public void triggerEvent( EngineEventsSubscription sub , SourceEvent event ) {
		if( event.eventType == EngineEvents.EVENT_FINISHCHILDSTATE ) {
			ScopeState state = ( ScopeState )event.data;
			if( state.action instanceof ActionRedist ) {
				if( state.type == STATETYPE.TypeTarget )
					addRedistStatus( state.target.envServer , state.state );
			}
			else
			if( state.action instanceof ActionStopServer ) {
				if( state.type == STATETYPE.TypeTarget )
					addStopServerStatus( state.target.envServer , state.state );
			}
			else
			if( state.action instanceof ActionRollout ) {
				if( state.type == STATETYPE.TypeTarget )
					addRolloutStatus( state.target.envServer , state.state );
			}
			else
			if( state.action instanceof ActionStartServer ) {
				if( state.type == STATETYPE.TypeTarget )
					addStartServerStatus( state.target.envServer , state.state );
			}
			else
			if( state.action instanceof ActionApplyAutomatic ) {
				if( state.type == STATETYPE.TypeTarget )
					addDatabaseApplyStatus( state.target.envServer , state.state );
			}
		}
	}
	
	public static DeployPlan create( ActionBase action , EngineEventsApp app , EngineEventsListener listener , Dist dist , MetaEnv env , boolean redist , boolean deploy ) {
		EngineEvents events = action.engine.getEvents();
		DeployPlan plan = new DeployPlan( dist , env , redist , deploy , events , "build-plan-" + action.ID );
		app.subscribe( plan , listener );
		return( plan );
	}
	
	public void clearRun() {
		error = null;
		for( DeployPlanSegment sg : listSg )
			sg.clearRun();
	}
	
	public int getSegmentCount() {
		return( listSg.size() );
	}
	
	public void addSegment( DeployPlanSegment sg ) {
		listSg.add( sg );
		mapSg.put( sg.sg.NAME , sg );
	}
	
	public DeployPlanSegment findSet( String sgName ) {
		return( mapSg.get( sgName ) );
	}
	
	public void selectSegment( DeployPlanSegment sg ) {
		this.selectSg = sg;
	}
			
	public void selectSet( String setName ) {
		if( setName.isEmpty() )
			selectSet = null;
		else
			selectSet = selectSg.findSet( setName );
		
		for( DeployPlanSegment sg : listSg ) {
			for( DeployPlanSet set : sg.listSets ) {
				for( DeployPlanItem item : set.listItems ) {
					if( selectSet != null && set != selectSet )
						item.setExecute( false );
				}
			}
		}
	}

	public void setDeploy( boolean deploy ) {
		this.deploy = deploy;
	}
	
	public boolean hasExecute() {
		for( DeployPlanSegment sg : listSg ) {
			for( DeployPlanSet set : sg.listSets ) {
				for( DeployPlanItem item : set.listItems ) {
					if( item.execute )
						return( true );
				}
			}
		}
		return( false );
	}

	public boolean executeRedist( ActionBase action , CommandOptions options ) {
		String[] args = null;
		
		// redist
		if( selectSet == null )
			args = new String[] { dist.RELEASEDIR , "all" };
		else {
			DeployPlanSet set = selectSet;
			String[] selected = set.getSelected();
			if( selected.length > 0 ) {
				args = new String[ 1 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				for( int k = 0; k < selected.length; k++ )
					args[ 1 + k ] = Common.getPartAfterFirst( selected[ k ] , "::" );
			}
		}
		
		MetaEnvSegment sg = ( selectSg == null )? null : selectSg.sg;
		error = action.runNotifyMethod( METHOD_REDIST , null , eventsApp , this , env.meta , env , sg , DeployCommandMeta.NAME , DeployCommandMeta.METHOD_REDIST , args , options );
		boolean res = ( error != null )? false : true;
		finishPlanRedist();
		return( res );
	}

	public boolean executeDeploy( ActionBase action , CommandOptions options ) {
		String[] args = null;
		
		// redist
		if( selectSet == null )
			args = new String[] { dist.RELEASEDIR , "all" };
		else {
			DeployPlanSet set = selectSet;
			String[] selected = set.getSelected();
			if( selected.length > 0 ) {
				args = new String[ 1 + selected.length ];
				args[ 0 ] = dist.RELEASEDIR;
				for( int k = 0; k < selected.length; k++ )
					args[ 1 + k ] = Common.getPartAfterFirst( selected[ k ] , "::" );
			}
		}
		
		MetaEnvSegment sg = ( selectSg == null )? null : selectSg.sg;
		error = action.runNotifyMethod( METHOD_DEPLOYDIST , null , eventsApp , this , env.meta , env , sg , DeployCommandMeta.NAME , DeployCommandMeta.METHOD_DEPLOYREDIST , args , options );
		boolean res = ( error != null )? false : true;
		finishPlanDeploy();
		return( res );
	}
	
	public DeployPlanSegment getSegment( MetaEnvSegment sg ) {
		for( DeployPlanSegment psg : listSg ) {
			if( psg.sg == sg )
				return( psg );
		}
		return( null );
	}
	
	public DeployPlanItem getItem( MetaEnvServer server ) {
		DeployPlanSegment sg = getSegment( server.sg );
		for( DeployPlanSet set : sg.listSets ) {
			for( DeployPlanItem item : set.listItems ) {
				if( item.server == server )
					return( item );
			}
		}
		return( null );
	}
	
	private void addRedistStatus( MetaEnvServer server , SCOPESTATE state ) {
		DeployPlanItem item = getItem( server );
		if( item == null )
			return;
		
		boolean success = ( state == SCOPESTATE.RunFail || state == SCOPESTATE.RunBeforeFail )? false : true;
		item.setDoneRedist( success );
		super.notify( EngineEvents.OWNER_ENGINEDEPLOYPLAN , EVENT_ITEMFINISHED , item );
	}
	
	private void addStopServerStatus( MetaEnvServer server , SCOPESTATE state ) {
		DeployPlanItem item = getItem( server );
		if( item == null )
			return;
		
		if( state == SCOPESTATE.RunSuccess )
			item.setDeployStarted();
		else
		if( state != SCOPESTATE.NotRun )
			item.setDeployDone( false );
		super.notify( EngineEvents.OWNER_ENGINEDEPLOYPLAN , EVENT_ITEMFINISHED , item );
	}
	
	private void addRolloutStatus( MetaEnvServer server , SCOPESTATE state ) {
		DeployPlanItem item = getItem( server );
		if( item == null )
			return;
		
		if( state != SCOPESTATE.RunSuccess )
			item.setDeployDone( false );
		else {
			if( !item.startDeploy )
				item.setDeployDone( true );
		}
		
		super.notify( EngineEvents.OWNER_ENGINEDEPLOYPLAN , EVENT_ITEMFINISHED , item );
	}
	
	private void addStartServerStatus( MetaEnvServer server , SCOPESTATE state ) {
		DeployPlanItem item = getItem( server );
		if( item == null )
			return;
		
		if( state != SCOPESTATE.RunSuccess )
			item.setDeployDone( false );
		else
			item.setDeployDone( true );
		
		super.notify( EngineEvents.OWNER_ENGINEDEPLOYPLAN , EVENT_ITEMFINISHED , item );
	}
	
	private void addDatabaseApplyStatus( MetaEnvServer server , SCOPESTATE state ) {
		DeployPlanItem item = getItem( server );
		if( item == null )
			return;
		
		if( state != SCOPESTATE.RunSuccess )
			item.setDeployDone( false );
		else
			item.setDeployDone( true );
		
		super.notify( EngineEvents.OWNER_ENGINEDEPLOYPLAN , EVENT_ITEMFINISHED , item );
	}
	
	private void finishPlanRedist() {
		for( DeployPlanSegment sg : listSg ) {
			for( DeployPlanSet set : sg.listSets ) {
				for( DeployPlanItem item : set.listItems ) {
					if( selectSet != null && set != selectSet )
						item.setRedistNotRun();
				}
			}
		}
		super.notify( EngineEvents.OWNER_ENGINEDEPLOYPLAN , EVENT_REDISTFINISHED , this );
	}
	
	private void finishPlanDeploy() {
		for( DeployPlanSegment sg : listSg ) {
			for( DeployPlanSet set : sg.listSets ) {
				for( DeployPlanItem item : set.listItems ) {
					if( selectSet != null && set != selectSet )
						item.setDeployNotRun();
				}
			}
		}
		super.notify( EngineEvents.OWNER_ENGINEDEPLOYPLAN , EVENT_DEPLOYFINISHED , this );
	}
	
}
