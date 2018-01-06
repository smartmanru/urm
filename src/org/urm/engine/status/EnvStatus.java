package org.urm.engine.status;

import org.urm.meta.product.MetaEnv;

public class EnvStatus extends Status {

	public MetaEnv env;
	
	public String RELEASEDIR;
	public String RELEASEVER;
	public String CHANGEID;
	
	public String LASTOP;
	public long LASTOPTIME;
	
	public boolean refreshReleaseInfo;
	public boolean refreshStatus;
	
	public EnvStatus( MetaEnv env ) {
		super( STATETYPE.TypeEnv , null , env );
		this.env = env;
		
		refreshReleaseInfo = false;
		refreshStatus = false;
	}

}
