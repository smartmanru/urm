package org.urm.server.action.main;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.common.jmx.RemoteCall;
import org.urm.common.meta.BuildCommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.common.meta.MainCommandMeta;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.FinalMetaLoader;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.MetadataStorage;
import org.urm.server.storage.UrmStorage;

public class ActionConfigure extends ActionBase {

	boolean configureLinux;
	String USEENV;
	String USEDC;

	LocalFolder pfMaster = null;
	List<String> linesProxy;
	List<String> linesAffected;

	String executorMasterFolderRel;
	String envMasterFolderRel;
	String dcMasterFolderRel;
	String envDbMasterFolderRel;
	String dcDbMasterFolderRel;
	String buildMasterFolderRel;

	public ActionConfigure( ActionBase action , String stream , boolean configureLinux , String USEENV , String USEDC ) {
		super( action , stream );
		this.configureLinux = configureLinux;
		this.USEENV = USEENV;
		this.USEDC = USEDC;
	}

	@Override protected boolean executeSimple() throws Exception {
		commentExecutor( "configure ..." );
		
		executorMasterFolderRel = "..";
		envMasterFolderRel = "../..";
		dcMasterFolderRel = "../../..";
		envDbMasterFolderRel = "../../..";
		dcDbMasterFolderRel = "../../../..";
		buildMasterFolderRel = "../..";

		boolean serverMode = false;
		UrmStorage urm = artefactory.getUrmStorage();
		if( urm.isServerMode( this ) )
			serverMode = true;
		else
		if( urm.isStandaloneMode( this ) )
			serverMode = false;
		else
			exit( "Installation is not configured, default is not applicable" );
		
		if( serverMode ) {
			executorMasterFolderRel += "/../../../master";
			envMasterFolderRel += "/../../../master";
			dcMasterFolderRel += "/../../../master";
			envDbMasterFolderRel += "/../../../master";
			dcDbMasterFolderRel += "/../../../master";
			buildMasterFolderRel += "/../../../master";
		}

		// set execution context
		configureDefault( serverMode );
		return( true );
	}

	private void configureServer( boolean serverMode ) throws Exception {
		FinalMetaLoader meta = engine.metaLoader;
		
		meta.loadServerSettings();
		engine.loadProducts();
		
		for( String name : meta.getProducts() ) {
			info( "configure product name=" + name + " ..." );
			
			setServerProductLayout( name );
			configureProduct( serverMode , false );
			clearServerProductLayout();
		}
	}

	private void configureProduct( boolean serverMode , boolean standalone ) throws Exception {
		meta.loadProduct( this );
		meta.loadDistr( this );
		
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder pf = urm.getProductFolder( this );
		pfMaster = pf.getSubFolder( this , "master" );
		String masterPath = pfMaster.getFilePath( this , MainCommandMeta.MASTERFILE );
		
		List<String> lines = null;
		if( standalone ) 
			lines = readFileLines( masterPath );
		else {
			pfMaster.ensureExists( this );
			if( pfMaster.checkFileExists( this , MainCommandMeta.MASTERFILE ) )
				lines = readFileLines( masterPath );
			else {
				lines = new LinkedList<String>();
				lines.add( MainCommandMeta.RELEASEPREFIX + MainCommandMeta.MASTERFILE );
			}
		}
		
		configureProduct( serverMode );
		createMasterFile( masterPath , lines );
	}

	private void configureDefault( boolean serverMode ) throws Exception {
		if( serverMode ) {
			context.session.setServerLayout( context.options );
			configureServer( serverMode );
		}
		else {
			context.session.setStandaloneLayout( context.options );
			configureProduct( serverMode , true );
		}
	}
	
	private void configureProduct( boolean serverMode ) throws Exception {
		linesProxy = new LinkedList<String>();
		linesAffected = new LinkedList<String>();
		
		USEENV = "";
		USEDC = "";
		
		configureProductDefault( serverMode );
	}

