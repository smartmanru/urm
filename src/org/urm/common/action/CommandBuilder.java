package org.urm.common.action;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.RunContext;
import org.urm.common.meta.BuildCommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.common.meta.MainCommandMeta;
import org.urm.common.meta.MonitorCommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.common.meta.XDocCommandMeta;

public class CommandBuilder {

	public RunContext clientrc;
	public RunContext execrc;
	public CommandOptions options = null;
	
	void out( String s ) {
		System.out.println( "# " + s );
	}

	public CommandBuilder( RunContext clientrc , RunContext execrc ) {
		this.clientrc = clientrc;
		this.execrc = execrc;
	}

	public CommandMeta buildCommand( String[] args ) throws Exception {
		String urmName = ( execrc.isWindows() )? "urm.cmd" : "./urm.sh";
		if( args.length == 0 ) {
			out( "URM HELP" );
			out( "Available operations:" );
			out( "\t" + urmName + " help" );
			out( "\t" + urmName + " help cmd" );
			out( "\t" + urmName + " cmd help" );
			out( "\t" + urmName + " cmd [options] [args]" );
			return( null );
		}
		
		String cmd = args[0]; 
		String helpName = ( execrc.isWindows() )? "help.cmd" : "./help.sh";
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
		CommandMeta commandInfo = createMeta( cmd );
		if( commandInfo == null )
			return( null );
	
		if( !setOptions( commandInfo , args ) )
			return( null );
		
		return( commandInfo );
	}

	public CommandMeta createMeta( String cmd ) {
		// discriminate
		CommandMeta commandInfo = null;
		if( cmd.equals( BuildCommandMeta.NAME ) )
			commandInfo = new BuildCommandMeta( this );
		else if( cmd.equals( DeployCommandMeta.NAME ) )
			commandInfo = new DeployCommandMeta( this );
		else if( cmd.equals( DatabaseCommandMeta.NAME ) )
			commandInfo = new DatabaseCommandMeta( this );
		else if( cmd.equals( MonitorCommandMeta.NAME ) )
			commandInfo = new MonitorCommandMeta( this );
		else if( cmd.equals( ReleaseCommandMeta.NAME ) )
			commandInfo = new ReleaseCommandMeta( this );
		else if( cmd.equals( XDocCommandMeta.NAME ) )
			commandInfo = new XDocCommandMeta( this );
		else
			out( "Unexpected URM args - unknown command executor=" + cmd + " (expected one of build/deploy/database/monitor)" );
			
		return( commandInfo );
	}
	
	public boolean setOptions( CommandMeta commandInfo , String[] args ) throws Exception {
		// process options
		options = new CommandOptions( commandInfo );
		if( !options.parseArgs( args ) ) {
			if( options.action != null && !options.action.equals( "help" ) )
				return( false );
				
			if( commandInfo.name.equals( MainCommandMeta.NAME ) ) {
				MainCommandMeta main = new MainCommandMeta( this );
				CommandMeta[] executors = getExecutors( true , true );
				options.showTopHelp( this , main , executors );
				return( false );
			}
				
			options.showCommandHelp( this , commandInfo );
			return( false );
		}

		return( true );
	}

	public CommandMeta[] getExecutors( boolean build , boolean deploy ) throws Exception {
		List<CommandMeta> list = new LinkedList<CommandMeta>();
		if( build )
			list.add( new BuildCommandMeta( this ) );
		if( deploy ) {
			list.add( new DeployCommandMeta( this ) );
			list.add( new MonitorCommandMeta( this ) );
		}
		if( build || deploy ) {
			list.add( new DatabaseCommandMeta( this ) );
			list.add( new ReleaseCommandMeta( this ) );
			list.add( new XDocCommandMeta( this ) );
		}
		
		return( list.toArray( new CommandMeta[0] ) );
	}

	public String getCommandMBeanName( String productDir , String command ) {
		return( "urm-" + productDir + ":" + "name=" + command );
	}
	
	public boolean isLocalRun() {
		return( options.getFlagValue( "OPT_LOCAL" , false ) );
	}
	
}
