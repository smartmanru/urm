package org.urm.engine.properties;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.db.core.DBEnums;
import org.urm.db.core.DBVersions;
import org.w3c.dom.Node;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.meta.EngineLoader;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.EngineResources;

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
	public DBEnumObjectVersionType DATA_OBJECTVERSION_TYPE;		// type of module object, owning entity data (can be different for different objects)
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
	
	public static PropertyEntity getAppAttrsEntity( DBEnumObjectType objectType , DBEnumParamEntityType entityType , DBEnumObjectVersionType dataObjectVersionType ) throws Exception {
		PropertyEntity entity = new PropertyEntity( DBVersions.APP_ID , entityType , false , false , null , objectType , DBVersions.APP_ID , DBEnumObjectVersionType.APP , dataObjectVersionType , null );
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
	
	public String[] getVarNames() {
		return( Common.getSortedKeys( map ) );
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
		dblist.clear();
		map.clear();
		xmlmap.clear();
	}

	public void removeVar( EntityVar var ) throws Exception {
		int index = list.indexOf( var );
		if( index < 0 )
			Common.exitUnexpected();
		
		list.remove( var );
		map.remove( var.NAME );
		if( var.XMLNAME != null )
			xmlmap.remove( var.XMLNAME );
		
		if( var.DBNAME != null )
			dblist.remove( var );
		
		// decrement column indexes
		for( int k = index; k < list.size(); k++ ) {
			var = list.get( k );
			int newEntityColumn = var.ENTITYCOLUMN - 1;
			int newDatabaseColumn = ( var.XMLNAME == null )? var.databaseColumn : var.databaseColumn - 1;
			var.setEntity( this , newEntityColumn , newDatabaseColumn );
		}
	}
	
	public void updateVar( EntityVar var ) throws Exception {
		Common.changeMapKey( map , var , var.NAME );
		if( var.XMLNAME != null )
			Common.changeMapKey( xmlmap , var , var.XMLNAME );
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
	
	public String importxmlStringAttr( Node root , String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getAttrValue( root , var.XMLNAME ) );
	}
	
	public String importxmlStringProperty( Node root , String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getPropertyValue( root , var.XMLNAME ) );
	}
	
	public boolean importxmlBooleanAttr( Node root , String prop , boolean defValue ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getBooleanAttrValue( root , var.XMLNAME , defValue ) );
	}

	public boolean importxmlBooleanProperty( Node root , String prop , boolean defValue ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getBooleanPropertyValue( root , var.XMLNAME , defValue ) );
	}

	public int importxmlIntAttr( Node root , String prop ) throws Exception {
		return( importxmlIntAttr( root , prop , 0 ) );
	}
	
	public int importxmlIntAttr( Node root , String prop , int defaultValue ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getIntegerAttrValue( root , var.XMLNAME , defaultValue ) );
	}
	
	public int importxmlIntProperty( Node root , String prop ) throws Exception {
		return( importxmlIntProperty( root , prop , 0 ) );
	}
	
	public int importxmlIntProperty( Node root , String prop , int defaultValue ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		return( ConfReader.getIntegerPropertyValue( root , var.XMLNAME , defaultValue ) );
	}
	
	public int importxmlEnumAttr( Node root , String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		String xmlvalue = ConfReader.getAttrValue( root , var.XMLNAME );
		return( DBEnums.getEnumCode( var.enumClass , xmlvalue ) );
	}
	
	public int importxmlEnumProperty( Node root , String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		String xmlvalue = ConfReader.getPropertyValue( root , var.XMLNAME );
		return( DBEnums.getEnumCode( var.enumClass , xmlvalue ) );
	}
	
	public Integer importxmlObjectAttr( EngineLoader loader , Node root , String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		
		String value = ConfReader.getAttrValue( root , var.XMLNAME );
		return( var.importxmlObjectValue( loader , value ) );
	}
	
	public Integer importxmlObjectProperty( EngineLoader loader , Node root , String prop ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		
		String value = ConfReader.getPropertyValue( root , var.XMLNAME );
		if( value == null || value.isEmpty() )
			return( null );
		
		if( var.OBJECT_TYPE == DBEnumObjectType.RESOURCE ) {
			EngineResources resources = loader.getResources();
			AuthResource resource = resources.getResource( value );
			return( resource.ID );
		}
		
		Common.exitUnexpected();
		return( null );
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

	public String loaddbString( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		String value = rs.getString( column );
		if( value == null )
			return( "" );
		return( value );
	}
	
	public int loaddbInt( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		return( rs.getInt( column ) );
	}
	
	public int loaddbEnum( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		return( rs.getInt( column ) );
	}
	
	public boolean loaddbBoolean( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		return( rs.getBoolean( column ) );
	}

	public Integer loaddbObject( ResultSet rs , String prop ) throws Exception {
		int column = getDatabaseColumn( prop );
		int value = rs.getInt( column );
		if( value == 0 )
			return( null );
		return( value );
	}

	public int loaddbId( ResultSet rs ) throws Exception {
		return( rs.getInt( 1 ) );
	}
	
	public int loaddbVersion( ResultSet rs ) throws Exception {
		return( rs.getInt( dblist.size() + 2 ) );
	}
	
	public MatchItem loaddbMatchItem( ResultSet rs , String propId , String propName ) throws Exception {
		Integer id = loaddbObject( rs , propId );
		if( id != null )
			return( new MatchItem( id ) );
		String name = loaddbString( rs , propName );
		if( name.isEmpty() )
			return( null );
		return( new MatchItem( name ) );
	}
	
	public String exportxmlString( String value ) {
		return( value );
	}
	
	public String exportxmlEnum( Enum<?> value ) {
		return( value.name().toLowerCase() );
	}
	
	public String exportxmlBoolean( boolean value ) {
		return( Common.getBooleanValue( value ) );
	}
	
	public String exportxmlInt( int value ) {
		return( "" + value );
	}
	
	public String exportxmlObject( EngineLoader loader , String prop , Integer value ) throws Exception {
		EntityVar var = findVar( prop );
		if( var == null )
			Common.exitUnexpected();
		
		if( !var.isObject() )
			Common.exitUnexpected();

		return( var.exportxmlObjectValue( loader , value ) );
	}
	
	
}
