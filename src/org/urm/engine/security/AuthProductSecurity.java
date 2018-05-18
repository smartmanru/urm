package org.urm.engine.security;

import org.urm.engine.AuthService.SpecialRights;

public class AuthProductSecurity {

	public AuthRoleSet roles;
	public boolean specialSecured;
	
	public AuthProductSecurity() {
		roles = new AuthRoleSet();
		specialSecured = false;
	}
	
	public void clear() {
		roles.clear();
		specialSecured = false;
	}
	
	public void addRoles( AuthRoleSet rolesrc ) {
		roles.add( rolesrc );
	}

	public void addSpecial( SpecialRights sr ) {
		if( sr == SpecialRights.SPECIAL_SECURED )
			specialSecured = true;
	}
	
}
