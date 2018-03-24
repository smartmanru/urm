package org.urm.engine.dist;

import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.release.Release;

public class ReleaseDistScopeDeliveryItem {

	public Release release;
	public MetaDistrDelivery distDelivery;
	
	public MetaDistrBinaryItem binary;
	public MetaDistrConfItem conf;
	public MetaDatabaseSchema schema;
	public MetaProductDoc doc;
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrBinaryItem binary ) {
		this.release = release;
		this.binary = binary;
	}
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrConfItem conf ) {
		this.release = release;
		this.conf = conf;
	}
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrDelivery distDelivery , MetaDatabaseSchema schema ) {
		this.release = release;
		this.distDelivery = distDelivery;
		this.schema = schema;
	}
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrDelivery distDelivery , MetaProductDoc doc ) {
		this.release = release;
		this.distDelivery = distDelivery;
		this.doc = doc;
	}
	
}
