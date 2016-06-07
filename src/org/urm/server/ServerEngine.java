package org.urm.server;

import org.urm.client.meta.BuildCommandMeta;
import org.urm.client.meta.DatabaseCommandMeta;
import org.urm.client.meta.DeployCommandMeta;
import org.urm.client.meta.MonitorCommandMeta;
import org.urm.client.meta.ReleaseCommandMeta;
import org.urm.client.meta.XDocCommandMeta;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandContext;
import org.urm.server.meta.Metadata;

public class ServerEngine {

	public boolean runArgs( String[] args ) throws Exception {
		RunContext rc = new RunContext();
		rc.load();

		if( !rc.isMain() )
			throw new ExitException( "only main executor id expected" );

		CommandBuilder builder = new CommandBuilder( rc );
		CommandExecutor executor = MainExecutor.create( builder );
		
		return( runExecutor( builder , executor ) );
	}
	
	public boolean runClientMode( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		// init action stream
		CommandExecutor executor = createExecutor( commandInfo );
		return( runExecutor( builder , executor ) );
	}
		
	private boolean runExecutor( CommandBuilder builder , CommandExecutor executor ) throws Exception {
		ActionInit action = createAction( builder , executor );
		if( action == null )
			return( false );
		
		// execute
		try {
			executor.run( action );
			action.context.killPool( action );
		}
		catch( Throwable e ) {
			action.log( e );
		}

		boolean res = ( action.context.isFailed() )? false : true;
		
		if( res )
			action.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			action.commentExecutor( "COMMAND FAILED" );
			
		executor.finish( action );
		action.context.stopPool( action );

		return( res );
	}

	private CommandExecutor createExecutor( CommandMeta commandInfo ) throws Exception {
		CommandExecutor executor = null;
		String cmd = commandInfo.name;
		if( cmd.equals( BuildCommandMeta.NAME ) )
			executor = new BuildCommandExecutor( commandInfo );
		else if( cmd.equals( DeployCommandMeta.NAME ) )
			executor = new DeployCommandExecutor( commandInfo );
		else if( cmd.equals( DatabaseCommandMeta.NAME ) )
			executor = new DatabaseCommandExecutor( commandInfo );
		else if( cmd.equals( MonitorCommandMeta.NAME ) )
			executor = new MonitorCommandExecutor( commandInfo );
		else if( cmd.equals( ReleaseCommandMeta.NAME ) )
			executor = new ReleaseCommandExecutor( commandInfo );
		else if( cmd.equals( XDocCommandMeta.NAME ) )
			executor = new XDocCommandExecutor( commandInfo );
		else
			throw new ExitException( "Unexpected URM args - unknown command executor=" + cmd + " (expected one of build/deploy/database/monitor)" );
		
		return( executor );
	}

	public ActionInit createAction( CommandBuilder builder , CommandExecutor executor ) throws Exception {
		// create context
		CommandContext context = new CommandContext( builder.options );
		if( !context.loadDefaults( builder.rc ) )
			return( null );
		
		context.prepareExecution( executor , builder.options );
		
		Metadata meta = new Metadata();
		ActionInit action = executor.prepare( context , meta , builder.options.action );
		return( action );
	}
	
}
