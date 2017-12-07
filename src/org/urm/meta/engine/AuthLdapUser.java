package org.urm.meta.engine;

public class AuthLdapUser {

	public String name;
	public String email;
	public String displayName;
	
	public AuthLdapUser( String name , String email , String displayName ) {
		this.name = name;
		this.email = email;
		this.displayName = displayName;
	}
	
}
