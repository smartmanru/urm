package org.urm.engine.status;

import org.urm.meta.product.MetaEnv;

public class EnvStatus extends Status {

	public MetaEnv env;
	
	public EnvStatus( ObjectState parent , MetaEnv env ) {
		super( STATETYPE.TypeEnv , parent , env );
		this.env = env;
	}

}
