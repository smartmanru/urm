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
	public boolean partial;
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrBinaryItem binary ) {
		this.release = release;
		this.distDelivery = binary.delivery;
		this.binary = binary;
		this.partial = false;
	}
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrConfItem conf ) {
		this.release = release;
		this.distDelivery = conf.delivery;
		this.conf = conf;
		this.partial = false;
	}
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrDelivery distDelivery , MetaDatabaseSchema schema ) {
		this.release = release;
		this.distDelivery = distDelivery;
		this.schema = schema;
		this.partial = false;
	}
	
	public ReleaseDistScopeDeliveryItem( Release release , MetaDistrDelivery distDelivery , MetaProductDoc doc ) {
		this.release = release;
		this.distDelivery = distDelivery;
		this.doc = doc;
		this.partial = false;
	}

	public boolean isBinary() {
		if( binary != null )
			return( true );
		return( false );
	}
	
	public boolean isConf() {
		if( conf != null )
			return( true );
		return( false );
	}

	public boolean isSchema() {
		if( schema != null )
			return( true );
		return( false );
	}

	public boolean isDoc() {
		if( doc != null )
			return( true );
		return( false );
	}

}
