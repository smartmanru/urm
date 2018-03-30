package org.urm.meta.release;

import org.urm.db.core.DBEnums.*;
import org.urm.meta.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;

public class ReleaseDistTarget {

	public static String PROPERTY_SCOPETARGET = "scopetarget";
	public static String PROPERTY_TARGETTYPE = "targettype";
	public static String PROPERTY_DELIVERY = "delivery";
	public static String PROPERTY_BINARY = "binary";
	public static String PROPERTY_CONF = "conf";
	public static String PROPERTY_SCHEMA = "schema";
	public static String PROPERTY_DOC = "doc";
	public static String PROPERTY_ALL = "all";
	
	public Release release;
	public ReleaseScope scope;
	
	public int ID;
	public DBEnumDistTargetType TYPE;
	public MatchItem DELIVERY;
	public MatchItem BINARY;
	public MatchItem CONF;
	public MatchItem SCHEMA;
	public MatchItem DOC;
	public boolean ALL;
	public int RV;
	
	public ReleaseDistTarget( Release release ) {
		this.release = release;
	}

	public ReleaseDistTarget( ReleaseScope scope ) {
		this.release = scope.release;
		this.scope = scope;
	}

	public ReleaseDistTarget copy( ReleaseScope rscope ) {
		ReleaseDistTarget r = new ReleaseDistTarget( rscope );
		
		r.ID = ID;
		r.TYPE = TYPE;
		r.DELIVERY = MatchItem.copy( DELIVERY );
		r.BINARY = MatchItem.copy( BINARY );
		r.CONF = MatchItem.copy( CONF );
		r.SCHEMA = MatchItem.copy( SCHEMA );
		r.DOC = MatchItem.copy( DOC );
		r.ALL = ALL;
		r.RV = RV;
		
		return( r );
	}
	
	public void createAll() {
		TYPE = DBEnumDistTargetType.DISTALL;
		ALL = true;
	}
	
	public void create( MetaDistrBinaryItem item ) {
		TYPE = DBEnumDistTargetType.BINARYITEM;
		BINARY = MatchItem.create( item.ID );
		ALL = false;
	}
	
	public void create( MetaDistrConfItem item ) {
		TYPE = DBEnumDistTargetType.CONFITEM;
		CONF = MatchItem.create( item.ID );
		ALL = false;
	}
	
	public void create( MetaDistrDelivery delivery , DBEnumDistTargetType type ) {
		this.TYPE = type;
		DELIVERY = MatchItem.create( delivery.ID );
		ALL = false;
	}
	
	public void create( MetaDistrDelivery delivery , MetaDatabaseSchema schema ) {
		TYPE = DBEnumDistTargetType.SCHEMA;
		DELIVERY = MatchItem.create( delivery.ID );
		SCHEMA = MatchItem.create( schema.ID );
	}
	
	public void create( MetaDistrDelivery delivery , MetaProductDoc doc ) {
		TYPE = DBEnumDistTargetType.DOC;
		DELIVERY = MatchItem.create( delivery.ID );
		DOC = MatchItem.create( doc.ID );
	}
	
	public MetaDistrConfItem getConf() throws Exception {
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		return( distr.getConfItem( CONF ) );
	}

	public boolean isDistAll() {
		return( TYPE == DBEnumDistTargetType.DISTALL );
	}
	
	public boolean isDeliveryBinaries() {
		return( TYPE == DBEnumDistTargetType.DELIVERYBINARIES );
	}
	
	public boolean isDeliveryConfs() {
		return( TYPE == DBEnumDistTargetType.DELIVERYCONFS );
	}
	
	public boolean isDeliveryDatabase() {
		return( TYPE == DBEnumDistTargetType.DELIVERYDATABASE );
	}
	
	public boolean isDeliveryDocs() {
		return( TYPE == DBEnumDistTargetType.DELIVERYDOC );
	}
	
	public boolean isBinaryItem() {
		return( TYPE == DBEnumDistTargetType.BINARYITEM );
	}
	
	public boolean isConfItem() {
		return( TYPE == DBEnumDistTargetType.CONFITEM );
	}
	
	public boolean isSchema() {
		return( TYPE == DBEnumDistTargetType.SCHEMA );
	}
	
	public boolean isDoc() {
		return( TYPE == DBEnumDistTargetType.DOC );
	}
	
}
