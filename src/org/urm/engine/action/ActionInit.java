package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.engine.AuthService;
import org.urm.engine.DataService;
import org.urm.engine.EventService;
import org.urm.engine.ScheduleService;
import org.urm.engine.StateService;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineContext;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.session.EngineSession;
import org.urm.engine.status.ScopeState;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.product.Meta;

public class ActionInit extends ActionBase {

	public enum RootActionType {
		Core ,
		InteractiveSession ,
		Temporary ,
		Command
	};
	
	public RootActionType type;
	public CommandMethod commandAction;
	public String actionName;
	public Engine engine;
	private DataService data;
	
	protected TransactionBase transaction;
	private boolean memoryOnly;
	private EngineEventsApp eventsApp;

	public ActionInit( Engine engine , DataService data , EngineSession session , Artefactory artefactory , CommandExecutor executor , CommandOutput output , String actionInfo ) {
		super( session , artefactory , executor , output , actionInfo );
		this.engine = engine;
		this.data = data;
		this.actionInit = this;
	}

	@Override
	protected void runBefore( ScopeState state ) throws Exception {
		Common.exitUnexpected();
	}
	
	@Override
	protected void runBefore( ScopeState state , ActionScope scope ) throws Exception {
		Common.exitUnexpected();
	}

	public void create( RootActionType type , CommandMethod commandAction , String actionName , boolean memoryOnly ) throws Exception {
		this.type = type;
		this.commandAction = commandAction;
		this.actionName = actionName;
		this.memoryOnly = memoryOnly;
		
		if( !memoryOnly ) {
			EventService events = data.engine.getEvents();
			eventsApp = events.createApp( "session-" + super.session.sessionId );
		}
	}

	public void tee() throws Exception {
		LocalFolder folder = artefactory.getWorkFolder( this );
		String fname = folder.getFilePath( this , "executor.log" );
		output.tee( execrc , NAME , fname );
	}
	
	public void stopAllOutputs() throws Exception {
		output.stopAllOutputs();
		context.logStopCapture();
	}

	public void close() {
		if( eventsApp != null ) {
			EventService events = data.engine.getEvents();
			events.deleteApp( eventsApp );
		}
	}
	
	public EngineEventsApp getEventsApp() {
		return( eventsApp );
	}
	
	public boolean isMemoryOnly() {
		return( memoryOnly );
	}
	
	public void setTransaction( TransactionBase transaction ) {
		this.transaction = transaction;
	}
	
	public void clearTransaction() {
		this.transaction = null;
	}

	public void clearSession() {
		this.session = null;
	}
	
	public EngineEntities getActiveEntities() {
		if( transaction != null )
			transaction.getEntities();
		return( data.getEntities() );
	}
	
	public EngineSettings getActiveServerSettings() {
		if( transaction != null )
			transaction.getSettings();
		return( data.getEngineSettings() );
	}

	public EngineContext getActiveServerContext() {
		EngineSettings settings = getActiveServerSettings();
		return( settings.getServerContext() );
	}

	public EngineMirrors getActiveMirrors() {
		if( transaction != null )
			return( transaction.getMirrors() );
		return( data.getMirrors() );
	}
	
	public EngineResources getActiveResources() {
		if( transaction != null )
			return( transaction.getResources() );
		return( data.getResources() );
	}

	public EngineBuilders getActiveBuilders() {
		if( transaction != null )
			return( transaction.getBuilders() );
		return( data.getBuilders() );
	}
	
	public EngineDirectory getActiveDirectory() {
		if( transaction != null )
			return( transaction.getDirectory() );
		return( data.getDirectory() );
	}
	
	public EngineInfrastructure getActiveInfrastructure() {
		if( transaction != null )
			return( transaction.getInfrastructure() );
		return( data.getInfrastructure() );
	}
	
	public EngineLifecycles getActiveReleaseLifecycles() {
		if( transaction != null )
			return( transaction.getLifecycles() );
		return( data.getReleaseLifecycles() );
	}
	
	public EngineMonitoring getActiveMonitoring() {
		return( data.getMonitoring() );
	}
	
	public EngineBase getServerBase() {
		return( data.getEngineBase() );
	}

	public EngineLifecycles getServerReleaseLifecycles() {
		return( data.getReleaseLifecycles() );
	}
	
	public EngineInfrastructure getServerInfrastructure() {
		return( data.getInfrastructure() );
	}
	
	public EngineMonitoring getServerMonitoring() {
		return( data.getMonitoring() );
	}
	
	public AuthService getServerAuth() {
		return( engine.getAuth() );
	}
	
	public StateService getServerStatus() {
		return( engine.getStatus() );
	}
	
	public ScheduleService getServerScheduler() {
		return( engine.getScheduler() );
	}
	
	public Meta getActiveProductMetadata( String productName ) throws Exception {
		if( transaction != null ) {
			Meta meta = transaction.findTransactionSessionProductMetadata( productName );
			if( meta != null )
				return( meta );
		}
		return( data.getSessionProductMetadata( this , productName , false ) );
	}

	public Meta getActiveProductMetadata( int metaId ) throws Exception {
		if( transaction != null ) {
			Meta meta = transaction.findTransactionSessionProductMetadata( metaId );
			if( meta != null )
				return( meta );
		}
		return( data.getSessionProductMetadata( this , metaId , false ) );
	}

	public Meta findActiveProductMetadata( String productName ) {
		if( transaction != null ) {
			Meta meta = transaction.findTransactionSessionProductMetadata( productName );
			if( meta != null )
				return( meta );
		}
		return( data.findSessionProductMetadata( this , productName ) );
	}

	public void releaseProductMetadata( TransactionBase transaction , Meta sessionMeta ) throws Exception {
		data.releaseSessionProductMetadata( transaction.action , sessionMeta );
	}

	public Meta reloadProductMetadata( String productName ) throws Exception {
		return( data.getSessionProductMetadata( this , productName , true ) );
	}

}
