package org.urm.engine.properties;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.db.core.DBVersions;
import org.w3c.dom.Node;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;

public class PropertyEntity {

	public int PARAM_OBJECT_ID;									// object, owning entity meta
	public DBEnumParamEntityType PARAMENTITY_TYPE;				// entity type
	public boolean CUSTOM;										// user-defined properties
	public boolean USE_PROPS;									// store data in properties
	public String APP_TABLE;									// data table
	public DBEnumObjectType OBJECT_TYPE;						// object type
	public int META_OBJECT_ID;									// module object, owning entity meta
																// app module meta: APP (change only with app version upgrade) 
																// custom module meta: specific module object   
	public DBEnumObjectVersionType META_OBJECTVERSION_TYPE;		// type of module object, owning entity meta
	public DBEnumObjectVersionType DATA_OBJECTVERSION_TYPE;		// type of module object, owning entity data
	public String ID_FIELD;										// object identifier table field 
	public int VERSION;
	
	private List<EntityVar> list;
	private List<EntityVar> dblist;
	private Map<String,EntityVar> map;
	private Map<String,EntityVar> xmlmap;
	
	public PropertyEntity( int paramObjectId , DBEnumParamEntityType entityType , boolean custom , boolean saveAsProps , String appTable , DBEnumObjectType objectType , int metaObjectId , DBEnumObjectVersionType metaObjectVersionType , DBEnumObjectVersionType dataObjectVersionType , String idField ) {
		this.PARAM_OBJECT_ID = paramObjectId;
		this.PARAMENTITY_TYPE = entityType;
		this.CUSTOM = custom;
		this.USE_PROPS = saveAsProps;
		this.APP_TABLE = appTable;
		this.OBJECT_TYPE = objectType;
		this.META_OBJECT_ID = metaObjectId;
		this.META_OBJECTVERSION_TYPE = metaObjectVersionType;
		this.DATA_OBJECTVERSION_TYPE = dataObjectVersionType;
		this.ID_FIELD = idField;
		this.VERSION = 0;
		list = new LinkedList<EntityVar>();
		dblist = new LinkedList<EntityVar>();
		map = new HashMap<String,EntityVar>();
		xmlmap = new HashMap<String,EntityVar>();
	}

	public static PropertyEntity getAppObjectEntity( DBEnumObjectType objectType , DBEnumParamEntityType entityType , DBEnumObjectVersionType dataObjectVersionType , String appTable , String idField ) throws Exception {
		PropertyEntity entity = new PropertyEntity( DBVersions.APP_ID , entityType , false , false , appTable , objectType , DBVersions.APP_ID , DBEnumObjectVersionType.APP , dataObjectVersionType , idField );
		return( entity );
	}
	
	public static PropertyEntity getAppPropsEntity( DBEnumObjectType objectType , DBEnumParamEntityType entityType , DBEnumObjectVersionType dataObjectVersionType ) throws Exception {
		PropertyEntity entity = new PropertyEntity( DBVersions.APP_ID , entityType , false , true , null , objectType , DBVersions.APP_ID , DBEnumObjectVersionType.APP , dataObjectVersionType , null );
		return( entity );
	}
	
	public static PropertyEntity getCustomEntity( int paramObjectId , DBEnumObjectType objectType , DBEnumParamEntityType entityType , int metaObjectId , DBEnumObjectVersionType dataObjectVersionType ) throws Exception {
		PropertyEntity entity = new PropertyEntity( paramObjectId , entityType , true , true , null , objectType , metaObjectId , dataObjectVersionType , dataObjectVersionType , null );
		return( entity );
	}
	
	public PropertyEntity copy() {
		PropertyEntity r = new PropertyEntity( 
				this.PARAM_OBJECT_ID ,
				this.PARAMENTITY_TYPE ,
				this.CUSTOM ,
				this.USE_PROPS ,
				this.APP_TABLE ,
				this.OBJECT_TYPE ,
				this.META_OBJECT_ID ,
				this.META_OBJECTVERSION_TYPE ,
				this.DATA_OBJECTVERSION_TYPE ,
				this.ID_FIELD
				);
		r.VERSION = VERSION;
		for( EntityVar var : list ) {
			EntityVar rvar = var.copy();
			r.addVar( rvar );
		}
		return( r );
	}
	
	public EntityVar[] getVars() {
		return( list.toArray( new EntityVar[0] ) );
	}
	
	public EntityVar[] getDatabaseVars() {
		return( dblist.toArray( new EntityVar[0] ) );
	}
	
	public EntityVar[] getXmlVars() {
		List<EntityVar> vars = new LinkedList<EntityVar>();
		for( EntityVar var : list ) {
			if( var.XMLNAME != null )
				vars.add( var );
		}
		return( vars.toArray( new EntityVar[0] ) );
	}
	
	public void addVar( EntityVar var ) {
		list.add( var );
		map.put( var.NAME , var );
		if( var.XMLNAME != null )
			xmlmap.put( var.XMLNAME , var );
		
		int databaseColumn = 0;
		if( var.DBNAME != null ) {
			dblist.add( var );
			databaseColumn = dblist.size() + 1;
		}
		
		int entityColumn = list.size();
		var.setEntity( this , entityColumn , databaseColumn );
	}

	public void clear() {
		list.clear();
		map.clear();
	}

	public EntityVar findVar( String name ) {
		return( map.get( name ) );
	}

	public EntityVar findXmlVar( String xmlname ) {
		return( xmlmap.get( xmlname ) );
	}

	public String getIdField() {
		return( ID_FIELD );
	}
	
	public String getVersionField() {
		if( DATA_OBJECTVERSION_TYPE == DBEnumObjectVersionType.APP )
			return( EngineEntities.FIELD_VERSION_APP );
		if( DATA_OBJECTVERSION_TYPE == DBEnumObjectVersionType.CORE )
			return( EngineEntities.FIELD_VERSION_CORE );
		if( DATA_OBJECTVERSION_TYPE == DBEnumObjectVersionType.SYSTEM )
			return( EngineEntities.FIELD_VERSION_SYSTEM );
		if( DATA_OBJECTVERSION_TYPE == DBEnumObjectVersionType.PRODUCT )
			return( EngineEntities.FIELD_VERSION_PRODUCT );
		if( DATA_OBJECTVERSION_TYPE == DBEnumObjectVersionType.ENVIRONMENT )
			return( EngineEntities.FIELD_VERSION_ENVIRONMENT );
		return( null );
	}

	public String getAttrValue( Node root , String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getAttrValue( root , var.XMLNAME ) );
	}
	
	public boolean getBooleanAttrValue( Node root , String prop , boolean defValue ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getBooleanAttrValue( root , var.XMLNAME , defValue ) );
	}

	public int getDatabaseOnlyVarCount() {
		int n = 0;
		for( EntityVar var : dblist ) {
			if( var.XMLNAME == null )
				n++;
		}
		return( n );
	}

	public int getDatabaseColumn( String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		
		if( var.isXmlOnly() )
			Common.exitUnexpected();
		
		return( var.databaseColumn );
	}

	public String getString( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		return( rs.getString( column ) );
	}
	
	public int getInt( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		return( rs.getInt( column ) );
	}
	
	public int getEnum( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		return( rs.getInt( column ) );
	}
	
	public boolean getBoolean( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		return( rs.getBoolean( column ) );
	}

	public int getId( ResultSet rs ) throws Exception {
		return( rs.getInt( 1 ) );
	}
	
	public int getVersion( ResultSet rs ) throws Exception {
		return( rs.getInt( dblist.size() + 2 ) );
	}
	
}
