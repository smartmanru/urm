package org.urm.engine;

public class ServerObject {

	static int objectIdSequence = 0;
	public int objectId;
	
	public ServerObject() {
		objectId = ++objectIdSequence;
	}
	
}
