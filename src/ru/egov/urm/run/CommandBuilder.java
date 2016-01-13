package ru.egov.urm.run;

import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.build.BuildCommandExecutor;
import ru.egov.urm.run.database.DatabaseCommandExecutor;
import ru.egov.urm.run.deploy.DeployCommandExecutor;
import ru.egov.urm.run.monitor.MonitorCommandExecutor;
import ru.egov.urm.run.release.ReleaseCommandExecutor;
import ru.egov.urm.run.xdoc.XDocCommandExecutor;

public class CommandBuilder {

	public String cmd;
	public CommandOutput output = null;
	public CommandOptions options = null;
	
	CommandContext context;
	
	void out( String s ) {
		System.out.println( s );
	}

	public CommandBuilder() {
	}

	public CommandExecutor buildCommand( String[] args ) throws Exception {
		if( args.length == 0 ) {
			out( "invalid URM call, execute one of:" );
			out( "urm help" );
			out( "urm cmd help" );
			out( "urm help cmd" );
			out( "urm cmd [options] [args]" );
			return( null );
		}
		
		cmd = args[0]; 
		if( cmd.equals( "help" ) ) { 
			out( "URM HELP" );
			out( "Available commands are:" );
			out( "\tbuild - build sources, codebase and distributive management" );
			out( "\tdeploy - deploy distributive items to environment and environment maintenance operations" );
			out( "\tdatabase - apply database changes, perform database maintenance operations" );
			out( "\tmonitor - check environments and create monitoring reports" );
			out( "\trelease - release operations" );
			out( "\txdoc - create technical documentation" );
			out( "" );
			out( "To see help on operations run ./help.sh <command> [<action>]" );
			return( null );
		}

		// discriminate
		CommandExecutor executor;
		if( cmd.equals( "build" ) )
			executor = new BuildCommandExecutor( this );
		else if( cmd.equals( "deploy" ) )
			executor = new DeployCommandExecutor( this );
		else if( cmd.equals( "database" ) )
			executor = new DatabaseCommandExecutor( this );
		else if( cmd.equals( "monitor" ) )
			executor = new MonitorCommandExecutor( this );
		else if( cmd.equals( "release" ) )
			executor = new ReleaseCommandExecutor( this );
		else if( cmd.equals( "xdoc" ) )
			executor = new XDocCommandExecutor( this );
		else {
			out( "invalid URM args - unknown command category=" + cmd + " (expected one of build/deploy/database/monitor)" );
			return( null );
		}
		
		// process options
		options = executor.getOptions();
		if( !options.parseArgs( args , executor.manualActions ) ) {
			if( options.command.equals( "help" ) )
				executor.showTopHelp();
			return( null );
		}

		if( !executor.setOptions( options ) )
			return( null );

		// scatter into variables
		options.scatter();
		
		// print
		if( options.OPT_SHOWALL )
			options.printRunningOptions();
		
		return( executor );
	}

	public boolean run( CommandExecutor executor ) throws Exception {
		// create context
		if( !createCommandContext( executor ) )
			return( false );

		// init environment
		Metadata meta = new Metadata();
		ActionInit action = executor.prepare( context , meta );
		
		// execute
		boolean res = false;
		try {
			res = executor.run( action );
		}
		finally {
			executor.finish( action );
			context.killPool( action );
		}

		String name = "URM " + cmd + "::" + executor.commandAction.name;
		if( res )
			action.comment( name + ": COMMAND SUCCESSFUL" );
		else
			action.comment( name + ": COMMAND FAILED" );
			
		return( res );
	}

	private boolean createCommandContext( CommandExecutor executor ) throws Exception {
		context = new CommandContext();
		if( !context.loadDefaults() )
			return( false );

		return( true );
	}
	
}
