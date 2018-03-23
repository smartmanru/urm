package org.urm.engine.run;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.engine.DataService;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.status.ScopeState;

public class EngineMethod extends EngineExecutorTask {
	
	private ActionBase action;
	private CommandExecutor executor;
	private CommandMethod command;
	private ScopeState parentState;

	private DBConnection connection;
	private List<DBConnection> dbstatus;
	
	public EngineMethod( ActionBase action , CommandExecutor executor , CommandMethod command , ScopeState parentState ) {
		super( executor.commandInfo.name + "::" + command.method.name );
		this.action = action;
		this.executor = executor;
		this.command = command;
		this.parentState = parentState;
		dbstatus = new LinkedList<DBConnection>();
	}

	@Override
	public void execute() throws Exception {
		boolean res = false;
		try {
			action.setMethod( this );
			if( executor.runExecutorImpl( parentState , action , command ) )
				res = false;
		}
		catch( Throwable e ) {
			action.log( "execute method error" , e );
		}

		try {
			if( res ) {
				commit();
				super.finishSuccessful();
			}
			else {
				abort();
				super.finishFailed( null );
			}
			
			finishDatabase();
		}
		catch( Throwable e ) {
			action.log( "execute method finish error" , e );
		}
	}
	
	private void commit() throws Exception {
		if( connection != null ) {
			connection.close( true );
			connection = null;
		}
	}
	
	private void abort() throws Exception {
		if( connection != null ) {
			connection.close( false );
			connection = null;
		}
	}

	public ActionBase getAction() {
		return( action );
	}
	
	public synchronized DBConnection getMethodConnection( ActionBase action ) throws Exception {
		if( connection == null ) {
			Engine engine = action.engine;
			DataService data = engine.getData();
			EngineDB db = data.getDatabase();
			connection = db.getConnection( action );
		}
		
		return( connection );
	}
	
	public synchronized DBConnection getStatusConnection( ActionBase action ) throws Exception {
		Engine engine = action.engine;
		DataService data = engine.getData();
		EngineDB db = data.getDatabase();
		DBConnection c = db.getConnection( action );
		dbstatus.add( c );
		return( c );
	}

	private void finishDatabase() throws Exception {
		for( DBConnection c : dbstatus ) {
			if( c.isConnected() )
				c.close( true );
		}
		dbstatus.clear();
	}
	
}
