package org.urm.engine.security;

import org.urm.common.Common;
import org.urm.engine.AuthService;

public class AuthUser {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_FULLNAME = "fullname";
	public static String PROPERTY_EMAIL = "email";
	public static String PROPERTY_ADMIN = "admin";
	public static String PROPERTY_LOCAL = "local";
	
	AuthService auth;
	
	public int ID;
	public String NAME;
	public String DESC;
	public String FULLNAME;
	public String EMAIL;
	public boolean ADMIN;
	public boolean LOCAL;
	public int UV;
	
	private boolean master;
	
	public AuthUser( AuthService auth ) {
		this.auth = auth;
		this.master = false;
	}

	public void createMaster() {
		ID = 0;
		NAME = "admin";
		DESC = "(master administrator)";
		FULLNAME = "(master administrator)";
		EMAIL = "";
		ADMIN = true;
		LOCAL = true;
		UV = -1;
		
		master = true;
	}
	
	public void createUser( String name , String desc , String full , String email , boolean admin , boolean local ) {
		master = false;
		modifyUser( name , desc , full , email , admin , local );
	}

	public void modifyUser( String name , String desc , String full , String email , boolean admin , boolean local ) {
		this.LOCAL = local;
		this.NAME = name;
		this.EMAIL = Common.nonull( email );
		this.FULLNAME = Common.nonull( full );
		this.ADMIN = admin;
	}

	public boolean isMaster() {
		return( master );
	}
	
}
