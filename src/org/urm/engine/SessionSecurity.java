package org.urm.engine;

public class SessionSecurity {

	ServerAuth auth;
	
	private ServerAuthUser user;
	private ServerAuthContext ac;
	
	public SessionSecurity( ServerAuth auth ) {
		this.auth = auth;
	}
	
	public ServerAuthUser getUser() {
		return( user );
	}

	public void setUser( ServerAuthUser user ) {
		this.user = user;
	}
	
	public ServerAuthContext getContext() {
		return( ac );
	}

	public void setContext( ServerAuthContext ac ) {
		this.ac = ac;
	}

	public void setPermissions() throws Exception {
	}
	
}
