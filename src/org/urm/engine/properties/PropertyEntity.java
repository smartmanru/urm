package org.urm.engine.properties;

import java.util.HashMap;
import java.util.Map;

import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;

public class PropertyEntity {

	public DBEnumObjectVersionType ownerType;
	public int ownerId;
	public DBEnumParamEntityType entityType;
	public boolean custom;
	
	private Map<String,EntityVar> vars;
	
	public PropertyEntity( DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom ) {
		this.ownerType = ownerType;
		this.ownerId = ownerId;
		this.entityType = entityType;
		this.custom = custom;
		vars = new HashMap<String,EntityVar>(); 
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
