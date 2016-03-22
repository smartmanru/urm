package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;

public class MetaFapBase {

	public enum VarBASESRCTYPE {
		LINUX_ARCHIVE_LINK ,
		LINUX_ARCHIVE_DIRECT
	};
	
	public enum VarBASESRCFORMAT {
		TARGZ_SINGLEDIR
	};
	
	public PropertySet properties;
	public String ID;
	public VarBASESRCTYPE type;
	public boolean adm;

	public VarBASESRCFORMAT srcFormat;
	public String SRCFILE;
	public String SRCSTOREDIR;
	public String INSTALLPATH;
	public String INSTALLLINK;
	
	public List<String> dependencies;
	public Map<String,String> compatibilityMap;
	
	public MetaFapBase() {
	}
	
	public VarBASESRCTYPE getType( ActionBase action , String TYPE ) throws Exception {
		VarBASESRCTYPE value = null;		
		
		try {
			value = VarBASESRCTYPE.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid basesrctype=" + TYPE );
		}
		
		if( value == null )
			action.exit( "unknown basesrctype=" + TYPE );
		
		return( value );
	}

	public VarBASESRCFORMAT getSrcFormat( ActionBase action , String TYPE ) throws Exception {
		VarBASESRCFORMAT value = null;		
		
		try {
			value = VarBASESRCFORMAT.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid basesrcformat=" + TYPE );
		}
		
		if( value == null )
			action.exit( "unknown basesrcformat=" + TYPE );
		
		return( value );
	}

	public void load( ActionBase action , Node node , PropertySet parentProperties ) throws Exception {
		ID = ConfReader.getRequiredAttrValue( action , node , "id" );
		type = getType( action , ConfReader.getRequiredAttrValue( action , node , "type" ) );
		adm = ConfReader.getBooleanAttrValue( action , node , "adminstall" , false );
		
		properties = new PropertySet( "fapbase" , parentProperties );
		properties.loadFromElements( action , node );
		if( action.isDebug() )
			properties.printValues( action );
		
		scatterVariables( action );
		
		loadCompatibility( action , node );
		loadDependencies( action , node );
	}

	private void loadCompatibility( ActionBase action , Node node ) throws Exception {
		compatibilityMap = new HashMap<String,String>();
		Node comp = ConfReader.xmlGetFirstChild( action , node , "compatibility" );
		if( comp == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( action , comp , "os" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			String TYPE = ConfReader.getRequiredAttrValue( action , snnode , "type" );
			String VERSION = ConfReader.getRequiredAttrValue( action , snnode , "version" );
			if( compatibilityMap.get( TYPE ) != null )
				action.exit( "unexpected duplicate type=" + TYPE );
			
			compatibilityMap.put( TYPE , VERSION );
		}
	}
	
	private void loadDependencies( ActionBase action , Node node ) throws Exception {
		dependencies = new LinkedList<String>();
		Node deps = ConfReader.xmlGetFirstChild( action , node , "dependencies" );
		if( deps == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( action , deps , "base" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			String BASEID = ConfReader.getRequiredAttrValue( action , snnode , "id" );
			dependencies.add( BASEID );
		}
	}

	public boolean isLinuxArchiveLink() {
		if( type == VarBASESRCTYPE.LINUX_ARCHIVE_LINK )
			return( true );
		return( false );
	}
	
	public boolean isLinuxArchiveDirect() {
		if( type == VarBASESRCTYPE.LINUX_ARCHIVE_DIRECT )
			return( true );
		return( false );
	}
	
	private void scatterVariables( ActionBase action ) throws Exception {
		List<String> systemProps = new LinkedList<String>();
		if( isLinuxArchiveLink() )
			scatterLinuxArchiveLink( action , systemProps );
		else
		if( isLinuxArchiveDirect() )
			scatterLinuxArchiveDirect( action , systemProps );
		else
			action.exitUnexpectedState();
		
		properties.checkUnexpected( action , systemProps );
	}

	private void scatterLinuxArchiveLink( ActionBase action , List<String> systemProps ) throws Exception {
		srcFormat = getSrcFormat( action , properties.getSystemRequiredProperty( action , "srcformat" , systemProps ) );
		SRCFILE = properties.getSystemRequiredProperty( action , "srcfile" , systemProps );
		SRCSTOREDIR = properties.getSystemRequiredProperty( action , "srcstoreddir" , systemProps );
		INSTALLPATH = properties.getSystemRequiredProperty( action , "installpath" , systemProps );
		INSTALLLINK = properties.getSystemRequiredProperty( action , "installlink" , systemProps );
	}
	
	private void scatterLinuxArchiveDirect( ActionBase action , List<String> systemProps ) throws Exception {
		srcFormat = getSrcFormat( action , properties.getSystemRequiredProperty( action , "srcformat" , systemProps ) );
		SRCFILE = properties.getSystemRequiredProperty( action , "srcfile" , systemProps );
		SRCSTOREDIR = properties.getSystemRequiredProperty( action , "srcstoreddir" , systemProps );
		INSTALLPATH = properties.getSystemRequiredProperty( action , "installpath" , systemProps );
	}
	
}
