package ru.egov.urm.action;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.RunContext;
import ru.egov.urm.UrmConfigurator;
import ru.egov.urm.action.build.BuildCommandExecutor;
import ru.egov.urm.action.database.DatabaseCommandExecutor;
import ru.egov.urm.action.deploy.DeployCommandExecutor;
import ru.egov.urm.action.monitor.MonitorCommandExecutor;
import ru.egov.urm.action.release.ReleaseCommandExecutor;
import ru.egov.urm.action.xdoc.XDocCommandExecutor;
import ru.egov.urm.meta.Metadata;

public class CommandBuilder {

	public RunContext rc;
	public String cmd;
	public CommandOutput output = null;
	public CommandOptions options = null;
	public CommandContext context = null;
	
	void out( String s ) {
		System.out.println( "# " + s );
	}

	public CommandBuilder() {
	}

	public CommandExecutor buildCommand( String[] args ) throws Exception {
		rc = new RunContext();
		rc.load();
		
		String urmName = ( rc.OSTYPE.equals( "WINDOWS" ) )? "urm.cmd" : "./urm.sh";
		if( args.length == 0 ) {
			out( "URM HELP" );
			out( "Available operations:" );
			out( "\t" + urmName + " help" );
			out( "\t" + urmName + " help cmd" );
			out( "\t" + urmName + " cmd help" );
			out( "\t" + urmName + " cmd [options] [args]" );
			return( null );
		}
		
		cmd = args[0]; 
		String helpName = ( rc.OSTYPE.equals( "WINDOWS" ) )? "help.cmd" : "./help.sh";
		if( cmd.equals( "help" ) ) { 
			out( "URM HELP" );
			out( "Syntax: " + urmName + " <command> <action> <args>" );
			out( "Available commands are:" );
			out( "\tbuild - build sources, codebase and distributive management" );
			out( "\tdeploy - deploy distributive items to environment and environment maintenance operations" );
			out( "\tdatabase - apply database changes, perform database maintenance operations" );
			out( "\tmonitor - check environments and create monitoring reports" );
			out( "\trelease - release operations" );
			out( "\txdoc - create technical documentation" );
			out( "" );
			out( "To see help on operations run " + helpName + " <command> [<action>]" );
			return( null );
		}

		// discriminate
		CommandExecutor executor;
		if( cmd.equals( "configure" ) )
			executor = new UrmConfigurator( this );
		else if( cmd.equals( "build" ) )
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
			out( "Unexpected URM args - unknown command category=" + cmd + " (need one of build/deploy/database/monitor)" );
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

		return( executor );
	}

	public CommandExecutor[] getExecutors( ActionBase action , boolean build , boolean deploy ) throws Exception {
		List<CommandExecutor> list = new LinkedList<CommandExecutor>();
		if( build )
			list.add( new BuildCommandExecutor( this ) );
		if( deploy ) {
			list.add( new DeployCommandExecutor( this ) );
			list.add( new MonitorCommandExecutor( this ) );
		}
		if( build || deploy ) {
			list.add( new DatabaseCommandExecutor( this ) );
			list.add( new ReleaseCommandExecutor( this ) );
			list.add( new XDocCommandExecutor( this ) );
		}
		
		return( list.toArray( new CommandExecutor[0] ) );
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
		if( !context.loadDefaults( executor.rc ) )
			return( false );

		return( true );
	}
	
}
