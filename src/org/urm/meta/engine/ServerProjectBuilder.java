package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.ServerObject;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerProjectBuilder extends ServerObject {

	public ServerBuilders builders;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String DESC;
	public VarBUILDERLANG languageType;
	public VarBUILDERTYPE builderType;
	public VarOSTYPE osType;
	public String HOSTLOGIN;
	public String AUTHRESOURCE;
	public VarBUILDERTARGET targetType;
	public String TARGETLOCALPATH;
	
	public String JAVA_JDKPATH;
	public String ANT_INSTALLPATH;
	public String MAVEN_HOMEPATH;
	public String MAVEN_COMMAND;
	public String MAVEN_OPTIONS;
	public String NEXUS_RESOURCE;

	public static String PROPERTY_NAME;
	public static String PROPERTY_DESC;
	public static String PROPERTY_LANGUAGETYPE;
	public static String PROPERTY_BUILDERTYPE;
	public static String PROPERTY_OSTYPE;
	public static String PROPERTY_HOSTLOGIN;
	public static String PROPERTY_AUTHRESOURCE;
	public static String PROPERTY_TARGETTYPE;
	public static String PROPERTY_TARGETLOCALPATH;
	
	public static String PROPERTY_JAVA_JDKHOMEPATH;
	public static String PROPERTY_ANT_HOMEPATH;
	public static String PROPERTY_MAVEN_INSTALLPATH;
	public static String PROPERTY_MAVEN_COMMAND;
	public static String PROPERTY_MAVEN_OPTIONS;
	public static String PROPERTY_NEXUS_RESOURCE;
	
	public ServerProjectBuilder( ServerBuilders builders ) {
		super( builders );
		this.builders = builders;
		loaded = false;
		loadFailed = false;
	}

	public ServerProjectBuilder copy( ServerBuilders builders ) throws Exception {
		ServerProjectBuilder r = new ServerProjectBuilder( builders );
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "builder" , null );
		properties.loadFromNodeElements( node , false );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}
	
	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root , false );
	}
	
	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		DESC = properties.getSystemStringProperty( "desc" );
		languageType = Types.getBuilderLanguage( properties.getSystemRequiredStringProperty( "langtype" ) , true );
		builderType = Types.getBuilderType( properties.getSystemRequiredStringProperty( "buildertype" ) , true );
		osType = Types.getOSType( properties.getSystemStringProperty( "ostype" ) , false );
		HOSTLOGIN = properties.getSystemStringProperty( "hostlogin" );
		AUTHRESOURCE = properties.getSystemStringProperty( "authresource" );
		targetType = Types.getBuilderTarget( properties.getSystemStringProperty( "targettype" ) , false );
		TARGETLOCALPATH = properties.getSystemStringProperty( "targetlocalpath" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "builder" , null );
		properties.setOriginalStringProperty( "name" , NAME );
		properties.setOriginalStringProperty( "type" , TYPE );
		properties.setOriginalStringProperty( "hostlogin" , HOSTLOGIN );
		properties.setOriginalStringProperty( "desc" , DESC );
		properties.setOriginalStringProperty( "ostype" , OSTYPE );
	}

	public boolean isMaven() {
		if( TYPE.equals( BUILDER_TYPE_MAVEN ) )
			return( true );
		return( false );
	}
	
	public boolean isGradle() {
		if( TYPE.equals( BUILDER_TYPE_GRADLE ) )
			return( true );
		return( false );
	}

	public boolean isDotNet() {
		if( TYPE.equals( BUILDER_TYPE_DOTNET ) )
			return( true );
		return( false );
	}

	public void updateBuilder( ServerTransaction transaction , ServerProjectBuilder src ) throws Exception {
		if( !NAME.equals( src.NAME ) )
			transaction.exit( _Error.TransactionBuilderOld1 , "mismatched buider name on change new name=" + src.NAME , new String[] { src.NAME } );
		
		TYPE = src.TYPE;
		HOSTLOGIN = src.HOSTLOGIN;
		DESC = src.DESC;
		OSTYPE = src.OSTYPE;
		
		createProperties();
	}
	
	public void createBuilder() throws Exception {
		createProperties();
	}

	public Account getAccount( ActionBase action ) throws Exception {
		VarOSTYPE osType = VarOSTYPE.valueOf( OSTYPE );
		return( Account.getAccount( action , "" , HOSTLOGIN , osType ) );
	}
	
}
