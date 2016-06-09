package org.urm.server.action.main;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.client.meta.BuildCommandMeta;
import org.urm.client.meta.DatabaseCommandMeta;
import org.urm.client.meta.DeployCommandMeta;
import org.urm.common.Common;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.MetadataStorage;

public class ActionConfigure extends ActionBase {

	boolean configureLinux;
	String ACTION;
	String USEENV;
	String USEDC;

	LocalFolder pfMaster = null;
	List<String> linesProxy;
	List<String> linesAffected;
	
	public ActionConfigure( ActionBase action , String stream , boolean configureLinux , String ACTION , String USEENV , String USEDC ) {
		super( action , stream );
		this.configureLinux = configureLinux;
		this.ACTION = ACTION;
		this.USEENV = USEENV;
		this.USEDC = USEDC;
	}

	@Override protected boolean executeSimple() throws Exception {
		commentExecutor( "run " + ACTION + " ..." );
		
		if( ACTION.equals( "server" ) ) {
			context.session.setServerLayout( context.options );
			configureServer( true );
		}
		else
		if( ACTION.equals( "standalone" ) ) {
			context.session.setStandaloneLayout( context.options );
			configureProduct( true , true );
		}
		else
		if( ACTION.equals( "default" ) )
			configureDefault();
		else
			exit( "action is not set, see help" );
		return( true );
	}

	private void configureServer( boolean initial ) throws Exception {
		LocalFolder pf = artefactory.getInstallFolder( this );
		LocalFolder pfProducts = pf.getSubFolder( this , "products" );
		if( !pfProducts.checkExists( this ) )
			exit( "before configure, please create directory: " + pfProducts.folderPath );

		boolean found = false;
		for( String product : pfProducts.getTopDirs( this ) ) {
			comment( "configure product=" + product + " ..." );
			found = true;
			context.session.setServerProductLayout( product );
			configureProduct( initial , false );
		}
		
		if( !found )
			info( "no products found in " + pfProducts.folderPath + ", nothing to configure" );
	}
	
	private void configureProduct( boolean initial , boolean standalone ) throws Exception {
		meta.loadProduct( this );
		meta.loadDistr( this );
		
		LocalFolder pf = artefactory.getProductFolder( this );
		pfMaster = pf.getSubFolder( this , "master" );
		String masterPath = pfMaster.getFilePath( this , MainMeta.MASTERFILE );
		
		List<String> lines = null;
		if( standalone ) 
			lines = readFileLines( masterPath );
		else {
			pfMaster.ensureExists( this );
			lines = new LinkedList<String>();
		}
		
		configureProduct( lines , initial );
		createMasterFile( masterPath , lines );
	}

	private void configureDefault() throws Exception {
		LocalFolder pf = artefactory.getInstallFolder( this );
		if( pf.checkFolderExists( this , "products" ) ) {
			context.session.setServerLayout( context.options );
			configureServer( false );
		}
		else {
			context.session.setStandaloneLayout( context.options );
			configureProduct( false , true );
		}
	}
	
	private void configureProduct( List<String> lines , boolean initial ) throws Exception {
		linesProxy = new LinkedList<String>();
		linesAffected = new LinkedList<String>();
		
		USEENV = "";
		USEDC = "";
		
		if( initial )
			configureProductAll( true , true , configureLinux );
		else
			configureProductDefault();
	}

