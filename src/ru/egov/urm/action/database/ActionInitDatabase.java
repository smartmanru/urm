package ru.egov.urm.run.database;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.UrmStorage;

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
		log( "initialize administrative database on database server " + server.NAME + ", node=" + node.POS + " ..." );
		if( !client.checkConnect( this , server , node ) )
			exit( "unable to connect to administrative db" );

		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder urmScripts = urm.getInitScripts( this , server.DBMSTYPE );
		
		LocalFolder logs = artefactory.getWorkFolder( this , "initdb" );
		logs.ensureExists( this );
		
		FileSet files = urmScripts.getFileSet( this );
		for( String file : Common.getSortedKeys( files.files ) )
			executeInitScript( client , urmScripts , logs , file );
		
		return( true );
	}
	
	private void executeInitScript( DatabaseClient client , LocalFolder scripts , LocalFolder logs , String file ) throws Exception {
		log( "apply " + file + " ..." );
		String logfile = file + ".out";
		if( !client.applyAdmScript( this , scripts , file , logs , logfile ) )
			exit( "unable to initialize database, see errors" );
	}

}
