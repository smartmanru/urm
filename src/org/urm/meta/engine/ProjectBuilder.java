package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.meta.EngineObject;

public class ProjectBuilder extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_VERSION = "version";
	
	public static String PROPERTY_BUILDERTYPE = "buildertype";
	public static String PROPERTY_TARGETTYPE = "target.type";
	
	public static String PROPERTY_TARGETPATH = "target.path";
	public static String PROPERTY_TARGETRESOURCE = "target.resource";
	public static String PROPERTY_TARGETPLATFORM = "target.platform";
	
	public static String PROPERTY_BUILDER_COMMAND = "builder.command";
	public static String PROPERTY_BUILDER_HOMEPATH = "builder.homepath";
	public static String PROPERTY_BUILDER_OPTIONS = "builder.options";
	public static String PROPERTY_JAVA_JDKHOMEPATH = "java.jdkhomepath";
	
	public static String PROPERTY_BUILDER_REMOTE = "remote";
	public static String PROPERTY_REMOTEHOSTLOGIN = "hostlogin";
	
	public EngineBuilders builders;

	// fixed
	public int ID;
	public String NAME;
	public String DESC;
	public String VERSION;
	
	public DBEnumBuilderMethodType BUILDER_METHOD_TYPE;
	public DBEnumBuilderTargetType BUILDER_TARGET_TYPE;
	
	public Integer TARGET_RESOURCE_ID;
	public String TARGET_PATH;
	public String TARGET_PLATFORM;
	
	public String BUILDER_COMMAND;
	public String BUILDER_HOMEPATH;
	public String BUILDER_OPTIONS;
	public String JAVA_JDKHOMEPATH;

	public boolean BUILDER_REMOTE;
	public Integer REMOTE_ACCOUNT_ID;
	public int CV;
	
	public ProjectBuilder( EngineBuilders builders ) {
		super( builders );
		this.builders = builders;
		ID = -1;
		CV = 0;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public ProjectBuilder copy( EngineBuilders builders ) throws Exception {
		ProjectBuilder r = new ProjectBuilder( builders );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.VERSION = VERSION;
		
		r.BUILDER_METHOD_TYPE = BUILDER_METHOD_TYPE;
		r.BUILDER_TARGET_TYPE = BUILDER_TARGET_TYPE;
		
		r.TARGET_RESOURCE_ID = TARGET_RESOURCE_ID;
		r.TARGET_PATH = TARGET_PATH;
		r.TARGET_PLATFORM = TARGET_PLATFORM;
		
		r.BUILDER_COMMAND = BUILDER_COMMAND;
		r.BUILDER_HOMEPATH = BUILDER_HOMEPATH;
		r.BUILDER_OPTIONS = BUILDER_OPTIONS;
		r.JAVA_JDKHOMEPATH = JAVA_JDKHOMEPATH;

		r.BUILDER_REMOTE = BUILDER_REMOTE;
		r.REMOTE_ACCOUNT_ID = REMOTE_ACCOUNT_ID;
		
		r.CV = CV;
		return( r );
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
	
	public void createBuilder( String name , String desc , String version ) {
		modifyBuilder( name , desc , version );
	}
		
	public void modifyBuilder( String name , String desc , String version ) {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
		this.VERSION = Common.nonull( version );
	}

	public void setMethodData( DBEnumBuilderMethodType type , String command , String home , String options , String jdkpath ) {
		this.BUILDER_METHOD_TYPE = type;
		this.BUILDER_COMMAND = Common.nonull( command );
		this.BUILDER_HOMEPATH = Common.nonull( home );
		this.BUILDER_OPTIONS = Common.nonull( options );
		this.JAVA_JDKHOMEPATH = Common.nonull( jdkpath );
	}
	
	public void setTargetData( DBEnumBuilderTargetType type , Integer resourceId , String targetpath , String platform ) {
		this.BUILDER_TARGET_TYPE = type;
		this.TARGET_RESOURCE_ID = resourceId; 
		this.TARGET_PATH = Common.nonull( targetpath );
		this.TARGET_PLATFORM = Common.nonull( platform );
	}
	
	public void setRemoteData( boolean remote , Integer accountId ) {
		this.BUILDER_REMOTE = remote;
		this.REMOTE_ACCOUNT_ID = accountId;
	}
	
	public Account getRemoteAccount( ActionBase action ) throws Exception {
		if( !BUILDER_REMOTE )
			return( action.getLocalAccount() );
		
		EngineInfrastructure infra = action.getServerInfrastructure();
		HostAccount account = infra.getHostAccount( REMOTE_ACCOUNT_ID );
		return( account.getAccount() );
	}

	public HostAccount getHostAccount( ActionBase action ) throws Exception {
		if( !BUILDER_REMOTE )
			return( null );
		
		EngineInfrastructure infra = action.getServerInfrastructure();
		HostAccount account = infra.getHostAccount( REMOTE_ACCOUNT_ID );
		return( account );
	}

	public ShellExecutor createShell( ActionBase action , boolean dedicated ) throws Exception {
		if( BUILDER_REMOTE ) {
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
