package org.urm.engine.status;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsState;
import org.urm.engine.status.EngineStatus.StatusType;
import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.meta.EngineObject;

public class StatusSource extends EngineEventsSource {

	public static int EVENT_UPDATESTARTED = 1000;
	public static int EVENT_UPDATEFINISHED = 1001;
	
	public StatusType type;
	public EngineObject object;
	public StatusData state;
	public Date runTime;
	public boolean updating;
	
	private StatusData primary;
	private Map<String,StatusData> extra;

	public StatusSource( EngineEvents events , EngineObject object , StatusType type , String name , Status status ) {
		super( events , name );
		this.object = object;
		this.type = type;
		
		state = new StatusData( this , status );
		primary = new StatusData( this , null );
		extra = new HashMap<String,StatusData>(); 
	}
	
	@Override
	public EngineEventsState getState() {
		return( getStatusState() );
	}

	public StatusData getStatusState() {
		return( new StatusData( state , runTime , updating ) );
	}
	
	public void setObject( EngineObject object ) {
		this.object = object;
	}
	
	public void clearState() {
		state.clear();
		primary.clear();
		extra.clear();
		runTime = null;
	}
	
	public boolean setFinalState( OBJECT_STATE newState ) {
		if( !primary.setState( newState ) )
			return( false );
		
		return( updateFinalState() );
	}

	public boolean setState( OBJECT_STATE newState , Status specific ) {
		if( !primary.setState( newState , specific ) )
			return( false );
		
		return( updateFinalState() );
	}

	private boolean updateFinalState() {
		OBJECT_STATE finalState = getFinalState();
		if( !state.setState( finalState ) )
			return( false );
		
		super.notify( EngineEvents.OWNER_ENGINE , EngineEvents.EVENT_STATECHANGED , state );
		return( true );
	}

	public synchronized StatusData getExtraState( String key ) {
		StatusData extraState = extra.get( key );
		if( extraState == null ) {
			extraState = new StatusData( this , null );
			extra.put( key , extraState );
		}
		
		return( extraState );
	}
	
	public boolean setExtraState( String key , OBJECT_STATE newState , Status specific ) {
		StatusData extraState = getExtraState( key );
		if( !extraState.setState( newState , specific ) )
			return( false );

		return( updateFinalState() );
	}
	
	private OBJECT_STATE getFinalState() {
		OBJECT_STATE state = primary.state;
		for( StatusData extraState : extra.values() )
			state = StatusData.addState( extraState.state , state );
		return( state );
	}
	
	public void setPrimaryLog( String[] log ) {
		primary.setLog( log );
	}

	public void setExtraLog( String key , String[] log ) {
		StatusData extraState = getExtraState( key );
		extraState.setLog( log );
	}

	public String[] getPrimaryLog() {
		return( primary.log );
	}

	public String[] getExtraLog( String key ) {
		StatusData extraState = getExtraState( key );
		return( extraState.log );
	}

	public void customEvent( int eventOwner , int eventType , Object data ) {
		super.notify( eventOwner , eventType , data );
	}

	public void updateRunTime() {
		runTime = new Date();
		updating = true;
		super.notify( EngineEvents.OWNER_ENGINESTATUS , EVENT_UPDATESTARTED , getStatusState() );
	}

	public void finishUpdate() {
		updating = false;
		super.notify( EngineEvents.OWNER_ENGINESTATUS , EVENT_UPDATEFINISHED , getStatusState() );
	}
	
}
