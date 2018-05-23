package org.urm.meta.env;

import org.urm.meta.product.Meta;

public class MetaEnvDeployGroup {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";

	public Meta meta;
	public MetaEnv env;
	
	public int ID;
	public String NAME;
	public String DESC;
	public int EV;

	public MetaEnvDeployGroup( Meta meta , MetaEnv env ) {
		this.meta = meta;
		this.env = env;
	}

	public MetaEnvDeployGroup copy( Meta rmeta , MetaEnv renv ) throws Exception {
		MetaEnvDeployGroup r = new MetaEnvDeployGroup( rmeta , renv );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.EV = EV;
		
		return( r );
	}
	
	public void createGroup( String name , String desc ) {
		modifyGroup( name , desc );
	}
	
	public void modifyGroup( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}
	
}
