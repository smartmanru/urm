package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
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
	public VarBUILDERTYPE builderMethod;
	public String VERSION;
	public boolean remote;
	public VarOSTYPE osType;
	public String HOSTLOGIN;
	public int port;
	public String AUTHRESOURCE;
	public VarBUILDERTARGET targetType;
	public String TARGETLOCALPATH;
	public String TARGETNEXUS;
	public boolean targetNuget;
	public String TARGETNUGETPLATFORM;
	
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
	public static String PROPERTY_TARGETNEXUS = "target.nexus";
	public static String PROPERTY_TARGETNUGET = "target.nuget";
	public static String PROPERTY_TARGETNUGETPLATFORM = "target.nugetplatform";
	
	public static String PROPERTY_GENERIC_COMMAND = "generic.command";
	public static String PROPERTY_JAVA_JDKHOMEPATH = "java.jdkhomepath";
	public static String PROPERTY_ANT_HOMEPATH = "ant.homepath";
	public static String PROPERTY_MAVEN_HOMEPATH = "maven.homepath";
	public static String PROPERTY_MAVEN_COMMAND = "maven.command";
	public static String PROPERTY_MAVEN_OPTIONS = "maven.options";
	public static String PROPERTY_GRADLE_HOMEPATH = "gradle.home";
	public static String PROPERTY_MSBUILD_HOMEPATH = "msbuild.home";
	public static String PROPERTY_MSBUILD_OPTIONS = "msbuild.options";
	
	public ServerProjectBuilder( ServerBuilders builders ) {
		super( builders );
		this.builders = builders;
		loaded = false;
		loadFailed = false;
	}

	@Override
	public String getName() {
		return( NAME );
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
		NAME = properties.getSystemRequiredStringProperty( PROPERTY_NAME );
		DESC = properties.getSystemStringProperty( PROPERTY_DESC );
		builderMethod = Types.getBuilderType( properties.getSystemRequiredStringProperty( PROPERTY_BUILDERTYPE ) , true );
		remote = properties.getSystemBooleanProperty( PROPERTY_REMOTE );
		
		HOSTLOGIN = "";
		port = 0;
		AUTHRESOURCE = "";
		if( remote ) {
			osType = Types.getOSType( properties.getSystemStringProperty( PROPERTY_OSTYPE ) , false );
			HOSTLOGIN = properties.getSystemStringProperty( PROPERTY_HOSTLOGIN );
			port = properties.getSystemIntProperty( PROPERTY_PORT , 22 , false );
			AUTHRESOURCE = properties.getSystemStringProperty( PROPERTY_AUTHRESOURCE );
		}
		
		targetType = Types.getBuilderTarget( properties.getSystemStringProperty( PROPERTY_TARGETTYPE ) , false );
		
		TARGETLOCALPATH = "";
		TARGETNEXUS = "";
		targetNuget = false;
		TARGETNUGETPLATFORM = "";
		if( targetType == VarBUILDERTARGET.LOCALPATH )
			TARGETLOCALPATH = properties.getSystemStringProperty( PROPERTY_TARGETLOCALPATH );
		else
		if( targetType == VarBUILDERTARGET.NEXUS ) {
			TARGETNEXUS = properties.getSystemStringProperty( PROPERTY_TARGETNEXUS );
			targetNuget = properties.getSystemBooleanProperty( PROPERTY_TARGETNUGET );
			TARGETNUGETPLATFORM = properties.getSystemStringProperty( PROPERTY_TARGETNUGETPLATFORM );
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
		
		if( builderMethod == VarBUILDERTYPE.GENERIC )
			GENERIC_COMMAND = properties.getSystemStringProperty( PROPERTY_GENERIC_COMMAND );
		else
		if( builderMethod == VarBUILDERTYPE.ANT )
			ANT_HOMEPATH = properties.getSystemStringProperty( PROPERTY_ANT_HOMEPATH );
		else
		if( builderMethod == VarBUILDERTYPE.MAVEN ) {
			MAVEN_HOMEPATH = properties.getSystemStringProperty( PROPERTY_MAVEN_HOMEPATH );
			MAVEN_COMMAND = properties.getSystemStringProperty( PROPERTY_MAVEN_COMMAND );
			MAVEN_OPTIONS = properties.getSystemStringProperty( PROPERTY_MAVEN_OPTIONS );
		}
		else
		if( builderMethod == VarBUILDERTYPE.GRADLE )
			GRADLE_HOMEPATH = properties.getSystemStringProperty( PROPERTY_GRADLE_HOMEPATH );
		else
		if( builderMethod == VarBUILDERTYPE.MSBUILD ) {
			MSBUILD_HOMEPATH = properties.getSystemStringProperty( PROPERTY_MSBUILD_HOMEPATH );
			MSBUILD_OPTIONS = properties.getSystemStringProperty( PROPERTY_MSBUILD_OPTIONS );
		}
		
		if( builderMethod == VarBUILDERTYPE.ANT || builderMethod == VarBUILDERTYPE.MAVEN || builderMethod == VarBUILDERTYPE.GRADLE )
			JAVA_JDKHOMEPATH = properties.getSystemStringProperty( PROPERTY_JAVA_JDKHOMEPATH );
		
		if( builderMethod == VarBUILDERTYPE.ANT || builderMethod == VarBUILDERTYPE.MAVEN || builderMethod == VarBUILDERTYPE.GRADLE || builderMethod == VarBUILDERTYPE.MSBUILD )
			VERSION = properties.getSystemRequiredStringProperty( PROPERTY_VERSION );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "builder" , null );
		properties.setOriginalStringProperty( PROPERTY_NAME , NAME );
		properties.setOriginalStringProperty( PROPERTY_DESC , DESC );
		properties.setOriginalStringProperty( PROPERTY_VERSION , VERSION );
		properties.setOriginalStringProperty( PROPERTY_BUILDERTYPE , Common.getEnumLower( builderMethod ) );
		properties.setOriginalBooleanProperty( PROPERTY_REMOTE , remote );
		if( remote ) {
			properties.setOriginalStringProperty( PROPERTY_OSTYPE , Common.getEnumLower( osType ) );
			properties.setOriginalStringProperty( PROPERTY_HOSTLOGIN , HOSTLOGIN );
			properties.setOriginalNumberProperty( PROPERTY_PORT , port );
			properties.setOriginalStringProperty( PROPERTY_AUTHRESOURCE , AUTHRESOURCE );
		}
		
		properties.setOriginalStringProperty( PROPERTY_TARGETTYPE , Common.getEnumLower( targetType ) );
		if( targetType == VarBUILDERTARGET.LOCALPATH )
			properties.setOriginalStringProperty( PROPERTY_TARGETLOCALPATH , TARGETLOCALPATH );
		else
		if( targetType == VarBUILDERTARGET.NEXUS ) {
			properties.setOriginalStringProperty( PROPERTY_TARGETNEXUS , TARGETNEXUS );
			properties.setOriginalBooleanProperty( PROPERTY_TARGETNUGET , targetNuget );
			properties.setOriginalStringProperty( PROPERTY_TARGETNUGETPLATFORM , TARGETNUGETPLATFORM );
		}
		
		if( builderMethod == VarBUILDERTYPE.GENERIC )
			properties.setOriginalStringProperty( PROPERTY_GENERIC_COMMAND , GENERIC_COMMAND );
		else
		if( builderMethod == VarBUILDERTYPE.ANT )
			properties.setOriginalStringProperty( PROPERTY_ANT_HOMEPATH , ANT_HOMEPATH );
		else
		if( builderMethod == VarBUILDERTYPE.MAVEN ) {
			properties.setOriginalStringProperty( PROPERTY_MAVEN_HOMEPATH , MAVEN_HOMEPATH );
			properties.setOriginalStringProperty( PROPERTY_MAVEN_COMMAND , MAVEN_COMMAND );
			properties.setOriginalStringProperty( PROPERTY_MAVEN_OPTIONS , MAVEN_OPTIONS );
		}
		else
		if( builderMethod == VarBUILDERTYPE.GRADLE )
			properties.setOriginalStringProperty( PROPERTY_GRADLE_HOMEPATH , GRADLE_HOMEPATH );
		else
		if( builderMethod == VarBUILDERTYPE.MSBUILD ) {
			properties.setOriginalStringProperty( PROPERTY_MSBUILD_HOMEPATH , MSBUILD_HOMEPATH );
			properties.setOriginalStringProperty( PROPERTY_MSBUILD_OPTIONS , MSBUILD_OPTIONS );
		}

		if( builderMethod == VarBUILDERTYPE.ANT || builderMethod == VarBUILDERTYPE.MAVEN || builderMethod == VarBUILDERTYPE.GRADLE )
			properties.setOriginalStringProperty( PROPERTY_JAVA_JDKHOMEPATH , JAVA_JDKHOMEPATH );
	}

	public boolean isGeneric() {
		if( builderMethod == VarBUILDERTYPE.GENERIC )
			return( true );
		return( false );
	}
	
	public boolean isAnt() {
		if( builderMethod == VarBUILDERTYPE.ANT )
			return( true );
		return( false );
	}
	
	public boolean isMaven() {
		if( builderMethod == VarBUILDERTYPE.MAVEN )
			return( true );
		return( false );
	}
	
	public boolean isGradle() {
		if( builderMethod == VarBUILDERTYPE.GRADLE )
			return( true );
		return( false );
	}

	public boolean isWinBuild() {
		if( builderMethod == VarBUILDERTYPE.MSBUILD )
			return( true );
		return( false );
	}

	public boolean isTargetLocal() {
		if( targetType == VarBUILDERTARGET.LOCALPATH )
			return( true );
		return( false );
	}
	
	public boolean isTargetNexus() {
		if( targetType == VarBUILDERTARGET.NEXUS )
			return( true );
		return( false );
	}
	
	public boolean isTargetNuget() {
		if( targetNuget )
			return( true );
		return( false );
	}
	
	public void setBuilderData( ServerTransaction transaction , ServerProjectBuilder src ) throws Exception {
		NAME = src.NAME;
		DESC = src.DESC;
		VERSION = src.VERSION;
		builderMethod = src.builderMethod;
		targetType = src.targetType;
		remote = src.remote;
		
		if( remote ) {
			osType = src.osType;
			HOSTLOGIN = src.HOSTLOGIN;
			port = src.port;
			AUTHRESOURCE = src.AUTHRESOURCE;
		}
		else {
			osType = VarOSTYPE.LINUX;
			HOSTLOGIN = "";
			AUTHRESOURCE = "";
		}

		if( targetType == VarBUILDERTARGET.LOCALPATH )
			TARGETLOCALPATH = src.TARGETLOCALPATH;
		else
			TARGETLOCALPATH = "";
		
		if( targetType == VarBUILDERTARGET.NEXUS ) {
			TARGETNEXUS = src.TARGETNEXUS;
			targetNuget = src.targetNuget;
			TARGETNUGETPLATFORM = src.TARGETNUGETPLATFORM;
		}
		else {
			TARGETNEXUS = "";
			targetNuget = false;
			TARGETNUGETPLATFORM = "";
		}

		if( builderMethod == VarBUILDERTYPE.GENERIC )
			GENERIC_COMMAND = src.GENERIC_COMMAND;

		if( builderMethod == VarBUILDERTYPE.ANT )
			ANT_HOMEPATH = src.ANT_HOMEPATH;
		else
			ANT_HOMEPATH = "";
		
		if( builderMethod == VarBUILDERTYPE.MAVEN ) {
			MAVEN_HOMEPATH = src.MAVEN_HOMEPATH;
			MAVEN_COMMAND = src.MAVEN_COMMAND;
			MAVEN_OPTIONS = src.MAVEN_OPTIONS;
		}
		else {
			MAVEN_HOMEPATH = "";
			MAVEN_COMMAND = "";
			MAVEN_OPTIONS = "";
		}

		if( builderMethod == VarBUILDERTYPE.GRADLE )
			GRADLE_HOMEPATH = src.GRADLE_HOMEPATH;
		else
			GRADLE_HOMEPATH = "";
		
		if( builderMethod == VarBUILDERTYPE.MSBUILD ) {
			MSBUILD_HOMEPATH = src.MSBUILD_HOMEPATH;
			MSBUILD_OPTIONS = src.MSBUILD_OPTIONS;
		}
		else {
			MSBUILD_HOMEPATH = "";
			MSBUILD_OPTIONS = "";
		}

		if( builderMethod == VarBUILDERTYPE.ANT || builderMethod == VarBUILDERTYPE.MAVEN || builderMethod == VarBUILDERTYPE.GRADLE )
			JAVA_JDKHOMEPATH = src.JAVA_JDKHOMEPATH;
		else
			JAVA_JDKHOMEPATH = "";

		createProperties();
	}
	
	public Account getRemoteAccount( ActionBase action ) throws Exception {
		if( !remote )
			return( action.getLocalAccount() );
		return( Account.getResourceAccount( action , AUTHRESOURCE , HOSTLOGIN , port , osType ) );
	}

	public ShellExecutor createShell( ActionBase action , boolean dedicated ) throws Exception {
		if( remote ) {
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
