package org.urm.server;

public class SessionContext {

	public boolean executorFailed;

	public SessionContext() {
		executorFailed = false;
	}
	
	public void setFailed() {
		executorFailed = true;
	}
	
	public boolean isFailed() {
		return( executorFailed );
	}
	
}
