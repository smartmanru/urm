package org.urm.engine.properties;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.RunError;
import org.urm.db.core.DBEnumInterface;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamRoleType;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;

public class ObjectProperties {

	public DBEnumObjectType objectType;				// owner object type
	public DBEnumObjectVersionType versionType;		// type of module object, owning entity data
	public DBEnumParamRoleType roleType;
	
	private String setName;
	private RunContext execrc;
	
	private ObjectProperties parent;
	private boolean customDefineAllowed;
	private boolean loadFailed;
	private boolean loadFinished;
	private RunError error;
	private ObjectMeta meta;
	private PropertySet properties;

	private List<ObjectProperties> childs;
	
	public ObjectProperties( DBEnumObjectType objectType , DBEnumObjectVersionType versionType , DBEnumParamRoleType roleType , String name , RunContext execrc ) {
		this.objectType = objectType;
		this.versionType = versionType;
		this.roleType = roleType;
		this.setName = name;
		this.execrc = execrc;
		
		loadFailed = false;
		loadFinished = false;
		customDefineAllowed = false;
		
		childs = new LinkedList<ObjectProperties>();
		meta = new ObjectMeta();
	}

	public ObjectProperties copy( ObjectProperties parent ) {
		ObjectProperties r = new ObjectProperties( objectType , versionType , roleType , setName , execrc );
		r.parent = parent;
		r.loadFailed = loadFailed;
		r.loadFinished = loadFinished;
		r.customDefineAllowed = customDefineAllowed;
		r.error = error;
		r.meta = meta.copy();
		
		PropertySet parentSet = ( parent == null )? null : parent.properties;
		r.properties = properties.copy( parentSet );
		return( r );
	}

	public void create( ObjectProperties parent , PropertyEntity entityFixed , PropertyEntity entityCustom , boolean customDefineAllowed ) throws Exception {
		create( parent , new PropertyEntity[] { entityFixed } , entityCustom , customDefineAllowed );
	}
	
	public void create( ObjectProperties parent , PropertyEntity[] entitiesFixed , PropertyEntity entityCustom , boolean customDefineAllowed ) throws Exception {
		this.customDefineAllowed = customDefineAllowed;
		initCreateStarted( parent );
		
		meta.create( entitiesFixed , entityCustom );
		for( EntityVar var : meta.getVars() )
			createProperty( var );
		
		initFinished();
	}

	public void createCustom() throws Exception {
		PropertyEntity entityCustom = meta.getCustomEntity();
		if( entityCustom == null )
			return;
		
		for( EntityVar var : entityCustom.getVars() )
			createProperty( var );
	}

	public PropertyValue createProperty( EntityVar var ) throws Exception {
		PropertyValue value = properties.createRawProperty( var.NAME , var.isCustom() , var.PARAMVALUE_TYPE , var.DESC );
		if( !var.isDefaultEmpty() )
			value.setDefault( var.EXPR_DEF );
		return( value );
	}
	
	public ObjectMeta getMeta() {
		return( meta );
	}
	
	public PropertySet getProperties() {
		return( properties );
	}
	
	public ObjectProperties getParent() {
		return( parent );
	}

	public String getName() {
		return( setName );
	}
	
	private boolean initCreateStarted( ObjectProperties parent ) {
		this.parent = parent; 
		loadFailed = false;
		loadFinished = false;
		
		PropertySet set = ( parent != null )? parent.properties : null;
		properties = new PropertySet( setName , set );
		
		return( true );
	}

	public RunError getError() {
		return( error );
	}
	
	public boolean isLoadFailed() {
		return( loadFailed );
	}
	
	public boolean isLoadFinished() {
		return( loadFinished );
	}
	
	public void initFinished() {
		loadFinished = true;
	}

	public boolean isCustomDefineAllowed() {
		return( customDefineAllowed );
	}
	
	public String getPathProperty( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		if( var.isApp() )
			return( properties.getSystemPathExprProperty( var.NAME , execrc , var.EXPR_DEF , var.REQUIRED ) );
		return( properties.getPathProperty( var.NAME , var.EXPR_DEF ) );
	}
	
	public int getIntProperty( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		if( var.isApp() )
			return( properties.getSystemIntExprProperty( var.NAME , var.EXPR_DEF , var.REQUIRED ) );
		return( properties.getIntProperty( var.NAME , Integer.parseInt( var.EXPR_DEF ) ) );
	}
	
	public boolean getBooleanProperty( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		if( var.isApp() )
			return( properties.getSystemBooleanExprProperty( var.NAME , var.EXPR_DEF , var.REQUIRED ) );
		return( properties.getBooleanProperty( var.NAME , Common.getBooleanValue( var.EXPR_DEF ) ) );
	}
	
