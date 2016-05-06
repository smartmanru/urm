package ru.egov.urm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Node;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;
import ru.egov.urm.meta.Metadata.VarOSTYPE;

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
		
		public void setString( ActionBase action , String value ) throws Exception {
			type = PropertyValueType.PROPERTY_STRING;
			if( value == null || value.isEmpty() )
				data = "";
			else
				data = value;
		}
		
		public void setNumber( ActionBase action , String value ) throws Exception {
			if( value == null || value.isEmpty() ) {
				type = PropertyValueType.PROPERTY_NUMBER;
				data = "";
				return;
			}
			
			try {
				Integer.parseInt( value );
			}
			catch( Throwable e ) {
				action.exit( "invalid number value=" + value );
			}
			
			type = PropertyValueType.PROPERTY_NUMBER;
			data = value;
		}
		
		public void setBool( ActionBase action , String value ) throws Exception {
			if( value == null || value.isEmpty() ) {
				type = PropertyValueType.PROPERTY_BOOL;
				data = "";
				return;
			}
			
			try {
				Common.getBooleanValue( value );
			}
			catch( Throwable e ) {
				action.exit( "invalid number value=" + value );
			}
			
			type = PropertyValueType.PROPERTY_BOOL;
			data = value;
		}
		
		public void setPath( ActionBase action , String value ) throws Exception {
			type = PropertyValueType.PROPERTY_PATH;
			if( value == null || value.isEmpty() )
				data = "";
			else
				data = Common.getLinuxPath( value );
			
			if( data.startsWith( "~/") )
				data = action.context.userHome + data.substring( 1 );
		}
	}
	
	public String set;
	PropertySet parent;
	
	private Map<String,PropertyValue> properties;
	private Map<String,String> raw;
	List<String> systemProps = new LinkedList<String>();

	public PropertySet( String set , PropertySet parent ) {
		properties = new HashMap<String,PropertyValue>();
		raw = new HashMap<String,String>();
		
		this.set = set;
		this.parent = parent;
	}
	
	public String[] getOwnProperties( ActionBase action ) throws Exception {
		return( Common.getSortedKeys( properties ) );
	}

	public void copyProperties( ActionBase action , PropertySet set ) throws Exception {
		for( String key : set.properties.keySet() ) {
			String primaryKey = Common.getPartAfterFirst( key , set.set + "." );
			setProperty( primaryKey , set.properties.get( key ) );
		}
		
		resolveProperties( action );
	}

	public void moveRawAsStrings( ActionBase action ) throws Exception {
		for( String key : raw.keySet() ) {
			PropertyValue pv = new PropertyValue();
			pv.setString( action , raw.get( key ) );
			properties.put( key , pv );
		}
		
		raw.clear();
		resolveProperties( action );
	}
	
	public void loadRawFromAttributes( ActionBase action , Node node ) throws Exception {
		Map<String,String> attrs = new HashMap<String,String>();
		ConfReader.addAttributes( action , node , attrs );
		for( String attr : attrs.keySet() )
			setRawProperty( attr , attrs.get( attr ) );
	}
	
	public void loadRawFromElements( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( action , node , "property" );
		if( items == null )
			return;
		
		for( Node property : items ) {
			String name = ConfReader.getNameAttr( action , property , VarNAMETYPE.ALPHANUMDOT );
			String value = ConfReader.getAttrValue( action , property , "value" );
			setRawProperty( name , value );
		}
	}

	public void loadRawFromFile( ActionBase action , String path ) throws Exception {
		Properties props = ConfReader.readPropertyFile( action , path );
		for( Object xkey : props.keySet() ) {
			String key = ( String )xkey;
			String value = props.getProperty( ( String )key );
			
			if( value.startsWith( "\"" ) && value.endsWith( "\"" ) )
				value = value.substring( 1 , value.length() - 1 );
			setRawProperty( key , value );
		}
	}
	
	private void resolveProperties( ActionBase action ) throws Exception {
		// resolve properties
		for( Entry<String,PropertyValue> entry : properties.entrySet() )
			processEntry( action , entry );
	}
	
	private void setProperty( String key , PropertyValue value ) {
		properties.put( set + "." + key , value );
	}

	public void setRawProperty( String key , String value ) {
		raw.put( set + "." + key , value );
	}

	private String getRawProperty( ActionBase action , String key ) throws Exception {
		String rawValue = raw.get( set + "." + key );
		return( processValue( action , rawValue , false , null ) );
	}

	public void setStringProperty( ActionBase action , String key , String value ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setString( action , value );
		properties.put( set + "." + key , pv );
	}

	public void setBooleanProperty( ActionBase action , String key , String value ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setBool( action , value );
		properties.put( set + "." + key , pv );
	}

	public void setNumberProperty( ActionBase action , String key , String value ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setNumber( action , value );
		properties.put( set + "." + key , pv );
	}

	public void setPathProperty( ActionBase action , String key , String value ) throws Exception {
		PropertyValue pv = new PropertyValue();
		pv.setPath( action , value );
		properties.put( set + "." + key , pv );
	}

	public Set<String> keySet() {
		return( properties.keySet() );
	}

	public PropertyValue processValue( ActionBase action , PropertyValue value ) throws Exception {
		PropertyValue newValue = new PropertyValue();
		newValue.type = value.type;
		newValue.data = processValue( action , value.data , false , null );
		if( newValue.data == null )
			newValue.data = "";
		return( newValue );
	}

	public String processFinalValue( ActionBase action , String value , VarOSTYPE osType ) throws Exception {
		return( processValue( action , value , true , osType ) );
	}
	
	private String processValue( ActionBase action , String value , boolean finalValue , VarOSTYPE osType ) throws Exception {
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
				PropertyValue pv = getPropertyInternal( action , var , false );
				if( pv.type == PropertyValueType.PROPERTY_PATH ) {
					String s = pv.data;
					if( finalValue ) {
						if( osType == VarOSTYPE.LINUX )
							s = Common.getLinuxPath( s );
						else
						if( osType == VarOSTYPE.WINDOWS )
							s = Common.getWinPath( s );
						else
							action.exitUnexpectedState();
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
	
	private void processEntry( ActionBase action , Entry<String,PropertyValue> entry ) throws Exception {
		PropertyValue res = processValue( action , entry.getValue() );
		if( res != null )
			entry.setValue( res );
	}

	public String getPropertyAny( ActionBase action , String name ) throws Exception {
		PropertyValue pv = getPropertyInternal( action , name , false );
		if( pv == null )
			return( null );
		return( pv.data );
	}
	
	public String getFinalProperty( ActionBase action , String name , boolean system ) throws Exception {
		PropertyValue pv = getPropertyInternal( action , name , system );
		if( pv == null )
			return( "" );
		if( pv.type != PropertyValueType.PROPERTY_PATH )
			return( pv.data );
		
		if( action.isLinux() )
			return( Common.getLinuxPath( pv.data ) );
		if( action.isWindows() )
			return( Common.getWinPath( pv.data ) );
		
		action.exitUnexpectedState();
		return( null );
	}
	
	private PropertyValue getPropertyInternal( ActionBase action , String name , boolean system ) throws Exception {
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
			action.exit( "set=" + set + ": unresolved variable=" + name );
			
		return( parent.getPropertyInternal( action , name , false ) );
	}

	public String findPropertyAny( ActionBase action , String name ) throws Exception {
		return( findPropertyAny( action , name , "" ) );
	}

	public String findPropertyAny( ActionBase action , String name , String defaultValue ) throws Exception {
		PropertyValue pv = findPropertyInternal( action , name , null , false );
		if( pv == null )
			return( defaultValue );
		return( pv.data );
	}
	
	private PropertyValue findPropertyInternal( ActionBase action , String name , PropertyValue defaultValue , boolean system ) throws Exception {
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
			
		return( parent.findPropertyInternal( action , setName , defaultValue , false ) );
	}

	public String getRequiredPropertyAny( ActionBase action , String name ) throws Exception {
		PropertyValue pv = getRequiredPropertyInternal( action , name , false );
		return( pv.data );
	}
	
	private PropertyValue getRequiredPropertyInternal( ActionBase action , String name , boolean system ) throws Exception {
		PropertyValue pv = getPropertyInternal( action , name , system );
		if( pv == null || pv.data.isEmpty() )
			action.exit( "set=" + set + ": empty property=" + name );
		return( pv );
	}

	private String getPathPropertyInternal( ActionBase action , String name , String defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( action , name , null , system );
		if( pv == null ) {
			if( defaultValue == null )
				return( null );
			return( Common.getLinuxPath( defaultValue ) );
		}
		if( pv.type != PropertyValueType.PROPERTY_PATH )
			action.exit( "property is not string name=" + name );
		return( pv.data );
	}

	public String getStringProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		return( getStringPropertyInternal( action , name , defaultValue , false ) );
	}
	
	public String getPathProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		return( getPathPropertyInternal( action , name , defaultValue , false ) );
	}
	
	private String getStringPropertyInternal( ActionBase action , String name , String defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( action , name , null , system );
		if( pv == null )
			return( defaultValue );
		if( pv.type != PropertyValueType.PROPERTY_STRING )
			action.exit( "property is not string name=" + name );
		return( pv.data );
	}

	public int getIntProperty( ActionBase action , String name , int defaultValue ) throws Exception {
		return( getIntPropertyInternal( action , name , defaultValue , false ) );
	}
	
	private int getIntPropertyInternal( ActionBase action , String name , int defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( action , name , null , system );
		if( pv == null )
			return( defaultValue );
		if( pv.type != PropertyValueType.PROPERTY_NUMBER )
			action.exit( "property is not number name=" + name );
		if( pv.data.isEmpty() )
			return( defaultValue );
		return( Integer.parseInt( pv.data ) );
	}

	public boolean getBooleanProperty( ActionBase action , String name , boolean defaultValue ) throws Exception {
		return( getBooleanPropertyInternal( action , name , defaultValue , false ) );
	}
	
	private boolean getBooleanPropertyInternal( ActionBase action , String name , boolean defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( action , name , null , system );
		if( pv == null )
			return( defaultValue );
		if( pv.type != PropertyValueType.PROPERTY_BOOL )
			action.exit( "property is not boolean name=" + name );
		if( pv.data.isEmpty() )
			return( defaultValue );
		return( Common.getBooleanValue( pv.data ) );
	}

	public void printValues( ActionBase action ) throws Exception {
		for( String prop : properties.keySet() ) {
			PropertyValue pv = properties.get( prop );
			action.log( "property " + prop + "=" + pv.data );
		}
	}

	public String getSystemRequiredStringProperty( ActionBase action , String name ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( action , name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setString( action , value );
			setProperty( name , pv );
		}
		
		PropertyValue pv = getRequiredPropertyInternal( action , name , true );
		if( pv.type != PropertyValueType.PROPERTY_STRING )
			action.exit( "property is not boolean name=" + name );
		
		return( pv.data );
	}

	public String getSystemRequiredPathProperty( ActionBase action , String name ) throws Exception {
		String path = getSystemPathProperty( action , name , null );
		if( path == null || path.isEmpty() )
			action.exit( "property is required name=" + name );
		return( path );
	}
	
	public String getSystemPathProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( action , name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setPath( action , value );
			setProperty( name , pv );
		}
		
		return( getPathPropertyInternal( action , name , defaultValue , true ) );
	}
	
	public String getSystemStringProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( action , name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setString( action , value );
			setProperty( name , pv );
		}
		
		return( getStringPropertyInternal( action , name , defaultValue , true ) );
	}

	public int getSystemIntProperty( ActionBase action , String name , int defaultValue ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( action , name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setNumber( action , value );
			setProperty( name , pv );
		}
		
		return( getIntPropertyInternal( action , name , defaultValue , true ) );
	}

	public boolean getSystemBooleanProperty( ActionBase action , String name , boolean defaultValue ) throws Exception {
		systemProps.add( set + "." + name );
		String value = getRawProperty( action , name );
		if( value != null ) {
			PropertyValue pv = new PropertyValue();
			pv.setBool( action , value );
			setProperty( name , pv );
		}
		
		return( getBooleanPropertyInternal( action , name , defaultValue , true ) );
	}

	public void finishRawProperties( ActionBase action ) throws Exception {
		for( String prop : raw.keySet() ) {
			if( !systemProps.contains( prop ) )
				action.exit( "set=" + set + ": unexpected property=" + prop );
		}
		
		raw.clear();
	}

}
