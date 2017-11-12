package org.urm.engine.properties;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.common._Error;
import org.urm.common.action.CommandOption.FLAG;
import org.urm.db.DBEnumTypes.*;
import org.urm.engine.properties.PropertyValue.PropertyValueOrigin;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PropertySet {

	public String set;
	public PropertySet parent;
	
	private Map<String,PropertyValue> data;
	private boolean resolved;
	
	public PropertySet( String set , PropertySet parent ) {
		this.set = set;
		this.parent = parent;
		
		data = new HashMap<String,PropertyValue>();
		resolved = true;
	}

	public int count() {
		return( data.size() );
	}
	
	public PropertySet copy( PropertySet parentNew ) {
		PropertySet r = new PropertySet( set , parentNew );
		for( PropertyValue value : data.values() ) {
			PropertyValue rv = new PropertyValue( value );
			r.setProperty( rv );
		}
		r.resolved = resolved;
		return( r );
	}
	
	public PropertySet copy( PropertySet parentNew , boolean system ) {
		PropertySet r = new PropertySet( set , parentNew );
		for( PropertyValue value : data.values() ) {
			if( value.isSystem() != system )
				continue;
			
			PropertyValue rv = new PropertyValue( value );
			r.setProperty( rv );
		}
		r.resolved = resolved;
		return( r );
	}
	
	public boolean isResolved() {
		for( PropertyValue p : data.values() ) {
			if( !p.isResolved() )
				return( false );
		}
		
		return( true );
	}
	
	public boolean isCorrect() {
		for( PropertyValue p : data.values() ) {
			if( !p.isCorrect() )
				return( false );
		}
		
		return( true );
	}
	
	public int getDepth() {
		int depth = 0;
		PropertySet ps = parent;
		while( ps != null ) {
			depth++;
			ps = ps.parent;
		}
		return( depth );
	}
	
	public String getPropertyByKey( String key ) {
		return( key.substring( set.length() + 1 ) );
	}
	
	public String getKeyByProperty( String prop ) {
		return( set + "#" + prop );
	}
	
	public String[] getRunningKeys() {
		List<String> props = new LinkedList<String>();
		for( PropertyValue p : data.values() ) {
			if( p.isResolved() )
				props.add( getKeyByProperty( p.property ) );
		}
		return( Common.getSortedList( props ) );		
	}

	public String[] getRunningProperties() {
		List<String> props = new LinkedList<String>();
		for( PropertyValue p : data.values() ) {
			if( p.isResolved() )
				props.add( p.property );
		}
		return( Common.getSortedList( props ) );		
	}

	public String[] getOriginalProperties() {
		List<String> props = new LinkedList<String>();
		for( PropertyValue p : data.values() ) {
			if( !p.isManual() )
				props.add( p.property );
		}
		return( Common.getSortedList( props ) );		
	}

	public String[] getManualProperties() {
		List<String> props = new LinkedList<String>();
		for( PropertyValue p : data.values() ) {
			if( p.isManual() )
				props.add( p.property );
		}
		return( Common.getSortedList( props ) );		
	}

	public String[] getCustomProperties() {
		List<String> props = new LinkedList<String>();
		for( PropertyValue p : data.values() ) {
			if( p.isCustom() )
				props.add( p.property );
		}
		return( Common.getSortedList( props ) );		
	}

	public PropertyValue[] getAllProperties() {
		List<PropertyValue> props = new LinkedList<PropertyValue>();
		for( String p : Common.getSortedKeys( data ) )
			props.add( data.get( p ) );
		return( props.toArray( new PropertyValue[0] ) );		
	}
	
	public String[] getAllKeys() {
		return( Common.getSortedKeys( data ) );
	}

	public String[] getRawKeys() {
		List<String> props = new LinkedList<String>();
		for( PropertyValue p : data.values() ) {
			if( !p.isResolved() )
				props.add( p.property );
		}
		return( Common.getSortedList( props ) );		
	}
	
	public PropertyValue getPropertyValue( String prop ) {
		return( data.get( getKeyByProperty( prop ) ) );
	}
	
	public void removeProperty( String prop ) {
		resolved = false;
		data.remove( getKeyByProperty( prop ) );
	}

	public String getExpressionByProperty( String prop ) {
		PropertyValue p = data.get( getKeyByProperty( prop ) );
		if( p == null )
			return( "" );
		return( p.getExpressionValue() );
	}

	public String getOriginalByProperty( String prop ) {
		PropertyValue p = data.get( getKeyByProperty( prop ) );
		if( p == null )
			return( "" );
		return( p.getOriginalValue() );
	}

	public void copyOriginalPropertiesToRaw( PropertySet set ) throws Exception {
		copyPropertiesToRaw( set , true );
	}

	public void copyPropertiesToRaw( PropertySet set ) throws Exception {
		copyPropertiesToRaw( set , false );
	}
	
	private void copyPropertiesToRaw( PropertySet set , boolean withoutManual ) throws Exception {
		resolved = false;
		for( PropertyValue p : set.data.values() ) {
			if( withoutManual && p.isManual() )
				continue;
			
			PropertyValue pv = new PropertyValue( p.property , PropertyValue.PropertyValueOrigin.PROPERTY_EXTRA , set , p.desc );
			pv.setOriginalAndFinalValue( p.getOriginalValue() );
			pv.setType( p.getType() );
			if( p.isSystem() )
				pv.setSystem();
			setProperty( pv );
		}
	}

	public void copyCustomPropertiesToRaw( PropertySet set ) throws Exception {
		resolved = false;
		for( PropertyValue p : set.data.values() ) {
			if( p.isCustom() )
				createOriginalAndRawProperty( p.property , p.getOriginalValue() , true , p.getType() , p.desc );
		}
	}

	public void copyOriginalPropertiesToRaw() throws Exception {
		resolved = false;
		for( PropertyValue p : data.values() )
			p.setFinalFromOriginalValue();
	}

	public void copyRunningPropertiesToRunning( PropertySet src ) throws Exception {
		resolved = false;
		for( PropertyValue p : src.data.values() ) {
			if( !p.isResolved() )
				continue;
			
			PropertyValue pv = new PropertyValue( p.property , PropertyValue.PropertyValueOrigin.PROPERTY_EXTRA , src , p.desc );
			pv.setType( p.getType() );
			pv.setOriginalAndFinalValue( p.getOriginalValue() , p.getFinalValue() );
			if( !pv.isResolved() )
				Common.exit2( _Error.UnresolvedRunningProperty2 , "cannot set unresolved running property set=" + set + ", prop=" + p.property , set , p.property );
			
			setProperty( pv );
		}
	}

	public void resolveRawProperties() throws Exception {
		resolveRawProperties( false );
	}

	public boolean resolveRawProperties( boolean allowUnresolved ) throws Exception {
		// resolve properties
		resolved = false;
		int unresolved = 0;
		while( true ) {
			int unresolvedCheck = 0;
			for( PropertyValue pv : data.values() ) {
				if( pv.isResolved() )
					continue;
				
				if( pv.isTemplated() )
					continue;
				
				if( !resolveProperty( pv , allowUnresolved ) )
					unresolvedCheck++;
			}
			
			if( unresolvedCheck == 0 ) {
				resolved = true;
				return( true );
			}
			
			if( unresolvedCheck == unresolved )
				return( false );
			
			unresolved = unresolvedCheck;
		}
	}

	public void recalculateProperties() throws Exception {
		resolved = false;
		for( PropertyValue pv : data.values() )
			pv.setFinalFromOriginalValue();
		resolveRawProperties( true );
	}

	public void setOriginalProperty( String prop , DBEnumParamValueType type , String value , boolean system , ShellExecutor target ) throws Exception {
		PropertyValueOrigin origin = ( system )? PropertyValueOrigin.PROPERTY_ORIGINAL : PropertyValueOrigin.PROPERTY_CUSTOM;
		PropertyValue pv = new PropertyValue( prop , origin , null , null );
		if( system )
			pv.setSystem();
		if( type == DBEnumParamValueType.BOOL )
			pv.setBool( value );
		else
		if( type == DBEnumParamValueType.NUMBER )
			pv.setNumber( value );
		else
		if( type == DBEnumParamValueType.PATH )
			pv.setPath( value , target );
		else
			pv.setString( value );
		setProperty( pv );
	}

	public PropertyValue setOriginalSystemStringProperty( String prop , String value ) throws Exception {
		PropertyValue pv = setOriginalStringProperty( prop , value );
		pv.setSystem();
		return( pv );
	}
	
	public PropertyValue setOriginalStringProperty( String prop , String value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setString( value );
		return( setProperty( pv ) );
	}

	public PropertyValue setOriginalBooleanProperty( String prop , boolean value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setBool( value );
		return( setProperty( pv ) );
	}

	public PropertyValue setOriginalSystemBooleanProperty( String prop , boolean value ) throws Exception {
		PropertyValue pv = setOriginalBooleanProperty( prop , value );
		pv.setSystem();
		return( pv );
	}
	
	public PropertyValue setOriginalBooleanProperty( String prop , FLAG value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		if( value != FLAG.DEFAULT )
			pv.setBool( value == FLAG.YES );
		return( setProperty( pv ) );
	}

	public PropertyValue setOriginalSystemBooleanProperty( String prop , FLAG value ) throws Exception {
		PropertyValue pv = setOriginalBooleanProperty( prop , value );
		pv.setSystem();
		return( pv );
	}
	
	public PropertyValue setOriginalNumberProperty( String prop , int value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setNumber( value );
		return( setProperty( pv ) );
	}

	public PropertyValue setOriginalSystemNumberProperty( String prop , int value ) throws Exception {
		PropertyValue pv = setOriginalNumberProperty( prop , value );
		pv.setSystem();
		return( pv );
	}
	
	public PropertyValue setOriginalPathProperty( String prop , String value ) throws Exception {
		return( setOriginalPathProperty( prop , value , null ) );
	}

	public PropertyValue setOriginalPathProperty( String prop , String value , ShellExecutor target ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setPath( value , target );
		return( setProperty( pv ) );
	}

	public PropertyValue setOriginalSystemPathProperty( String prop , String value , ShellExecutor target ) throws Exception {
		PropertyValue pv = setOriginalPathProperty( prop , value , target );
		pv.setSystem();
		return( pv );
	}
	
	public void setManualStringProperty( String prop , String value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null , null );
		pv.setString( value );
		setProperty( pv );
	}
	
	public void setManualNumberProperty( String prop , int value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null , null );
		pv.setNumber( value );
		setProperty( pv );
	}
	
	public void setManualBooleanProperty( String prop , boolean value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null , null );
		pv.setBool( value );
		setProperty( pv );
	}
	
	public void setManualPathProperty( String prop , String value , ShellExecutor shell ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_MANUAL , null , null );
		pv.setPath( value , shell );
		setProperty( pv );
	}
	
	public void setStringProperty( String prop , String value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setString( value );
		setProperty( pv );
	}

	public void setBooleanProperty( String prop , boolean value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setBool( value );
		setProperty( pv );
	}

	public void setBooleanProperty( String prop , FLAG value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		if( value != FLAG.DEFAULT )
			pv.setBool( value == FLAG.YES );
		setProperty( pv );
	}

	public void setNumberProperty( String prop , int value ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setNumber( value );
		setProperty( pv );
	}

	public void setPathProperty( String prop , String value ) throws Exception {
		setPathProperty( prop , value , null );
	}

	public void setPathProperty( String prop , String value , ShellExecutor target ) throws Exception {
		PropertyValue pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , null , null );
		pv.setPath( value , target );
		setProperty( pv );
	}

	public String getPropertyAny( String prop ) throws Exception {
		PropertyValue pv = getPropertyInternal( prop , true , true , true );
		if( pv == null )
			return( null );
		return( pv.getFinalValue() );
	}
	
	public PropertyValue getFinalProperty( String name , boolean allowParent , boolean allowUnresolved ) throws Exception {
		PropertyValue pv = getPropertyInternal( name , true , allowParent , allowUnresolved );
		if( pv == null )
			return( null );
		return( pv );
	}

	public String getFinalProperty( String name , Account account , boolean allowParent , boolean allowUnresolved ) throws Exception {
		PropertyValue pv = getFinalProperty( name , allowParent , allowUnresolved );
		if( pv == null )
			return( null );
		String data = pv.getFinalValue();
		if( pv.getType() != DBEnumParamValueType.PATH )
			return( data );
		return( account.getOSPath( data ) );
	}

	public String getFinalString( String value , boolean isWindows , boolean allowParent , boolean allowUnresolved ) throws Exception {
		PropertyValue pv = getFinalPropertyValue( value , isWindows , allowParent , allowUnresolved );
		return( pv.getFinalValue() );
	}
	
	public PropertyValue getFinalPropertyValue( String value , boolean isWindows , boolean allowParent , boolean allowUnresolved ) throws Exception {
		PropertyValue pv = new PropertyValue( "" , PropertyValueOrigin.PROPERTY_MANUAL , null , null );
		pv.setOriginalAndFinalValue( value );
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
		if( pv.isFinalEmpty() )
			return( defaultValue );
		return( pv.getFinalValue() );
	}
	
	public String getRequiredPropertyAny( String name ) throws Exception {
		PropertyValue pv = getRequiredPropertyInternal( name );
		return( pv.getFinalValue() );
	}

	public String getUrlProperty( String name , String defaultValue ) throws Exception {
		return( getStringPropertyInternal( name , defaultValue , false ) );
	}
	
	public String getStringProperty( String name , String defaultValue ) throws Exception {
		return( getStringPropertyInternal( name , defaultValue , false ) );
	}
	
	public String getPathProperty( String name , String defaultValue ) throws Exception {
		return( getPathPropertyInternal( name , defaultValue , false ) );
	}
	
	public int getIntProperty( String name , int defaultValue ) throws Exception {
		return( getIntPropertyInternal( name , defaultValue , false ) );
	}
	
	public boolean getBooleanProperty( String name , boolean defaultValue ) throws Exception {
		return( getBooleanPropertyInternal( name , defaultValue , false ) );
	}
	
	public void updateProperties( PropertySet src , boolean system ) throws Exception {
		if( !system ) {
			copyCustomPropertiesToRaw( src );
			return;
		}
		
		for( String prop : src.getOriginalProperties() ) {
			PropertyValue pv = src.getPropertyValue( prop );
			if( pv.isSystem() ) {
				String value = src.getOriginalByProperty( prop );
				updateOriginalProperty( prop , value );
			}
		}
	}

	public void finishRawProperties() throws Exception {
		resolveRawProperties();
		for( PropertyValue pv : data.values() ) {
			if( pv.isResolved() )
				continue;
			
			if( pv.isTemplated() )
				continue;
			
			Common.exit2( _Error.UnresolvedVariable2 , "set=" + set + ": unresolved property=" + pv.property , set , pv.property );
		}
	}

	public void saveToPropertyFile( String path , RunContext execrc , boolean custom , String comment ) throws Exception {
		Properties props = new Properties();
		for( PropertyValue pv : getAllProperties() ) {
			if( pv.isManual() )
				continue;
			
			String value = pv.getOriginalValue();
			props.setProperty( pv.property , value );
		}
		Common.createPropertyFile( execrc , path , props , comment );
	}
	
	public void saveAsElements( Document doc , Element parent , boolean custom ) throws Exception {
		for( PropertyValue pv : getAllProperties() ) {
			if( pv.isManual() )
				continue;
			
			if( custom != pv.isCustom() )
				continue;
			
			String value = pv.getOriginalValue();
			if( !value.isEmpty() ) {
				Element element = Common.xmlCreatePropertyElement( doc , parent , pv.property , value );
				if( custom ) {
					Common.xmlSetElementAttr( doc , element , "desc" , pv.desc );
					Common.xmlSetElementAttr( doc , element , "type" , Common.getEnumLower( DBEnumParamValueType.STRING ) );
				}
			}
		}
	}
	
	public void saveSplit( Document doc , Element parent ) throws Exception {
		for( PropertyValue pv : getAllProperties() ) {
			if( pv.isManual() )
				continue;
			
			String value = pv.getOriginalValue();
			if( !value.isEmpty() ) {
				if( pv.isSystem() )
					Common.xmlSetElementAttr( doc , parent , pv.property , value );
				else
				if( pv.isCustom() ) {
					Element element = Common.xmlCreatePropertyElement( doc , parent , pv.property , value );
					Common.xmlSetElementAttr( doc , element , "desc" , pv.desc );
					Common.xmlSetElementAttr( doc , element , "type" , Common.getEnumLower( DBEnumParamValueType.STRING ) );
				}
			}
		}
	}
	
	public int getSystemRequiredIntProperty( String prop ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , true , true );
		pv.setType( DBEnumParamValueType.NUMBER );
		return( pv.getNumber() );
	}

	public String getSystemRequiredStringProperty( String prop ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , true , true );
		pv.setType( DBEnumParamValueType.STRING );
		return( pv.getString() );
	}

	public String getSystemRequiredPathProperty( String prop , RunContext execrc ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , true , true );
		pv.setType( DBEnumParamValueType.PATH );
		return( pv.getPath( execrc.isWindows() ) );
	}
	
	public String getSystemPathExprProperty( String prop , RunContext execrc , String defaultExpr , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.PATH );
		if( setRequired )
			pv.setRequired();
		if( pv.isFinalEmpty() ) {
			pv.setDefault( defaultExpr );
			recalculateProperty( pv );
		}
		return( pv.getPath( execrc.isWindows() ) );
	}
	
	public String getSystemPathProperty( String prop , RunContext execrc , String defaultValue , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.PATH );
		if( setRequired )
			pv.setRequired();
		pv.setDefault( defaultValue );
		return( pv.getPath( execrc.isWindows() ) );
	}
	
	public String getSystemUrlProperty( String prop , String defaultValue , boolean setRequired ) throws Exception {
		return( getSystemStringProperty( prop , defaultValue , setRequired ) );
	}
	
	public String getSystemUrlExprProperty( String prop , String defaultValue , boolean setRequired ) throws Exception {
		return( getSystemStringExprProperty( prop , defaultValue , setRequired ) );
	}
	
	public String getSystemStringExprProperty( String prop , String defaultExpr , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.STRING );
		if( setRequired )
			pv.setRequired();
		if( pv.isFinalEmpty() ) {
			pv.setDefault( defaultExpr );
			recalculateProperty( pv );
		}
		return( pv.getString() );
	}

	public String getSystemStringProperty( String prop ) throws Exception {
		return( getSystemStringProperty( prop , "" , false ) );
	}

	public String getSystemStringProperty( String prop , String defaultValue , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.STRING );
		if( setRequired )
			pv.setRequired();
		pv.setDefault( defaultValue );
		return( pv.getString() );
	}

	public String getSystemTemplateProperty( String prop ) throws Exception {
		return( getSystemTemplateProperty( prop , "" , false ) );
	}

	public String getSystemTemplateProperty( String prop , String defaultValue , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , false );
		pv.setType( DBEnumParamValueType.STRING );
		if( setRequired )
			pv.setRequired();
		pv.setDefault( defaultValue );
		pv.setTemplated();
		return( pv.getString() );
	}

	public int getSystemIntExprProperty( String prop , String defaultExpr , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.NUMBER );
		if( setRequired )
			pv.setRequired();
		if( pv.isFinalEmpty() ) {
			pv.setDefault( defaultExpr );
			recalculateProperty( pv );
		}
		return( pv.getNumber() );
	}

	public int getSystemIntProperty( String prop , int defaultValue , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.NUMBER );
		if( setRequired )
			pv.setRequired();
		pv.setDefault( "" + defaultValue );
		return( pv.getNumber() );
	}

	public boolean getSystemBooleanExprProperty( String prop , String defaultExpr , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.BOOL );
		if( setRequired )
			pv.setRequired();
		if( pv.isFinalEmpty() ) {
			pv.setDefault( defaultExpr );
			recalculateProperty( pv );
		}
		return( pv.getFinalBool() );
	}

	public FLAG getSystemOptionProperty( String prop , boolean setRequired ) throws Exception {
		return( getSystemOptionProperty( prop , null , setRequired ) );
	}
	
	public FLAG getSystemOptionProperty( String prop , Boolean defaultValue , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.BOOL );
		if( setRequired )
			pv.setRequired();
		if( defaultValue != null )
			pv.setDefault( Common.getBooleanValue( defaultValue ) );
		String value = pv.getFinalValue();
		
		FLAG retval;
		if( value == null || value.isEmpty() )
			retval = FLAG.DEFAULT;
		else {
			if( Common.getBooleanValue( value ) )
				retval = FLAG.YES;
			else
				retval = FLAG.NO;
		}
		
		return( retval );
	}
	
	public boolean getSystemBooleanProperty( String prop ) throws Exception {
		return( getSystemBooleanProperty( prop , false , false ) );
	}
	
	public boolean getSystemBooleanProperty( String prop , boolean defaultValue , boolean setRequired ) throws Exception {
		PropertyValue pv = resolveSystemProperty( prop , false , true );
		pv.setType( DBEnumParamValueType.BOOL );
		if( setRequired )
			pv.setRequired();
		pv.setDefault( Common.getBooleanValue( defaultValue ) );
		return( pv.getFinalBool() );
	}

	public void loadFromNodeAttributes( Node node , boolean custom ) throws Exception {
		Map<String,String> attrs = new HashMap<String,String>();
		ConfReader.addAttributes( node , attrs );
		for( String prop : attrs.keySet() ) {
			String value = attrs.get( prop );
			createOriginalAndRawProperty( prop , value , custom , DBEnumParamValueType.STRING , null );
		}
	}
	
	public void loadFromNodeElements( Node node , boolean custom ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "property" );
		if( items == null )
			return;

		for( Node property : items ) {
			String prop = ConfReader.getAttrValue( property , "name" );
			String value = ConfReader.getAttrValue( property , "value" );
			
			String desc = null;
			DBEnumParamValueType type = DBEnumParamValueType.STRING;
			if( custom ) {
				desc = ConfReader.getAttrValue( property , "desc" );
				type = PropertyValue.getItemValueType( ConfReader.getAttrValue( property , "type" ) , false );
			}
			createOriginalAndRawProperty( prop , value , custom , type , desc );
		}
	}

	public void loadFromPropertyFile( String path , RunContext execrc , boolean custom ) throws Exception {
		Properties props = ConfReader.readPropertyFile( execrc , path );
		for( Object key : props.keySet() ) {
			String prop = ( String )key;
			String value = props.getProperty( ( String )key );
			
			if( value.startsWith( "\"" ) && value.endsWith( "\"" ) )
				value = value.substring( 1 , value.length() - 2 );
			createOriginalAndRawProperty( prop , value , custom , DBEnumParamValueType.STRING , null );
		}
	}
	
	// implementation
	private PropertyValue resolveSystemProperty( String prop , boolean required , boolean checkResolved ) throws Exception {
		PropertyValue pv = getPropertyValue( prop );
		if( required ) {
			if( pv == null )
				Common.exit2( _Error.MissingRequiredProperty2 , "set=" + set + ": missing required property=" + prop , set , prop );
			if( pv.isOriginalEmpty() )
				Common.exit2( _Error.EmptyRequiredProperty2 , "set=" + set + ": empty required property=" + prop , set , prop );
		}
		
		if( pv == null ) {
			pv = new PropertyValue( prop , PropertyValue.PropertyValueOrigin.PROPERTY_ORIGINAL , this , null );
			setProperty( pv );
		}
			
		pv.setSystem();
		if( required )
			pv.setRequired();
		recalculateProperty( pv );
		if( checkResolved && !pv.isResolved() )
			Common.exit3( _Error.UnresolvedVariableValue3 , "set=" + set + ": unresolved variable=" + prop + ", value=" + pv.getFinalValue() , set , prop , pv.getFinalValue() );
		
		if( required ) {
			if( pv.isFinalEmpty() )
				Common.exit2( _Error.EmptyRequiredProperty2 , "set=" + set + ": empty required property=" + prop , set , prop );
		}
		
		return( pv );
	}
	
	private void updateOriginalProperty( String prop , String value ) throws Exception {
		resolved = false;
		PropertyValue pv = getPropertyValue( prop );
		if( pv == null ) {
			pv = new PropertyValue( prop , PropertyValueOrigin.PROPERTY_ORIGINAL , this , null );
			setProperty( pv );
		}
		
		pv.setOriginalAndFinalValue( value );
	}

	private boolean resolveProperty( PropertyValue pv , boolean allowUnresolved ) throws Exception {
		if( pv.isResolved() )
			return( true );
		
		processValue( pv , false , false , true , true , allowUnresolved );
		if( !pv.isResolved() )
			return( false );
			
		return( true );
	}
	
	private PropertyValue setProperty( PropertyValue pv ) {
		resolved = false;
		PropertyValue pvc = getPropertyValue( pv.property );
		if( pvc != null ) {
			pvc.setValue( pv );
			return( pvc );
		}
		
		data.put( getKeyByProperty( pv.property ) , pv );
		return( pv );
	}
	
	private PropertyValue getRunningByKey( String key ) {
		PropertyValue p = data.get( key );
		if( p == null )
			return( null );
		if( p.isResolved() )
			return( p );
		return( null );
	}

	private PropertyValue getRunningByProperty( String prop ) {
		PropertyValue p = data.get( getKeyByProperty( prop ) );
		if( p == null )
			return( null );
		if( p.isResolved() )
			return( p );
		return( null );
	}

	private PropertyValue getRawByKey( String key ) {
		PropertyValue p = data.get( key );
		if( p == null )
			return( null );
		if( !p.isResolved() )
			return( p );
		return( null );
	}
	
	private PropertyValue getRawByProperty( String prop ) {
		PropertyValue p = data.get( getKeyByProperty( prop ) );
		if( p == null )
			return( null );
		if( !p.isResolved() )
			return( p );
		return( null );
	}
	
	private void recalculateProperty( PropertyValue pv ) throws Exception {
		processValue( pv , false , false , true , true , true );
	}
	
	public void createOriginalAndRawProperty( String prop , String value , boolean custom , DBEnumParamValueType type , String desc ) throws Exception {
		resolved = false;
		PropertyValueOrigin origin = ( custom )? PropertyValueOrigin.PROPERTY_CUSTOM : PropertyValueOrigin.PROPERTY_ORIGINAL;
		PropertyValue pv = new PropertyValue( prop , origin , this , desc );
		pv.setType( type );
		pv.setOriginalAndFinalValue( value );
		if( !custom )
			pv.setSystem();
		setProperty( pv );
	}

	private void processValue( PropertyValue pv , boolean finalValue , boolean isWindows , boolean useRaw , boolean allowParent , boolean allowUnresolved ) throws Exception {
		if( pv.isNull() ) {
			if( allowParent && parent != null ) {
				PropertyValue pvVar = parent.getPropertyInternal( pv.property , useRaw , allowParent , allowUnresolved );
				if( pvVar != null && !pvVar.isNull() ) {
					pv.setFinalValue( pvVar.getFinalValue() );
					if( finalValue && pv.getType() == DBEnumParamValueType.PATH )
						pv.setFinalValue( pv.getPath( finalValue , isWindows ) );
					return;
				}
			}
		}
		
		if( pv.isResolved() )
			return;
		
		String value = pv.getFinalValue();
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
				pv.setFinalValue( "@" );
				return;
			}
			
			PropertyValue pvVar;
			if( var.equals( "super" ) && allowParent && parent != null && pv.property.isEmpty() == false )
				pvVar = parent.getPropertyInternal( pv.property , useRaw , allowParent , allowUnresolved );
			else {
				pvVar = getPropertyInternal( var , useRaw , allowParent , allowUnresolved );
				if( pvVar == pv )
					return;
			}
			
			if( pvVar == null )
				return;
			
			pv.setFinalValue( pvVar.getFinalValue() );
			if( finalValue && pv.getType() == DBEnumParamValueType.PATH )
				pv.setFinalValue( pv.getPath( finalValue , isWindows ) );
			return;
		}
		
		// string expression
		String res = value.substring( 0 , indexFrom );
		while( true ) {
			String var = value.substring( indexFrom + 1 , indexTo );
			if( var.isEmpty() )
				res += "@";
			else {
				PropertyValue pvVar;
				if( var.equals( "super" ) && allowParent && parent != null && pv.property.isEmpty() == false )
					pvVar = parent.getPropertyInternal( pv.property , useRaw , allowParent , allowUnresolved );
				else
					pvVar = getPropertyInternal( var , useRaw , allowParent , allowUnresolved );
				
				if( pvVar == null )
					res += "@" + var + "@";
				else {
					String s;
					if( pvVar.getType() == DBEnumParamValueType.PATH )
						s = pvVar.getPath( finalValue , isWindows );
					else
						s = pvVar.getFinalValue();
					res += s;
				}
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
		
		pv.setFinalValue( res );
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
			pv = getRunningByKey( name );
		// unprefixed own var
		if( pv == null )
			pv = getRunningByProperty( name );
		
		if( pv == null || pv.isOriginalEmpty() ) {
			if( parent != null ) {
				// parent var
				if( !allowParent ) {
					if( parent == null )
						Common.exit2( _Error.UnresolvedVariable2 , "set=" + set + ": unresolved variable=" + name , set , name );
				}
				
				PropertyValue pvp = parent.getPropertyInternal( name , useRaw , allowParent , allowUnresolved );
				if( pvp != null )
					return( pvp );
			}
			
			if( !allowUnresolved )
				Common.exit2( _Error.UnresolvedVariable2 , "set=" + set + ": unresolved variable=" + name , set , name );
			
			return( pv );  
		}
		else {
			if( !allowUnresolved ) {
				if( !pv.isResolved() )
					Common.exit3( _Error.UnresolvedVariableValue3 , "set=" + set + ": unresolved variable=" + name + ", value=" + pv.getFinalValue() , set , name , pv.getFinalValue() );
			}
		}
		
		return( pv );
	}

	private PropertyValue findPropertyInternal( String name , PropertyValue defaultValue , boolean system ) throws Exception {
		// prefixed var
		PropertyValue pv = getRunningByKey( name );
		if( pv != null ) {
			if( pv.isFinalEmpty() )
				return( defaultValue );
			return( pv );
		}
		
		// unprefixed var
		pv = getRunningByProperty( name );
		if( pv != null )
			return( pv );
		
		// parent var
		if( parent == null || system )
			return( defaultValue );
			
		return( parent.findPropertyInternal( name , defaultValue , false ) );
	}

	private PropertyValue getRequiredPropertyInternal( String name ) throws Exception {
		PropertyValue pv = getPropertyInternal( name , false , true , false );
		if( pv == null )
			Common.exit2( _Error.MissingRequiredProperty2 , "set=" + set + ": missing property=" + name , set , name );
		if( pv.isOriginalEmpty() )
			Common.exit2( _Error.EmptyRequiredProperty2 , "set=" + set + ": missing property=" + name , set , name );
		return( pv );
	}

	private String getPathPropertyInternal( String name , String defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null ) {
			if( defaultValue == null )
				return( null );
			return( Common.getLinuxPath( defaultValue ) );
		}
		pv.setType( DBEnumParamValueType.PATH );
		pv.setDefault( defaultValue );
		return( pv.getPath( false ) );
	}

	private String getStringPropertyInternal( String name , String defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null )
			return( defaultValue );
		pv.setType( DBEnumParamValueType.STRING );
		pv.setDefault( defaultValue );
		return( pv.getString() );
	}

	private int getIntPropertyInternal( String name , int defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null )
			return( defaultValue );
		pv.setType( DBEnumParamValueType.NUMBER );
		pv.setDefault( "" + defaultValue );
		return( pv.getNumber() );
	}

	private boolean getBooleanPropertyInternal( String name , boolean defaultValue , boolean system ) throws Exception {
		PropertyValue pv = findPropertyInternal( name , null , system );
		if( pv == null )
			return( defaultValue );
		pv.setType( DBEnumParamValueType.BOOL );
		pv.setDefault( Common.getBooleanValue( defaultValue ) );
		return( pv.getFinalBool() );
	}

	public void removeUserProperties() throws Exception {
		resolved = false;
		List<PropertyValue> items = new LinkedList<PropertyValue>();
		for( PropertyValue pv : data.values() ) {
			if( pv.isCustom() )
				items.add( pv );
		}
		for( PropertyValue pv : items )
			data.remove( getKeyByProperty( pv.property ) );
	}

	public void removeUserProperty( String prop ) throws Exception {
		PropertyValue pv = getPropertyValue( prop );
		if( pv == null )
			Common.exit2( _Error.UnknownProperty2 , "set=" + set + ": missing property=" + prop , set , prop );
		
		if( !pv.isCustom() )
			Common.exit2( _Error.NotCustomProperty2 , "set=" + set + ": not a custom property=" + prop , set , prop );
		resolved = false;
		data.remove( getKeyByProperty( pv.property ) );
	}

	public void renameUserProperty( String prop , String newName ) throws Exception {
		PropertyValue pv = getPropertyValue( prop );
		if( pv == null )
			Common.exit2( _Error.UnknownProperty2 , "set=" + set + ": missing property=" + prop , set , prop );

		PropertyValue pvNew = getPropertyValue( newName );
		if( pvNew != null && pvNew != pv )
			Common.exit2( _Error.DuplicateProperty2 , "set=" + set + ": duplicate property=" + newName , set , newName );
		
		resolved = false;
		data.remove( getKeyByProperty( prop ) );
		pv.setName( newName );
		data.put( getKeyByProperty( newName ) , pv );
		recalculateProperties();
	}
	
	public static String getRef( String name ) {
		return( "@" + name + "@" );
	}
	
}