	public String getStringProperty( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		if( var.isApp() )
			return( properties.getSystemStringExprProperty( var.NAME , var.EXPR_DEF , var.REQUIRED ) );
		return( properties.getStringProperty( var.NAME , var.EXPR_DEF ) );
	}
	
	public String getUrlProperty( String prop ) throws Exception {
		return( getStringProperty( prop ) );
	}
	
	public int getEnumProperty( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		if( var.isApp() )
			return( properties.getSystemIntExprProperty( var.NAME , var.EXPR_DEF , var.REQUIRED ) );
		return( properties.getIntProperty( var.NAME , Integer.parseInt( var.EXPR_DEF ) ) );
	}
	
	public Integer getObjectProperty( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		int value = 0;
		if( var.isApp() )
			value = properties.getSystemIntExprProperty( var.NAME , var.EXPR_DEF , var.REQUIRED );
		else
			value = properties.getIntProperty( var.NAME , Integer.parseInt( var.EXPR_DEF ) );
		if( value == 0 )
			return( null );
		return( value );
	}
	
	public void finishProperties( Map<String,String> loadErrors ) throws Exception {
		properties.resolveRawProperties( true );
		if( properties.isCorrect() ) {
			properties.finishRawProperties();
			loadFailed = false;
		}
		else {
			for( PropertyValue p : properties.getAllProperties() ) {
				if( !p.isCorrect() ) {
					loadFailed = true;
					loadErrors.put( p.property , "set=" + properties.set + ", property is not correct: " + p.property );
				}
			}
			loadFailed = true;
		}
	}

