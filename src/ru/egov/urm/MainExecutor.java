package ru.egov.urm;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.action.ActionInit;
import ru.egov.urm.action.CommandAction;
import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;
import ru.egov.urm.action.build.BuildCommandExecutor;
import ru.egov.urm.action.database.DatabaseCommandExecutor;
import ru.egov.urm.action.deploy.DeployCommandExecutor;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.MetadataStorage;
import ru.egov.urm.vcs.SubversionVCS;

public class MainExecutor extends CommandExecutor {

	public static String NAME = "bin";
	public static String MASTERFILE = "master.files.info";
	public static String PROXYPREFIX = "proxy:";
	public static String CONTEXT_FILENAME_LIXUX = "_context.sh";
	public static String CONTEXT_FILENAME_WIN = "_context.cmd";
	
	public MainExecutor( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new Configure( true ) , "configure-linux" , true , "configure proxy files" , cmdOpts , "./configure.sh [OPTIONS] {linux|windows} {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandAction.newAction( new Configure( false ) , "configure-windows" , true , "configure proxy files" , cmdOpts , "configure.cmd [OPTIONS] {linux|windows} {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandAction.newAction( new SvnSave() , "svnsave" , true , "save master file set in svn" , cmdOpts , "svnsave [OPTIONS]" ) );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}
	
	private class Configure extends CommandAction {
		boolean linux;
		LocalFolder pfMaster = null;
		String USEENV;
		String USEDC;
		List<String> linesProxy;
		List<String> linesAffected;
		
	public Configure( boolean linux ) {
		this.linux = linux;
	}
	
