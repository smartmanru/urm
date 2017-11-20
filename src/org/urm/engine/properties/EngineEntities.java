package org.urm.engine.properties;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBEnums.*;
import org.urm.engine.Engine;
import org.urm.meta.EngineCore;

public class EngineEntities {

	public static String nameRunContextSet = "execrc";
	
	Engine engine;
	EngineCore core;

	public EngineEntities( EngineCore core ) {
		this.core = core;
		this.engine = core.engine;
	}
	
	public void upgradeData( DBConnection connection ) throws Exception {
	}
	
	public void useData( DBConnection connection ) throws Exception {
	}

	public ObjectProperties createRunContextProps() throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRIMARY , "execrc" , engine.execrc );
		props.create( null , new PropertyEntity[] { 
				getFixedEntity( DBEnumParamEntityType.RC ) , 
				getCustomEntity( DBEnumParamEntityType.RC ) 
				} );
		return( props );
	}

	public ObjectProperties createEngineProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRIMARY , "engine" , engine.execrc );
		props.create( parent , new PropertyEntity[] { 
				getFixedEntity( DBEnumParamEntityType.ENGINE ) , 
				getCustomEntity( DBEnumParamEntityType.ENGINE ) 
				} );
		return( props );
	}

	public ObjectProperties createDefaultProductProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRIMARY , "product.primary" , engine.execrc );
		props.create( parent , new PropertyEntity[] { 
				getFixedEntity( DBEnumParamEntityType.PRODUCT ) , 
				getCustomEntity( DBEnumParamEntityType.PRODUCT ) 
				} );
		return( props );
	}

	public ObjectProperties createDefaultBuildCommonProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRIMARY , "build.common" , engine.execrc );
		props.create( parent , new PropertyEntity[] { 
				getFixedEntity( DBEnumParamEntityType.BUILD ) , 
				getCustomEntity( DBEnumParamEntityType.BUILD ) 
				} );
		return( props );
	}

	public ObjectProperties createDefaultBuildModeProps( ObjectProperties parent , DBEnumBuildModeType mode ) throws Exception {
		DBEnumParamRoleType role = null;
		if( mode == DBEnumBuildModeType.BRANCH )
			role = DBEnumParamRoleType.BUILDMODE_BRANCH;
		else
		if( mode == DBEnumBuildModeType.DEVBRANCH )
			role = DBEnumParamRoleType.BUILDMODE_DEVBRANCH;
		else
		if( mode == DBEnumBuildModeType.DEVTRUNK )
			role = DBEnumParamRoleType.BUILDMODE_DEVTRUNK;
		else
		if( mode == DBEnumBuildModeType.MAJORBRANCH )
			role = DBEnumParamRoleType.BUILDMODE_MAJORBRANCH;
		else
		if( mode == DBEnumBuildModeType.TRUNK )
			role = DBEnumParamRoleType.BUILDMODE_TRUNK;
		ObjectProperties props = new ObjectProperties( role , "build." + Common.getEnumLower( mode ) , engine.execrc );
		props.create( null , new PropertyEntity[] { 
				getFixedEntity( DBEnumParamEntityType.BUILD ) , 
				getCustomEntity( DBEnumParamEntityType.BUILD ) } );
		return( props );
	}

	public ObjectProperties createSystemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRIMARY , "system" , engine.execrc );
		props.create( parent , new PropertyEntity[] { 
				getFixedEntity( DBEnumParamEntityType.SYSTEM ) ,
				createEmptyCustomEntity( DBEnumParamEntityType.SYSTEM )
				} );
		return( props );
	}

	public PropertyEntity getFixedEntity( DBEnumParamEntityType type ) throws Exception {
		PropertyEntity entity = new PropertyEntity( type , false );
		return( entity );
	}
	
	public PropertyEntity getCustomEntity( DBEnumParamEntityType type ) throws Exception {
		PropertyEntity entity = new PropertyEntity( type , true );
		return( entity );
	}
	
	public PropertyEntity createEmptyCustomEntity( DBEnumParamEntityType type ) throws Exception {
		PropertyEntity entity = new PropertyEntity( type , true );
		return( entity );
	}
	
}
