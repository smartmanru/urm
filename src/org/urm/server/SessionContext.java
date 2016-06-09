package org.urm.server;

import org.urm.common.RunContext;

public class SessionContext {

	public RunContext rc;
	public String ENV;
	public String DC;
	public boolean executorFailed;

	public SessionContext( RunContext rc ) {
		this.rc = rc;
		this.ENV = rc.envName;
		this.DC = rc.dcName;
		
		executorFailed = false;
	}
	
	public void setFailed() {
		executorFailed = true;
	}
	
	public boolean isFailed() {
		return( executorFailed );
	}
	
}