	public void run( ActionInit action ) throws Exception {
		String ACTION = options.getRequiredArg( action , 0 , "ACTION" );
		
		LocalFolder pf = action.artefactory.getProductFolder( action );
		pfMaster = pf.getSubFolder( action , "master" );
		
		String masterPath = pfMaster.getFilePath( action , MASTERFILE );
		List<String> lines = ConfReader.readFileLines( action , masterPath );
		linesProxy = new LinkedList<String>();
		linesAffected = new LinkedList<String>();
		
		if( ACTION.equals( "default" ) )
			configureDefault( action , pfMaster );
		else
		if( ACTION.equals( "build" ) )
			configureAll( action , pfMaster , true , false , linux );
		else
		if( ACTION.equals( "deploy" ) ) {
			USEENV = options.getArg( 2 );
			USEDC = options.getArg( 3 );
			configureAll( action , pfMaster , false , true , linux );
		}
		else
		if( ACTION.equals( "all" ) )
			configureAll( action , pfMaster , true , true , linux );
		else
			action.exitUnexpectedState();
		
		// recreate master file
		List<String> linesNew = new LinkedList<String>();
		for( String s : lines ) {
			if( !s.startsWith( PROXYPREFIX ) )
				linesNew.add( s );
			else {
				boolean affected = false;
				String filePath = Common.getPartAfterFirst( s , PROXYPREFIX );
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
		Common.createFileFromStringList( masterPath , linesNew );
	}

	private void configureDefault( ActionInit action , LocalFolder pfMaster ) throws Exception {
		LocalFolder pfBuild = pfMaster.getSubFolder( action , BuildCommandExecutor.NAME );
		LocalFolder pfDeploy = pfMaster.getSubFolder( action , DeployCommandExecutor.NAME );
		
		boolean buildUnix = false;
		boolean buildWindows = false;
		if( pfBuild.checkExists( action ) ) {
			if( pfBuild.findFiles( action , "*.sh" ).length > 0 )
				buildUnix = true;
			if( pfBuild.findFiles( action , "*.cmd" ).length > 0 )
				buildWindows = true;
		}
		boolean deployUnix = false;
		boolean deployWindows = false;
		if( pfDeploy.checkExists( action ) ) {
			if( pfDeploy.findFiles( action , "*.sh" ).length > 0 )
				deployUnix = true;
			if( pfDeploy.findFiles( action , "*.cmd" ).length > 0 )
				deployWindows = true;
		}
		
		if( buildUnix || deployUnix )
			configureAll( action , pfMaster , buildUnix , deployUnix , true );
		if( buildWindows || deployWindows )
			configureAll( action , pfMaster , buildWindows , deployWindows , false );
	}
	
	private void configureAll( ActionInit action , LocalFolder pfMaster , boolean build , boolean deploy , boolean linux ) throws Exception {
		CommandExecutor[] executors = builder.getExecutors( action , build , deploy );
		CommandExecutor dbe = null;
		for( CommandExecutor executor : executors ) {
			if( executor.name.equals( DatabaseCommandExecutor.NAME ) ) {
				dbe = executor;
				break;
			}
		}
			
		for( CommandExecutor executor : executors )
			configureExecutor( action , pfMaster , executor , linux , dbe );
	}

	private void configureExecutor( ActionInit action , LocalFolder pfMaster , CommandExecutor executor , boolean linux , CommandExecutor dbe ) throws Exception {
		LocalFolder exeFolder = pfMaster.getSubFolder( action , executor.name );
		exeFolder.ensureExists( action );

		// add help action
		configureExecutorWrapper( action , exeFolder , executor , "help" , linux , ".." , false , null );
		
		// top items
		for( CommandAction cmdAction : executor.actionsList ) {
			if( cmdAction.top )
				configureExecutorWrapper( action , exeFolder , executor , cmdAction.name , linux , ".." , false , null );
		}
		
		if( executor.name.equals( DeployCommandExecutor.NAME ) ) {
			String proxyPath = DeployCommandExecutor.NAME;
			
			Map<String,MetaEnv> envs = new HashMap<String,MetaEnv>(); 
			MetadataStorage ms = action.artefactory.getMetadataStorage( action );
			
			MetaEnvDC dc = null;
			if( USEENV.isEmpty() ) {
				addAffected( action , linux , proxyPath , true );
				String[] envFiles = ms.getEnvFiles( action );
				for( String envFile : envFiles ) {
					MetaEnv env = meta.loadEnvData( action , envFile , false );
					envs.put( envFile , env );
				}
			}
			else {
				MetaEnv env = null;
				String[] envFiles = ms.getEnvFiles( action );
				for( String envFile : envFiles ) {
					MetaEnv envx = meta.loadEnvData( action , envFile , false );
					if( envx.ID.equals( USEENV ) ) {
						env = envx;
						envs.put( envFile , envx );
						break;
					}
				}
				
				if( env == null )
					action.exit( "unknown environment ID=" + USEENV );
				
				addAffected( action , linux , proxyPath , false );
				
				if( USEDC.isEmpty() ) {
					proxyPath = Common.getPath( proxyPath , env.ID );
					addAffected( action , linux , proxyPath , true );
				}
				else {
					dc = env.getDC( action , USEDC );
					proxyPath = Common.getPath( proxyPath , env.ID , USEDC );
					addAffected( action , linux , proxyPath , true );
				}
			}
			
			for( String envFile : envs.keySet() ) {
				MetaEnv env = envs.get( envFile );
				configureDeploymentEnv( action , exeFolder , executor , envFile , env , dc , linux , dbe );
			}
		}
		else {
			String proxyPath = executor.name;
			addAffected( action , linux , proxyPath , true );
		}
		
		if( executor.name.equals( BuildCommandExecutor.NAME ) ) {
			for( VarBUILDMODE mode : VarBUILDMODE.values() ) {
				if( mode == VarBUILDMODE.UNKNOWN )
					continue;
				
				configureBuildMode( action , exeFolder , executor , mode , linux );
			}
		}
	}

	private void addAffected( ActionInit action , boolean linux , String proxyPath , boolean recursive ) throws Exception {
		String item = ( linux )? ".sh" : ".cmd";
		item += ":" + proxyPath + "/";
		item += ( recursive )? ":*" : ":F";
		linesAffected.add( item );
	}
	
	private void configureDeploymentEnv( ActionInit action , LocalFolder ef , CommandExecutor executor , String envFile , MetaEnv env , MetaEnvDC dc , boolean linux , CommandExecutor dbe ) throws Exception {
		LocalFolder efEnv = ef.getSubFolder( action , "env" );
		efEnv.ensureExists( action );
		
		// env-level
		if( USEDC.isEmpty() || !env.isMultiDC( action ) )
			configureDeploymentEnvContent( action , efEnv , executor , envFile , null , linux , dbe );
		
		if( env.isMultiDC( action ) ) {
			if( USEDC.isEmpty() ) {
				for( MetaEnvDC envdc : env.getOriginalDCList( action ) ) {
					LocalFolder efEnvDC = efEnv.getSubFolder( action , envdc.NAME );
					configureDeploymentEnvContent( action , efEnvDC , executor , envFile , envdc.NAME , linux , dbe );
				}
			}
			else {
				LocalFolder efEnvDC = efEnv.getSubFolder( action , dc.NAME );
				configureDeploymentEnvContent( action , efEnvDC , executor , envFile , dc.NAME , linux , dbe );
			}
		}
	}

	private void configureDeploymentEnvContent( ActionInit action , LocalFolder ef , CommandExecutor executor , String ENVFILE , String DC , boolean linux , CommandExecutor dbe ) throws Exception {
		// env-level context
		ef.ensureExists( action );
		configureExecutorContextDeployment( action , ef , ENVFILE , DC , linux );

		String xp = ( DC == null )? "../.." : "../../..";
		
		// env-level wrappers
		for( CommandAction cmdAction : executor.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( action , ef , executor , cmdAction.name , linux , xp , true , null );
		}
		
		// database wrappers
		LocalFolder efDB = ef.getSubFolder( action , DatabaseCommandExecutor.NAME );
		efDB.ensureExists( action );
		for( CommandAction cmdAction : dbe.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( action , efDB , executor , cmdAction.name , linux , xp + "/.." , true , ".." );
		}
	}
	
	private void configureBuildMode( ActionInit action , LocalFolder ef , CommandExecutor executor , VarBUILDMODE mode , boolean linux ) throws Exception {
		LocalFolder efBuild = ef.getSubFolder( action , Common.getEnumLower( mode ) );
		efBuild.ensureExists( action );
		configureExecutorContextBuildMode( action , efBuild , mode , linux );
		
		// env-level wrappers
		for( CommandAction cmdAction : executor.actionsList ) {
			if( !cmdAction.top )
				configureExecutorWrapper( action , efBuild , executor , cmdAction.name , linux , "../.." , true , null );
		}
	}

	private void configureExecutorContextDeployment( ActionInit action , LocalFolder ef , String ENVFILE , String DC , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "export C_CONTEXT_ENV=" + ENVFILE );
			lines.add( "export C_CONTEXT_DC=" + DC );
			Common.createFileFromStringList( ef.getFilePath( action , CONTEXT_FILENAME_LIXUX ) , lines );
		}
		else {
			lines.add( "set C_CONTEXT_ENV=" + ENVFILE );
			lines.add( "set C_CONTEXT_DC=" + DC );			
			Common.createFileFromStringList( ef.getFilePath( action , CONTEXT_FILENAME_WIN ) , lines );
		}
	}
	
