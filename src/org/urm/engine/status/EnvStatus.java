package org.urm.engine.status;

import org.urm.meta.product.MetaEnv;

public class EnvStatus extends Status {

	public MetaEnv env;
	
	public EnvStatus( MetaEnv env ) {
		super( STATETYPE.TypeEnv , null , env );
		this.env = env;
	}

}
