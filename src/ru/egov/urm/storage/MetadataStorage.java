package ru.egov.urm.storage;

import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;

public class MetadataStorage {

	public Artefactory artefactory;
	public Metadata meta;
	
	public MetadataStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}
	
	public String getDistrFile( ActionBase action ) throws Exception {
		 return( action.context.productHome + "/etc/distr.xml" );
	}
	
	public String getLastProdTagFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/last-prod-tag.txt" );
	}
	
	public String getProductConfFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/product.conf" );
	}
	
	public String getSourceConfFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/source.xml" );
	}

	public String getMonitoringFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/monitoring.xml" );
	}
	
	public String getOrgInfoFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/orginfo.txt" );
	}

	public String getEnvFile( ActionBase action , String envFile ) throws Exception {
		 return( action.context.productHome + "/etc/env/" + envFile );
	}
	
}
