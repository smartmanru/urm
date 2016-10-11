package org.urm.action.main;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.common.jmx.RemoteCall;
import org.urm.common.meta.BuildCommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.common.meta.MainCommandMeta;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.engine.ServerDirectory;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.Meta.VarBUILDMODE;

public class ActionConfigure extends ActionBase {

	String USEPRODUCT;
	String USEENV;
	String USEDC;
	boolean runLinux;
	boolean runWindows;

	LocalFolder pfMaster = null;
	List<String> linesProxy;
	List<String> linesAffected;

	String executorMasterFolderRel;
	String envMasterFolderRel;
	String dcMasterFolderRel;
	String envDbMasterFolderRel;
	String dcDbMasterFolderRel;
	String buildMasterFolderRel;

	public ActionConfigure( ActionBase action , String stream , String OSTYPE , String USEENV , String USEDC ) {
		super( action , stream );
		this.USEPRODUCT = "";
		this.USEENV = USEENV;
		this.USEDC = USEDC;
		this.runLinux = ( OSTYPE.equals( "all" ) || OSTYPE.equals( "linux" ) || ( OSTYPE.isEmpty() && super.execrc.isLinux() ) )? true : false;
		this.runWindows = ( OSTYPE.equals( "all" ) || OSTYPE.equals( "windows" ) || ( OSTYPE.isEmpty() && super.execrc.isWindows() ) )? true : false;
	}

	public ActionConfigure( ActionBase action , String stream , String USEPRODUCT , String USEENV , boolean confLinux , boolean confWindows ) {
		super( action , stream );
		this.USEPRODUCT = USEPRODUCT;
		this.USEENV = USEENV;
		this.USEDC = "";
		this.runLinux = confLinux;
		this.runWindows = confWindows;
	}

	@Override protected boolean executeSimple() throws Exception {
		commentExecutor( "configure ..." );
		
		executorMasterFolderRel = "..";
		envMasterFolderRel = "../..";
		dcMasterFolderRel = "../../..";
		envDbMasterFolderRel = "../../..";
		dcDbMasterFolderRel = "../../../..";
		buildMasterFolderRel = "../..";

		// set execution context
		configureDefault();
		return( true );
	}

	private void configureDefault() throws Exception {
		if( super.isStandalone() ) {
			context.session.setStandaloneLayout( context.options );
			Meta meta = super.getContextMeta();
			configureProduct( meta );
		}
		else {
			context.session.setServerLayout( context.options );
			configureServer();
		}
	}
	
	private void configureServer() throws Exception {
		ServerDirectory directory = actionInit.getDirectory();
		for( String name : directory.getProducts() ) {
			if( USEPRODUCT.isEmpty() || USEPRODUCT.equals( name ) ) {
				info( "configure product name=" + name + " ..." );
				Meta meta = super.getProductMetadata( name );
				configureProduct( meta );
			}
		}
	}

	private void configureProduct( Meta meta ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder pf = urm.getProductHome( this , meta.name );
		pfMaster = pf.getSubFolder( this , "master" );
		String masterPath = pfMaster.getFilePath( this , MainCommandMeta.MASTERFILE );
		
		List<String> lines = null;
		if( super.isStandalone() ) 
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
		
		linesProxy = new LinkedList<String>();
		linesAffected = new LinkedList<String>();
		
		USEENV = "";
		USEDC = "";
		
		configureProductDefault( meta );
		createMasterFile( masterPath , lines );
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
	
	private void configureProductDefault( Meta meta ) throws Exception {
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
			configureProductAll( meta , buildUnix , deployUnix , true );
		if( buildWindows || deployWindows )
			configureProductAll( meta , buildWindows , deployWindows , false );
	}
	
	private void configureProductAll( Meta meta , boolean build , boolean deploy , boolean linux ) throws Exception {
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
			configureExecutor( meta , executor , dbe , linux );
	}

	private void configureExecutor( Meta meta , CommandMeta executor , CommandMeta dbe , boolean linux ) throws Exception {
		LocalFolder exeFolder = pfMaster.getSubFolder( this , executor.name );
		exeFolder.ensureExists( this );

		// context
		configureExecutorContextSimple( meta , exeFolder , linux );
		
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
			MetadataStorage ms = artefactory.getMetadataStorage( this , meta );
			
			MetaEnvDC dc = null;
			if( USEENV.isEmpty() ) {
				addAffected( linux , proxyPath , true );
				String[] envFiles = ms.getEnvFiles( this );
				for( String envFile : envFiles ) {
					MetaEnv env = meta.getEnvData( this , envFile , false );
					envs.put( envFile , env );
				}
			}
			else {
				MetaEnv env = null;
				String[] envFiles = ms.getEnvFiles( this );
				for( String envFile : envFiles ) {
					MetaEnv envx = meta.getEnvData( this , envFile , false );
					if( envx.ID.equals( USEENV ) ) {
						env = envx;
						envs.put( envFile , envx );
						break;
					}
				}
				
				if( env == null )
					exit1( _Error.UnknownEnvironment1 , "unknown environment ID=" + USEENV , USEENV );
				
				addAffected( linux , proxyPath , false );
				
				if( USEDC.isEmpty() ) {
					proxyPath = Common.getPath( proxyPath , env.ID );
					addAffected( linux , proxyPath , true );
				}
				else {
					dc = env.getDC( this , USEDC );
					proxyPath = Common.getPath( proxyPath , env.ID , USEDC );
					addAffected( linux , proxyPath , true );
				}
			}
			
			for( String envFile : envs.keySet() ) {
				MetaEnv env = envs.get( envFile );
				configureDeploymentEnv( meta , exeFolder , executor , envFile , env , dc , linux , dbe );
			}
		}
		else {
			String proxyPath = executor.name;
			addAffected( linux , proxyPath , true );
		}
		
		if( executor.name.equals( BuildCommandMeta.NAME ) ) {
			for( VarBUILDMODE mode : VarBUILDMODE.values() ) {
				if( mode == VarBUILDMODE.UNKNOWN )
					continue;
				
				configureBuildMode( meta , exeFolder , executor , mode , linux );
			}
		}
	}

