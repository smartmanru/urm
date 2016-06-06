package org.urm.server.action.database;

public class DatabaseRegistryRelease {
	
	public enum RELEASE_STATE {
		UNKNOWN ,
		STARTED ,
		FINISHED ,
		ROLLBACK
	};
	
	public String version;
	public RELEASE_STATE state;
}
