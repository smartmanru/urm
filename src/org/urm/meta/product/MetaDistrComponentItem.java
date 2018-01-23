package org.urm.meta.product;

import org.urm.db.core.DBEnums.*;

public class MetaDistrComponentItem {

	public static String PROPERTY_NAME = "name";	
	public static String PROPERTY_WSDL = "url";
	public static String PROPERTY_DEPLOYNAME = "deployname";
	
	public Meta meta;
	public MetaDistrComponent comp;

	public int ID;
	public DBEnumCompItemType COMPITEM_TYPE;
	public String DEPLOY_NAME;
	public String WSDL_REQUEST;
	public String PV;
	
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	public MetaDatabaseSchema schema;

	public MetaDistrComponentItem( Meta meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public MetaDistrComponentItem copy( Meta rmeta , MetaDistrComponent rcomp ) throws Exception {
		MetaDistrComponentItem r = new MetaDistrComponentItem( rmeta , rcomp );
		
		r.ID = ID;
		r.COMPITEM_TYPE = COMPITEM_TYPE;
		r.WSDL_REQUEST = WSDL_REQUEST;
		r.DEPLOY_NAME = DEPLOY_NAME;
		r.PV = PV;
		
		if( binaryItem != null ) {
			MetaDistr rdistr = rmeta.getDistr();
			r.binaryItem = rdistr.findBinaryItem( binaryItem.NAME );
		}
		else
		if( confItem != null ) {
			MetaDistr rdistr = rmeta.getDistr();
			r.confItem = rdistr.getConfItem( confItem.NAME );
		}
		else
		if( schema != null ) {
			MetaDatabase database = rmeta.getDatabase();
			r.schema = database.getSchema( schema.NAME );
		}
		return( r );
	}

	public void createBinaryItem( MetaDistrBinaryItem binaryItem , String DEPLOYNAME ) throws Exception {
		this.binaryItem = binaryItem;
		this.COMPITEM_TYPE = DBEnumCompItemType.BINARY;
		this.DEPLOY_NAME = DEPLOYNAME;
		this.WSDL_REQUEST = "";
	}
	
	public void createConfItem( MetaDistrConfItem confItem ) throws Exception {
		this.confItem = confItem;
		this.COMPITEM_TYPE = DBEnumCompItemType.CONF;
		this.DEPLOY_NAME = "";
		this.WSDL_REQUEST = "";
	}
	
	public void createSchemaItem( MetaDatabaseSchema schema , String DEPLOYNAME ) throws Exception {
		this.schema = schema;
		this.COMPITEM_TYPE = DBEnumCompItemType.SCHEMA;
		this.DEPLOY_NAME = DEPLOYNAME;
		this.WSDL_REQUEST = "";
	}
	
	public void createWsdlItem( String wsdl ) throws Exception {
		this.COMPITEM_TYPE = DBEnumCompItemType.WSDL;
		this.WSDL_REQUEST = wsdl;
	}
	
	public String getURL( String ACCESSPOINT ) {
		return( ACCESSPOINT + "/" + WSDL_REQUEST + "?wsdl" );
	}
	
}
