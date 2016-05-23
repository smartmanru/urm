package ru.egov.urm.action.conf;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.Metadata;

public class ConfSourceFolder {
	
	Metadata meta;
	
	public MetaDistrConfItem distrComp;
	public MetaReleaseTarget releaseComp;
	
	public boolean productFolder = false;
	public boolean releaseFolder = false;
	
	public ConfSourceFolder( Metadata meta ) {
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
