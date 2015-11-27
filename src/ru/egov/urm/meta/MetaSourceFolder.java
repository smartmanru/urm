package ru.egov.urm.meta;

import ru.egov.urm.run.ActionBase;

public class MetaSourceFolder {
	
	Metadata meta;
	
	public MetaDistrConfItem distrComp;
	public MetaReleaseTarget releaseComp;
	
	public boolean productFolder = false;
	public boolean releaseFolder = false;
	
	public MetaSourceFolder( Metadata meta ) {
		this.meta = meta;
	}

	public void createProductConfigurationFolder( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		this.distrComp = distrComp;
		this.productFolder = true;
	}

	public void createReleaseConfigurationFolder( ActionBase action , MetaReleaseTarget releaseComp ) throws Exception {
		this.releaseComp = releaseComp;
		this.distrComp = releaseComp.distConfItem;
		
		action.checkRequired( releaseComp.distConfItem != null , "releaseComp.distConfItem" );
		this.releaseFolder = true;
	}

}
