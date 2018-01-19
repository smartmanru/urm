package org.urm.meta.product;

import org.urm.meta.ProductMeta;

public class MetaProductPolicy {

	public Meta meta;
	
	public String RELEASELC_MAJOR;
	public String RELEASELC_MINOR;
	public boolean releaseLCUrgentAll;
	public String[] RELEASELC_URGENT_LIST;
	
	public MetaProductPolicy( ProductMeta storage , Meta meta ) {
		releaseLCUrgentAll = false;
		RELEASELC_MAJOR = "";
		RELEASELC_MINOR = "";
		RELEASELC_URGENT_LIST = new String[0];
	}

	public MetaProductPolicy copy( Meta rmeta ) throws Exception {
		MetaProductPolicy r = new MetaProductPolicy( rmeta.getStorage() , rmeta );
		
		// stored
		r.RELEASELC_MAJOR = RELEASELC_MAJOR;
		r.RELEASELC_MINOR = RELEASELC_MINOR;
		r.releaseLCUrgentAll = releaseLCUrgentAll;
		r.RELEASELC_URGENT_LIST = RELEASELC_URGENT_LIST.clone();
		
		return( r );
	}

	public void setLifecycles( String major , String minor , boolean urgentsAll , String[] urgents ) throws Exception {
		RELEASELC_MAJOR = major;
		RELEASELC_MINOR = minor;
		releaseLCUrgentAll = urgentsAll;
		if( !urgentsAll )
			RELEASELC_URGENT_LIST = urgents.clone();
		else
			RELEASELC_URGENT_LIST = new String[0];
	}
	
}
