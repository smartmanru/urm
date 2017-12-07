package org.urm.meta.engine;

public class AuthUser {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_FULLNAME = "fullname";
	public static String PROPERTY_EMAIL = "email";
	public static String PROPERTY_ADMIN = "admin";
	public static String PROPERTY_LOCAL = "local";
	
	EngineAuth auth;
	
	public int ID;
	public String NAME;
	public String DESC;
	public String FULLNAME;
	public String EMAIL;
	public boolean ADMIN;
	public boolean LOCAL;
	public int UV;
	
	public AuthUser( EngineAuth auth ) {
		this.auth = auth;
	}

	public void createUser( String name , String desc , String full , String email , boolean admin , boolean local ) {
		modifyUser( name , desc , full , email , admin , local );
	}

	public void modifyUser( String name , String desc , String full , String email , boolean admin , boolean local ) {
		this.LOCAL = local;
		this.NAME = name;
		this.EMAIL = email;
		this.FULLNAME = full;
		this.ADMIN = admin;
	}
	
}
