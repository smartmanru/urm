package org.urm.common;

import org.urm.action.ActionBase;

public abstract class PropertyController {

	public String name;
	
	private boolean loaded;
	private boolean loadFailed;
	private boolean loadFinished;
	protected PropertySet properties;

	abstract public boolean isValid();
	
	public PropertyController( String name ) {
		this.name = name;
		
		loaded = false;
		loadFailed = false;
		loadFinished = false;
	}

	public PropertySet getProperties() {
		return( properties );
	}
	
	protected boolean initCopyStarted( PropertyController src , PropertySet parent ) {
		if( loaded )
			return( false );
		
		loaded = true;
		loadFailed = false;
		loadFinished = false;
		
		if( src.properties != null )
			properties = src.properties.copy( parent );
		
		return( true );
	}
	
	protected boolean initCreateStarted( PropertySet parent ) {
		if( loaded )
			return( false );
		
		loaded = true;
		loadFailed = false;
		loadFinished = false;
		
		properties = new PropertySet( name , parent );
		
		return( true );
	}
	
	public boolean isLoadFailed() {
		return( loadFailed );
	}
	
	public boolean isLoadFinished() {
		return( loadFinished );
	}
	
	protected void initFinished() {
		loadFinished = true;
	}

	protected boolean isLoaded() {
		return( loaded );
	}
	
	protected void setLoadFailed( ActionBase action , String msg ) {
		loadFailed = true;
		action.error( msg );
	}
	
	protected String getPathProperty( ActionBase action , String prop ) throws Exception {
		return( properties.getSystemPathProperty( prop , "" , action.session.execrc ) );
	}
	
	protected String getPathPropertyRequired( ActionBase action , String prop ) throws Exception {
		String value = properties.getSystemPathProperty( prop , "" , action.session.execrc );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	protected int getIntProperty( ActionBase action , String prop , int defaultValue ) throws Exception {
		return( properties.getSystemIntProperty( prop , defaultValue ) );
	}
	
	protected int getIntPropertyRequired( ActionBase action , String prop ) throws Exception {
		int value = properties.getSystemIntProperty( prop , 0 );
		String sv = properties.getPropertyAny( prop );
		if( sv == null || sv.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	protected String getStringProperty( ActionBase action , String prop ) throws Exception {
		return( properties.getSystemStringProperty( prop , "" ) );
	}
	
	public String getStringProperty( ActionBase action , String prop , String defaultValue ) throws Exception {
		String value = properties.getSystemStringProperty( prop , defaultValue );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	protected String getStringExprProperty( ActionBase action , String prop , String defaultExpr ) throws Exception {
		String value = properties.getSystemStringExprProperty( prop , defaultExpr );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}

	protected String getVarExpr( String var ) {
		return( "@" + var + "@" );
	}
	
	protected void finishProperties( ActionBase action ) throws Exception {
		properties.resolveRawProperties( true );
		if( properties.isResolved() ) {
			properties.finishRawProperties();
			loadFailed = false;
		}
		else {
			for( String key : properties.getRawKeys() )
				setLoadFailed( action , "set=" + properties.set + ", property is not resolved: " + key );
		}
	}
	
}
