package org.urm.action.deploy;

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
import org.urm.common.meta.DeployCommandMeta;
import org.urm.engine.ServerEvents;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerEventsSource;
import org.urm.engine.ServerEventsState;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerSourceEvent;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;

public class DeployPlan extends ServerEventsSource implements ServerEventsListener {
	
	public List<DeployPlanSegment> listSg;
	Map<String,DeployPlanSegment> mapSg;
	
	public Dist dist;
	public MetaEnv env;
	public DeployPlanSegment selectSg;
	public DeployPlanSet selectSet;
	public RunError error; 

	boolean redist;
	boolean deploy;
	
	ServerEventsApp eventsApp;

	public static int EVENT_ITEMFINISHED = 1000;
	
	private DeployPlan( Dist dist , MetaEnv env , boolean redist , boolean deploy , ServerEvents events , String id ) {
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
	public ServerEventsState getState() {
		return( null );
	}
	
	@Override
	public void triggerEvent( ServerSourceEvent event ) {
		if( event.eventType == ServerEvents.EVENT_FINISHCHILDSTATE ) {
			ScopeState state = ( ScopeState )event.data;
			if( state.action instanceof ActionRedist ) {
				if( state.type == SCOPETYPE.TypeTarget )
					addRedistStatus( state.target.envServer , state.state );
			}
		}
	}
	
	@Override
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub ) {
	}
	
	public static DeployPlan create( ActionBase action , ServerEventsApp app , ServerEventsListener listener , Dist dist , MetaEnv env , boolean redist , boolean deploy ) {
		ServerEvents events = action.engine.getEvents();
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
		error = action.runNotifyMethod( eventsApp , this , env.meta , env , sg , DeployCommandMeta.NAME , DeployCommandMeta.METHOD_REDIST , args , options );
		if( error != null )
			return( false );
		return( true );
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
		error = action.runNotifyMethod( eventsApp , this , env.meta , env , sg , DeployCommandMeta.NAME , DeployCommandMeta.METHOD_DEPLOYREDIST , args , options );
		if( error != null )
			return( false );
		return( true );
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
		
		item.doneRedist = true;
		if( state != SCOPESTATE.RunSuccess )
			item.failedRedist = true;
		super.trigger( EVENT_ITEMFINISHED , item );
	}
	
}
