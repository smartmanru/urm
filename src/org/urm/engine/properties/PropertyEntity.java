package org.urm.engine.properties;

import org.urm.db.DBEnums.DBEnumParamEntityType;

public class PropertyEntity {

	public DBEnumParamEntityType entity;
	public boolean custom;
	
	public PropertyEntity( DBEnumParamEntityType entity , boolean custom ) {
		this.entity = entity;
		this.custom = custom;
	}
	
}
