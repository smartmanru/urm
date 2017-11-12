package org.urm.engine.properties;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.RunContext;
import org.urm.common.RunError;
import org.urm.common._Error;
import org.urm.common.action.CommandOption.FLAG;
import org.urm.engine.EngineTransaction;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ObjectProperties {

	private String setName;
	private ObjectProperties parent;
	private RunContext execrc;
	
	private boolean loadFailed;
	private boolean loadFinished;
	private PropertySet properties;
	private Map<String,String> loadErrors;
	private RunError error;

	private List<ObjectProperties> childs;
	
	public ObjectProperties( String name , RunContext execrc ) {
		this.setName = name;
		this.execrc = execrc;
		
		loadFailed = false;
		loadFinished = false;
		childs = new LinkedList<ObjectProperties>();
		loadErrors = new HashMap<String,String>(); 
	}

	public ObjectProperties copy( ObjectProperties parent ) {
		ObjectProperties r = new ObjectProperties( setName , execrc );
		r.properties = r.properties.copy( parent.properties );
		return( r );
	}
	
	public PropertySet getProperties() {
		return( properties );
	}
	
	public ObjectProperties getParent() {
		return( parent );
	}

	public void create( ObjectProperties parent ) throws Exception {
		initCreateStarted( parent );
		initFinished();
	}
	
	public void load( Node root , ObjectProperties parent ) throws Exception {
		initCreateStarted( parent );
		if( root != null )
			loadFromNodeElements( root , false );
		initFinished();
		resolveRawProperties();
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
		return( properties.getSystemPathProperty( prop , execrc , "" , false ) );
	}
	
	public String getPathProperty( String prop , String defaultValue ) throws Exception {
		return( properties.getSystemPathProperty( prop , execrc , defaultValue , false ) );
	}
	
	public String getPathPropertyRequired( String prop ) throws Exception {
		String value = properties.getSystemPathProperty( prop , execrc , "" , true );
		if( value.isEmpty() )
			setLoadFailed( prop , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	public int getIntProperty( String prop , int defaultValue ) throws Exception {
		return( properties.getSystemIntProperty( prop , defaultValue , false ) );
	}
	
	public int getIntPropertyRequired( String prop ) throws Exception {
		int value = properties.getSystemIntProperty( prop , 0 , true );
		String sv = properties.getPropertyAny( prop );
		if( sv == null || sv.isEmpty() )
			setLoadFailed( prop , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	public boolean getBooleanProperty( String prop ) throws Exception {
		return( getBooleanProperty( prop , false ) );
	}
	
	public boolean getBooleanProperty( String prop , boolean defaultValue ) throws Exception {
		return( properties.getSystemBooleanProperty( prop , defaultValue , false ) );
	}
	
	public FLAG getOptionProperty( String prop ) throws Exception {
		return( properties.getSystemOptionProperty( prop , false ) );
	}
	
	public FLAG getOptionProperty( String prop , Boolean defaultValue ) throws Exception {
		return( properties.getSystemOptionProperty( prop , defaultValue , false ) );
	}
	
	public String getStringProperty( String prop ) throws Exception {
		return( properties.getSystemStringProperty( prop ) );
	}
	
	public String getUrlProperty( String prop ) throws Exception {
		return( properties.getSystemStringProperty( prop ) );
	}
	
	public String getStringProperty( String prop , String defaultValue ) throws Exception {
		String value = properties.getSystemStringProperty( prop , defaultValue , false );
		return( value );
	}
	
	public String getStringPropertyRequired( String prop ) throws Exception {
		return( getStringPropertyRequired( prop , "" ) );
	}
	
	public String getStringPropertyRequired( String prop , String defaultValue ) throws Exception {
		String value = properties.getSystemStringProperty( prop , defaultValue , true );
		if( value.isEmpty() )
			setLoadFailed( prop , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}
	
	public String getStringExprProperty( String prop , String defaultExpr ) throws Exception {
		String value = properties.getSystemStringExprProperty( prop , defaultExpr , false );
		if( value.isEmpty() )
			setLoadFailed( prop , "set=" + properties.set + ", property is not set: " + prop );
		return( value );
	}

	public String getVarExpr( String var ) {
		return( "@" + var + "@" );
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
			properties.removeUserProperties();
		properties.updateProperties( props , system );
	}
	
	public void updateProperties( EngineTransaction transaction ) throws Exception {
		properties.recalculateProperties();
	}

	public void setStringProperty( String prop , String value ) throws Exception {
		properties.setOriginalStringProperty( prop , value );
	}

	public void setSystemStringProperty( String prop , String value ) throws Exception {
		properties.setOriginalSystemStringProperty( prop , value );
	}

	public void setManualStringProperty( String prop , String value ) throws Exception {
		properties.setManualStringProperty( prop , value );
	}

	public void setNumberProperty( String prop , int value ) throws Exception {
		properties.setOriginalNumberProperty( prop , value );
	}

	public void setSystemNumberProperty( String prop , int value ) throws Exception {
		properties.setOriginalSystemNumberProperty( prop , value );
	}

	public void setManualNumberProperty( String prop , int value ) throws Exception {
		properties.setManualNumberProperty( prop , value );
	}

	public void setBooleanProperty( String prop , boolean value ) throws Exception {
		properties.setOriginalBooleanProperty( prop , value );
	}

	public void setSystemBooleanProperty( String prop , boolean value ) throws Exception {
		properties.setOriginalSystemBooleanProperty( prop , value );
	}

	public void setManualBooleanProperty( String prop , boolean value ) throws Exception {
		properties.setManualBooleanProperty( prop , value );
	}

	public void setUrlProperty( String prop , String value ) throws Exception {
		properties.setOriginalStringProperty( prop , value );
	}

	public void setSystemUrlProperty( String prop , String value ) throws Exception {
		properties.setOriginalSystemStringProperty( prop , value );
	}

	public void setManualUrlProperty( String prop , String value ) throws Exception {
		properties.setManualStringProperty( prop , value );
	}

	public void setPathProperty( String prop , String value ) throws Exception {
		properties.setOriginalPathProperty( prop , value );
	}

	public void setSystemPathProperty( String prop , String value ) throws Exception {
		properties.setOriginalSystemPathProperty( prop , value , null );
	}

	public void setManualPathProperty( String prop , String value , ShellExecutor shell ) throws Exception {
		properties.setManualPathProperty( prop , value , shell );
	}

	public String[] getPropertyList() {
		return( properties.getRunningProperties() );
	}

	public String getPropertyValue( String var ) throws Exception {
		return( properties.getPropertyAny( var ) );
	}

	public void loadFromNodeAttributes( Node root , boolean custom ) {
		try {
			properties.loadFromNodeAttributes( root , custom );
		}
		catch( Throwable e ) {
			loadFailed = true;
			error = new RunError( e , _Error.UnexpectedState0 , "load from attributes" , null );
		}
	}

	public void loadFromNodeElements( Node root , boolean custom ) {
		try {
			properties.loadFromNodeElements( root , custom );
		}
		catch( Throwable e ) {
			loadFailed = true;
			error = new RunError( e , _Error.UnexpectedState0 , "load from elements" , null );
		}
	}

	public void saveAsElements( Document doc , Element root , boolean custom ) throws Exception {
		properties.saveAsElements( doc , root , custom );
	}

	public void saveSplit( Document doc , Element root ) throws Exception {
		properties.saveSplit( doc , root );
	}

	public String getFinalProperty( String name , Account account , boolean allowParent , boolean allowUnresolved ) throws Exception {
		return( properties.getFinalProperty( name , account , allowParent , allowUnresolved ) );		
	}

	public void recalculateChildProperties() throws Exception {
		for( ObjectProperties child : childs )
			child.parentPropertiesModified();
	}
	
	public void parentPropertiesModified() throws Exception {
		recalculateProperties();
		recalculateChildProperties();
	}

}
