package ru.egov.urm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Node;

import ru.egov.urm.run.ActionBase;

public class PropertySet {

	public String set;
	PropertySet parent;
	
	private Map<String,String> properties;

	public PropertySet( String set , PropertySet parent ) {
		properties = new HashMap<String,String>();
		
		this.set = set;
		this.parent = parent;
	}
	
	public String[] getOwnProperties( ActionBase action ) throws Exception {
		return( Common.getSortedKeys( properties ) );
	}
	
	public void loadFromAttributes( ActionBase action , Node node ) throws Exception {
		Map<String,String> attrs = new HashMap<String,String>();
		ConfReader.addAttributes( action , node , attrs );
		for( String attr : attrs.keySet() )
			setProperty( attr , attrs.get( attr ) );
		
		resolveProperties( action );
	}
	
	public void loadFromElements( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( action , node , "property" );
		if( items == null )
			return;
		
		for( Node property : items ) {
			String name = ConfReader.getNameAttr( action , property );
			String value = ConfReader.getAttrValue( action , property , "value" );
			setProperty( name , value );
		}

		resolveProperties( action );
	}

	public void loadFromFile( ActionBase action , String path ) throws Exception {
		Properties props = ConfReader.readPropertyFile( action , path );
		for( Object xkey : props.keySet() ) {
			String key = ( String )xkey;
			String value = props.getProperty( ( String )key );
			setProperty( key , value );
		}
		
		resolveProperties( action );
	}
	
	private void resolveProperties( ActionBase action ) throws Exception {
		// resolve properties
		for( Entry<String,String> entry : properties.entrySet() )
			processEntry( action , entry );
	}
	
	public void setProperty( String key , String value ) {
		properties.put( set + "." + key , value );
	}

	public Set<String> keySet() {
		return( properties.keySet() );
	}
	
	public String processValue( ActionBase action , String value ) throws Exception {
		int indexFrom = value.indexOf( '@' );
		if( indexFrom < 0 )
			return( null );
		
		int indexTo = value.indexOf( '@' , indexFrom + 1 );
		if( indexTo < 0 )
			return( null );
		
		String res = value.substring( 0 , indexFrom );
		while( true ) {
			String var = value.substring( indexFrom + 1 , indexTo );
			if( var.isEmpty() )
				res += "@";
			else
				res += getProperty( action , var );
			
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
	
	private void processEntry( ActionBase action , Entry<String,String> entry ) throws Exception {
		String res = processValue( action , entry.getValue() );
		if( res != null )
			entry.setValue( res );
	}

	public String getProperty( ActionBase action , String name ) throws Exception {
		return( getPropertyInternal( action , name , false ) );
	}
	
	private String getPropertyInternal( ActionBase action , String name , boolean system ) throws Exception {
		// prefixed var
		if( properties.containsKey( name ) )
			return( properties.get( name ) );
		
		// unprefixed var
		String setName = set + "." + name;
		if( properties.containsKey( setName ) )
			return( properties.get( setName ) );
		
		// parent var
		if( parent == null || system )
			action.exit( "unexpected unresolved variable=" + name );
			
		return( parent.getProperty( action , name ) );
	}

	public String findProperty( ActionBase action , String name ) throws Exception {
		return( findProperty( action , name , "" ) );
	}

	public String findProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		return( findPropertyInternal( action , name , defaultValue , false ) );
	}
	
	private String findPropertyInternal( ActionBase action , String name , String defaultValue , boolean system ) throws Exception {
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
			
		return( parent.getProperty( action , setName , defaultValue ) );
	}

	public String getSystemRequiredProperty( ActionBase action , String name , List<String> props ) throws Exception {
		props.add( set + "." + name );
		return( getRequiredPropertyInternal( action , name , true ) );
	}

	public String getRequiredProperty( ActionBase action , String name ) throws Exception {
		return( getRequiredPropertyInternal( action , name , false ) );
	}
	
	private String getRequiredPropertyInternal( ActionBase action , String name , boolean system ) throws Exception {
		String value = getPropertyInternal( action , name , system );
		if( value == null || value.isEmpty() )
			action.exit( "empty property=" + name );
		return( value );
	}

	public String getSystemProperty( ActionBase action , String name , String defaultValue , List<String> props ) throws Exception {
		props.add( set + "." + name );
		return( getPropertyInternal( action , name , defaultValue , true ) );
	}

	public String getProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		return( getPropertyInternal( action , name , defaultValue , false ) );
	}
	
	private String getPropertyInternal( ActionBase action , String name , String defaultValue , boolean system ) throws Exception {
		String value = findPropertyInternal( action , name , defaultValue , system );
		if( value == null || value.isEmpty() )
			return( defaultValue );
		return( value );
	}

	public int getSystemIntProperty( ActionBase action , String name , int defaultValue , List<String> props ) throws Exception {
		props.add( set + "." + name );
		return( getIntPropertyInternal( action , name , defaultValue , true ) );
	}

	public int getIntProperty( ActionBase action , String name , int defaultValue ) throws Exception {
		return( getIntPropertyInternal( action , name , defaultValue , false ) );
	}
	
	private int getIntPropertyInternal( ActionBase action , String name , int defaultValue , boolean system ) throws Exception {
		String value = findPropertyInternal( action , name , null , system );
		if( value == null || value.isEmpty() )
			return( defaultValue );
		return( Integer.parseInt( value ) );
	}

	public boolean getSystemBooleanProperty( ActionBase action , String name , boolean defaultValue , List<String> props ) throws Exception {
		props.add( set + "." + name );
		return( getBooleanPropertyInternal( action , name , defaultValue , true ) );
	}

	public boolean getBooleanProperty( ActionBase action , String name , boolean defaultValue ) throws Exception {
		return( getBooleanPropertyInternal( action , name , defaultValue , false ) );
	}
	
	private boolean getBooleanPropertyInternal( ActionBase action , String name , boolean defaultValue , boolean system ) throws Exception {
		String value = findPropertyInternal( action , name , null , system );
		if( value == null || value.isEmpty() )
			return( defaultValue );
		return( Common.getBooleanValue( value ) );
	}

	public void checkUnexpected( ActionBase action , List<String> props ) throws Exception {
		for( String prop : properties.keySet() ) {
			if( !props.contains( prop ) )
				action.exit( "set=" + set + " has unexpected property=" + prop );
		}
	}

}
