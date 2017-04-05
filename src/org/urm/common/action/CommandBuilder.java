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
	public OptionsMeta optionsMeta;
	public CommandMeta commandInfo;
	
	public void out( String s ) {
		System.out.println( "# " + s );
	}

	public CommandBuilder( RunContext clientrc , RunContext execrc , OptionsMeta options ) {
		this.clientrc = clientrc;
		this.execrc = execrc;
	}

	public CommandMeta buildCommand( String[] args , CommandOptions options ) throws Exception {
		if( args.length == 0 ) {
			showTopHelp( options );
			return( null );
		}
		
		// discriminate
		String cmd = args[0]; 
		CommandMeta commandInfo = createMeta( cmd );
		if( commandInfo == null )
			return( null );
	
		if( !setOptions( commandInfo , args , options ) )
			return( null );
		
		return( commandInfo );
	}

	public CommandMeta createMeta( String cmd ) {
		// discriminate
		CommandMeta commandInfo = null;
		if( cmd.equals( MainCommandMeta.NAME ) )
			commandInfo = new MainCommandMeta( optionsMeta );
		else if( cmd.equals( BuildCommandMeta.NAME ) )
			commandInfo = new BuildCommandMeta( optionsMeta );
		else if( cmd.equals( DeployCommandMeta.NAME ) )
			commandInfo = new DeployCommandMeta( optionsMeta );
		else if( cmd.equals( DatabaseCommandMeta.NAME ) )
			commandInfo = new DatabaseCommandMeta( optionsMeta );
		else if( cmd.equals( MonitorCommandMeta.NAME ) )
			commandInfo = new MonitorCommandMeta( optionsMeta );
		else if( cmd.equals( ReleaseCommandMeta.NAME ) )
			commandInfo = new ReleaseCommandMeta( optionsMeta );
		else if( cmd.equals( XDocCommandMeta.NAME ) )
			commandInfo = new XDocCommandMeta( optionsMeta );
		else
			out( "Unexpected URM args - unknown command executor=" + cmd + " (expected one of " + 
					MainCommandMeta.NAME + "/" + 
					BuildCommandMeta.NAME + "/" + 
					DeployCommandMeta.NAME + "/" + 
					DatabaseCommandMeta.NAME + "/" + 
					MonitorCommandMeta.NAME + "/" + 
					ReleaseCommandMeta.NAME + "/" + 
					XDocCommandMeta.NAME + ")" );
			
		return( commandInfo );
	}
	
	public boolean setOptions( CommandMeta commandInfo , String[] args , CommandOptions options ) throws Exception {
		this.commandInfo = commandInfo;
		
		// process options
		if( !options.parseArgs( clientrc , args ) ) {
			showTopHelp( options );
			return( false );
		}

		if( checkHelp( options ) )
			return( false );
		
		return( true );
	}

	public CommandMeta[] getExecutors( boolean build , boolean deploy ) {
		List<CommandMeta> list = new LinkedList<CommandMeta>();
		if( build )
			list.add( new BuildCommandMeta( optionsMeta ) );
		if( deploy ) {
			list.add( new DeployCommandMeta( optionsMeta ) );
			list.add( new MonitorCommandMeta( optionsMeta ) );
		}
		if( build || deploy ) {
			list.add( new DatabaseCommandMeta( optionsMeta ) );
			list.add( new ReleaseCommandMeta( optionsMeta ) );
			list.add( new XDocCommandMeta( optionsMeta ) );
		}
		
		return( list.toArray( new CommandMeta[0] ) );
	}

	public boolean isInteractive( CommandOptions options ) throws Exception {
		CommandMethodMeta method = commandInfo.getMethod( options.method );
		return( method.isInteractive() );
	}
	
	public void showTopHelp( CommandOptions options ) {
		CommandMeta main = new MainCommandMeta( optionsMeta );
		options.showTopHelp( this , main , getExecutors( true , true ) );
	}

	public boolean checkHelp( CommandOptions options ) throws Exception {
		// top help
		if( options.command.equals( MainCommandMeta.NAME ) && 
			options.method.equals( "help" ) && 
			options.getArgCount() == 0 ) {
			showTopHelp( options );
			return( true );
		}

		// command help
		if( ( options.command.equals( MainCommandMeta.NAME ) && 
				options.method.equals( "help" ) && 
				options.getArgCount() == 1 ) ||
			( options.command.equals( MainCommandMeta.NAME ) == false &&
				options.method.equals( "help" ) && 
				options.getArgCount() == 0 ) ) {
			String command = ( options.command.equals( MainCommandMeta.NAME ) )? options.getArg( 0 ) : options.command;
			CommandMeta meta = ( command.equals( MainCommandMeta.NAME ) )? new MainCommandMeta( optionsMeta ) : createMeta( command );
			boolean main = options.command.equals( MainCommandMeta.NAME );
			
			options.showCommandHelp( this , meta , main );
			return( true );
		}

		// action help
		if( ( options.command.equals( MainCommandMeta.NAME ) && 
				options.method.equals( "help" ) && 
				options.getArgCount() >= 2 ) ||
			( options.command.equals( MainCommandMeta.NAME ) && 
				options.method.equals( "help" ) == false && 
				options.getArgCount() >= 1 &&
				options.getArg( 0 ).equals( "help" ) ) ||
			( options.command.equals( MainCommandMeta.NAME ) == false &&
				options.method.equals( "help" ) && 
				options.getArgCount() > 0 ) ||
			( options.command.equals( MainCommandMeta.NAME ) == false &&
				options.method.equals( "help" ) == false && 
				options.getArgCount() > 0 &&
				options.getArg( 0 ).equals( "help" ) ) ) {
			String command = ( options.command.equals( MainCommandMeta.NAME ) && options.method.equals( "help" ) )? options.getArg( 0 ) : options.command;
			CommandMeta meta = ( command.equals( "bin" ) )? new MainCommandMeta( optionsMeta ) : createMeta( command );
			
			String action = ( options.command.equals( MainCommandMeta.NAME ) && options.method.equals( "help" ) )? options.getArg( 1 ) :
				( ( options.method.equals( "help" ) )? options.getArg( 0 ) : options.method );
			CommandMethodMeta method = meta.getMethod( action );
			options.showActionHelp( this , method );
			return( true );
		}
		
		return( false );
	}
	
}
