package org.urm.action.conf;

import org.urm.action.ActionBase;
import org.urm.dist.ReleaseTarget;
import org.urm.meta.MetaDistrConfItem;
import org.urm.meta.Metadata;

public class ConfSourceFolder {
	
	Metadata meta;
	
	public MetaDistrConfItem distrComp;
	public ReleaseTarget releaseComp;
	
	public boolean productFolder = false;
	public boolean releaseFolder = false;
	
	public ConfSourceFolder( Metadata meta ) {
		this.meta = meta;
	}

	public void createProductConfigurationFolder( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		this.distrComp = distrComp;
		this.productFolder = true;
	}

	public void createReleaseConfigurationFolder( ActionBase action , ReleaseTarget releaseComp ) throws Exception {
		this.releaseComp = releaseComp;
		this.distrComp = releaseComp.distConfItem;
		
		action.checkRequired( releaseComp.distConfItem != null , "releaseComp.distConfItem" );
		this.releaseFolder = true;
	}

}
