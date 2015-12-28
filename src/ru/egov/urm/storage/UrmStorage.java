package ru.egov.urm.storage;

import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.run.ActionBase;

public class UrmStorage {

	public Artefactory artefactory;
	public Metadata meta;
	
	public UrmStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	private String getSpecificFolder( ActionBase action , VarDBMSTYPE dbtype ) throws Exception {
		String dbFolder = "";
		if( dbtype == VarDBMSTYPE.ORACLE )
			dbFolder = "oracle";
		else
		if( dbtype == VarDBMSTYPE.POSTGRESQL )
			dbFolder = "postgres";
		else
			action.exitUnexpectedState();
		
		return( dbFolder );
	}
	
	public LocalFolder getInitScripts( ActionBase action , VarDBMSTYPE dbtype ) throws Exception {
		String dbFolder = getSpecificFolder( action , dbtype );
		return( artefactory.getAnyFolder( action , action.context.productHome + "/master/database/init/" + dbFolder ) );
	}
	
	public LocalFolder getDatapumpScripts( ActionBase action , VarDBMSTYPE dbtype ) throws Exception {
		String dbFolder = getSpecificFolder( action , dbtype );
		return( artefactory.getAnyFolder( action , action.context.productHome + "/master/database/datapump/" + dbFolder ) );
	}
	
}
