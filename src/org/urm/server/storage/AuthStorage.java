package org.urm.server.storage;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta;
import org.urm.server.meta.MetaWebResource;

public class AuthStorage {

	Artefactory artefactory;
	Meta meta;
	
	public AuthStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}
	
	public String getAuthData( ActionBase action , MetaWebResource res ) throws Exception {
		String SVNAUTH = "";
		if( !res.AUTHFILE.isEmpty() )
			SVNAUTH = getAuthData( action , res.AUTHFILE );
		return( SVNAUTH );
	}
	
	public String getAuthData( ActionBase action , String fileName ) throws Exception {
		String filePath = Common.getPath( action.session.execrc.userHome , ".auth" , fileName );
		String content = action.readStringFile( filePath );
		return( content );
	}

}
