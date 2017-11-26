package org.urm.engine.properties;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.RunError;
import org.urm.db.core.DBEnums.DBEnumParamRoleType;
import org.urm.engine.EngineTransaction;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;

public class ObjectProperties {

	public DBEnumParamRoleType type;
	private String setName;
	private RunContext execrc;
	
	private ObjectProperties parent;
	private boolean loadFailed;
	private boolean loadFinished;
	private Map<String,String> loadErrors;
	private RunError error;
	private ObjectMeta meta;
	private PropertySet properties;

	private List<ObjectProperties> childs;
	
	public ObjectProperties( DBEnumParamRoleType type , String name , RunContext execrc ) {
		this.type = type;
		this.setName = name;
		this.execrc = execrc;
		
		loadFailed = false;
		loadFinished = false;
		childs = new LinkedList<ObjectProperties>();
		loadErrors = new HashMap<String,String>();
		meta = new ObjectMeta();
	}

	public ObjectProperties copy( ObjectProperties parent ) {
		ObjectProperties r = new ObjectProperties( type , setName , execrc );
		r.parent = parent;
		r.loadFailed = loadFailed;
		r.loadFinished = loadFinished;
		r.loadErrors.putAll( loadErrors );
		r.error = error;
		r.meta = meta.copy();
		r.properties = properties.copy( parent.properties );
		return( r );
	}

	public void create( ObjectProperties parent , PropertyEntity entityFixed , PropertyEntity entityCustom ) throws Exception {
		initCreateStarted( parent );
		
		meta.create( entityFixed , entityCustom );
		for( EntityVar var : meta.getVars() ) {
			PropertyValue value = properties.createRawProperty( var.NAME , var.isCustom() , var.PARAMVALUE_TYPE , var.DESC );
			if( var.EXPR_DEF != null )
				value.setDefault( var.EXPR_DEF );
		}
		
		initFinished();
	}

	public void createCustom() throws Exception {
		PropertyEntity entityCustom = meta.getCustomEntity();
		if( entityCustom == null )
			return;
		
		for( EntityVar var : entityCustom.getVars() ) {
			PropertyValue value = properties.createRawProperty( var.NAME , var.isCustom() , var.PARAMVALUE_TYPE , var.DESC );
			if( var.EXPR_DEF != null )
				value.setDefault( var.EXPR_DEF );
		}
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

	boolean initCopyStarted( ObjectProperties src , PropertySet parent ) {
		loadFailed = false;
		loadFinished = false;
		
		if( src.properties != null )
			properties = src.properties.copy( parent );
		
		return( true );
	}
	
	public boolean initCreateStarted( ObjectProperties parent ) {
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

	public void setLoadFailed( String property , String msg ) {
		loadFailed = true;
		loadErrors.put( property , msg );
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
	
	public void finishProperties() throws Exception {
		properties.resolveRawProperties( true );
		if( properties.isCorrect() ) {
			properties.finishRawProperties();
			loadFailed = false;
		}
		else {
			for( PropertyValue p : properties.getAllProperties() ) {
				if( !p.isCorrect() )
					setLoadFailed( p.property , "set=" + properties.set + ", property is not correct: " + p.property );
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
	
	public void updateProperties() throws Exception {
		recalculateProperties();
		finishProperties();
	}
	
	public void updateProperties( EngineTransaction transaction , PropertySet props , boolean system ) throws Exception {
		if( !system )
			properties.removeCustomProperties();
		properties.updateProperties( props , system );
	}
	
	public void updateProperties( EngineTransaction transaction ) throws Exception {
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

	public void setPathProperty( String prop , String value ) throws Exception {
		setProperty( prop , value );
	}

	public void setIntProperty( String prop , int value ) throws Exception {
		setProperty( prop , "" + value );
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
		return( properties.getPropertyAny( var.NAME ) );
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

	public String getOriginalPropertyValue( String prop ) throws Exception {
		EntityVar var = meta.getVar( prop );
		PropertyValue value = properties.getPropertyValue( var.NAME );
		return( value.getOriginalValue() );
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

}
