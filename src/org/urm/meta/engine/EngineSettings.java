package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;

public class EngineSettings extends EngineObject {

	public Engine engine;
	public RunContext execrc;
	public EngineContext context;
	public int version;

	public ObjectProperties execrcProperties;
	public ObjectProperties engineProperties;
	public ObjectProperties defaultProductProperties;
	public ObjectProperties defaultProductBuildProperties;
	private Map<DBEnumBuildModeType,ObjectProperties> mapBuildModeDefaults;
	
	public EngineSettings( Engine engine ) {
		super( null );
		this.engine = engine;
		this.execrc = engine.execrc;
		
		mapBuildModeDefaults = new HashMap<DBEnumBuildModeType,ObjectProperties>();
	}

	@Override
	public String getName() {
		return( "server-settings" );
	}

	public void addBuildModeDefaults( DBEnumBuildModeType mode , ObjectProperties properties ) {
		mapBuildModeDefaults.put( mode , properties );
	}
	
	public void setData( ActionBase action , EngineSettings src , int version ) throws Exception {
		this.version = version;
		
		execrcProperties = src.execrcProperties;
		engineProperties = src.engineProperties;
		defaultProductProperties = src.defaultProductProperties;
		defaultProductBuildProperties = src.defaultProductBuildProperties;
		mapBuildModeDefaults = src.mapBuildModeDefaults;
		
		context = new EngineContext( execrc , engineProperties );
		context.scatterProperties();
	}
	
	public int getVersion() {
		return( version );
	}
	
	public EngineContext getServerContext() {
		return( context );
	}

	public ObjectProperties getExecProperties() {
		return( execrcProperties );
	}
	
	public ObjectProperties getEngineProperties() {
		return( engineProperties );
	}
	
	public PropertySet getDefaultProductProperties() {
		return( defaultProductProperties.getProperties() );
	}

	public ObjectProperties getDefaultProductSettigns() {
		return( defaultProductProperties );
	}

	public PropertySet getDefaultProductBuildProperties() {
		return( defaultProductBuildProperties.getProperties() );
	}

	public ObjectProperties getDefaultProductBuildObjectProperties( DBEnumBuildModeType mode ) {
		return( mapBuildModeDefaults.get( mode ) );
	}
	
	public ObjectProperties[] getDefaultBuildModeObjectProperties() {
		return( mapBuildModeDefaults.values().toArray( new ObjectProperties[0] ) );
	}
	
	public PropertySet getDefaultProductBuildProperties( DBEnumBuildModeType mode ) {
		ObjectProperties properties = mapBuildModeDefaults.get( mode );
		return( properties.getProperties() );
	}
	
	public PropertySet[] getBuildModeDefaults() {
		return( mapBuildModeDefaults.values().toArray( new PropertySet[0] ) );
	}

	public EngineSettings copy() throws Exception {
		EngineSettings r = new EngineSettings( engine );
		r.version = version;
		r.execrcProperties = execrcProperties;
		r.engineProperties = engineProperties.copy( execrcProperties );
		r.context = context.copy( r.engineProperties );
		
		r.defaultProductProperties = defaultProductProperties.copy( r.engineProperties );
		r.defaultProductBuildProperties = defaultProductBuildProperties.copy( r.defaultProductProperties );
		
		for( DBEnumBuildModeType mode : mapBuildModeDefaults.keySet() ) {
			ObjectProperties set = mapBuildModeDefaults.get( mode );
			ObjectProperties rs = set.copy( r.defaultProductBuildProperties );
			r.mapBuildModeDefaults.put( mode , rs );
		}
		
		return( r );
	}
	
	public void setEngineProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		engineProperties.updateProperties( transaction , props , false );
		engineProperties.resolveRawProperties();
	}

	public void setProductBuildCommonDefaultsProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		defaultProductBuildProperties.updateProperties( transaction , props , true );
		defaultProductBuildProperties.resolveRawProperties();
	}
	
	public void setProductBuildModeDefaultsProperties( EngineTransaction transaction , DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		ObjectProperties set = mapBuildModeDefaults.get( mode );
		if( set == null ) {
			EngineEntities entities = transaction.getEntities();
			set = entities.createDefaultBuildModeProps( defaultProductBuildProperties , mode );
			mapBuildModeDefaults.put( mode , set );
		}
		
		set.updateProperties( transaction , props , true );
		set.resolveRawProperties();
	}

	public void setExecProperties( EngineLoader loader ) throws Exception {
		EngineEntities entities = loader.getEntities();
		execrcProperties = entities.createRunContextProps();
		
		RunContext rc = execrc;
		execrcProperties.setStringProperty( RunContext.PROPERTY_HOSTNAME , rc.hostName );
		execrcProperties.setPathProperty( RunContext.PROPERTY_USER_HOME , rc.userHome );
		execrcProperties.setStringProperty( RunContext.PROPERTY_OS_TYPE , Common.getEnumLower( rc.osType ) );
		execrcProperties.setPathProperty( RunContext.PROPERTY_INSTALL_PATH , rc.installPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_WORK_PATH , rc.workPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_AUTH_PATH , rc.authPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_DB_PATH , rc.dbPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_SERVER_CONFPATH , Common.getPath( rc.installPath , "etc" ) );
		execrcProperties.setPathProperty( RunContext.PROPERTY_SERVER_PRODUCTSPATH , Common.getPath( rc.installPath , "products" ) );

		// get custom properties from environment variables if any
		ObjectMeta meta = execrcProperties.getMeta();
		for( PropertyEntity entity : meta.getEntities() ) {
			if( !entity.CUSTOM ) 
				continue;
			
			for( EntityVar var : entity.getVars() ) {
				String value = RunContext.getProperty( var.NAME );
				if( !value.isEmpty() )
					execrcProperties.setStringProperty( var.NAME , value );
			}
		}
		
		execrcProperties.resolveRawProperties();
	}
}