	private void createMasterFile( String masterPath , List<String> lines ) throws Exception {
		// recreate master file
		List<String> linesNew = new LinkedList<String>();
		for( String s : lines ) {
			boolean process = true;
			if( !s.startsWith( MainCommandMeta.PROXYPREFIX ) )
				process = false;
			else {
				if( isLocalLinux() && !s.endsWith( ".sh" ) )
					process = false;
				else
				if( isLocalWindows() && !s.endsWith( ".cmd" ) )
					process = false;
			}

			if( !process ) {
				linesNew.add( s );
				continue;
			}
			
			boolean affected = false;
			String filePath = Common.getPartAfterFirst( s , MainCommandMeta.PROXYPREFIX );
			for( String x : linesAffected ) {
				String platform = Common.getListItem( x , ":" , 0 );
				String findpath = Common.getListItem( x , ":" , 1 );
				String files = Common.getListItem( x , ":" , 2 );
				
				if( !filePath.startsWith( findpath ) )
					continue;
				
				if( !filePath.endsWith( platform ) )
					continue;
				
				if( files.equals( "*" ) ) {
					affected = true;
					break;
				}
				
				if( filePath.indexOf( findpath.length() , '/' ) < 0 ) {
					affected = true;
					break;
				}
			}
			
			if( !affected )
				linesNew.add( s );
		}
		
		linesNew.addAll( linesProxy );
		Collections.sort( linesNew );
		Common.createFileFromStringList( masterPath , linesNew );
	}
	
	private void configureProductDefault( boolean serverMode ) throws Exception {
		LocalFolder pfBuild = pfMaster.getSubFolder( this , BuildCommandMeta.NAME );
		LocalFolder pfDeploy = pfMaster.getSubFolder( this , DeployCommandMeta.NAME );
		
		boolean buildUnix = false;
		boolean buildWindows = false;
		if( pfBuild.checkExists( this ) ) {
			if( pfBuild.findFiles( this , "*.sh" ).length > 0 )
				buildUnix = true;
			if( pfBuild.findFiles( this , "*.cmd" ).length > 0 )
				buildWindows = true;
		}
		boolean deployUnix = false;
		boolean deployWindows = false;
		if( pfDeploy.checkExists( this ) ) {
			if( pfDeploy.findFiles( this , "*.sh" ).length > 0 )
				deployUnix = true;
			if( pfDeploy.findFiles( this , "*.cmd" ).length > 0 )
				deployWindows = true;
		}
		
		if( isLocalLinux() )
			buildUnix = deployUnix = true;
		else
			buildWindows = deployWindows = true;
		
		if( buildUnix || deployUnix )
			configureProductAll( serverMode , buildUnix , deployUnix , true );
		if( buildWindows || deployWindows )
			configureProductAll( serverMode , buildWindows , deployWindows , false );
	}
	
	private void configureProductAll( boolean serverMode , boolean build , boolean deploy , boolean linux ) throws Exception {
		CommandBuilder builder = new CommandBuilder( context.session.clientrc , context.session.execrc );
		
		CommandMeta[] executors = builder.getExecutors( build , deploy );
		CommandMeta dbe = null;
		for( CommandMeta executor : executors ) {
			if( executor.name.equals( DatabaseCommandMeta.NAME ) ) {
				dbe = executor;
				break;
			}
		}
			
		for( CommandMeta executor : executors )
			configureExecutor( serverMode , executor , dbe , linux );
	}

	private void configureExecutor( boolean serverMode , CommandMeta executor , CommandMeta dbe , boolean linux ) throws Exception {
		LocalFolder exeFolder = pfMaster.getSubFolder( this , executor.name );
		exeFolder.ensureExists( this );

		// context
		configureExecutorContextSimple( serverMode , exeFolder , linux );
		
		// add help action
		configureExecutorWrapper( exeFolder , executor , "help" , linux , executorMasterFolderRel , null );
		
		// top items
		for( CommandMethodMeta cmdAction : executor.actionsList ) {
			if( cmdAction.top )
				configureExecutorWrapper( exeFolder , executor , cmdAction.name , linux , executorMasterFolderRel , null );
		}
		
		if( executor.name.equals( DeployCommandMeta.NAME ) ) {
			String proxyPath = DeployCommandMeta.NAME;
			
			Map<String,MetaEnv> envs = new HashMap<String,MetaEnv>(); 
			MetadataStorage ms = artefactory.getMetadataStorage( this );
			
			MetaEnvDC dc = null;
			if( USEENV.isEmpty() ) {
				addAffected( proxyPath , true );
				String[] envFiles = ms.getEnvFiles( this );
				for( String envFile : envFiles ) {
					MetaEnv env = meta.loadEnvData( this , envFile , false );
					envs.put( envFile , env );
				}
			}
			else {
				MetaEnv env = null;
				String[] envFiles = ms.getEnvFiles( this );
				for( String envFile : envFiles ) {
					MetaEnv envx = meta.loadEnvData( this , envFile , false );
					if( envx.ID.equals( USEENV ) ) {
						env = envx;
						envs.put( envFile , envx );
						break;
					}
				}
				
				if( env == null )
					exit( "unknown environment ID=" + USEENV );
				
				addAffected( proxyPath , false );
				
				if( USEDC.isEmpty() ) {
					proxyPath = Common.getPath( proxyPath , env.ID );
					addAffected( proxyPath , true );
				}
				else {
					dc = env.getDC( this , USEDC );
					proxyPath = Common.getPath( proxyPath , env.ID , USEDC );
					addAffected( proxyPath , true );
				}
			}
			
			for( String envFile : envs.keySet() ) {
				MetaEnv env = envs.get( envFile );
				configureDeploymentEnv( serverMode , exeFolder , executor , envFile , env , dc , linux , dbe );
			}
		}
		else {
			String proxyPath = executor.name;
			addAffected( proxyPath , true );
		}
		
		if( executor.name.equals( BuildCommandMeta.NAME ) ) {
			for( VarBUILDMODE mode : VarBUILDMODE.values() ) {
				if( mode == VarBUILDMODE.UNKNOWN )
					continue;
				
				configureBuildMode( serverMode , exeFolder , executor , mode , linux );
			}
		}
	}

