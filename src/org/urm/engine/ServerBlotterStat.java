package org.urm.engine;

import org.urm.action.ActionBase;

public class ServerBlotterStat {

	ServerBlotterSet blotterSet;
	
	public long day;
	public int dayItemsPrimaryDone;
	public int dayItemsPrimaryFailed;
	public int dayItemsPrimaryRunning;
	public int dayItemsChildDone;
	public int dayItemsChildFailed;
	public int dayItemsChildRunning;
	public int dayItemsTotalDone;
	public int dayItemsTotalFailed;
	public int dayItemsTotalRunning;
	public long dayLastRunTime;
	
	public ServerBlotterStat( ServerBlotterSet blotterSet ) {
		this.blotterSet = blotterSet;
		
		statClear();
	}

	public ServerBlotterStat copy() {
		ServerBlotterStat r = new ServerBlotterStat( blotterSet );
		r.day = day;
		r.dayItemsPrimaryDone = dayItemsPrimaryDone;
		r.dayItemsPrimaryFailed = dayItemsPrimaryFailed;
		r.dayItemsPrimaryRunning = dayItemsPrimaryRunning;
		r.dayItemsChildDone = dayItemsChildDone;
		r.dayItemsChildFailed = dayItemsChildFailed;
		r.dayItemsChildRunning = dayItemsChildRunning;
		r.dayItemsTotalDone = dayItemsTotalDone;
		r.dayItemsTotalFailed = dayItemsTotalFailed;
		r.dayItemsTotalRunning = dayItemsTotalRunning;
		r.dayLastRunTime = dayLastRunTime;
		return( r );
	}
	
	public boolean isEmpty() {
		if( dayLastRunTime == 0 )
			return( true );
		return( false );
	}
	
	private void statClear() {
		day = 0;
		dayItemsPrimaryDone = 0;
		dayItemsPrimaryFailed = 0;
		dayItemsPrimaryRunning = 0;
		dayItemsChildDone = 0;
		dayItemsChildFailed = 0;
		dayItemsChildRunning = 0;
		dayItemsTotalDone = 0;
		dayItemsTotalFailed = 0;
		dayItemsTotalRunning = 0;
		dayLastRunTime = 0;
	}
	
	public void statAddItem( ServerBlotterItem item ) {
		long itemDay = item.startTime - item.startTime % ( 24 * 60 * 60 * 1000 );
		if( itemDay != day ) {
			statClear();
			day = itemDay;
			dayItemsPrimaryRunning++;
			dayItemsTotalRunning++;
		}
		
		dayLastRunTime = item.startTime;
	}
	
	public void statFinishItem( ServerBlotterItem item ) {
		long itemDay = item.startTime - item.startTime % ( 24 * 60 * 60 * 1000 );
		if( itemDay != day )
			return;
		
		dayItemsPrimaryRunning--;
		dayItemsTotalRunning--;
		
		if( item.success ) {
			dayItemsPrimaryDone++;
			dayItemsTotalDone++;
		}
		else {
			dayItemsPrimaryFailed++;
			dayItemsTotalFailed++;
		}
	}
	
	public void statAddChildItem( ServerBlotterItem item , ActionBase action ) {
		long itemDay = item.startTime - item.startTime % ( 24 * 60 * 60 * 1000 );
		if( itemDay != day )
			return;
		
		dayItemsChildRunning++;
		dayItemsTotalRunning++;
	}
	
	public void statFinishChildItem( ServerBlotterItem item , ActionBase action , boolean success ) {
		long itemDay = item.startTime - item.startTime % ( 24 * 60 * 60 * 1000 );
		if( itemDay != day )
			return;
		
		dayItemsChildRunning--;
		dayItemsTotalRunning--;
		
		if( success ) {
			dayItemsChildDone++;
			dayItemsTotalDone++;
		}
		else {
			dayItemsChildFailed++;
			dayItemsTotalFailed++;
		}
	}
	
}
