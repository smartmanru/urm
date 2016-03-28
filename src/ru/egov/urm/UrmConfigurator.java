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

	public UrmConfigurator( CommandBuilder builder ) {
		super( builder , "bin" );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ConfigureLinux() , "configure-linux" , true , "configure as linux" , cmdOpts , "./configure.sh [OPTIONS] {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandAction.newAction( new ConfigureWindows() , "configure-windows" , true , "configure as windows" , cmdOpts , "./configure.cmd [OPTIONS] {all|build|env} [envname [dcname]]" ) );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private void configureDefault( ActionInit action , LocalFolder pf ) throws Exception {
		LocalFolder pfBuild = pf.getSubFolder( action , "makedistr" );
		LocalFolder pfDeploy = pf.getSubFolder( action , "deployment" );
		
		boolean buildUnix = ( pfBuild.findFiles( action , "*.sh" ).length > 0 )? true : false;
		boolean buildWindows = ( pfBuild.findFiles( action , "*.cmd" ).length > 0 )? true : false;
		boolean deployUnix = ( pfDeploy.findFiles( action , "*.sh" ).length > 0 )? true : false;
		boolean deployWindows = ( pfDeploy.findFiles( action , "*.cmd" ).length > 0 )? true : false;
		
		if( buildUnix || deployUnix )
			configureAll( action , pf , buildUnix , deployUnix , "" , true );
		if( buildWindows || deployWindows )
			configureAll( action , pf , buildWindows , deployWindows , "" , false );
	}
	
	private void configureAll( ActionInit action , LocalFolder pf , boolean build , boolean deploy , String ENV , boolean linux ) throws Exception {
		CommandExecutor[] executors = builder.getExecutors( action , build , deploy );
		CommandExecutor dbe = null;
		for( CommandExecutor executor : executors ) {
			if( executor.name.equals( "database" ) ) {
				dbe = executor;
				break;
			}
		}
			
		for( CommandExecutor executor : executors )
			configureExecutor( action , pf , executor , ENV , linux , dbe );
	}

	private void configureExecutor( ActionInit action , LocalFolder pf , CommandExecutor executor , String ENV , boolean linux , CommandExecutor dbe ) throws Exception {
		LocalFolder exeFolder = pf.getSubFolder( action , executor.name );
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

		File f = new File( fileName );
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
		
		Common.createFileFromStringList( fileName , lines );
	}
	
	private class ConfigureLinux extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String ACTION = options.getRequiredArg( action , 0 , "ACTION" );
		
		LocalFolder pf = action.artefactory.getProductFolder( action );
		if( ACTION.equals( "default" ) )
			configureDefault( action , pf );
		else
		if( ACTION.equals( "build" ) )
			configureAll( action , pf , true , false , null , true );
		else
		if( ACTION.equals( "deploy" ) ) {
			String ENV = options.getRequiredArg( action , 1 , "ENV" );
			configureAll( action , pf , false , true , ENV , true );
		}
		else
		if( ACTION.equals( "all" ) )
			configureAll( action , pf , true , true , "" , true );
		else
			action.exitUnexpectedState();
	}
	}

	private class ConfigureWindows extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String ACTION = options.getRequiredArg( action , 0 , "ACTION" );
		
		LocalFolder pf = action.artefactory.getProductFolder( action );
		if( ACTION.equals( "default" ) )
			configureDefault( action , pf );
		else
		if( ACTION.equals( "build" ) )
			configureAll( action , pf , true , false , null , false );
		else
		if( ACTION.equals( "deploy" ) ) {
			String ENV = options.getRequiredArg( action , 1 , "ENV" );
			configureAll( action , pf , false , true , ENV , false );
		}
		else
		if( ACTION.equals( "all" ) )
			configureAll( action , pf , true , true , "" , false );
		else
			action.exitUnexpectedState();
	}
	}

}
