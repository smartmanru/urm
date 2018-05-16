package org.urm.engine.security;

import org.urm.common.Common;

public class AuthLdapUser {

	public String name;
	public String email;
	public String displayName;
	
	public AuthLdapUser( String name , String email , String displayName ) {
		this.name = name;
		this.email = Common.nonull( email );
		this.displayName = Common.nonull( displayName );
	}
	
}
