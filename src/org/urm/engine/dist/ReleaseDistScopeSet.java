package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.meta.product.MetaDistrDelivery;
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
	
	public ReleaseDistScopeDelivery findDelivery( MetaDistrDelivery delivery ) {
		return( mapDelivery.get( delivery.NAME ) );
	}

	public ReleaseDistScopeDelivery findDelivery( String deliveryName ) {
		return( mapDelivery.get( deliveryName ) );
	}

	public ReleaseDistScopeDeliveryItem findDeliveryItem( String itemName ) {
		for( ReleaseDistScopeDelivery delivery : mapDelivery.values() ) {
			ReleaseDistScopeDeliveryItem item = delivery.findItem( itemName );
			if( item != null )
				return( item );
		}
		return( null );
	}
	
	public boolean isEmpty() {
		for( ReleaseDistScopeDelivery delivery : mapDelivery.values() ) {
			if( !delivery.isEmpty() )
				return( false );
		}
		return( true );
	}

	public String[] getDeliveryNames() {
		return( Common.getSortedKeys( mapDelivery ) );
	}

	public String[] getDeliveryItemNames() {
		Map<String,String> map = new HashMap<String,String>();
		for( ReleaseDistScopeDelivery delivery : mapDelivery.values() ) {
			for( String name : delivery.getItemNames() )
				map.put( name , name );
		}
		return( Common.getSortedKeys( map ) );
	}
	
}
