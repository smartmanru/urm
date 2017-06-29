package org.urm.action.database;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionInitDatabase extends ActionBase {

	MetaEnvServer server;
	MetaEnvServerNode node;

	public ActionInitDatabase( ActionBase action , String stream , MetaEnvServer server , MetaEnvServerNode node ) {
		super( action , stream , "Initialize database, server=" + server.NAME + ", node=" + node.POS );
		this.server = server;
		this.node = node;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		DatabaseClient client = new DatabaseClient();
		info( "initialize administrative database on database server " + server.NAME + ", node=" + node.POS + " ..." );
		if( !client.checkConnect( this , server , node ) )
			exit0( _Error.UnableConnectAdmin0 , "unable to connect to administrative db" );

		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder urmScripts = urm.getDatabaseInitScripts( this , server );
		
		LocalFolder logs = artefactory.getWorkFolder( this , "initdb" );
		logs.ensureExists( this );
		
		FileSet files = urmScripts.getFileSet( this );
		for( String file : files.getAllFiles() )
			executeInitScript( client , urmScripts , logs , file );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private void executeInitScript( DatabaseClient client , LocalFolder scripts , LocalFolder logs , String file ) throws Exception {
		info( "apply " + file + " ..." );
		String logfile = file + ".out";
		if( !client.applyAdmScript( this , scripts , file , logs , logfile ) )
			exit0( _Error.UnableInitializeDatabase0 , "unable to initialize database, see errors" );
	}

}
