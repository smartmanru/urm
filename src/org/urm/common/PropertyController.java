package org.urm.common;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.action.CommandOption.FLAG;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class PropertyController extends ServerObject {

	private String setName;
	
	private boolean loadFailed;
	private boolean loadFinished;
	private PropertySet properties;

	private PropertyController propertyParent;
	private List<PropertyController> propertyChilds;
	
	abstract public boolean isValid();
	abstract public void scatterProperties( ActionBase action ) throws Exception;
	
	public PropertyController( ServerObject dataParent , PropertyController propertyParent , String name ) {
		super( dataParent );
		create( propertyParent , name );
	}

	public PropertyController( PropertyController parent , String name ) {
		super( parent );
		create( parent , name );
	}

	@Override
	public void deleteObject() {
		deleteObjectDown();
		if( propertyParent != null )
			propertyParent.propertyChilds.remove( this );
		super.deleteObject();
	}

	@Override
	public void deleteObjectDown() {
		for( PropertyController child : propertyChilds )
			child.deleteObjectDown();
	}
	
	private void create( PropertyController parent , String name ) {
		this.setName = name;
		
		loadFailed = false;
		loadFinished = false;
		propertyChilds = new LinkedList<PropertyController>();
		
		this.propertyParent = parent;
		if( propertyParent != null )
			propertyParent.propertyChilds.add( this );
	}
	
	public PropertySet getProperties() {
		return( properties );
	}
	
	protected boolean initCopyStarted( PropertyController src , PropertySet parent ) {
		loadFailed = false;
		loadFinished = false;
		
		if( src.properties != null )
			properties = src.properties.copy( parent );
		
		return( true );
	}
	
	protected boolean initCreateStarted( PropertySet parent ) {
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

	protected void setLoadFailed( ActionBase action , String msg ) {
		loadFailed = true;
		action.fail0( _Error.PropertyLoadFailed0 , msg );
		action.error( msg );
	}
	
	protected String getPathProperty( ActionBase action , String prop ) throws Exception {
		return( properties.getSystemPathProperty( prop , action.execrc , "" , false ) );
	}
	
	protected String getPathProperty( ActionBase action , String prop , String defaultValue ) throws Exception {
		return( properties.getSystemPathProperty( prop , action.execrc , defaultValue , false ) );
	}
	
	protected String getPathPropertyRequired( ActionBase action , String prop ) throws Exception {
		String value = properties.getSystemPathProperty( prop , action.execrc , "" , true );
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
	
	protected String getUrlProperty( ActionBase action , String prop ) throws Exception {
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

	protected void finishRawProperties() throws Exception {
		properties.finishRawProperties();
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	protected void resolveRawProperties() throws Exception {
		properties.resolveRawProperties();
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	protected void updateProperties( PropertySet props ) throws Exception {
		properties.updateProperties( props , true );
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	protected void recalculateProperties() throws Exception {
		properties.recalculateProperties();
		if( properties.isCorrect() )
			loadFailed = false;
	}
	
	protected void copyOriginalPropertiesToRaw( PropertySet src ) throws Exception {
		properties.copyOriginalPropertiesToRaw( src );
	}
	
	protected void updateProperties( ActionBase action ) throws Exception {
		recalculateProperties();
		scatterProperties( action );
		finishProperties( action );
	}
	
	protected void updateProperties( ServerTransaction transaction , PropertySet props , boolean system ) throws Exception {
		if( !system )
			properties.removeUserProperties();
		properties.updateProperties( props , system );
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

	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getPropertyAny( var ) );
	}

	protected void loadFromNodeAttributes( ActionBase action , Node root , boolean custom ) {
		try {
			properties.loadFromNodeAttributes( root , custom );
		}
		catch( Throwable e ) {
			action.log( "loadFromNodeAttributes" , e );
			loadFailed = true;
		}
	}

	protected void loadFromNodeElements( ActionBase action , Node root , boolean custom ) {
		try {
			properties.loadFromNodeElements( root , custom );
		}
		catch( Throwable e ) {
			action.log( "loadFromNodeElements" , e );
			loadFailed = true;
		}
	}

	protected void saveAsElements( Document doc , Element root , boolean custom ) throws Exception {
		properties.saveAsElements( doc , root , custom );
	}

	public void saveSplit( Document doc , Element root ) throws Exception {
		properties.saveSplit( doc , root );
	}

	public String getFinalProperty( String name , Account account , boolean allowParent , boolean allowUnresolved ) throws Exception {
		return( properties.getFinalProperty( name , account , allowParent , allowUnresolved ) );		
	}

	public void recalculateChildProperties( ActionBase action ) throws Exception {
		for( PropertyController child : propertyChilds )
			child.parentPropertiesModified( action );
	}
	
	public void parentPropertiesModified( ActionBase action ) throws Exception {
		recalculateProperties();
		scatterProperties( action );
		recalculateChildProperties( action );
	}

}
