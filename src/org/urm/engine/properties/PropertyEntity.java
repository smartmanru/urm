package org.urm.engine.properties;

import java.util.HashMap;
import java.util.Map;

import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;

public class PropertyEntity {

	public int OWNER_OBJECT_ID;
	public DBEnumParamEntityType PARAMENTITY_TYPE;
	public boolean CUSTOM;
	public boolean APP_PROPS;
	public String APP_TABLE;
	public DBEnumObjectVersionType OWNER_OBJECT_TYPE;
	public int VERSION;
	
	private Map<String,EntityVar> vars;
	
	public PropertyEntity( DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom , boolean saveAppAsProps , String appTable ) {
		this.OWNER_OBJECT_TYPE = ownerType;
		this.OWNER_OBJECT_ID = ownerId;
		this.PARAMENTITY_TYPE = entityType;
		this.CUSTOM = custom;
		this.APP_PROPS = saveAppAsProps;
		this.APP_TABLE = appTable;
		vars = new HashMap<String,EntityVar>();
		VERSION = 0;
	}
	
	public PropertyEntity copy() {
		PropertyEntity r = new PropertyEntity( 
				this.OWNER_OBJECT_TYPE , 
				this.OWNER_OBJECT_ID ,
				this.PARAMENTITY_TYPE ,
				this.CUSTOM ,
				this.APP_PROPS ,
				this.APP_TABLE );
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