	private void addAffected( boolean configureLinux , String proxyPath , boolean recursive ) throws Exception {
		String item = ( configureLinux )? ".sh" : ".cmd";
		item += ":" + proxyPath + "/";
		item += ( recursive )? ":*" : ":F";
		linesAffected.add( item );
	}
	
	private void configureDeploymentEnv( Meta meta , LocalFolder ef , CommandMeta executor , String envFile , MetaEnv env , MetaEnvDC dc , boolean linux , CommandMeta dbe ) throws Exception {
		LocalFolder efEnv = ef.getSubFolder( this , env.ID );
		efEnv.ensureExists( this );
		
		// env-level
		if( USEDC.isEmpty() || !env.isMultiDC( this ) )
			configureDeploymentEnvContent( meta , efEnv , executor , env , envFile , null , linux , dbe );
		
		if( env.isMultiDC( this ) ) {
			if( USEDC.isEmpty() ) {
				if( context.CTX_ALL ) {
					for( MetaEnvDC envdc : env.getOriginalDCList( this ) ) {
						LocalFolder efEnvDC = efEnv.getSubFolder( this , envdc.NAME );
						configureDeploymentEnvContent( meta , efEnvDC , executor , env , envFile , envdc.NAME , linux , dbe );
					}
				}
			}
			else {
				LocalFolder efEnvDC = efEnv.getSubFolder( this , dc.NAME );
				configureDeploymentEnvContent( meta , efEnvDC , executor , env , envFile , dc.NAME , linux , dbe );
			}
		}
	}

	private void configureDeploymentEnvContent( Meta meta , LocalFolder ef , CommandMeta executor , MetaEnv env , String ENVFILE , String DC , boolean linux , CommandMeta dbe ) throws Exception {
		// env-level context
		ef.ensureExists( this );
		String CTXDC = DC;
		if( DC == null ) {
			if( env.isMultiDC( this ) )
				CTXDC = "";
			else
				CTXDC = env.getMainDC( this ).NAME;
		}
		configureExecutorContextDeployment( meta , ef , ENVFILE , CTXDC , linux );

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
	
	private void configureBuildMode( Meta meta , LocalFolder ef , CommandMeta executor , VarBUILDMODE mode , boolean linux ) throws Exception {
		LocalFolder efBuild = ef.getSubFolder( this , Common.getEnumLower( mode ) );
		efBuild.ensureExists( this );
		configureExecutorContextBuildMode( meta , efBuild , mode , linux );
		
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
	
	private void addExecutorContextBase( Meta meta , LocalFolder ef , boolean linux , List<String> lines ) throws Exception {
		if( super.isStandalone() && context.session.productName.isEmpty() )
			return;
		
		addExecutorContextItem( ef , linux , lines , "C_URM_PRODUCT" , meta.name );
		if( !super.isStandalone() ) { 
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

	private void saveExecutorContext( LocalFolder ef , boolean linux , List<String> lines ) throws Exception {
		String fileName = ( linux )? MainCommandMeta.CONTEXT_FILENAME_LIXUX : MainCommandMeta.CONTEXT_FILENAME_WIN;
		if( context.CTX_FORCE == true || !ef.checkFileExists( this , fileName ) )
			Common.createFileFromStringList( ef.getFilePath( this , fileName ) , lines );
		addProxyLine( ef , fileName );
	}
	
	private void configureExecutorContextSimple( Meta meta , LocalFolder ef , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		addExecutorContextBase( meta , ef , linux , lines );
		saveExecutorContext( ef , linux , lines );
	}
	
	private void configureExecutorContextDeployment( Meta meta , LocalFolder ef , String ENVFILE , String DC , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		addExecutorContextBase( meta , ef , linux , lines );
		addExecutorContextItem( ef , linux , lines , "C_URM_ENV" , ENVFILE );
		addExecutorContextItem( ef , linux , lines , "C_URM_DC" , DC );
		saveExecutorContext( ef , linux , lines );
	}
	
	private void configureExecutorContextBuildMode( Meta meta , LocalFolder ef , VarBUILDMODE mode , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		addExecutorContextBase( meta , ef , linux , lines );
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
			lines.add( "@cd /D %~dp0" );
			
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
