package org.urm.storage;

import org.urm.ConfReader;
import org.urm.action.ActionBase;
import org.urm.meta.Metadata;

public class AuthStorage {

	Artefactory artefactory;
	Metadata meta;
	
	public AuthStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}
	
	public String getOldSvnAuthParams( ActionBase action ) throws Exception {
		String fileName = action.meta.product.CONFIG_SVNOLD_AUTH;
		String content = ConfReader.readStringFile( action , fileName );
		return( content );
	}
	
	public String getNewSvnAuthParams( ActionBase action ) throws Exception {
		String fileName = action.meta.product.CONFIG_SVNNEW_AUTH;
		String content = ConfReader.readStringFile( action , fileName );
		return( content );
	}

}
