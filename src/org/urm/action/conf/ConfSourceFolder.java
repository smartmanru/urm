package org.urm.action.conf;

import org.urm.action.ActionBase;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaDistrConfItem;

public class ConfSourceFolder {
	
	Meta meta;
	
	public MetaDistrConfItem distrComp;
	public ReleaseTarget releaseComp;
	
	public boolean productFolder = false;
	public boolean releaseFolder = false;
	
	public ConfSourceFolder( Meta meta ) {
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
