package org.urm.action.database;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.MetaEnvServerNode;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;

public class ActionInitDatabase extends ActionBase {

	MetaEnvServer server;
	MetaEnvServerNode node;

	public ActionInitDatabase( ActionBase action , String stream , MetaEnvServer server , MetaEnvServerNode node ) {
		super( action , stream );
		this.server = server;
		this.node = node;
	}

	@Override protected boolean executeSimple() throws Exception {
		DatabaseClient client = new DatabaseClient();
		info( "initialize administrative database on database server " + server.NAME + ", node=" + node.POS + " ..." );
		if( !client.checkConnect( this , server , node ) )
			exit0( _Error.UnableConnectAdmin0 , "unable to connect to administrative db" );

		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder urmScripts = urm.getDatabaseInitScripts( this , server );
		
		LocalFolder logs = artefactory.getWorkFolder( this , "initdb" );
		logs.ensureExists( this );
		
		FileSet files = urmScripts.getFileSet( this );
		for( String file : Common.getSortedKeys( files.files ) )
			executeInitScript( client , urmScripts , logs , file );
		
		return( true );
	}
	
	private void executeInitScript( DatabaseClient client , LocalFolder scripts , LocalFolder logs , String file ) throws Exception {
		info( "apply " + file + " ..." );
		String logfile = file + ".out";
		if( !client.applyAdmScript( this , scripts , file , logs , logfile ) )
			exit0( _Error.UnableInitializeDatabase0 , "unable to initialize database, see errors" );
	}

}