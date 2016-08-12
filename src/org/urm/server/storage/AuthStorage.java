package org.urm.server.storage;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta;

public class AuthStorage {

	Artefactory artefactory;
	Meta meta;
	
	public AuthStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}
	
	public String getOldSvnAuthParams( ActionBase action ) throws Exception {
		String fileName = action.meta.product.CONFIG_SVNOLD_AUTH;
		String content = action.readStringFile( fileName );
		return( content );
	}
	
	public String getNewSvnAuthParams( ActionBase action ) throws Exception {
		String fileName = action.meta.product.CONFIG_SVNNEW_AUTH;
		String content = action.readStringFile( fileName );
		return( content );
	}

}
