package org.urm.common;

import org.urm.action.ActionBase;
import org.urm.common.action.CommandVar.FLAG;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerObject;

public abstract class PropertyController extends ServerObject {

	private String setName;
	
	private boolean loaded;
	private boolean loadFailed;
	private boolean loadFinished;
	protected PropertySet properties;

	abstract public boolean isValid();
	abstract public void scatterProperties( ActionBase action ) throws Exception;
	
	public PropertyController( ServerObject parent , String name ) {
		super( parent );
		this.setName = name;
		
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
		
		properties = new PropertySet( setName , parent );
		
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
		action.fail0( _Error.PropertyLoadFailed0 , msg );
		action.error( msg );
	}
	
	protected String getPathProperty( ActionBase action , String prop ) throws Exception {
		return( properties.getSystemPathProperty( prop , "" , action.execrc , false ) );
	}
	
	protected String getPathProperty( ActionBase action , String prop , String defaultValue ) throws Exception {
		return( properties.getSystemPathProperty( prop , defaultValue , action.execrc , false ) );
	}
	
	protected String getPathPropertyRequired( ActionBase action , String prop ) throws Exception {
		String value = properties.getSystemPathProperty( prop , "" , action.execrc , true );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	protected int getIntProperty( ActionBase action , String prop , int defaultValue ) throws Exception {
		return( properties.getSystemIntProperty( prop , defaultValue , false ) );
	}
	
	protected int getIntPropertyRequired( ActionBase action , String prop ) throws Exception {
		int value = properties.getSystemIntProperty( prop , 0 , true );
		String sv = properties.getPropertyAny( prop );
		if( sv == null || sv.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	protected boolean getBooleanProperty( ActionBase action , String prop ) throws Exception {
		return( getBooleanProperty( action , prop , false ) );
	}
	
	protected boolean getBooleanProperty( ActionBase action , String prop , boolean defaultValue ) throws Exception {
		return( properties.getSystemBooleanProperty( prop , defaultValue , false ) );
	}
	
	protected FLAG getOptionProperty( ActionBase action , String prop ) throws Exception {
		return( properties.getSystemOptionProperty( prop , false ) );
	}
	
	protected FLAG getOptionProperty( ActionBase action , String prop , Boolean defaultValue ) throws Exception {
		return( properties.getSystemOptionProperty( prop , defaultValue , false ) );
	}
	
	protected String getStringProperty( ActionBase action , String prop ) throws Exception {
		return( properties.getSystemStringProperty( prop ) );
	}
	
	public String getStringProperty( ActionBase action , String prop , String defaultValue ) throws Exception {
		String value = properties.getSystemStringProperty( prop , defaultValue , false );
		return( value );
	}
	
	public String getStringPropertyRequired( ActionBase action , String prop ) throws Exception {
		return( getStringPropertyRequired( action , prop , "" ) );
	}
	
	public String getStringPropertyRequired( ActionBase action , String prop , String defaultValue ) throws Exception {
		String value = properties.getSystemStringProperty( prop , defaultValue , true );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	protected String getStringExprProperty( ActionBase action , String prop , String defaultExpr ) throws Exception {
		String value = properties.getSystemStringExprProperty( prop , defaultExpr , false );
		if( value.isEmpty() )
			setLoadFailed( action , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}

	protected String getVarExpr( String var ) {
		return( "@" + var + "@" );
	}
	
	protected void finishProperties( ActionBase action ) throws Exception {
		properties.resolveRawProperties( true );
		if( properties.isCorrect() ) {
			properties.finishRawProperties();
			loadFailed = false;
		}
		else {
			for( PropertyValue p : properties.getAllProperties() ) {
				if( !p.isCorrect() )
					setLoadFailed( action , "set=" + properties.set + ", property is not correct: " + p.property );
			}
			loadFailed = true;
		}
	}

	protected void updateProperties( ActionBase action ) throws Exception {
		finishProperties( action );
		scatterProperties( action );
	}
	
	protected void updateProperties( ServerTransaction transaction , PropertySet props , boolean system ) throws Exception {
		if( !system )
			properties.removeUserProperties();
		properties.updateProperties( props );
		if( system )
			scatterProperties( transaction.action );
	}
	
	public void updateProperties( ServerTransaction transaction ) throws Exception {
		properties.recalculateProperties();
		scatterProperties( transaction.action );
	}

	public void setStringProperty( String prop , String value ) throws Exception {
		properties.setOriginalStringProperty( prop , value );
	}

	public void setNumberProperty( String prop , int value ) throws Exception {
		properties.setOriginalNumberProperty( prop , value );
	}

	public void setBooleanProperty( String prop , boolean value ) throws Exception {
		properties.setOriginalBooleanProperty( prop , value );
	}

}
