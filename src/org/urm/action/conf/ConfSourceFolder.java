package org.urm.action.conf;

import org.urm.action.ActionBase;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;

public class ConfSourceFolder {
	
	Meta meta;
	
	public MetaDistrConfItem distrComp;
	public ReleaseDistScopeDeliveryItem releaseComp;
	
	public boolean productFolder = false;
	public boolean releaseFolder = false;
	
	public ConfSourceFolder( Meta meta ) {
		this.meta = meta;
	}

	public void createProductConfigurationFolder( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		this.distrComp = distrComp;
		this.productFolder = true;
	}

	public void createReleaseConfigurationFolder( ActionBase action , ReleaseDistScopeDeliveryItem releaseComp ) throws Exception {
		this.releaseComp = releaseComp;
		this.distrComp = releaseComp.conf;
		this.releaseFolder = true;
	}

}
