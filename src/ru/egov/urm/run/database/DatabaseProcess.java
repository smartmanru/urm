package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.run.ActionBase;

public class DatabaseProcess {

	MetaEnvServerNode node;
	
	public DatabaseProcess( MetaEnvServerNode node ) {
		this.node = node;
	}

	public VarPROCESSMODE getStatus( ActionBase action ) throws Exception {
		try {
			DatabaseSpecific specific = DatabaseSpecific.getSpecificHandler( action , node.server.DBMSTYPE );
			VarPROCESSMODE mode = specific.getProcessStatus( action , action.getAccount( node ) , node.INSTANCE );
			return( mode );
		}
		catch( Throwable e ) {
			action.log( "exception when obtaining status of database node=" + node.HOSTLOGIN , e );
		}
		return( VarPROCESSMODE.UNKNOWN );
	}
	
}
