package org.urm.action.monitor;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionSet;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandContext;
import org.urm.engine.meta.MetaMonitoring;
import org.urm.engine.meta.MetaMonitoringItem;
import org.urm.engine.meta.MetaMonitoringTarget;
import org.urm.engine.storage.MonitoringStorage;

public class ActionMonitorTop extends ActionBase {

	boolean continueRunning;
	MetaMonitoring mon;
	
	MonitoringStorage storage;
	MonitorInfo info;
	List<ActionMonitorCheckEnv> checkenvActions;
	
	public ActionMonitorTop( ActionBase action , String stream , MetaMonitoring mon ) {
		super( action , stream );
		this.mon = mon;
	}

	public void stopRunning() throws Exception {
		continueRunning = false;
	}
	
	@Override protected boolean executeSimple() throws Exception {
		continueRunning = true;

		long lastStartMajor = 0;
		long lastStartMinor = 0;
		long lastStopMajor = 0;
		long lastStopMinor = 0;
		
		storage = artefactory.getMonitoringStorage( this , mon );
		info = new MonitorInfo( this , storage );
		checkenvActions = new LinkedList<ActionMonitorCheckEnv>();
		
		boolean runMajor = true;
		while( continueRunning ) {
			long current = System.currentTimeMillis();
			try {
				if( runMajor ) {
					runMajor = false;
					lastStartMajor = current;
					info( "start major checks: " );
					executeOnceMajor();
					current = System.currentTimeMillis();
					lastStopMajor = current;
					info( "major checks done in : " + ( lastStopMajor - lastStartMajor ) + "ms" );
					continue;
				}
				else {
					lastStartMinor = current;
					info( "start minor checks: " );
					executeOnceMinor();
					current = System.currentTimeMillis();
					lastStopMinor = current; 
					info( "minor checks done in : " + ( lastStopMinor - lastStartMinor ) + "ms" );
				}
			}
			catch( Throwable e ) {
				handle( e );
			}

			if( runMajor && lastStartMinor == 0 ) {
				runMajor = false;
				continue;
			}
			
			// calculate sleep and next action
			long nextMinor = 0;
			nextMinor = lastStartMinor + mon.MINORINTERVAL * 1000;
			if( nextMinor < ( current + mon.MINSILENT * 1000 ) )
				nextMinor = current + mon.MINSILENT * 1000;

			long nextMajor = 0;
			nextMajor = lastStartMajor + mon.MINORINTERVAL * 1000;
			if( nextMajor < ( current + mon.MINSILENT * 1000 ) )
				nextMajor = current + mon.MINSILENT * 1000;
			
			if( nextMajor < nextMinor ) {
				runMajor = true;
				if( !runSleep( nextMajor - current ) )
					stopRunning();
			}
			else {
				runMajor = false;
				if( !runSleep( nextMinor - current ) )
					stopRunning();
			}
		}
		
		return( true );
	}

	private boolean runSleep( long millis ) {
		try {
			synchronized( this ) {
				Thread.sleep( millis );
				return( true );
			}
		}
		catch( Throwable e ) {
			handle( e );
		}
		
		return( false );
	}

	private void executeOnceMajor() throws Exception {
		checkenvActions.clear();
		
		// run checkenv for all targets
		ActionSet set = new ActionSet( this , "major" );
		for( MetaMonitoringTarget target : mon.getTargets( this ).values() ) {
			ActionInit init = initProduct( target );
			init.context.loadEnv( init , target.ENVFILE , target.DC , false );
			checkEnv( set , init , target );
		}
		
		set.waitDone();
		
		for( ActionMonitorCheckEnv action : checkenvActions )
			info.addCheckEnvData( action.target , action.timePassedMillis , action.isOK() );
	}
	
	private void executeOnceMinor() throws Exception {
		// run checkenv for all targets
		for( MetaMonitoringTarget target : mon.getTargets( this ).values() ) {
			ActionInit init = initProduct( target ); 
			checkTargetItems( init , target );
		}
	}

	private void checkEnv( ActionSet set , ActionInit init , MetaMonitoringTarget target ) throws Exception {
		ActionMonitorCheckEnv action = new ActionMonitorCheckEnv( init , target.NAME , storage , target );
		set.runSimple( action );
		checkenvActions.add( action );
	}

	private ActionInit initProduct( MetaMonitoringTarget target ) throws Exception {
		CommandContext initContext = context.getProductContext( target.NAME );
		ActionInit action = engine.createAction( initContext , this );
		return( action );
	}

	private void checkTargetItems( ActionInit init , MetaMonitoringTarget target ) throws Exception {
		ActionSet set = new ActionSet( this , "minor" );
		
		for( MetaMonitoringItem item : target.getUrlsList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( init , target.NAME , mon , item );
			set.runSimple( action );
		}
		
		for( MetaMonitoringItem item : target.getWSList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( init , target.NAME , mon , item );
			set.runSimple( action );
		}
		
		boolean ok = set.waitDone();  
		if( !ok )
			super.fail1( _Error.MonitorTargetFailed1 , "Monitoring target failed name=" + target.NAME , target.NAME );
		
		info.addCheckMinorsData( target , ok );
	}
	
}
