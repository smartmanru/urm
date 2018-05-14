package org.urm.meta.system;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.ProductEnvs;

public class MetaMonitoring {

	public ProductEnvs envs;
	
	private Map<Integer,MetaMonitoringTarget> mapTargets;

	public MetaMonitoring( ProductEnvs envs ) {
		this.envs = envs;
		
		mapTargets = new HashMap<Integer,MetaMonitoringTarget>();
	}
	
	public MetaMonitoring copy( ProductEnvs renvs ) throws Exception {
		MetaMonitoring r = new MetaMonitoring( renvs );
		
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			MetaMonitoringTarget rtarget = target.copy( renvs , r );
			r.addTarget( rtarget );
		}
		
		return( r );
	}

	public MetaMonitoringTarget[] getTargets() { 
		return( mapTargets.values().toArray( new MetaMonitoringTarget[0] ) );
	}
	
	public void addTarget( MetaMonitoringTarget target ) {
		mapTargets.put( target.ID , target );
	}

	public MetaMonitoringTarget findTarget( MetaEnvSegment sg ) {
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			if( target.SEGMENT_ID == sg.ID )
				return( target );
		}
		return( null );
	}

	public MetaMonitoringTarget getTarget( int id ) throws Exception {
		MetaMonitoringTarget target = mapTargets.get( id );
		if( target == null )
			Common.exitUnexpected();
		return( target );
	}
	
}
