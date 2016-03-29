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
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.MetadataStorage;

public class UrmConfigurator extends CommandExecutor {

	public static String NAME = "bin";
	
	public UrmConfigurator( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ConfigureLinux() , "configure-linux" , true , "configure as linux" , cmdOpts , "./configure.sh [OPTIONS] {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandAction.newAction( new ConfigureWindows() , "configure-windows" , true , "configure as windows" , cmdOpts , "./configure.cmd [OPTIONS] {all|build|env} [envname [dcname]]" ) );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
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
			configureAll( action , pfMaster , buildUnix , deployUnix , "" , true );
		if( buildWindows || deployWindows )
			configureAll( action , pfMaster , buildWindows , deployWindows , "" , false );
	}
	
	private void configureAll( ActionInit action , LocalFolder pfMaster , boolean build , boolean deploy , String ENV , boolean linux ) throws Exception {
		CommandExecutor[] executors = builder.getExecutors( action , build , deploy );
		CommandExecutor dbe = null;
		for( CommandExecutor executor : executors ) {
			if( executor.name.equals( "database" ) ) {
				dbe = executor;
				break;
			}
		}
			
		for( CommandExecutor executor : executors )
			configureExecutor( action , pfMaster , executor , ENV , linux , dbe );
	}

	private void configureExecutor( ActionInit action , LocalFolder pfMaster , CommandExecutor executor , String ENV , boolean linux , CommandExecutor dbe ) throws Exception {
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
			List<MetaEnv> envs = new LinkedList<MetaEnv>(); 
			MetadataStorage ms = action.artefactory.getMetadataStorage( action );
			
			if( ENV.isEmpty() ) {
				String[] envFiles = ms.getEnvFiles( action );
				for( String envFile : envFiles ) {
					MetaEnv env = meta.loadEnvData( action , envFile , false );
					envs.add( env );
				}
			}
			else {
				MetaEnv env = meta.loadEnvData( action , ENV , false );
				envs.add( env );
			}
			
			for( MetaEnv env : envs )
				configureDeploymentEnv( action , exeFolder , executor , env , linux , dbe );
		}
		
		if( executor.name.equals( "makedistr" ) ) {
			for( VarBUILDMODE mode : VarBUILDMODE.values() ) {
				if( mode == VarBUILDMODE.UNKNOWN )
					continue;
				
				configureMakedistrMode( action , exeFolder , executor , mode , linux );
			}
		}
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
	}

	private void configureAny( ActionInit action , boolean linux ) throws Exception {
		String ACTION = options.getRequiredArg( action , 0 , "ACTION" );
		
		LocalFolder pf = action.artefactory.getProductFolder( action );
		LocalFolder pfMaster = pf.getSubFolder( action , "master" );
		if( ACTION.equals( "default" ) )
			configureDefault( action , pfMaster );
		else
		if( ACTION.equals( "build" ) )
			configureAll( action , pfMaster , true , false , null , linux );
		else
		if( ACTION.equals( "deploy" ) ) {
			String ENV = options.getArg( 1 );
			configureAll( action , pfMaster , false , true , ENV , linux );
		}
		else
		if( ACTION.equals( "all" ) )
			configureAll( action , pfMaster , true , true , "" , linux );
		else
			action.exitUnexpectedState();
	}
	
	private class ConfigureLinux extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		configureAny( action , true );
	}
	}

	private class ConfigureWindows extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		configureAny( action , false );
	}
	}

}
