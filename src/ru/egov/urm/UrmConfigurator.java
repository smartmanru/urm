package ru.egov.urm;

import ru.egov.urm.action.ActionInit;
import ru.egov.urm.action.CommandAction;
import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;
import ru.egov.urm.storage.LocalFolder;

public class UrmConfigurator extends CommandExecutor {

	public UrmConfigurator( CommandBuilder builder ) {
		super( builder );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new ConfigureLinux() , "configure-linux" , "configure as linux" , cmdOpts , "./configure.sh [OPTIONS] {all|build|env} [envname [dcname]]" ) );
		super.defineAction( CommandAction.newAction( new ConfigureWindows() , "configure-windows" , "configure as windows" , cmdOpts , "./configure.cmd [OPTIONS] {all|build|env} [envname [dcname]]" ) );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private void configureDefault( ActionInit action ) throws Exception {
		LocalFolder pf = action.artefactory.getProductFolder( action );
		LocalFolder pfBuild = pf.getSubFolder( action , "makedistr" );
		LocalFolder pfDeploy = pf.getSubFolder( action , "deployment" );
		
		boolean buildUnix = ( pfBuild.findFiles( action , "*.sh" ).length > 0 )? true : false;
		boolean buildWindows = ( pfBuild.findFiles( action , "*.cmd" ).length > 0 )? true : false;
		boolean deployUnix = ( pfDeploy.findFiles( action , "*.sh" ).length > 0 )? true : false;
		boolean deployWindows = ( pfDeploy.findFiles( action , "*.cmd" ).length > 0 )? true : false;
		
		if( buildUnix || deployUnix )
			configureAll( action , buildUnix , deployUnix , "" , true );
		if( buildWindows || deployWindows )
			configureAll( action , buildWindows , deployWindows , "" , true );
	}
	
	private void configureAll( ActionInit action , boolean build , boolean deploy , String ENV , boolean linux ) throws Exception {
	}
	
	private class ConfigureLinux extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String ACTION = options.getRequiredArg( action , 0 , "ACTION" );
		
		if( ACTION.equals( "default" ) )
			configureDefault( action );
		else
		if( ACTION.equals( "build" ) )
			configureAll( action , true , false , null , true );
		else
		if( ACTION.equals( "deploy" ) ) {
			String ENV = options.getRequiredArg( action , 1 , "ENV" );
			configureAll( action , false , true , ENV , true );
		}
		else
		if( ACTION.equals( "all" ) )
			configureAll( action , true , true , "" , true );
		else
			action.exitUnexpectedState();
	}
	}

	private class ConfigureWindows extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String ACTION = options.getRequiredArg( action , 0 , "ACTION" );
		
		if( ACTION.equals( "default" ) )
			configureDefault( action );
		else
		if( ACTION.equals( "build" ) )
			configureAll( action , true , false , null , false );
		else
		if( ACTION.equals( "deploy" ) ) {
			String ENV = options.getRequiredArg( action , 1 , "ENV" );
			configureAll( action , false , true , ENV , false );
		}
		else
		if( ACTION.equals( "all" ) )
			configureAll( action , true , true , "" , false );
		else
			action.exitUnexpectedState();
	}
	}

}
