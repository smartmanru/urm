package ru.egov.urm.storage;

import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;

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