	private void createMasterFile( String masterPath , List<String> lines ) throws Exception {
		// recreate master file
		List<String> linesNew = new LinkedList<String>();
		for( String s : lines ) {
			if( !s.startsWith( MainMeta.PROXYPREFIX ) )
				linesNew.add( s );
			else {
				boolean affected = false;
				String filePath = Common.getPartAfterFirst( s , MainMeta.PROXYPREFIX );
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
		}
		
		linesNew.addAll( linesProxy );
		Collections.sort( linesNew );
		Common.createFileFromStringList( masterPath , linesNew );
	}
	
	private void configureProductDefault() throws Exception {
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
		
		if( buildUnix || deployUnix )
			configureProductAll( buildUnix , deployUnix , true );
		if( buildWindows || deployWindows )
			configureProductAll( buildWindows , deployWindows , false );
	}
	
	private void configureProductAll( boolean build , boolean deploy , boolean linux ) throws Exception {
		CommandBuilder builder = new CommandBuilder( context.rc );
		CommandMeta[] executors = builder.getExecutors( build , deploy );
		CommandMeta dbe = null;
		for( CommandMeta executor : executors ) {
			if( executor.name.equals( DatabaseCommandMeta.NAME ) ) {
				dbe = executor;
				break;
			}
		}
			
		for( CommandMeta executor : executors )
			configureExecutor( executor , dbe , linux );
	}

	private void configureExecutor( CommandMeta executor , CommandMeta dbe , boolean linux ) throws Exception {
		LocalFolder exeFolder = pfMaster.getSubFolder( this , executor.name );
		exeFolder.ensureExists( this );

		// add help action
		configureExecutorWrapper( exeFolder , executor , "help" , linux , ".." , false , null );
		
		// top items
		for( CommandMethod cmdAction : executor.actionsList ) {
			if( cmdAction.top )
				configureExecutorWrapper( exeFolder , executor , cmdAction.name , linux , ".." , false , null );
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
				configureDeploymentEnv( exeFolder , executor , envFile , env , dc , linux , dbe );
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
				
				configureBuildMode( exeFolder , executor , mode , linux );
			}
		}
	}

	private void addAffected( String proxyPath , boolean recursive ) throws Exception {
		String item = ( configureLinux )? ".sh" : ".cmd";
		item += ":" + proxyPath + "/";
		item += ( recursive )? ":*" : ":F";
		linesAffected.add( item );
	}
	
	private void configureDeploymentEnv( LocalFolder ef , CommandMeta executor , String envFile , MetaEnv env , MetaEnvDC dc , boolean linux , CommandMeta dbe ) throws Exception {
		LocalFolder efEnv = ef.getSubFolder( this , env.ID );
		efEnv.ensureExists( this );
		
		// env-level
		if( USEDC.isEmpty() || !env.isMultiDC( this ) )
			configureDeploymentEnvContent( efEnv , executor , env , envFile , null , linux , dbe );
		
		if( env.isMultiDC( this ) ) {
			if( USEDC.isEmpty() ) {
				if( context.CTX_ALL ) {
					for( MetaEnvDC envdc : env.getOriginalDCList( this ) ) {
						LocalFolder efEnvDC = efEnv.getSubFolder( this , envdc.NAME );
						configureDeploymentEnvContent( efEnvDC , executor , env , envFile , envdc.NAME , linux , dbe );
					}
				}
			}
			else {
				LocalFolder efEnvDC = efEnv.getSubFolder( this , dc.NAME );
				configureDeploymentEnvContent( efEnvDC , executor , env , envFile , dc.NAME , linux , dbe );
			}
		}
	}

	private void configureDeploymentEnvContent( LocalFolder ef , CommandMeta executor , MetaEnv env , String ENVFILE , String DC , boolean linux , CommandMeta dbe ) throws Exception {
		// env-level context
		ef.ensureExists( this );
		String CTXDC = DC;
		if( DC == null ) {
			if( env.isMultiDC( this ) )
				CTXDC = "";
			else
				CTXDC = env.getMainDC( this ).NAME;
		}
		configureExecutorContextDeployment( ef , ENVFILE , CTXDC , linux );

		String xp = ( DC == null )? "../.." : "../../..";
		
		// env-level wrappers
		for( CommandMethod cmdAction : executor.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( ef , executor , cmdAction.name , linux , xp , true , null );
		}
		
		// database wrappers
		LocalFolder efDB = ef.getSubFolder( this , DatabaseCommandMeta.NAME );
		efDB.ensureExists( this );
		for( CommandMethod cmdAction : dbe.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( efDB , dbe , cmdAction.name , linux , xp + "/.." , true , ".." );
		}
	}
	
