package org.urm.engine.properties;

import java.util.LinkedList;
import java.util.List;

import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;

public class PropertyEntity {

	public DBEnumObjectVersionType ownerType;
	public int ownerId;
	public DBEnumParamEntityType entityType;
	public boolean custom;
	
	private List<EntityVar> vars;
	
	public PropertyEntity( DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom ) {
		this.ownerType = ownerType;
		this.ownerId = ownerId;
		this.entityType = entityType;
		this.custom = custom;
		vars = new LinkedList<EntityVar>(); 
	}
	
	public EntityVar[] getVars() {
		return( vars.toArray( new EntityVar[0] ) );
	}
	
	public void addVar( EntityVar var ) {
		vars.add( var );
	}
	
}
