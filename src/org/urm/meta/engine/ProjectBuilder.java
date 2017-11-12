package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnumTypes.*;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.meta.EngineObject;
import org.urm.meta.Types;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ProjectBuilder extends EngineObject {

	public EngineBuilders builders;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	// fixed
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumBuilderMethodType BUILDER_METHOD_TYPE;
	public String VERSION;
	
	public boolean REMOTE;
	public VarOSTYPE REMOTE_OS_TYPE;
	public String REMOTE_HOSTLOGIN;
	public int REMOTE_PORT;
	public String REMOTE_AUTHRESOURCE;
	
	public DBEnumBuilderTargetType BUILDER_TARGET_TYPE;
	public String TARGET_RESOURCE;
	public String TARGET_PATH;
	public String TARGET_PLATFORM;
	
	public String GENERIC_COMMAND;
	public String JAVA_JDKHOMEPATH;
	public String ANT_HOMEPATH;
	public String MAVEN_HOMEPATH;
	public String MAVEN_COMMAND;
	public String MAVEN_OPTIONS;
	public String GRADLE_HOMEPATH;
	public String MSBUILD_HOMEPATH;
	public String MSBUILD_OPTIONS;

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_VERSION = "version";
	public static String PROPERTY_BUILDERTYPE = "buildertype";
	public static String PROPERTY_REMOTE = "remote";
	public static String PROPERTY_OSTYPE = "ostype";
	public static String PROPERTY_HOSTLOGIN = "hostlogin";
	public static String PROPERTY_PORT = "port";
	public static String PROPERTY_AUTHRESOURCE = "authresource";
	
	public static String PROPERTY_TARGETTYPE = "target.type";
	public static String PROPERTY_TARGETLOCALPATH = "target.localpath";
	public static String PROPERTY_TARGETRESOURCE = "target.resource";
	public static String PROPERTY_TARGETPLATFORM = "target.platform";
	
	public static String PROPERTY_GENERIC_COMMAND = "generic.command";
	public static String PROPERTY_JAVA_JDKHOMEPATH = "java.jdkhomepath";
	public static String PROPERTY_ANT_HOMEPATH = "ant.homepath";
	public static String PROPERTY_MAVEN_HOMEPATH = "maven.homepath";
	public static String PROPERTY_MAVEN_COMMAND = "maven.command";
	public static String PROPERTY_MAVEN_OPTIONS = "maven.options";
	public static String PROPERTY_GRADLE_HOMEPATH = "gradle.home";
	public static String PROPERTY_MSBUILD_HOMEPATH = "msbuild.home";
	public static String PROPERTY_MSBUILD_OPTIONS = "msbuild.options";
	
	public ProjectBuilder( EngineBuilders builders ) {
		super( builders );
		this.builders = builders;
		loaded = false;
		loadFailed = false;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public ProjectBuilder copy( EngineBuilders builders ) throws Exception {
		ProjectBuilder r = new ProjectBuilder( builders );
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
		NAME = properties.getSystemRequiredStringProperty( PROPERTY_NAME );
		DESC = properties.getSystemStringProperty( PROPERTY_DESC );
		BUILDER_METHOD_TYPE = DBEnumBuilderMethodType.getValue( properties.getSystemRequiredStringProperty( PROPERTY_BUILDERTYPE ) , true );
		REMOTE = properties.getSystemBooleanProperty( PROPERTY_REMOTE );
		
		REMOTE_HOSTLOGIN = "";
		REMOTE_PORT = 0;
		REMOTE_AUTHRESOURCE = "";
		if( REMOTE ) {
			REMOTE_OS_TYPE = Types.getOSType( properties.getSystemStringProperty( PROPERTY_OSTYPE ) , false );
			REMOTE_HOSTLOGIN = properties.getSystemStringProperty( PROPERTY_HOSTLOGIN );
			REMOTE_PORT = properties.getSystemIntProperty( PROPERTY_PORT , 22 , false );
			REMOTE_AUTHRESOURCE = properties.getSystemStringProperty( PROPERTY_AUTHRESOURCE );
		}
		
		BUILDER_TARGET_TYPE = DBEnumBuilderTargetType.getValue( properties.getSystemStringProperty( PROPERTY_TARGETTYPE ) , false );
		
		TARGET_PATH = "";
		TARGET_RESOURCE = "";
		TARGET_PLATFORM = "";
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.LOCALPATH )
			TARGET_PATH = properties.getSystemStringProperty( PROPERTY_TARGETLOCALPATH );
		else
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.NEXUS ) {
			TARGET_RESOURCE = properties.getSystemStringProperty( PROPERTY_TARGETRESOURCE );
			TARGET_PLATFORM = properties.getSystemStringProperty( PROPERTY_TARGETPLATFORM );
		}

		GENERIC_COMMAND = "";
		JAVA_JDKHOMEPATH = "";
		ANT_HOMEPATH = "";
		MAVEN_HOMEPATH = "";
		MAVEN_COMMAND = "";
		MAVEN_OPTIONS = "";
		GRADLE_HOMEPATH = "";
		MSBUILD_HOMEPATH = "";
		MSBUILD_OPTIONS = "";
		VERSION = "";
		
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GENERIC )
			GENERIC_COMMAND = properties.getSystemTemplateProperty( PROPERTY_GENERIC_COMMAND );
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT )
			ANT_HOMEPATH = properties.getSystemStringProperty( PROPERTY_ANT_HOMEPATH );
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN ) {
			MAVEN_HOMEPATH = properties.getSystemStringProperty( PROPERTY_MAVEN_HOMEPATH );
			MAVEN_COMMAND = properties.getSystemTemplateProperty( PROPERTY_MAVEN_COMMAND );
			MAVEN_OPTIONS = properties.getSystemTemplateProperty( PROPERTY_MAVEN_OPTIONS );
		}
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE )
			GRADLE_HOMEPATH = properties.getSystemStringProperty( PROPERTY_GRADLE_HOMEPATH );
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MSBUILD ) {
			MSBUILD_HOMEPATH = properties.getSystemStringProperty( PROPERTY_MSBUILD_HOMEPATH );
			MSBUILD_OPTIONS = properties.getSystemTemplateProperty( PROPERTY_MSBUILD_OPTIONS );
		}
		
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE )
			JAVA_JDKHOMEPATH = properties.getSystemStringProperty( PROPERTY_JAVA_JDKHOMEPATH );
		
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MSBUILD )
			VERSION = properties.getSystemRequiredStringProperty( PROPERTY_VERSION );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "builder" , null );
		properties.setOriginalStringProperty( PROPERTY_NAME , NAME );
		properties.setOriginalStringProperty( PROPERTY_DESC , DESC );
		properties.setOriginalStringProperty( PROPERTY_VERSION , VERSION );
		properties.setOriginalStringProperty( PROPERTY_BUILDERTYPE , Common.getEnumLower( BUILDER_METHOD_TYPE ) );
		properties.setOriginalBooleanProperty( PROPERTY_REMOTE , REMOTE );
		if( REMOTE ) {
			properties.setOriginalStringProperty( PROPERTY_OSTYPE , Common.getEnumLower( REMOTE_OS_TYPE ) );
			properties.setOriginalStringProperty( PROPERTY_HOSTLOGIN , REMOTE_HOSTLOGIN );
			properties.setOriginalNumberProperty( PROPERTY_PORT , REMOTE_PORT );
			properties.setOriginalStringProperty( PROPERTY_AUTHRESOURCE , REMOTE_AUTHRESOURCE );
		}
		
		properties.setOriginalStringProperty( PROPERTY_TARGETTYPE , Common.getEnumLower( BUILDER_TARGET_TYPE ) );
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.LOCALPATH )
			properties.setOriginalStringProperty( PROPERTY_TARGETLOCALPATH , TARGET_PATH );
		else
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.NEXUS ) {
			properties.setOriginalStringProperty( PROPERTY_TARGETRESOURCE , TARGET_RESOURCE );
			properties.setOriginalStringProperty( PROPERTY_TARGETPLATFORM , TARGET_PLATFORM );
		}
		
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GENERIC )
			properties.setOriginalStringProperty( PROPERTY_GENERIC_COMMAND , GENERIC_COMMAND );
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT )
			properties.setOriginalStringProperty( PROPERTY_ANT_HOMEPATH , ANT_HOMEPATH );
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN ) {
			properties.setOriginalStringProperty( PROPERTY_MAVEN_HOMEPATH , MAVEN_HOMEPATH );
			properties.setOriginalStringProperty( PROPERTY_MAVEN_COMMAND , MAVEN_COMMAND );
			properties.setOriginalStringProperty( PROPERTY_MAVEN_OPTIONS , MAVEN_OPTIONS );
		}
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE )
			properties.setOriginalStringProperty( PROPERTY_GRADLE_HOMEPATH , GRADLE_HOMEPATH );
		else
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MSBUILD ) {
			properties.setOriginalStringProperty( PROPERTY_MSBUILD_HOMEPATH , MSBUILD_HOMEPATH );
			properties.setOriginalStringProperty( PROPERTY_MSBUILD_OPTIONS , MSBUILD_OPTIONS );
		}

		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE )
			properties.setOriginalStringProperty( PROPERTY_JAVA_JDKHOMEPATH , JAVA_JDKHOMEPATH );
	}

	public boolean isGeneric() {
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GENERIC )
			return( true );
		return( false );
	}
	
	public boolean isAnt() {
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT )
			return( true );
		return( false );
	}
	
	public boolean isMaven() {
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN )
			return( true );
		return( false );
	}
	
	public boolean isGradle() {
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE )
			return( true );
		return( false );
	}

	public boolean isWinBuild() {
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MSBUILD )
			return( true );
		return( false );
	}

	public boolean isTargetLocal() {
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.LOCALPATH )
			return( true );
		return( false );
	}
	
	public boolean isTargetNexus() {
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.NEXUS )
			return( true );
		return( false );
	}
	
	public boolean isTargetNuget() {
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.NUGET )
			return( true );
		return( false );
	}
	
	public void setBuilderData( EngineTransaction transaction , ProjectBuilder src ) throws Exception {
		NAME = src.NAME;
		DESC = src.DESC;
		VERSION = src.VERSION;
		BUILDER_METHOD_TYPE = src.BUILDER_METHOD_TYPE;
		BUILDER_TARGET_TYPE = src.BUILDER_TARGET_TYPE;
		REMOTE = src.REMOTE;
		
		if( REMOTE ) {
			REMOTE_OS_TYPE = src.REMOTE_OS_TYPE;
			REMOTE_HOSTLOGIN = src.REMOTE_HOSTLOGIN;
			REMOTE_PORT = src.REMOTE_PORT;
			REMOTE_AUTHRESOURCE = src.REMOTE_AUTHRESOURCE;
		}
		else {
			REMOTE_OS_TYPE = VarOSTYPE.LINUX;
			REMOTE_HOSTLOGIN = "";
			REMOTE_AUTHRESOURCE = "";
		}

		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.LOCALPATH )
			TARGET_PATH = src.TARGET_PATH;
		else
			TARGET_PATH = "";
		
		if( BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.NEXUS ||
			BUILDER_TARGET_TYPE == DBEnumBuilderTargetType.NUGET ) {
			TARGET_RESOURCE = src.TARGET_RESOURCE;
			TARGET_PLATFORM = src.TARGET_PLATFORM;
		}
		else {
			TARGET_RESOURCE = "";
			TARGET_PLATFORM = "";
		}

		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GENERIC )
			GENERIC_COMMAND = src.GENERIC_COMMAND;

		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT )
			ANT_HOMEPATH = src.ANT_HOMEPATH;
		else
			ANT_HOMEPATH = "";
		
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN ) {
			MAVEN_HOMEPATH = src.MAVEN_HOMEPATH;
			MAVEN_COMMAND = src.MAVEN_COMMAND;
			MAVEN_OPTIONS = src.MAVEN_OPTIONS;
		}
		else {
			MAVEN_HOMEPATH = "";
			MAVEN_COMMAND = "";
			MAVEN_OPTIONS = "";
		}

		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE )
			GRADLE_HOMEPATH = src.GRADLE_HOMEPATH;
		else
			GRADLE_HOMEPATH = "";
		
		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MSBUILD ) {
			MSBUILD_HOMEPATH = src.MSBUILD_HOMEPATH;
			MSBUILD_OPTIONS = src.MSBUILD_OPTIONS;
		}
		else {
			MSBUILD_HOMEPATH = "";
			MSBUILD_OPTIONS = "";
		}

		if( BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.ANT || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.MAVEN || BUILDER_METHOD_TYPE == DBEnumBuilderMethodType.GRADLE )
			JAVA_JDKHOMEPATH = src.JAVA_JDKHOMEPATH;
		else
			JAVA_JDKHOMEPATH = "";

		createProperties();
	}
	
	public Account getRemoteAccount( ActionBase action ) throws Exception {
		if( !REMOTE )
			return( action.getLocalAccount() );
		return( Account.getResourceAccount( action , REMOTE_AUTHRESOURCE , REMOTE_HOSTLOGIN , REMOTE_PORT , REMOTE_OS_TYPE ) );
	}

	public ShellExecutor createShell( ActionBase action , boolean dedicated ) throws Exception {
		if( REMOTE ) {
			Account account = getRemoteAccount( action );
			if( dedicated )
				return( action.createDedicatedRemoteShell( "builder" , account , true ) );
			
			return( action.getShell( account ) );
		}
		
		if( dedicated )
			return( action.createDedicatedShell( "builder" ) );
		
		return( action.shell );
	}
	
}
