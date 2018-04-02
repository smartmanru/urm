package org.urm.meta.release;

import org.urm.db.core.DBEnums.*;
import org.urm.meta.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaDocs;
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
	public ReleaseChanges changes;
	public ReleaseScope scope;
	
	public int ID;
	public boolean SCOPETARGET;
	public DBEnumDistTargetType TYPE;
	public MatchItem DELIVERY;
	public MatchItem BINARY;
	public MatchItem CONF;
	public MatchItem SCHEMA;
	public MatchItem DOC;
	public boolean ALL;
	public int RV;
	
	public ReleaseDistTarget( ReleaseChanges changes ) {
		this.release = changes.release;
		this.changes = changes;
	}

	public ReleaseDistTarget( ReleaseScope scope ) {
		this.release = scope.release;
		this.scope = scope;
	}

	public ReleaseDistTarget copy( ReleaseChanges rchanges , ReleaseScope rscope ) {
		ReleaseDistTarget r = ( SCOPETARGET )? new ReleaseDistTarget( rscope ) : new ReleaseDistTarget( rchanges );
		
		r.ID = ID;
		r.SCOPETARGET = SCOPETARGET;
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

	public void create( DBEnumDistTargetType TYPE , MatchItem DELIVERY , MatchItem BINARY , MatchItem CONF , MatchItem SCHEMA , MatchItem DOC , boolean ALL ) {
		this.TYPE = TYPE;
		this.DELIVERY = MatchItem.copy( DELIVERY );
		this.BINARY = MatchItem.copy( BINARY );
		this.CONF = MatchItem.copy( CONF );
		this.SCHEMA = MatchItem.copy( SCHEMA );
		this.DOC = MatchItem.copy( DOC );
		this.ALL = ALL;
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

	public boolean isScopeTarget() {
		if( scope != null )
			return( true );
		return( false );
	}
	
	public boolean isDistAll() {
		return( TYPE == DBEnumDistTargetType.DISTALL );
	}
	
	public boolean isDelivery() {
		if( TYPE.isDelivery() )
			return( true );
		return( false );
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
	
	public boolean isParentOf( ReleaseDistTarget targetCheck ) {
		if( targetCheck.isBinaryItem() ) {
			if( isDistAll() )
				return( true );
			
			MetaDistrBinaryItem binary = targetCheck.getBinaryItem();
					
			if( isDeliveryBinaries() ) {
				if( MatchItem.equals( BINARY , binary.ID ) )
					return( true );
				return( false );
			}
			
			return( false );
		}
		
		if( targetCheck.isConfItem() ) {
			if( isDistAll() )
				return( true );
			
			MetaDistrConfItem conf = targetCheck.getConfItem();
			
			if( isDeliveryConfs() ) {
				if( MatchItem.equals( CONF , conf.ID ) )
					return( true );
				return( false );
			}
			
			return( false );
		}
		
		if( targetCheck.isSchema() ) {
			if( isDistAll() )
				return( true );
			
			MetaDatabaseSchema schema = targetCheck.getSchema();
			
			if( isDeliveryDatabase() ) {
				if( MatchItem.equals( SCHEMA , schema.ID ) )
					return( true );
				return( false );
			}
			
			return( false );
		}
		
		if( targetCheck.isDoc() ) {
			if( isDistAll() )
				return( true );
			
			MetaProductDoc doc = targetCheck.getDoc();
			
			if( isDeliveryDocs() ) {
				if( MatchItem.equals( DOC , doc.ID ) )
					return( true );
				return( false );
			}
			
			return( false );
		}
		
		if( targetCheck.isDelivery() ) {
			if( isDistAll() )
				return( true );
			
			return( false );
		}
		
		return( false );
	}

	public MetaDistrDelivery getDelivery() {
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		return( distr.findDelivery( DELIVERY ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem() {
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		return( distr.findBinaryItem( BINARY ) );
	}
	
	public MetaDistrConfItem getConfItem() {
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		return( distr.findConfItem( CONF ) );
	}
	
	public MetaDatabaseSchema getSchema() {
		Meta meta = release.getMeta();
		MetaDatabase database = meta.getDatabase();
		return( database.findSchema( SCHEMA ) );
	}
	
	public MetaProductDoc getDoc() {
		Meta meta = release.getMeta();
		MetaDocs docs = meta.getDocs();
		return( docs.findDoc( DOC ) );
	}
	
}
