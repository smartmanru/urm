package org.urm.engine.properties;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;

public class ObjectMeta {

	PropertyEntity[] entitiesApp;
	PropertyEntity entityCustom;
	
	Map<String,EntityVar> varNames;
	Map<Integer,EntityVar> varIds;
	
	ObjectMeta() {
		varNames = new HashMap<String,EntityVar>();
		varIds = new HashMap<Integer,EntityVar>();
	}

	public void create( PropertyEntity[] entitiesApp , PropertyEntity entityCustom ) throws Exception {
		this.entitiesApp = entitiesApp;
		this.entityCustom = entityCustom;
		
		if( entitiesApp == null )
			Common.exitUnexpected();
		rebuild();
	}

	public ObjectMeta copy() {
		ObjectMeta r = new ObjectMeta();
		r.entitiesApp = new PropertyEntity[ entitiesApp.length ];
		for( int k = 0; k < entitiesApp.length; k++ )
			r.entitiesApp[ k ] = entitiesApp[ k ];
		
		if( entityCustom != null )
			r.entityCustom = entityCustom.copy();
		r.rebuild();
		return( r );
	}
	
	public void rebuild() {
		varNames.clear();
		varIds.clear();
		for( PropertyEntity entity : entitiesApp ) {
			for( EntityVar var : entity.getVars() ) {
				varNames.put( var.NAME , var );
				varIds.put( var.PARAM_ID , var );
			}
		}
		
		if( entityCustom != null ) {
			for( EntityVar var : entityCustom.getVars() ) {
				varNames.put( var.NAME , var );
				varIds.put( var.PARAM_ID , var );
			}
		}
	}
	
	public PropertyEntity[] getAppEntities() {
		return( entitiesApp );
	}

	public PropertyEntity getAppEntity( DBEnumParamEntityType entityType ) throws Exception {
		for( PropertyEntity entity : entitiesApp ) {
			if( entity.PARAMENTITY_TYPE == entityType )
				return( entity );
		}
		
		Common.exitUnexpected();
		return( null );
	}
	
	public boolean hasAppAttrs() {
		for( PropertyEntity entity : entitiesApp ) {
			if( !entity.USE_PROPS )
				return( true );
		}
		return( false );
	}
	
	public boolean hasAppProps() {
		for( PropertyEntity entity : entitiesApp ) {
			if( entity.USE_PROPS )
				return( true );
		}
		return( false );
	}
	
	public PropertyEntity getCustomEntity() {
		return( entityCustom );
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
	
	public EntityVar findVar( int propId ) {
		return( varIds.get( propId ) );
	}
	
	public EntityVar getVar( String name ) throws Exception {
		EntityVar var = varNames.get( name );
		if( var == null )
			Common.exit1( _Error.UnknownVar1 , "Unknown variable name=" + name , name );
		return( var );
	}
	
	public EntityVar getVar( int propId ) throws Exception {
		EntityVar var = varIds.get( propId );
		if( var == null )
			Common.exit1( _Error.UnknownVar1 , "Unknown variable name=" + propId , "" + propId );
		return( var );
	}
	
	public EntityVar findAppVar( int propId ) {
		EntityVar var = varIds.get( propId );
		if( var != null && var.isApp() )
			return( var );
		return( null );
	}
	
	public EntityVar findCustomVar( int propId ) {
		EntityVar var = varIds.get( propId );
		if( var != null && var.isCustom() )
			return( var );
		return( null );
	}
	
	public EntityVar findAppVar( String prop ) {
		EntityVar var = varNames.get( prop );
		if( var != null && var.isApp() )
			return( var );
		return( null );
	}
	
	public EntityVar findCustomVar( String prop ) {
		EntityVar var = varNames.get( prop );
		if( var != null && var.isCustom() )
			return( var );
		return( null );
	}

	public EntityVar findAppXmlVar( String xmlprop ) {
		for( PropertyEntity entity : entitiesApp ) {
			EntityVar var = entity.findXmlVar( xmlprop );
			if( var != null )
				return( var );
		}
		return( null );
	}
	
	public EntityVar findCustomXmlVar( String xmlprop ) {
		if( entityCustom != null ) {
			EntityVar var = entityCustom.findXmlVar( xmlprop );
			if( var != null )
				return( var );
		}
		
		return( null );
	}
	
	public EntityVar findXmlVar( String xmlprop ) {
		EntityVar var = findAppXmlVar( xmlprop );
		if( var != null )
			return( var );
		
		var = findCustomXmlVar( xmlprop );
		return( var );
	}

	public EntityVar[] getAppDatabaseVars() {
		List<EntityVar> list = new LinkedList<EntityVar>();
		for( PropertyEntity entity : entitiesApp ) {
			for( EntityVar var : entity.getDatabaseVars() )
				list.add( var );
		}
		return( list.toArray( new EntityVar[0] ) );
	}
	
	public EntityVar[] getAppVars() {
		List<EntityVar> list = new LinkedList<EntityVar>();
		for( PropertyEntity entity : entitiesApp ) {
			for( EntityVar var : entity.getVars() )
				list.add( var );
		}
		return( list.toArray( new EntityVar[0] ) );
	}
	
}
