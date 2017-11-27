package org.urm.engine.properties;

import java.util.HashMap;
import java.util.Map;

import org.urm.db.core.DBVersions;
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
	
	private Map<String,EntityVar> vars;
	
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
		vars = new HashMap<String,EntityVar>();
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
		for( EntityVar var : vars.values() ) {
			EntityVar rvar = var.copy();
			r.addVar( rvar );
		}
		return( r );
	}
	
	public EntityVar[] getVars() {
		return( vars.values().toArray( new EntityVar[0] ) );
	}
	
	public void addVar( EntityVar var ) {
		vars.put( var.NAME , var );
		var.setEntity( this );
	}

	public void clear() {
		vars.clear();
	}

	public EntityVar findVar( String name ) {
		return( vars.get( name ) );
	}

}
