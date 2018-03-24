package org.urm.engine.dist;

import java.util.Map;

import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.meta.release.Release;

public class ReleaseDistScopeSet {

	public Release release;
	public DBEnumScopeCategoryType CATEGORY;
	
	private Map<String,ReleaseDistScopeDelivery> mapDelivery;
	
	public ReleaseDistScopeSet( Release release , DBEnumScopeCategoryType CATEGORY ) {
		this.release = release;
		this.CATEGORY = CATEGORY;
	}
	
	public void addDelivery( ReleaseDistScopeDelivery scopeDelivery ) {
		mapDelivery.put( scopeDelivery.distDelivery.NAME , scopeDelivery );
	}

	public ReleaseDistScopeDelivery[] getDeliveries() {
		return( mapDelivery.values().toArray( new ReleaseDistScopeDelivery[0] ) );
	}
	
	public ReleaseDistScopeDelivery findDelivery( String deliveryName ) {
		return( mapDelivery.get( deliveryName ) );
	}
	
}
