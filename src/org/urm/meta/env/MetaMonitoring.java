package org.urm.meta.env;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class MetaMonitoring {

	public Meta meta;
	
	private Map<Integer,MetaMonitoringTarget> mapTargets;

	public MetaMonitoring( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		
		mapTargets = new HashMap<Integer,MetaMonitoringTarget>();
	}
	
	public MetaMonitoring copy( Meta rmeta ) throws Exception {
		MetaMonitoring r = new MetaMonitoring( rmeta.getStorage() , rmeta );
		
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			MetaMonitoringTarget rtarget = target.copy( meta , r );
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

	public MetaMonitoringTarget findMonitoringTarget( MetaEnvSegment sg ) {
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			if( target.SEGMENT_ID == sg.ID )
				return( target );
		}
		return( null );
	}

}