	private void configureExecutorContextBuildMode( ActionInit action , LocalFolder ef , VarBUILDMODE mode , boolean linux ) throws Exception {
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "export C_CONTEXT_VERSIONMODE=" + Common.getEnumLower( mode ) );
			Common.createFileFromStringList( ef.getFilePath( action , CONTEXT_FILENAME_LIXUX ) , lines );
		}
		else {
			lines.add( "set C_CONTEXT_VERSIONMODE=" + Common.getEnumLower( mode ) );
			Common.createFileFromStringList( ef.getFilePath( action , CONTEXT_FILENAME_WIN ) , lines );
		}
	}
	
	private void configureExecutorWrapper( ActionInit action , LocalFolder ef , CommandExecutor executor , String method , boolean linux , String relativePath , boolean context , String relativeContext ) throws Exception {
		String fileName = method + ( ( linux )? ".sh" : ".cmd" );
		String filePath = ef.getFilePath( action , fileName );

		File f = new File( filePath );
		if( f.exists() )
			f.delete();
		
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "#!/bin/bash" );
			lines.add( "cd `dirname $0`" );
			if( context ) {
				if( relativeContext == null )
					lines.add( ". ./" + CONTEXT_FILENAME_LIXUX );
				else
					lines.add( ". " + relativeContext + "/" + CONTEXT_FILENAME_LIXUX );
			}
			lines.add( relativePath + "/bin/urm.sh " + executor.name + " " + method + " " + Common.getQuoted( "$@" ) );			
		}
		else {
			relativePath = Common.getWinPath( relativePath );
			lines.add( "@cd %~dp0" );
			if( context ) {
				if( relativeContext == null )
					lines.add( "call " + CONTEXT_FILENAME_WIN );
				else
					lines.add( "call " + Common.getWinPath( relativeContext + "/" + CONTEXT_FILENAME_WIN ) );
			}
			lines.add( "@" + relativePath + "\\bin\\urm.cmd " + executor.name + " " + method + " %*" );			
		}
		
		Common.createFileFromStringList( filePath , lines );
		addProxyLine( action , ef , fileName );
	}

	private void addProxyLine( ActionInit action , LocalFolder ef , String fileName ) throws Exception {
		String subPath = Common.getPartAfterFirst( ef.getFilePath( action , fileName ) , pfMaster.folderPath + "/" );
		linesProxy.add( PROXYPREFIX + subPath );
	}
	}

	private class SvnSave extends CommandAction {
		LocalFolder pfMaster = null;
		SubversionVCS vcs = null;
		
	public void run( ActionInit action ) throws Exception {
		LocalFolder pf = action.artefactory.getProductFolder( action );
		pfMaster = pf.getSubFolder( action , "master" );
		
		// read master file and make up all files to the list
		String masterPath = pfMaster.getFilePath( action , MASTERFILE );
		List<String> lines = ConfReader.readFileLines( action , masterPath );
		FileSet set = pfMaster.getFileSet( action );
		
		vcs = action.artefactory.getSvnDirect( action );
		List<String> filesNotInSvn = vcs.getFilesNotInSvn( action , pfMaster );
		
		executeDir( action , set , lines , filesNotInSvn );
		vcs.commitMasterFolder( pfMaster , "" , "" , "svnsave" );
	}

	private void executeDir( ActionInit action , FileSet set , List<String> lines , List<String> filesNotInSvn ) throws Exception {
		for( FileSet dir : set.dirs.values() ) {
			// check dir in lines
			boolean dirInLines = false;
			for( String line : lines ) {
				String filePath = Common.getPartAfterFirst( line , ":" );
				if( filePath.startsWith( dir.dirPath ) ) {
					dirInLines = true;
					action.trace( "executeDir: dirInLines " + filePath + " in " + dir.dirPath );
					break;
				}
			}
			
			boolean dirInSvn = checkDirInSvn( action , dir.dirPath , filesNotInSvn );
			if( dirInLines && dirInSvn )
				executeDir( action , dir , lines , filesNotInSvn );
			else {
				if( dirInLines )
					vcs.addDirToSvn( action , pfMaster , dir.dirPath );
				else
					vcs.deleteDirFromSvn( action , pfMaster , dir.dirPath );
			}
		}
		
		for( String fileActual : set.files.values() ) {
			// check file in lines
			boolean fileInLines = false;
			for( String line : lines ) {
				String filePath = Common.getPartAfterFirst( line , ":" );
				if( fileActual.equals( filePath ) ) {
					fileInLines = true;
					action.trace( "executeDir: fileInLines " + filePath );
					break;
				}
			}
			
			boolean fileInSvn = checkFileInSvn( action , fileActual , filesNotInSvn );
			if( fileInLines && fileInSvn )
				continue;
			
			if( fileInLines )
				vcs.addFileToSvn( action , pfMaster , fileActual );
			else
				vcs.deleteFileFromSvn( action , pfMaster , fileActual );
		}
	}

	private boolean checkDirInSvn( ActionInit action , String dirPath , List<String> filesNotInSvn ) throws Exception {
		for( String xMissing : filesNotInSvn ) {
			if( dirPath.equals( xMissing ) || dirPath.startsWith( xMissing + "/" ) ) {
				action.trace( "checkDirInSvn: false, dirPath=" + dirPath + ", filesNotInSvn=" + xMissing );
				return( false );
			}
		}
		
		action.trace( "checkDirInSvn: true, dirPath=" + dirPath );
		return( true );
	}
	
	private boolean checkFileInSvn( ActionInit action , String filePath , List<String> filesNotInSvn ) throws Exception {
		for( String xMissing : filesNotInSvn ) {
			if( filePath.equals( xMissing ) || filePath.startsWith( xMissing + "/" ) ) {
				action.trace( "checkFileInSvn: false, filePath=" + filePath + ", filesNotInSvn=" + xMissing );
				return( false );
			}
		}
		
		action.trace( "checkFileInSvn: true, filePath=" + filePath );
		return( true );
	}
	}

}
