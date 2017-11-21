package org.urm.engine.properties;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;

public class ObjectMeta {

	PropertyEntity entityApp;
	PropertyEntity entityCustom;
	
	PropertyEntity[] entities;
	Map<String,EntityVar> varNames;
	
	ObjectMeta() {
	}

	public void create( PropertyEntity entityApp , PropertyEntity entityCustom ) throws Exception {
		this.entityApp = entityApp;
		this.entityCustom = entityCustom;
		
		if( entityCustom != null )
			entities = new PropertyEntity[] { entityApp , entityCustom };
		else
			entities = new PropertyEntity[] { entityApp };
		varNames = new HashMap<String,EntityVar>();
		rebuild();
	}
	
	public void rebuild() {
		varNames.clear();
		for( PropertyEntity entity : entities ) {
			for( EntityVar var : entity.getVars() ) {
				varNames.put( var.NAME , var );
			}
		}
	}
	
	public PropertyEntity getAppEntity() {
		return( entityApp );
	}
	
	public PropertyEntity getCustomEntity() {
		return( entityCustom );
	}
	
	public PropertyEntity[] getEntities() {
		return( entities );
	}
	
	public EntityVar[] getVars() {
		return( varNames.values().toArray( new EntityVar[0] ) );
	}

	public String[] getVarNames() {
		return( Common.getSortedKeys( varNames ) );
	}

	public EntityVar findVar( String name ) {
		return( varNames.get( name ) );
	}
	
	public EntityVar getVar( String name ) throws Exception {
		EntityVar var = varNames.get( name );
		if( var == null )
			Common.exit1( _Error.UnknownVar1 , "Unknown variable name=" + name , name );
		return( var );
	}
	
}
