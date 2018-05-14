package org.urm.meta.system;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.loader.MatchItem;

public class AppProductMonitoring {

	public AppProduct product;
	
	private Map<Integer,AppProductMonitoringTarget> mapTargets;

	public AppProductMonitoring( AppProduct product ) {
		this.product = product;
		
		mapTargets = new HashMap<Integer,AppProductMonitoringTarget>();
	}
	
	public AppProductMonitoring copy( AppProduct rproduct ) {
		AppProductMonitoring r = new AppProductMonitoring( rproduct );
		
		for( AppProductMonitoringTarget target : mapTargets.values() ) {
			AppProductMonitoringTarget rtarget = target.copy( rproduct , r );
			r.addTarget( rtarget );
		}
		
		return( r );
	}

	public AppProductMonitoringTarget[] getTargets() { 
		return( mapTargets.values().toArray( new AppProductMonitoringTarget[0] ) );
	}
	
	public void addTarget( AppProductMonitoringTarget target ) {
		mapTargets.put( target.ID , target );
	}

	public AppProductMonitoringTarget findTarget( MetaEnvSegment sg ) {
		for( AppProductMonitoringTarget target : mapTargets.values() ) {
			if( MatchItem.equals( target.SEGMENT , sg.ID ) )
				return( target );
		}
		return( null );
	}

	public AppProductMonitoringTarget getTarget( int id ) throws Exception {
		AppProductMonitoringTarget target = mapTargets.get( id );
		if( target == null )
			Common.exitUnexpected();
		return( target );
	}
	
}
