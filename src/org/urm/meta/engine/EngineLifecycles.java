package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.blotter.EngineBlotterSet;
import org.urm.engine.Engine;
import org.urm.engine.blotter.EngineBlotter.BlotterType;
import org.urm.meta.EngineObject;

public class EngineLifecycles extends EngineObject {

	public Engine engine;
	
	private Map<String,ReleaseLifecycle> lcMap;
	private Map<Integer,ReleaseLifecycle> lcMapById;
	
	public EngineLifecycles( Engine engine ) {
		super( null );
		this.engine = engine;
		lcMap = new HashMap<String,ReleaseLifecycle>(); 
		lcMapById = new HashMap<Integer,ReleaseLifecycle>();
	}
	
	@Override
	public String getName() {
		return( "server-lifecycles" );
	}
	
	public void addLifecycle( ReleaseLifecycle lc ) {
		lcMap.put( lc.NAME , lc );
		lcMapById.put( lc.ID , lc );
	}

	public void updateLifecycle( ReleaseLifecycle lc ) throws Exception {
		Common.changeMapKey( lcMap , lc , lc.NAME );
	}
	
	public ReleaseLifecycle findLifecycle( String name ) {
		return( lcMap.get( name ) );
	}

	public ReleaseLifecycle getLifecycle( String name ) throws Exception {
		ReleaseLifecycle lc = findLifecycle( name );
		if( lc == null )
			Common.exit1( _Error.UnknownLifecycle1 , "unknown lifecycle name=" + name , name );
		return( lc );
	}

	public ReleaseLifecycle getLifecycle( int id ) throws Exception {
		ReleaseLifecycle lc = lcMapById.get( id );
		if( lc == null )
			Common.exit1( _Error.UnknownLifecycle1 , "unknown lifecycle id=" + id , "" + id );
		return( lc );
	}

	public void addPhase( LifecyclePhase phase ) {
		phase.lc.addPhase( phase );
	}

	
	public String[] getLifecycleNames() {
		return( Common.getSortedKeys( lcMap ) );
	}

	public ReleaseLifecycle[] getLifecycles() {
		return( lcMap.values().toArray( new ReleaseLifecycle[0] ) );
	}
	
	public String[] getLifecycles( DBEnumLifecycleType type , boolean enabledOnly ) {
		List<String> list = new LinkedList<String>();
		for( String lcName : Common.getSortedKeys( lcMap ) ) {
			ReleaseLifecycle lc = lcMap.get( lcName );
			if( lc.LIFECYCLE_TYPE == type ) {
				if( enabledOnly == false || lc.ENABLED )
					list.add( lcName );
			}
		}
		return( list.toArray( new String[0] ) );
	}

	public void removeLifecycle( ReleaseLifecycle lc ) throws Exception {
		lcMap.remove( lc.NAME );
		lcMapById.remove( lc.ID );
	}

	public boolean isUsed( ReleaseLifecycle lc ) {
		EngineBlotterSet blotter = engine.blotter.getBlotterSet( BlotterType.BLOTTER_RELEASE );
		if( blotter.checkLifecycleUsed( lc.NAME ) )
			return( true );
		return( false );
	}
	
}
