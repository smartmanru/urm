package ru.egov.urm.action.database;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;

public class DatabaseProcess {

	MetaEnvServerNode node;
	
	public DatabaseProcess( MetaEnvServerNode node ) {
		this.node = node;
	}

	public VarPROCESSMODE getStatus( ActionBase action ) throws Exception {
		try {
			MetaEnvServer server = node.server;
			DatabaseSpecific specific = DatabaseSpecific.getSpecificHandler( action , server.DBMSTYPE , server , node );
			VarPROCESSMODE mode = specific.getProcessStatus( action );
			return( mode );
		}
		catch( Throwable e ) {
			action.log( "exception when obtaining status of database node=" + node.HOSTLOGIN , e );
		}
		return( VarPROCESSMODE.UNKNOWN );
	}
	
}
