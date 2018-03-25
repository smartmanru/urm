package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.release.Release;

public class ReleaseDistScopeDelivery {

	public Release release;
	public MetaDistrDelivery distDelivery;
	public DBEnumScopeCategoryType CATEGORY;
	
	public boolean all;
	private Map<String,ReleaseDistScopeDeliveryItem> mapItems;
	
	public ReleaseDistScopeDelivery( Release release , MetaDistrDelivery distDelivery , DBEnumScopeCategoryType CATEGORY ) {
		this.release = release;
		this.distDelivery = distDelivery;
		this.CATEGORY = CATEGORY;
		
		mapItems = new HashMap<String,ReleaseDistScopeDeliveryItem>();
		all = false;
	}

	public void addBinaryItem( ReleaseDistScopeDeliveryItem item ) {
		mapItems.put( item.binary.NAME , item );
	}
	
	public void addConfItem( ReleaseDistScopeDeliveryItem item ) {
		mapItems.put( item.conf.NAME , item );
	}
	
	public void addSchemaItem( ReleaseDistScopeDeliveryItem item ) {
		mapItems.put( item.schema.NAME , item );
	}
	
	public void addDocItem( ReleaseDistScopeDeliveryItem item ) {
		mapItems.put( item.doc.NAME , item );
	}

	public ReleaseDistScopeDeliveryItem findBinaryItem( MetaDistrBinaryItem binary ) {
		return( mapItems.get( binary.NAME ) );
	}
	
	public ReleaseDistScopeDeliveryItem findConfItem( MetaDistrConfItem conf ) {
		return( mapItems.get( conf.NAME ) );
	}
	
	public ReleaseDistScopeDeliveryItem findSchema( MetaDatabaseSchema schema ) {
		return( mapItems.get( schema.NAME ) );
	}
	
	public ReleaseDistScopeDeliveryItem findDoc( MetaProductDoc doc ) {
		return( mapItems.get( doc.NAME ) );
	}

	public boolean isEmpty() {
		if( mapItems.isEmpty() )
			return( true );
		return( false );
	}

	public ReleaseDistScopeDeliveryItem[] getItems() {
		return( mapItems.values().toArray( new ReleaseDistScopeDeliveryItem[0] ) );
	}

	public void setAll( boolean all ) {
		this.all = all;
	}

	public ReleaseDistScopeDeliveryItem findItem( String itemName ) {
		return( mapItems.get( itemName ) );
	}
	
}
