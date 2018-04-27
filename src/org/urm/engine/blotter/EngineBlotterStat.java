package org.urm.engine.blotter;

import org.urm.common.Common;

public class EngineBlotterStat {

	EngineBlotterSet blotterSet;

	public long statDay;
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
	
	public EngineBlotterStat( EngineBlotterSet blotterSet ) {
		this.blotterSet = blotterSet;
		
		statInit( blotterSet.blotter.day );
	}

	public EngineBlotterStat copy() {
		EngineBlotterStat r = new EngineBlotterStat( blotterSet );
		r.statDay = statDay;
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
	
	public void statInit( long day ) {
		statDay = day;
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
	
	public void statAddItem( EngineBlotterActionItem item ) {
		long itemDay = Common.getDayNoTime( item.startTime.getTime() );
		if( itemDay != statDay )
			return;
		
		dayItemsPrimaryRunning++;
		dayItemsTotalRunning++;
		dayLastRunTime = item.startTime.getTime();
	}
	
	public void statFinishItem( EngineBlotterActionItem item ) {
		long itemDay = Common.getDayNoTime( item.startTime.getTime() );
		if( itemDay != statDay )
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
	
	public void statAddChildItem( EngineBlotterActionItem baseItem , EngineBlotterTreeItem treeItem ) {
		long itemDay = Common.getDayNoTime( baseItem.startTime.getTime() );
		if( itemDay != statDay )
			return;
		
		dayItemsChildRunning++;
		dayItemsTotalRunning++;
	}
	
	public void statFinishChildItem( EngineBlotterItem baseItem , EngineBlotterTreeItem treeItem , boolean success ) {
		long itemDay = Common.getDayNoTime( treeItem.startTime.getTime() );
		if( itemDay != statDay )
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