	public void finishRawProperties() throws Exception {
		properties.finishRawProperties();
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	public void resolveRawProperties() throws Exception {
		properties.resolveRawProperties( true );
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	public void updateProperties( PropertySet props ) throws Exception {
		properties.updateProperties( props , true );
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	public void recalculateProperties() throws Exception {
		properties.recalculateProperties();
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	public void copyOriginalPropertiesToRaw( PropertySet src ) throws Exception {
		properties.copyOriginalPropertiesToRaw( src );
	}
	
	public void updateProperties( PropertySet props , boolean system ) throws Exception {
		if( !system )
			properties.removeCustomProperties();
		properties.updateProperties( props , system );
	}
	
	public void updateProperties() throws Exception {
		properties.recalculateProperties();
	}

	public void setProperty( String prop , String value ) throws Exception {
		EntityVar var = meta.getVar( prop );
		properties.setOriginalProperty( var.NAME , var.PARAMVALUE_TYPE , value , var.isApp() , null );
	}

	public void setProperty( int propId , String value ) throws Exception {
		EntityVar var = getVar( propId );
		properties.setOriginalProperty( var.NAME , var.PARAMVALUE_TYPE , value , var.isApp() , null );
	}
	
	public void setProperty( EntityVar var , String value ) throws Exception {
		properties.setOriginalProperty( var.NAME , var.PARAMVALUE_TYPE , value , var.isApp() , null );
	}
	
	public void setStringProperty( String prop , String value ) throws Exception {
		setProperty( prop , value );
	}

	public void setObjectProperty( String prop , Integer value ) throws Exception {
		int pv = ( value == null )? 0 : value;
		setProperty( prop , "" + pv );
	}

	public void setPathProperty( String prop , String value ) throws Exception {
		setProperty( prop , value );
	}

	public void setIntProperty( String prop , int value ) throws Exception {
		setProperty( prop , "" + value );
	}

	public void setEnumProperty( String prop , Enum<?> value ) throws Exception {
		DBEnumInterface e = ( DBEnumInterface )value;
		setIntProperty( prop , e.code() );
	}
	
	public void setBooleanProperty( String prop , boolean value ) throws Exception {
		setProperty( prop , Common.getBooleanValue( value ) );
	}

	public void setUrlProperty( String prop , String value ) throws Exception {
		setProperty( prop , value );
	}

	public void setManualStringProperty( String prop , String value ) throws Exception {
		EntityVar var = meta.getVar( prop );
		properties.setManualStringProperty( var.NAME , value );
	}

	public void setManualIntProperty( String prop , int value ) throws Exception {
		EntityVar var = meta.getVar( prop );
		properties.setManualNumberProperty( var.NAME , value );
	}

	public void setManualBooleanProperty( String prop , boolean value ) throws Exception {
		EntityVar var = meta.getVar( prop );
		properties.setManualBooleanProperty( var.NAME , value );
	}

	public void setManualUrlProperty( String prop , String value ) throws Exception {
		EntityVar var = meta.getVar( prop );
		properties.setManualStringProperty( var.NAME , value );
	}

	public void setManualPathProperty( String prop , String value , ShellExecutor shell ) throws Exception {
		EntityVar var = meta.getVar( prop );
		properties.setManualPathProperty( var.NAME , value , shell );
	}

	public String[] getPropertyList() {
		return( properties.getRunningProperties() );
	}

	public String getPropertyValue( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		String value = properties.getPropertyAny( var.NAME );
		return( getPropertyValue( var , value ) );
	}

	public String getPropertyValue( EntityVar var ) throws Exception {
		String value = properties.getPropertyAny( var.NAME );
		return( getPropertyValue( var , value ) );
	}
	
	public static String getPropertyValue( EntityVar var , String value ) {
		if( var.isPath() ) {
			if( var.isLinuxPath() )
				value = Common.getLinuxPath( value );
			else
			if( var.isWindowsPath() )
				value = Common.getWinPath( value );
		}
		return( value );
	}
	
	public String getFinalProperty( String prop , Account account , boolean allowParent , boolean allowUnresolved ) throws Exception {
		EntityVar var = meta.getVar( prop );
		return( properties.getFinalProperty( var.NAME , account , allowParent , allowUnresolved ) );		
	}

	public void recalculateChildProperties() throws Exception {
		for( ObjectProperties child : childs )
			child.parentPropertiesModified();
	}
	
	public void parentPropertiesModified() throws Exception {
		recalculateProperties();
		recalculateChildProperties();
	}

	public String getExpressionValue( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		PropertyValue value = properties.getPropertyValue( var.NAME );
		return( value.getExpressionValue() );
	}

	public String getOriginalPropertyValue( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		PropertyValue value = properties.getPropertyValue( var.NAME );
		return( value.getOriginalValue() );
	}

	public String getOriginalPropertyValue( EntityVar var ) {
		PropertyValue value = properties.getPropertyValue( var.NAME );
		return( value.getOriginalValue() );
	}

	public void clearProperties( DBEnumParamEntityType entityType ) throws Exception {
		PropertyEntity entity = meta.getEntity( entityType );
		for( EntityVar var : entity.getVars() )
			properties.clearProperty( var.NAME );
	}
	
	public void clearProperty( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		properties.clearProperty( var.NAME );
	}

	public EntityVar getVar( int propId ) throws Exception {
		EntityVar var = meta.findAppVar( propId );
		if( var != null )
			return( var );
		
		ObjectProperties hfind = this;
		while( hfind != null ) {
			var = meta.findCustomVar( propId );
			if( var != null ) {
				if( var.isApp() )
					Common.exit1( _Error.UnexpectedCustomVar1 , "Unexpected builtin variable name=" + var.NAME  , "" + var.NAME );
				return( var );
			}
			
			hfind = hfind.parent;
		}
		
		Common.exit1( _Error.UnknownVarId1 , "Unable to find variable id=" + propId , "" + propId );
		return( null );
	}

	public EntityVar getVar( String prop ) throws Exception {
		EntityVar var = meta.findAppVar( prop );
		if( var != null )
			return( var );
		
		ObjectProperties hfind = this;
		while( hfind != null ) {
			var = meta.findCustomVar( prop );
			if( var != null ) {
				if( var.isApp() )
					Common.exit1( _Error.UnexpectedCustomVar1 , "Unexpected builtin variable name=" + var.NAME  , "" + var.NAME );
				return( var );
			}
			
			hfind = hfind.parent;
		}
		
		Common.exit1( _Error.UnknownVarName1 , "Unable to find variable id=" + prop , "" + prop );
		return( null );
	}

	public EntityVar findVar( String prop ) {
		EntityVar var = meta.findAppVar( prop );
		if( var != null )
			return( var );
		
		ObjectProperties hfind = this;
		while( hfind != null ) {
			var = meta.findCustomVar( prop );
			if( var != null ) {
				if( var.isApp() )
					return( null );
				return( var );
			}
			
			hfind = hfind.parent;
		}
		
		return( null );
	}

	public int getDepth() {
		int depth = 0;
		ObjectProperties ps = parent;
		while( ps != null ) {
			depth++;
			ps = ps.parent;
		}
		return( depth );
	}

	public void removeProperty( String prop ) {
		properties.removeProperty( prop );
	}

	public PropertyValue getFinalValue( String value , boolean isWindows , boolean allowParent , boolean allowUnresolved ) throws Exception {
		return( properties.getFinalPropertyValue( value , isWindows , allowParent , allowUnresolved ) );
	}

	public PropertyValue getProperty( String prop ) {
		return( properties.getPropertyValue( prop ) );
	}
	
	public PropertyValue getProperty( String prop , boolean allowParent , boolean allowUnresolved ) throws Exception {
		return( properties.getFinalProperty( prop , allowParent , allowUnresolved ) );
	}
	
}
