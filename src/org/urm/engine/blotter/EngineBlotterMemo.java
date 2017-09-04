package org.urm.engine.blotter;

public class EngineBlotterMemo {

	EngineBlotterSet blotterSet;
	String key;
	
	private long totalTime;
	private int totalCount;
	
	public long averageDuration;
	public long lastDuration;
	
	public EngineBlotterMemo( EngineBlotterSet blotterSet , String key ) {
		this.blotterSet = blotterSet;
		
		totalTime = 0;
		totalCount = 0;
		averageDuration = 0;
		lastDuration = 0;
	}

	public boolean isNew() {
		if( totalCount == 0 )
			return( true );
		return( false );
	}
	
	public synchronized void addEvent( long duration ) {
		lastDuration = duration;
		totalCount++;
		totalTime += duration;
		averageDuration = totalTime / totalCount;
	}
	
}
