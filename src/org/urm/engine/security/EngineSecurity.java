package org.urm.engine.security;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.SecurityService;

public class EngineSecurity {

	SecurityService ss;
	
	private Map<String,CryptoContainer> data;
	
	public EngineSecurity( SecurityService ss ) {
		data = new HashMap<String,CryptoContainer>(); 
	}

	public synchronized void closeAll() throws Exception {
		data.clear();
	}
	
	public synchronized CryptoContainer openContainer( ActionBase action , String name , String password ) throws Exception {
		if( data.get( name ) != null )
			Common.exitUnexpected();

		CryptoContainer crypto = new CryptoContainer( this , name );
		crypto.open( action , password );
		data.put( name , crypto );
		
		return( crypto );
	}

	public synchronized CryptoContainer createContainer( ActionBase action , String name , String password ) throws Exception {
		if( data.get( name ) != null )
			Common.exitUnexpected();
		
		CryptoContainer crypto = new CryptoContainer( this , name );
		crypto.create( action , password );
		data.put( name , crypto );
		
		return( crypto );
	}
	
}
