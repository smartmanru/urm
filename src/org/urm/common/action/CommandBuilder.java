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
		if( args.length == 0 ) {
			showTopHelp();
			return( null );
		}
		
		String cmd = args[0]; 

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
		options = new CommandOptions();
		if( !options.parseArgs( clientrc , args ) ) {
			showTopHelp();
			return( false );
		}

		if( checkHelp() )
			return( false );
		
		return( true );
	}

	public CommandMeta[] getExecutors( boolean build , boolean deploy ) {
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

	public boolean isLocalRun() {
		return( options.getFlagValue( "OPT_LOCAL" , false ) );
	}

	public void showTopHelp() {
		CommandMeta main = new MainCommandMeta( this );
		options = new CommandOptions();
		options.showTopHelp( this , main , getExecutors( true , true ) );
	}

	public boolean checkHelp() throws Exception {
		// top help
		if( options.command.equals( MainCommandMeta.NAME ) && 
			options.action.equals( "help" ) && 
			options.getArgCount() == 0 ) {
			showTopHelp();
			return( true );
		}

		// command help
		if( ( options.command.equals( MainCommandMeta.NAME ) && 
				options.action.equals( "help" ) && 
				options.getArgCount() == 1 ) ||
			( options.command.equals( MainCommandMeta.NAME ) == false &&
				options.action.equals( "help" ) && 
				options.getArgCount() == 0 ) ) {
			String command = ( options.command.equals( MainCommandMeta.NAME ) )? options.getArg( 0 ) : options.command;
			CommandMeta meta = ( command.equals( MainCommandMeta.NAME ) )? new MainCommandMeta( this ) : createMeta( command );
			boolean main = options.command.equals( MainCommandMeta.NAME );
			
			CommandOptions ho = new CommandOptions();
			ho.showCommandHelp( this , meta , main );
			return( true );
		}

		// action help
		if( ( options.command.equals( MainCommandMeta.NAME ) && 
				options.action.equals( "help" ) && 
				options.getArgCount() >= 2 ) ||
			( options.command.equals( MainCommandMeta.NAME ) && 
				options.action.equals( "help" ) == false && 
				options.getArgCount() >= 1 &&
				options.getArg( 0 ).equals( "help" ) ) ||
			( options.command.equals( MainCommandMeta.NAME ) == false &&
				options.action.equals( "help" ) && 
				options.getArgCount() > 0 ) ||
			( options.command.equals( MainCommandMeta.NAME ) == false &&
				options.action.equals( "help" ) == false && 
				options.getArgCount() > 0 &&
				options.getArg( 0 ).equals( "help" ) ) ) {
			String command = ( options.command.equals( MainCommandMeta.NAME ) && options.action.equals( "help" ) )? options.getArg( 0 ) : options.command;
			CommandMeta meta = ( command.equals( "bin" ) )? new MainCommandMeta( this ) : createMeta( command );
			CommandOptions ho = new CommandOptions();
			
			String action = ( options.command.equals( MainCommandMeta.NAME ) && options.action.equals( "help" ) )? options.getArg( 1 ) :
				( ( options.action.equals( "help" ) )? options.getArg( 0 ) : options.action );
			CommandMethod method = meta.getAction( action );
			ho.showActionHelp( this , method );
			return( true );
		}
		
		return( false );
	}
	
}
