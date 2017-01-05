package org.urm.engine;

public class ServerBlotterMemo {

	ServerBlotterSet blotterSet;
	String key;
	
	private long totalTime;
	private int totalCount;
	
	public long averageDuration;
	public long lastDuration;
	
	public ServerBlotterMemo( ServerBlotterSet blotterSet , String key ) {
		this.blotterSet = blotterSet;
		
		totalTime = 0;
		totalCount = 0;
		averageDuration = 0;
		lastDuration = 0;
	}

	public synchronized void addEvent( long duration ) {
		lastDuration = duration;
		totalCount++;
		totalTime += duration;
		averageDuration = totalTime / totalCount;
	}
	
}