	private void addAffected( String proxyPath , boolean recursive ) throws Exception {
		String item = ( configureLinux )? ".sh" : ".cmd";
		item += ":" + proxyPath + "/";
		item += ( recursive )? ":*" : ":F";
		linesAffected.add( item );
	}
	
	private void configureDeploymentEnv( boolean serverMode , LocalFolder ef , CommandMeta executor , String envFile , MetaEnv env , MetaEnvDC dc , boolean linux , CommandMeta dbe ) throws Exception {
		LocalFolder efEnv = ef.getSubFolder( this , env.ID );
		efEnv.ensureExists( this );
		
		// env-level
		if( USEDC.isEmpty() || !env.isMultiDC( this ) )
			configureDeploymentEnvContent( serverMode , efEnv , executor , env , envFile , null , linux , dbe );
		
		if( env.isMultiDC( this ) ) {
			if( USEDC.isEmpty() ) {
				if( context.CTX_ALL ) {
					for( MetaEnvDC envdc : env.getOriginalDCList( this ) ) {
						LocalFolder efEnvDC = efEnv.getSubFolder( this , envdc.NAME );
						configureDeploymentEnvContent( serverMode , efEnvDC , executor , env , envFile , envdc.NAME , linux , dbe );
					}
				}
			}
			else {
				LocalFolder efEnvDC = efEnv.getSubFolder( this , dc.NAME );
				configureDeploymentEnvContent( serverMode , efEnvDC , executor , env , envFile , dc.NAME , linux , dbe );
			}
		}
	}

	private void configureDeploymentEnvContent( boolean serverMode , LocalFolder ef , CommandMeta executor , MetaEnv env , String ENVFILE , String DC , boolean linux , CommandMeta dbe ) throws Exception {
		// env-level context
		ef.ensureExists( this );
		String CTXDC = DC;
		if( DC == null ) {
			if( env.isMultiDC( this ) )
				CTXDC = "";
			else
				CTXDC = env.getMainDC( this ).NAME;
		}
		configureExecutorContextDeployment( serverMode , ef , ENVFILE , CTXDC , linux );

		String xp = ( DC == null )? envMasterFolderRel : dcMasterFolderRel;
		String xpdb = ( DC == null )? envDbMasterFolderRel : dcDbMasterFolderRel;
		
		// env-level wrappers
		for( CommandMethodMeta cmdAction : executor.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( ef , executor , cmdAction.name , linux , xp , null );
		}
		
		// database wrappers
		LocalFolder efDB = ef.getSubFolder( this , DatabaseCommandMeta.NAME );
		efDB.ensureExists( this );
		for( CommandMethodMeta cmdAction : dbe.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( efDB , dbe , cmdAction.name , linux , xpdb , ".." );
		}
	}
	
	private void configureBuildMode( boolean serverMode , LocalFolder ef , CommandMeta executor , VarBUILDMODE mode , boolean linux ) throws Exception {
		LocalFolder efBuild = ef.getSubFolder( this , Common.getEnumLower( mode ) );
		efBuild.ensureExists( this );
		configureExecutorContextBuildMode( serverMode , efBuild , mode , linux );
		
		// env-level wrappers
		for( CommandMethodMeta cmdAction : executor.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( efBuild , executor , cmdAction.name , linux , buildMasterFolderRel , null );
		}
	}

