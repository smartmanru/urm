package org.urm.action.conf;

import org.urm.action.ActionBase;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.release.ReleaseScopeTarget;

public class ConfSourceFolder {
	
	Meta meta;
	
	public MetaDistrConfItem distrComp;
	public ReleaseScopeTarget releaseComp;
	
	public boolean productFolder = false;
	public boolean releaseFolder = false;
	
	public ConfSourceFolder( Meta meta ) {
		this.meta = meta;
	}

	public void createProductConfigurationFolder( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		this.distrComp = distrComp;
		this.productFolder = true;
	}

	public void createReleaseConfigurationFolder( ActionBase action , ReleaseScopeTarget releaseComp ) throws Exception {
		this.releaseComp = releaseComp;
		this.distrComp = releaseComp.distConfItem;
		
		action.checkRequired( releaseComp.distConfItem != null , "releaseComp.distConfItem" );
		this.releaseFolder = true;
	}

}