	private void configureBuildMode( LocalFolder ef , CommandMeta executor , VarBUILDMODE mode , boolean linux ) throws Exception {
		LocalFolder efBuild = ef.getSubFolder( this , Common.getEnumLower( mode ) );
		efBuild.ensureExists( this );
		configureExecutorContextBuildMode( efBuild , mode , linux );
		
		// env-level wrappers
		for( CommandMethod cmdAction : executor.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( efBuild , executor , cmdAction.name , linux , "../.." , true , null );
		}
	}

	private void configureExecutorContextDeployment( LocalFolder ef , String ENVFILE , String DC , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "export C_CONTEXT_ENV=" + ENVFILE );
			lines.add( "export C_CONTEXT_DC=" + DC );
			Common.createFileFromStringList( ef.getFilePath( this , MainMeta.CONTEXT_FILENAME_LIXUX ) , lines );
			addProxyLine( ef , MainMeta.CONTEXT_FILENAME_LIXUX );
		}
		else {
			lines.add( "@set C_CONTEXT_ENV=" + ENVFILE );
			lines.add( "@set C_CONTEXT_DC=" + DC );			
			Common.createFileFromStringList( ef.getFilePath( this , MainMeta.CONTEXT_FILENAME_WIN ) , lines );
			addProxyLine( ef , MainMeta.CONTEXT_FILENAME_WIN );
		}
	}
	
	private void configureExecutorContextBuildMode( LocalFolder ef , VarBUILDMODE mode , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "export C_CONTEXT_VERSIONMODE=" + Common.getEnumLower( mode ) );
			Common.createFileFromStringList( ef.getFilePath( this , MainMeta.CONTEXT_FILENAME_LIXUX ) , lines );
			addProxyLine( ef , MainMeta.CONTEXT_FILENAME_LIXUX );
		}
		else {
			lines.add( "@set C_CONTEXT_VERSIONMODE=" + Common.getEnumLower( mode ) );
			Common.createFileFromStringList( ef.getFilePath( this , MainMeta.CONTEXT_FILENAME_WIN ) , lines );
			addProxyLine( ef , MainMeta.CONTEXT_FILENAME_WIN );
		}
	}
	
	private void configureExecutorWrapper( LocalFolder ef , CommandMeta executor , String method , boolean linux , String relativePath , boolean context , String relativeContext ) throws Exception {
		String fileName = method + ( ( linux )? ".sh" : ".cmd" );
		String filePath = ef.getFilePath( this , fileName );

		File f = new File( filePath );
		if( f.exists() )
			f.delete();
		
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "#!/bin/bash" );
			lines.add( "cd `dirname $0`" );
			if( context ) {
				if( relativeContext == null )
					lines.add( ". ./" + MainMeta.CONTEXT_FILENAME_LIXUX );
				else
					lines.add( ". " + relativeContext + "/" + MainMeta.CONTEXT_FILENAME_LIXUX );
			}
			lines.add( relativePath + "/bin/urm.sh " + executor.name + " " + method + " " + Common.getQuoted( "$@" ) );			
		}
		else {
			relativePath = Common.getWinPath( relativePath );
			lines.add( "@cd %~dp0" );
			if( context ) {
				if( relativeContext == null )
					lines.add( "@call " + MainMeta.CONTEXT_FILENAME_WIN );
				else
					lines.add( "@call " + Common.getWinPath( relativeContext + "/" + MainMeta.CONTEXT_FILENAME_WIN ) );
			}
			lines.add( "@" + relativePath + "\\bin\\urm.cmd " + executor.name + " " + method + " %*" );			
		}
		
		Common.createFileFromStringList( filePath , lines );
		addProxyLine( ef , fileName );
	}

	private void addProxyLine( LocalFolder ef , String fileName ) throws Exception {
		String subPath = Common.getPartAfterFirst( ef.getFilePath( this , fileName ) , pfMaster.folderPath + "/" );
		linesProxy.add( MainMeta.PROXYPREFIX + subPath );
	}

}
