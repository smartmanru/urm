package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.LocalFolder;

abstract public class DatabaseSpecific {

	public static DatabaseSpecific getSpecificHandler( ActionBase action , VarDBMSTYPE dbmsType ) throws Exception {
		if( dbmsType == VarDBMSTYPE.ORACLE )
			return( new DatabaseOracleSpecific() );
		if( dbmsType == VarDBMSTYPE.POSTGRESQL )
			return( new DatabasePostgresSpecific() );
		action.exit( "unexpected database type=" + dbmsType.name() );
		return( null );
	}

	abstract public VarPROCESSMODE getProcessStatus( ActionBase action , String hostLogin , String instance ) throws Exception;
	abstract public boolean checkConnect( ActionBase action , MetaEnvServer server , String user , String password ) throws Exception;
	abstract public boolean applyScript( ActionBase action , MetaEnvServer server , boolean sys , String user , String password , String schema , String file , String fileLog ) throws Exception;
	
	abstract public boolean validateScriptContent( ActionBase action , LocalFolder dir , String script ) throws Exception;
	abstract public String getComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile ) throws Exception;
	abstract public void grepComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void addComment( ActionBase action , String comment , LocalFolder dstDir , String outfile ) throws Exception;
	
	abstract public void uddiBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void uddiEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void uddiAddEndpoint( ActionBase action , String UDDI_KEY , String UDDI_UAT , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void smevAttrBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void smevAttrEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void smevAttrAddValue( ActionBase action , String UDDI_ATTR_ID , String UDDI_ATTR_NAME , String UDDI_ATTR_CODE , String UDDI_ATTR_REGION , String UDDI_ATTR_ACCESSPOINT , 
			LocalFolder dstDir , String outfile ) throws Exception;
}
