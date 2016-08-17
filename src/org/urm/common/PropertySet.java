package org.urm.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.urm.common.PropertyValue.PropertyValueOrigin;
import org.urm.common.PropertyValue.PropertyValueType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PropertySet {

	public String set;
	public PropertySet parent;
	
	private Map<String,PropertyValue> running;	// final use - key2object, final, no variables allowed
	private Map<String,PropertyValue> raw;		// construction - key2object, with variables
	private Map<String,String> original;		// source - property2value
	private List<String> system;				// predefined
	private boolean resolved;
	
	public PropertySet( String set , PropertySet parent ) {
		this.set = set;
		this.parent = parent;
		
		running = new HashMap<String,PropertyValue>();
		raw = new HashMap<String,PropertyValue>();
		original = new HashMap<String,String>();
		system = new LinkedList<String>();
		resolved = false;
	}
	
	public PropertySet copy( PropertySet parentNew ) {
		PropertySet r = new PropertySet( set , parentNew );
		r.running.putAll( running );
		r.raw.putAll( raw );
		r.original.putAll( original );
		r.system.addAll( system );
		return( r );
	}
	
	public String getPropertyByKey( String key ) {
		return( key.substring( set.length() + 1 ) );
	}
	
	public String getKeyByProperty( String prop ) {
		return( set + "." + prop );
	}
	
	public String[] getOwnKeys() {
		return( Common.getSortedKeys( running ) );		
	}

	public String[] getOwnProperties() {
		String[] keys = Common.getSortedKeys( running );
		for( int k = 0; k < keys.length; k++ )
			keys[k] = getPropertyByKey( keys[k] );
		return( keys );
	}

	public String[] getOriginalProperties() {
		return( Common.getSortedKeys( original ) );
	}

	public String[] getManualProperties() {
		String[] own = getOwnProperties();
		int count = 0;
		for( String prop : own ) {
			if( !original.containsKey( prop ) )
				count++;
		}
		String[] manual = new String[count];
		count = 0;
		for( String prop : own ) {
			if( !original.containsKey( prop ) )
				manual[ count++ ] = prop;
		}
			
		return( manual );
	}

	public String[] getAllProperties() {
		if( parent == null )
			return( getOwnProperties() );
		
		Map<String,String> props = new HashMap<String,String>();
		for( String prop : getOwnProperties() )
			props.put( prop , prop );
		for( String prop : parent.getAllProperties() )
			props.put( prop , prop );
		return( Common.getSortedKeys( props ) );
	}
	
	public String[] getAllKeys() {
		if( parent == null )
			return( getOwnKeys() );
		
		Map<String,String> props = new HashMap<String,String>();
		for( String prop : getOwnKeys() )
			props.put( prop , prop );
		for( String prop : parent.getAllKeys() )
			props.put( prop , prop );
		return( Common.getSortedKeys( props ) );
	}
	
	private void setRunningProperty( PropertyValue value ) throws Exception {
		if( !value.resolved )
			throw new ExitException( "cannot set unresolved running property set=" + set + ", prop=" + value.property );
		running.put( getKeyByProperty( value.property ) , value );
	}

	private void setRawProperty( PropertyValue value ) {
		raw.put( getKeyByProperty( value.property ) , value );
	}

	private void setOriginalProperty( String prop , String value ) {
		original.put( prop , value );
	}

	private void removeRawProperty( PropertyValue value ) {
		raw.remove( getKeyByProperty( value.property ) );
	}

	private void removeRunningProperty( PropertyValue value ) {
		running.remove( getKeyByProperty( value.property ) );
	}

	public String getOriginalByProperty( String prop ) {
		String value = original.get( prop );
		if( value == null )
			return( "" );
		return( value );
	}
	
	public PropertyValue getRawByProperty( String prop ) {
		return( raw.get( getKeyByProperty( prop ) ) );
	}
	
	public PropertyValue getRawByKey( String key ) {
		return( raw.get( key ) );
	}
	
	public PropertyValue getOwnByKey( String key ) {
		return( running.get( key ) );
	}

	public PropertyValue getOwnByProperty( String prop ) {
		return( running.get( getKeyByProperty( prop ) ) );
	}

	private void createOriginalAndRawProperty( String prop , String value , boolean addToRaw ) throws Exception {
		setOriginalProperty( prop , value );
		if( addToRaw ) {
			PropertyValue pv = new PropertyValue( prop , PropertyValue.PropertyValueOrigin.PROPERTY_ORIGINAL , this );
			pv.setString( getOriginalByProperty( prop ) );
			setRawProperty( pv );
		}
	}

	public void loadOriginalFromNodeAttributes( Node node ) throws Exception {
		loadFromNodeAttributes( node , false );
	}

	public void loadRawFromNodeAttributes( Node node ) throws Exception {
		loadFromNodeAttributes( node , true );
	}
		
	private void loadFromNodeAttributes( Node node , boolean addToRaw ) throws Exception {
		Map<String,String> attrs = new HashMap<String,String>();
		ConfReader.addAttributes( node , attrs );
		for( String prop : attrs.keySet() ) {
			String value = attrs.get( prop );
			createOriginalAndRawProperty( prop , value , addToRaw );
		}
	}
	
	public void loadOriginalFromNodeElements( Node node ) throws Exception {
		loadFromNodeElements( node , false );
	}

	public void loadRawFromNodeElements( Node node ) throws Exception {
		loadFromNodeElements( node , true );
	}
	
	private void loadFromNodeElements( Node node , boolean addToRaw ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "property" );
		if( items == null )
			return;
		
		for( Node property : items ) {
			String prop = ConfReader.getAttrValue( property , "name" );
			String value = ConfReader.getAttrValue( property , "value" );
			createOriginalAndRawProperty( prop , value , addToRaw );
		}
	}

	public void loadOriginalFromPropertyFile( String path , RunContext execrc ) throws Exception {
		loadFromPropertyFile( path , execrc , false );
	}
	
	public void loadRawFromPropertyFile( String path , RunContext execrc ) throws Exception {
		loadFromPropertyFile( path , execrc , true );
	}
	
	private void loadFromPropertyFile( String path , RunContext execrc , boolean addToRaw ) throws Exception {
		Properties props = ConfReader.readPropertyFile( execrc , path );
		for( Object key : props.keySet() ) {
			String prop = ( String )key;
			String value = props.getProperty( ( String )key );
			
			if( value.startsWith( "\"" ) && value.endsWith( "\"" ) )
				value = value.substring( 1 , value.length() - 2 );
			createOriginalAndRawProperty( prop , value , addToRaw );
		}
	}
	
	public void copyOriginalPropertiesToRaw( PropertySet set ) throws Exception {
		for( String prop : set.getOriginalProperties() ) {
			PropertyValue pv = new PropertyValue( prop , PropertyValue.PropertyValueOrigin.PROPERTY_EXTRA , set );
			String value = set.getOriginalByProperty( prop );
			pv.setString( value );
			setRawProperty( pv );
		}
	}

	public void copyRunningPropertiesToRunning( PropertySet set ) throws Exception {
		for( String prop : set.getOwnProperties() ) {
			PropertyValue pv = new PropertyValue( prop , PropertyValue.PropertyValueOrigin.PROPERTY_EXTRA , set );
			PropertyValue value = set.getOwnByProperty( prop );
			pv.setData( value );
			setRunningProperty( pv );
		}
	}

	public void resolveRawProperties() throws Exception {
		// resolve properties
		for( PropertyValue pv : raw.values() )
			removeRunningProperty( pv );
			
		for( PropertyValue pv : raw.values() ) {
			PropertyValue rv = new PropertyValue( pv );
			processValue( rv , false , false , true , true , false );
			setRunningProperty( rv );
		}
		raw.clear();
		resolved = true;
	}

	private PropertyValue resolveSystemProperty( String prop , boolean required ) throws Exception {
		if( resolved ) {
			PropertyValue pv = getOwnByProperty( prop );
			if( required ) {
				if( pv == null || pv.data.isEmpty() )
					throw new ExitException( "set=" + set + ": missing or empty property=" + prop );
			}
			return( pv );
		}
		
		system.add( getKeyByProperty( prop ) );
		PropertyValue pv = getRawByProperty( prop );
		if( required ) {
			if( pv == null || pv.data.isEmpty() )
				throw new ExitException( "set=" + set + ": missing or empty property=" + prop );
		}
		
		if( pv == null )
			pv = setOriginalProperty( prop , PropertyValueType.PROPERTY_STRING , null );
			
		pv.setSystem();
		PropertyValue fp = new PropertyValue( pv );
		processValue( fp , false , false , true , true , false );
		
		removeRawProperty( pv );
		setRunningProperty( fp );
		return( fp );
	}
	
	private String getPathValue( PropertyValue pv , boolean finalValue , boolean isWindows ) throws Exception {
		String s = pv.data;
		if( finalValue ) {
			if( isWindows == false )
				s = Common.getLinuxPath( s );
			else
			if( isWindows == true )
				s = Common.getWinPath( s );
			else
				throw new ExitException( "UnexpectedState" );
		}

		return( s );
	}
	
	private void processValue( PropertyValue pv , boolean finalValue , boolean isWindows , boolean useRaw , boolean allowParent , boolean allowUnresolved ) throws Exception {
		String value = pv.data;
		if( value == null ) {
			pv.data = "";
			return;
		}
		
		int indexFrom = value.indexOf( '@' );
		if( indexFrom < 0 )
			return;
		
		int indexTo = value.indexOf( '@' , indexFrom + 1 );
		if( indexTo < 0 )
			return;

		// variable
		if( indexFrom == 0 && indexTo == value.length() - 1 ) {
			String var = value.substring( indexFrom + 1 , indexTo );
			if( var.isEmpty() ) {
				pv.setString( "@" );
				return;
			}
			
			PropertyValue pvVar = getPropertyInternal( var , useRaw , allowParent , allowUnresolved );
			pv.setData( pvVar );
			if( pv.type == PropertyValueType.PROPERTY_PATH )
				pv.data = getPathValue( pv , finalValue , isWindows );
			return;
		}
		
		// string expression
		String res = value.substring( 0 , indexFrom );
		while( true ) {
			String var = value.substring( indexFrom + 1 , indexTo );
			if( var.isEmpty() )
				res += "@";
			else {
				PropertyValue pvVar = getPropertyInternal( var , useRaw , allowParent , allowUnresolved );
				if( pvVar.type == PropertyValueType.PROPERTY_PATH ) {
					String s = getPathValue( pvVar , finalValue , isWindows );
					res += s;
				}
				else
					res += pvVar.data; 
			}
			
			indexFrom = value.indexOf( '@' , indexTo + 1 );
			if( indexFrom < 0 ) {
				res += value.substring( indexTo + 1 );
				break;
			}
			
			int index = value.indexOf( '@' , indexFrom + 1 );
			if( index < 0 ) {
				res += value.substring( indexTo + 1 );
				break;
			}

			res += value.substring( indexTo + 1 , indexFrom );
			indexTo = index;	
		}
		
		pv.setString( res );
	}
	
	public PropertyValue getPropertyValue( String prop ) {
		PropertyValue pv = getOwnByProperty( prop );
		if( pv == null )
			pv = getRawByProperty( prop );
		return( pv );
	}
	
	private PropertyValue getPropertyInternal( String name , boolean useRaw , boolean allowParent , boolean allowUnresolved ) throws Exception {
		PropertyValue pv = null;
		if( useRaw ) {
			// prefixed raw var
			pv = getRawByKey( name );
			// unprefixed raw var
			if( pv == null )
				pv = getRawByProperty( name );
		}
		
		// prefixed own var
		if( pv == null )
			pv = getOwnByKey( name );
		// unprefixed own var
		if( pv == null )
			pv = getOwnByProperty( name );
		
		if( pv == null || pv.isMissing() ) {
			if( parent != null ) {
				// parent var
				if( !allowParent ) {
					if( parent == null )
						throw new ExitException( "set=" + set + ": unresolved variable=" + name );
				}
				
				return( parent.getPropertyInternal( name , false , allowParent , allowUnresolved ) );
			}
			
			if( !allowUnresolved )
				throw new ExitException( "set=" + set + ": unresolved variable=" + name );
			
			pv = new PropertyValue( "@" + name + "@" );  
		}
		else {
			if( !allowUnresolved ) {
				if( !pv.resolved )
					throw new ExitException( "set=" + set + ": unresolved variable=" + name + ", value=" + pv.data );
			}
		}
		
		return( pv );
	}

	public void setStringProperty( String prop , String value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null );
		pv.setString( value );
		setRunningProperty( pv );
	}

	public void setBooleanProperty( String prop , String value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null );
		pv.setBool( value );
		setRunningProperty( pv );
	}

	public void setNumberProperty( String prop , String value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null );
		pv.setNumber( value );
		setRunningProperty( pv );
	}

	public void setPathProperty( String prop , String value , RunContext execrc ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null );
		pv.setPath( value , execrc );
		setRunningProperty( pv );
	}

	public String getPropertyAny( String prop ) throws Exception {
		PropertyValue pv = getPropertyInternal( prop , true , true , true );
		if( pv == null )
			return( null );
		return( pv.data );
	}
	
	public PropertyValue getFinalProperty( String name , RunContext execrc , boolean allowParent , boolean allowUnresolved ) throws Exception {
		PropertyValue pv = getPropertyInternal( name , false , allowParent , allowUnresolved );
		if( pv == null )
			return( null );
		if( pv.type != PropertyValueType.PROPERTY_PATH )
			return( pv );
		
		pv.setValue( execrc.getLocalPath( pv.data ) );
		return( pv );
	}
	
	public PropertyValue getFinalValue( String value , boolean isWindows , boolean allowParent , boolean allowUnresolved ) throws Exception {
		PropertyValue pv = new PropertyValue( value );
		processValue( pv , true , isWindows , true , allowParent , allowUnresolved );
		return( pv );
	}
	
	public String findPropertyAny( String name ) throws Exception {
		return( findPropertyAny( name , "" ) );
	}

	public String findPropertyAny( String name , String defaultValue ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , false );
		if( pv == null )
			return( defaultValue );
		return( pv.data );
	}
	
	private PropertyValue findPropertyInternal( String name , PropertyValue defaultValue , boolean system ) throws Exception {
		// prefixed var
		PropertyValue pv = getOwnByKey( name );
		if( pv != null )
			return( pv );
		
		// unprefixed var
		pv = getOwnByProperty( name );
		if( pv != null )
			return( pv );
		
		// parent var
		if( parent == null || system )
			return( defaultValue );
			
		return( parent.findPropertyInternal( name , defaultValue , false ) );
	}

	public String getRequiredPropertyAny( String name ) throws Exception {
		PropertyValue pv = getRequiredPropertyInternal( name );
		return( pv.data );
	}
	
	private PropertyValue getRequiredPropertyInternal( String name ) throws Exception {
		PropertyValue pv = getPropertyInternal( name , false , true , false );
		if( pv == null || pv.data.isEmpty() )
			throw new ExitException( "set=" + set + ": empty property=" + name );
		return( pv );
	}

	private String getPathPropertyInternal( String name , String defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null ) {
			if( defaultValue == null )
				return( null );
			return( Common.getLinuxPath( defaultValue ) );
		}
		if( pv.type != PropertyValueType.PROPERTY_PATH )
			throw new ExitException( "property is not string name=" + name );
		return( pv.data );
	}

	public String getStringProperty( String name , String defaultValue ) throws Exception {
		return( getStringPropertyInternal( name , defaultValue , false ) );
	}
	
	public String getPathProperty( String name , String defaultValue ) throws Exception {
		return( getPathPropertyInternal( name , defaultValue , false ) );
	}
	
	private String getStringPropertyInternal( String name , String defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null )
			return( defaultValue );
		if( pv.type != PropertyValueType.PROPERTY_STRING )
			throw new ExitException( "property is not string name=" + name );
		return( pv.data );
	}

	public int getIntProperty( String name , int defaultValue ) throws Exception {
		return( getIntPropertyInternal( name , defaultValue , false ) );
	}
	
	private int getIntPropertyInternal( String name , int defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null )
			return( defaultValue );
		if( pv.type != PropertyValueType.PROPERTY_NUMBER )
			throw new ExitException( "property is not number name=" + name );
		if( pv.data.isEmpty() )
			return( defaultValue );
		return( Integer.parseInt( pv.data ) );
	}

	public boolean getBooleanProperty( String name , boolean defaultValue ) throws Exception {
		return( getBooleanPropertyInternal( name , defaultValue , false ) );
	}
	
	private boolean getBooleanPropertyInternal( String name , boolean defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null )
			return( defaultValue );
		if( pv.type != PropertyValueType.PROPERTY_BOOL )
			throw new ExitException( "property is not boolean name=" + name );
		if( pv.data.isEmpty() )
			return( defaultValue );
		return( Common.getBooleanValue( pv.data ) );
	}

	public String getSystemRequiredStringProperty( String prop ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , true );
		if( pv.type != PropertyValueType.PROPERTY_STRING )
			throw new ExitException( "property is not string name=" + prop );
		
		return( pv.data );
	}

	public String getSystemRequiredPathProperty( String prop , RunContext execrc ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , true );
		if( pv.type != PropertyValueType.PROPERTY_PATH ) {
			if( pv.type != PropertyValueType.PROPERTY_STRING )
				throw new ExitException( "property is not path name=" + prop );
			else
				pv.setType( PropertyValueType.PROPERTY_PATH );
		}
		return( getPathValue( pv , false , execrc.isWindows() ) );
	}
	
	public String getSystemPathProperty( String prop , String defaultValue , RunContext execrc ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false );
		if( pv == null || pv.data.isEmpty() ) {
			pv.setPath( defaultValue , execrc );
			return( defaultValue );
		}
		
		if( pv.type != PropertyValueType.PROPERTY_PATH ) {
			if( pv.type != PropertyValueType.PROPERTY_STRING )
				throw new ExitException( "property is not path name=" + prop );
			else
				pv.setType( PropertyValueType.PROPERTY_PATH );
		}
		return( getPathValue( pv , false , execrc.isWindows() ) );
	}
	
	public String getSystemStringProperty( String prop , String defaultValue ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false );
		if( pv == null || pv.data.isEmpty() ) {
			pv.setString( defaultValue );
			return( defaultValue );
		}
		if( pv.type != PropertyValueType.PROPERTY_STRING )
			throw new ExitException( "property is not string name=" + prop );
		return( pv.data );
	}

	public int getSystemIntProperty( String prop , int defaultValue ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false );
		if( pv == null || pv.data.isEmpty() ) {
			pv.setNumber( defaultValue );
			return( defaultValue );
		}
		if( pv.type != PropertyValueType.PROPERTY_NUMBER ) {
			if( pv.type != PropertyValueType.PROPERTY_STRING )
				throw new ExitException( "property is not integer name=" + prop );
			else
				pv.setType( PropertyValueType.PROPERTY_NUMBER );
		}
		return( Integer.parseInt( pv.data ) );
	}

	public boolean getSystemBooleanProperty( String prop , boolean defaultValue ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false );
		if( pv == null || pv.data.isEmpty() ) {
			pv.setType( PropertyValueType.PROPERTY_BOOL );
			return( defaultValue );
		}
		if( pv.type != PropertyValueType.PROPERTY_BOOL ) {
			if( pv.type != PropertyValueType.PROPERTY_STRING )
				throw new ExitException( "property is not boolean name=" + prop );
			else
				pv.setType( PropertyValueType.PROPERTY_BOOL );
		}
		return( Common.getBooleanValue( pv.data ) );
	}

	public PropertyValue setOriginalProperty( String prop , PropertyValueType type , String value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValue.PropertyValueOrigin.PROPERTY_ORIGINAL , this );
		pv.setType( type );
		pv.setValue( value );
		setOriginalProperty( prop , pv.data );
		setRawProperty( pv );
		removeRunningProperty( pv );
		return( pv );
	}
	
	public void setRunningProperty( String prop , String originalValue , PropertyValue runningValue ) throws Exception {
		setOriginalProperty( prop , originalValue );
		removeRawProperty( runningValue );
		setRunningProperty( runningValue );
	}
	
	public void finishRawProperties() throws Exception {
		resolveRawProperties();
		for( String prop : raw.keySet() )
			throw new ExitException( "set=" + set + ": unexpected property=" + prop );
		resolved = true;
	}

	public void saveAsElements( Document doc , Element parent ) throws Exception {
		for( String key : original.keySet() ) {
			String value = original.get( key );
			Common.xmlCreatePropertyElement( doc , parent , key , value );
		}
	}
	
}