	private void addExecutorContextItem( LocalFolder ef , boolean linux , List<String> lines , String var , String value ) throws Exception {
		if( linux )
			lines.add( "export " + var + "=" + value );
		else
			lines.add( "@set " + var + "=" + value );
	}
	
	private void addExecutorContextBase( boolean serverMode , LocalFolder ef , boolean linux , List<String> lines ) throws Exception {
		String productName = context.session.productName;
		
		if( !productName.isEmpty() ) {
			addExecutorContextItem( ef , linux , lines , "C_URM_PRODUCT" , productName );
			if( serverMode ) { 
				String hostName = "localhost";
				if( !context.CTX_HOST.isEmpty() )
					hostName = context.CTX_HOST;
				String value = hostName + ":";
				
				if( context.CTX_PORT > 0 )
					value += context.CTX_PORT;
				else
					value += RemoteCall.DEFAULT_SERVER_PORT; 
				addExecutorContextItem( ef , linux , lines , "C_URM_SERVER" , value );
			}
		}
	}

	private void saveExecutorContext( LocalFolder ef , boolean linux , List<String> lines ) throws Exception {
		String fileName = ( linux )? MainCommandMeta.CONTEXT_FILENAME_LIXUX : MainCommandMeta.CONTEXT_FILENAME_WIN;
		if( context.CTX_FORCE == true || !ef.checkFileExists( this , fileName ) )
			Common.createFileFromStringList( ef.getFilePath( this , fileName ) , lines );
		addProxyLine( ef , fileName );
	}
	
	private void configureExecutorContextSimple( boolean serverMode , LocalFolder ef , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		addExecutorContextBase( serverMode , ef , linux , lines );
		saveExecutorContext( ef , linux , lines );
	}
	
	private void configureExecutorContextDeployment( boolean serverMode , LocalFolder ef , String ENVFILE , String DC , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		addExecutorContextBase( serverMode , ef , linux , lines );
		addExecutorContextItem( ef , linux , lines , "C_URM_ENV" , ENVFILE );
		addExecutorContextItem( ef , linux , lines , "C_URM_DC" , DC );
		saveExecutorContext( ef , linux , lines );
	}
	
	private void configureExecutorContextBuildMode( boolean serverMode , LocalFolder ef , VarBUILDMODE mode , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		addExecutorContextBase( serverMode , ef , linux , lines );
		addExecutorContextItem( ef , linux , lines , "C_URM_VERSIONMODE" , Common.getEnumLower( mode ) );
		saveExecutorContext( ef , linux , lines );
	}
	
	private void configureExecutorWrapper( LocalFolder ef , CommandMeta executor , String method , boolean linux , String relativePath , String relativeContext ) throws Exception {
		String fileName = method + ( ( linux )? ".sh" : ".cmd" );
		String filePath = ef.getFilePath( this , fileName );

		File f = new File( filePath );
		if( f.exists() )
			f.delete();
		
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "#!/bin/bash" );
			lines.add( "cd `dirname $0`" );
			
			if( relativeContext == null )
				lines.add( ". ./" + MainCommandMeta.CONTEXT_FILENAME_LIXUX );
			else
				lines.add( ". " + relativeContext + "/" + MainCommandMeta.CONTEXT_FILENAME_LIXUX );
			
			lines.add( relativePath + "/bin/urm.sh " + executor.name + " " + method + " " + Common.getQuoted( "$@" ) );			
		}
		else {
			relativePath = Common.getWinPath( relativePath );
			lines.add( "@cd %~dp0" );
			
			if( relativeContext == null )
				lines.add( "@call " + MainCommandMeta.CONTEXT_FILENAME_WIN );
			else
				lines.add( "@call " + Common.getWinPath( relativeContext + "/" + MainCommandMeta.CONTEXT_FILENAME_WIN ) );
			
			lines.add( "@" + relativePath + "\\bin\\urm.cmd " + executor.name + " " + method + " %*" );			
		}
		
		Common.createFileFromStringList( filePath , lines );
		addProxyLine( ef , fileName );
	}

	private void addProxyLine( LocalFolder ef , String fileName ) throws Exception {
		String subPath = Common.getPartAfterFirst( ef.getFilePath( this , fileName ) , pfMaster.folderPath + "/" );
		linesProxy.add( MainCommandMeta.PROXYPREFIX + subPath );
	}

}
