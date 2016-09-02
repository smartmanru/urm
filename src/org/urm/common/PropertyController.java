package org.urm.common;

import org.urm.server.action.ActionBase;

public class PropertyController {

	private boolean loaded;
	private boolean loadFailed;
	private boolean loadFinished;

	public PropertyController() {
		loaded = false;
		loadFailed = false;
		loadFinished = false;
	}
	
	protected boolean loadStarted() {
		if( loaded )
			return( false );
		
		loaded = true;
		loadFailed = false;
		loadFinished = false;
		return( true );
	}
	
	public boolean isLoadFailed() {
		if( !loadFinished )
			return( true );
		return( loadFailed );
	}
	
	protected void loadFinished() {
		loadFinished = true;
	}

	protected boolean isLoaded() {
		return( loaded );
	}
	
	protected void setLoadFailed( ActionBase action , String msg ) {
		loadFailed = true;
		action.error( msg );
	}
	
	protected String getPathProperty( ActionBase action , PropertySet set , String name ) throws Exception {
		return( set.getSystemPathProperty( name , "" , action.session.execrc ) );
	}
	
	protected String getPathPropertyRequired( ActionBase action , PropertySet set , String name ) throws Exception {
		String value = set.getSystemPathProperty( name , "" , action.session.execrc );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + set.set + ", property is not set: " + name );
		return( value );
	}
	
	protected int getIntProperty( ActionBase action , PropertySet set , String name , int defaultValue ) throws Exception {
		return( set.getSystemIntProperty( name , defaultValue ) );
	}
	
	protected String getStringProperty( ActionBase action , PropertySet set , String name ) throws Exception {
		return( set.getSystemStringProperty( name , "" ) );
	}
	
	public String getStringProperty( ActionBase action , PropertySet set , String name , String defaultValue ) throws Exception {
		String value = set.getSystemStringProperty( name , defaultValue );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + set.set + ", property is not set: " + name );
		return( value );
	}
	
	protected String getStringExprProperty( ActionBase action , PropertySet set , String name , String defaultExpr ) throws Exception {
		String value = set.getSystemStringExprProperty( name , defaultExpr );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + set.set + ", property is not set: " + name );
		return( value );
	}

	protected String getVarExpr( String var ) {
		return( "@" + var + "@" );
	}
	
	protected void finishProperties( ActionBase action , PropertySet set ) throws Exception {
		set.resolveRawProperties( true );
		if( set.isResolved() ) {
			set.finishRawProperties();
			loadFailed = false;
		}
		else {
			for( String key : set.getRawKeys() )
				setLoadFailed( action , "set=" + set.set + ", property is not resolved: " + key );
		}
	}
	
}
