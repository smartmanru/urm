package ru.egov.urm;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.action.ActionInit;
import ru.egov.urm.action.CommandAction;
import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.MetadataStorage;
import ru.egov.urm.vcs.SubversionVCS;

public class MainExecutor extends CommandExecutor {

	public static String NAME = "bin";
	public static String MASTERFILE = "master.files.info";
	public static String PROXYPREFIX = "proxy:";
	
	public MainExecutor( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new Configure() , "configure" , true , "configure proxy files" , cmdOpts , "configure [OPTIONS] {linux|windows} {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandAction.newAction( new SvnSave() , "svnsave" , true , "save master file set in svn" , cmdOpts , "svnsave [OPTIONS]" ) );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}
	
	private class Configure extends CommandAction {
		LocalFolder pfMaster = null;
		String ENV;
		String DC;
		List<String> linesProxy;
		List<String> linesAffected;
		
	public void run( ActionInit action ) throws Exception {
		String PLATFORM = options.getRequiredArg( action , 0 , "PLATFORM" );
		String ACTION = options.getRequiredArg( action , 1 , "ACTION" );
		
		boolean linux = ( PLATFORM.equals( "linux" ) )? true : false;
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
			ENV = options.getArg( 2 );
			DC = options.getArg( 3 );
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
				
				if( affected )
					linesNew.add( s );
			}
		}
		
		linesNew.addAll( linesProxy );
		Common.createFileFromStringList( masterPath , linesNew );
	}

	private void configureDefault( ActionInit action , LocalFolder pfMaster ) throws Exception {
		LocalFolder pfBuild = pfMaster.getSubFolder( action , "makedistr" );
		LocalFolder pfDeploy = pfMaster.getSubFolder( action , "deployment" );
		
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
			if( executor.name.equals( "database" ) ) {
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
		configureExecutorWrapper( action , exeFolder , executor , "help" , linux );
		
		// top items
		for( CommandAction cmdAction : executor.actionsList ) {
			if( cmdAction.top )
				configureExecutorWrapper( action , exeFolder , executor , cmdAction.name , linux );
		}
		
		if( executor.name.equals( "deployment" ) ) {
			String proxyPath = "deployment";
			
			List<MetaEnv> envs = new LinkedList<MetaEnv>(); 
			MetadataStorage ms = action.artefactory.getMetadataStorage( action );
			
			if( ENV.isEmpty() ) {
				addAffected( action , linux , proxyPath , true );
				String[] envFiles = ms.getEnvFiles( action );
				for( String envFile : envFiles ) {
					MetaEnv env = meta.loadEnvData( action , envFile , false );
					envs.add( env );
				}
			}
			else {
				addAffected( action , linux , proxyPath , false );
				MetaEnv env = meta.loadEnvData( action , ENV , false );
				
				if( DC.isEmpty() ) {
					proxyPath = Common.getPath( proxyPath , ENV );
					addAffected( action , linux , proxyPath , true );
				}
				else {
					proxyPath = Common.getPath( proxyPath , ENV , DC );
					addAffected( action , linux , proxyPath , true );
				}
					
				envs.add( env );
			}
			
			for( MetaEnv env : envs )
				configureDeploymentEnv( action , exeFolder , executor , env , linux , dbe );
		}
		else {
			String proxyPath = executor.name;
			addAffected( action , linux , proxyPath , true );
		}
		
		if( executor.name.equals( "makedistr" ) ) {
			for( VarBUILDMODE mode : VarBUILDMODE.values() ) {
				if( mode == VarBUILDMODE.UNKNOWN )
					continue;
				
				configureMakedistrMode( action , exeFolder , executor , mode , linux );
			}
		}
	}

	private void addAffected( ActionInit action , boolean linux , String proxyPath , boolean recursive ) throws Exception {
		String item = ( linux )? ".sh" : ".cmd";
		item += ":" + proxyPath + "/";
		item += ( recursive )? ":*" : ":F";
		linesAffected.add( item );
	}
	
	private void configureDeploymentEnv( ActionInit action , LocalFolder ef , CommandExecutor executor , MetaEnv env , boolean linux , CommandExecutor dbe ) throws Exception {
	}

	private void configureMakedistrMode( ActionInit action , LocalFolder ef , CommandExecutor executor , VarBUILDMODE mode , boolean linux ) throws Exception {
	}
	
	private void configureExecutorWrapper( ActionInit action , LocalFolder ef , CommandExecutor executor , String method , boolean linux ) throws Exception {
		String fileName = method + ( ( linux )? ".sh" : ".cmd" );
		String filePath = ef.getFilePath( action , fileName );

		File f = new File( filePath );
		if( f.exists() )
			f.delete();
		
		List<String> lines = new LinkedList<String>();
		if( linux ) {
			lines.add( "#!/bin/bash" );
			lines.add( "cd `dirname $0`" );
			lines.add( "../bin/urm.sh " + executor.name + " " + method + " " + Common.getQuoted( "$@" ) );			
		}
		else {
			lines.add( "@cd %~dp0" );
			lines.add( "@..\\bin\\urm.cmd " + executor.name + " " + method + " %*" );			
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
				String filePath = Common.getPartAfterFirst( line , PROXYPREFIX );
				if( filePath.startsWith( set.dirPath ) ) {
					dirInLines = true;
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
				String filePath = Common.getPartAfterFirst( line , PROXYPREFIX );
				if( fileActual.equals( filePath ) ) {
					fileInLines = true;
					break;
				}
			}
			
			boolean fileInSvn = checkFileInSvn( action , Common.getPath( set.dirPath , fileActual ) , filesNotInSvn );
			if( fileInLines && fileInSvn )
				continue;
			
			if( fileInLines )
				vcs.addFileToSvn( action , pfMaster , fileActual );
			else
				vcs.deleteFileFromSvn( action , pfMaster , fileActual );
		}
	}

	private boolean checkDirInSvn( ActionInit action , String dirPath , List<String> filesNotInSvn ) throws Exception {
		for( String xMissing : filesNotInSvn )
			if( dirPath.equals( xMissing ) || dirPath.startsWith( xMissing + "/" ) )
				return( false );
		return( true );
	}
	
	private boolean checkFileInSvn( ActionInit action , String filePath , List<String> filesNotInSvn ) throws Exception {
		for( String xMissing : filesNotInSvn )
			if( filePath.equals( xMissing ) || filePath.startsWith( xMissing + "/" ) )
				return( false );
		return( true );
	}
	}

}
