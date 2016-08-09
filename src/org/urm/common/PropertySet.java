package org.urm.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PropertySet {

	private enum PropertyValueType {
		PROPERTY_STRING ,
		PROPERTY_NUMBER ,
		PROPERTY_BOOL ,
		PROPERTY_PATH
	};
	
	private class PropertyValue {
		PropertyValueType type;
		String data;
		
		public PropertyValue() {
		}
		
		public void setString( String value ) throws Exception {
			type = PropertyValueType.PROPERTY_STRING;
			if( value == null || value.isEmpty() )
				data = "";
			else
				data = value;
		}
		
		public void setNumber( String value ) throws Exception {
			if( value == null || value.isEmpty() ) {
				type = PropertyValueType.PROPERTY_NUMBER;
				data = "";
				return;
			}
			
			try {
				Integer.parseInt( value );
			}
			catch( Throwable e ) {
				throw new ExitException( "invalid number value=" + value );
			}
			
			type = PropertyValueType.PROPERTY_NUMBER;
			data = value;
		}
		
		public void setBool( String value ) throws Exception {
			if( value == null || value.isEmpty() ) {
				type = PropertyValueType.PROPERTY_BOOL;
				data = "";
				return;
			}
			
			try {
				Common.getBooleanValue( value );
			}
			catch( Throwable e ) {
				throw new ExitException( "invalid number value=" + value );
			}
			
			type = PropertyValueType.PROPERTY_BOOL;
			data = value;
		}
		
		public void setPath( String value , RunContext execrc ) throws Exception {
			type = PropertyValueType.PROPERTY_PATH;
			if( value == null || value.isEmpty() )
				data = "";
			else
				data = Common.getLinuxPath( value );
			
			if( data.startsWith( "~/") )
				data = execrc.userHome + data.substring( 1 );
		}
	}
	
	public String set;
	PropertySet parent;
	
	private Map<String,PropertyValue> properties;
	private Map<String,String> raw;
	private Map<String,String> original;
	List<String> systemProps = new LinkedList<String>();

	public PropertySet( String set , PropertySet parent ) {
		properties = new HashMap<String,PropertyValue>();
		raw = new HashMap<String,String>();
		original = new HashMap<String,String>();
		
		this.set = set;
		this.parent = parent;
	}
	
	public PropertySet copy( PropertySet parent ) {
		PropertySet r = new PropertySet( set , parent );
		r.properties.putAll( properties );
		r.raw.putAll( raw );
		r.systemProps.addAll( systemProps );
		return( r );
	}
	
	public String[] getOwnProperties() throws Exception {
		return( Common.getSortedKeys( properties ) );
	}

	public void copyProperties( PropertySet set ) throws Exception {
		for( String key : set.properties.keySet() ) {
			String primaryKey = Common.getPartAfterFirst( key , set.set + "." );
			setProperty( primaryKey , set.properties.get( key ) );
		}
		
		resolveProperties();
	}

	public void copyRawProperties( PropertySet set , String prefix ) throws Exception {
		for( String key : set.properties.keySet() ) {
			String primaryKey = prefix + Common.getPartAfterFirst( key , set.set + "." );
			setRawProperty( primaryKey , set.findPropertyAny( key ) );
		}
		
		resolveProperties();
	}

	public void moveRawAsStrings() throws Exception {
		moveRawAsIs();
		resolveProperties();
	}
	
	public void moveRawAsIs() throws Exception {
		for( String key : raw.keySet() ) {
			PropertyValue pv = new PropertyValue();
			pv.setString( raw.get( key ) );
			properties.put( key , pv );
		}
		raw.clear();
	}
	
	public void loadRawFromAttributes( Node node ) throws Exception {
		ConfReader.addAttributes( node , original );
		for( String attr : original.keySet() )
			setRawProperty( attr , original.get( attr ) );
	}
	
	public void loadRawFromElements( Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "property" );
		if( items == null )
			return;
		
		for( Node property : items ) {
			String name = ConfReader.getAttrValue( property , "name" );
			String value = ConfReader.getAttrValue( property , "value" );
			original.put( name , value );
			setRawProperty( name , value );
		}
	}

	public void loadRawFromElements( Node node , String prefix ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "property" );
		if( items == null )
			return;
		
		for( Node property : items ) {
			String name = prefix + ConfReader.getAttrValue( property , "name" );
			String value = ConfReader.getAttrValue( property , "value" );
			original.put( name , value );
			setRawProperty( name , value );
		}
	}

	public void loadRawFromFile( String path , RunContext execrc ) throws Exception {
		Properties props = ConfReader.readPropertyFile( execrc , path );
		for( Object xkey : props.keySet() ) {
			String key = ( String )xkey;
			String value = props.getProperty( ( String )key );
			
			if( value.startsWith( "\"" ) && value.endsWith( "\"" ) )
				value = value.substring( 1 , value.length() - 1 );
			original.put( key , value );
			setRawProperty( key , value );
		}
	}
	
	private void resolveProperties() throws Exception {
		// resolve properties
		for( Entry<String,PropertyValue> entry : properties.entrySet() )
			processEntry( entry );
	}
	
	private void setProperty( String key , PropertyValue value ) {
		properties.put( set + "." + key , value );
	}

	public void setRawProperty( String key , String value ) {
		raw.put( set + "." + key , value );
	}

	public String getRawProperty( String key ) throws Exception {
		String rawValue = raw.get( set + "." + key );
		if( rawValue == null )
			return( null );
		return( processValue( rawValue , false , false ) );
	}

	public void setStringProperty( String key , String value ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setString( value );
		properties.put( set + "." + key , pv );
	}

	public void setBooleanProperty( String key , String value ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setBool( value );
		properties.put( set + "." + key , pv );
	}

	public void setNumberProperty( String key , String value ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setNumber( value );
		properties.put( set + "." + key , pv );
	}

	public void setPathProperty( String key , String value , RunContext execrc ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setPath( value , execrc );
		properties.put( set + "." + key , pv );
	}

	public Set<String> keySet() {
		return( properties.keySet() );
	}

	public PropertyValue processValue( PropertyValue value ) throws Exception {
		PropertyValue newValue = new PropertyValue();
		newValue.type = value.type;
		newValue.data = processValue( value.data , false , false );
		if( newValue.data == null )
			newValue.data = "";
		return( newValue );
	}

	public String processFinalValue( String value , boolean isWindows ) throws Exception {
		return( processValue( value , true , isWindows ) );
	}
	
	private String processValue( String value , boolean finalValue , boolean isWindows ) throws Exception {
		if( value == null )
			return( "" );
		
		int indexFrom = value.indexOf( '@' );
		if( indexFrom < 0 )
			return( value );
		
		int indexTo = value.indexOf( '@' , indexFrom + 1 );
		if( indexTo < 0 )
			return( value );
		
		String res = value.substring( 0 , indexFrom );
		while( true ) {
			String var = value.substring( indexFrom + 1 , indexTo );
			if( var.isEmpty() )
				res += "@";
			else {
				PropertyValue pv = getPropertyInternal( var , false );
				if( pv.type == PropertyValueType.PROPERTY_PATH ) {
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
					
					res += s;
				}
				else
					res += pv.data; 
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
		
		return( res );
	}
	
	private void processEntry( Entry<String,PropertyValue> entry ) throws Exception {
		PropertyValue res = processValue( entry.getValue() );
		if( res != null )
			entry.setValue( res );
	}

	public String getPropertyAny( String name ) throws Exception {
		PropertyValue pv = getPropertyInternal( name , false );
		if( pv == null )
			return( null );
		return( pv.data );
	}
	
	public String getFinalProperty( String name , boolean system , RunContext execrc ) throws Exception {
		PropertyValue pv = getPropertyInternal( name , system );
		if( pv == null )
			return( "" );
		if( pv.type != PropertyValueType.PROPERTY_PATH )
			return( pv.data );
		
		if( execrc.isLinux() )
			return( Common.getLinuxPath( pv.data ) );
		if( execrc.isWindows() )
			return( Common.getWinPath( pv.data ) );
		
		throw new ExitException( "UnexpectedState" );
	}
	
	private PropertyValue getPropertyInternal( String name , boolean system ) throws Exception {
		// prefixed var
		if( properties.containsKey( name ) ) {
			PropertyValue pv = properties.get( name );
			return( pv );
		}
		
		// unprefixed var
		String setName = set + "." + name;
		if( properties.containsKey( setName ) )
			return( properties.get( setName ) );
		
		// parent var
		if( parent == null || system )
			throw new ExitException( "set=" + set + ": unresolved variable=" + name );
			
		return( parent.getPropertyInternal( name , false ) );
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
		if( properties.containsKey( name ) )
			return( properties.get( name ) );
		
		// unprefixed var
		String setName = set + "." + name;
		if( properties.containsKey( setName ) )
			return( properties.get( setName ) );
		
		// parent var
		if( parent == null || system )
			return( defaultValue );
			
		return( parent.findPropertyInternal( setName , defaultValue , false ) );
	}

	public String getRequiredPropertyAny( String name ) throws Exception {
		PropertyValue pv = getRequiredPropertyInternal( name , false );
		return( pv.data );
	}
	
	private PropertyValue getRequiredPropertyInternal( String name , boolean system ) throws Exception {
		PropertyValue pv = getPropertyInternal( name , system );
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

	public String getSystemRequiredStringProperty( String name ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setString( value );
			setProperty( name , pv );
		}
		
		PropertyValue pv = getRequiredPropertyInternal( name , true );
		if( pv.type != PropertyValueType.PROPERTY_STRING )
			throw new ExitException( "property is not boolean name=" + name );
		
		return( pv.data );
	}

	public String getSystemRequiredPathProperty( String name , RunContext execrc ) throws Exception {
		String path = getSystemPathProperty( name , null , execrc );
		if( path == null || path.isEmpty() )
			throw new ExitException( "property is required name=" + name );
		return( path );
	}
	
	public String getSystemPathProperty( String name , String defaultValue , RunContext execrc ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setPath( value , execrc );
			setProperty( name , pv );
		}
		
		return( getPathPropertyInternal( name , defaultValue , true ) );
	}
	
	public String getSystemStringProperty( String name , String defaultValue ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setString( value );
			setProperty( name , pv );
		}
		
		return( getStringPropertyInternal( name , defaultValue , true ) );
	}

	public int getSystemIntProperty( String name , int defaultValue ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setNumber( value );
			setProperty( name , pv );
		}
		
		return( getIntPropertyInternal( name , defaultValue , true ) );
	}

	public boolean getSystemBooleanProperty( String name , boolean defaultValue ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setBool( value );
			setProperty( name , pv );
		}
		
		return( getBooleanPropertyInternal( name , defaultValue , true ) );
	}

	public void finishRawProperties() throws Exception {
		for( String prop : raw.keySet() ) {
			if( !systemProps.contains( prop ) )
				throw new ExitException( "set=" + set + ": unexpected property=" + prop );
		}
		
		raw.clear();
	}

	public String[] getKeySet() {
		return( Common.getSortedKeys( properties ) );		
	}

	public void saveAsElements( Document doc , Element parent ) throws Exception {
		for( String key : original.keySet() ) {
			String value = original.get( key );
			Common.xmlCreatePropertyElement( doc , parent , key , value );
		}
	}
	
}
