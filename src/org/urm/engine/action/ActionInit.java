package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.engine.EngineSession;
import org.urm.engine.TransactionBase;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.schedule.EngineScheduler;
import org.urm.engine.status.EngineStatus;
import org.urm.engine.status.ScopeState;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.EngineData;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
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
	private Engine engine;
	private EngineData data;
	
	protected TransactionBase transaction;
	private boolean memoryOnly;
	private EngineEventsApp eventsApp;

	public ActionInit( Engine engine , EngineSession session , Artefactory artefactory , CommandExecutor executor , CommandOutput output , String actionInfo ) {
		super( session , artefactory , executor , output , actionInfo );
		this.actionInit = this;
		this.engine = engine;
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
		this.data = engine.getData();
		this.memoryOnly = memoryOnly;
		
		if( !memoryOnly ) {
			EngineEvents events = data.engine.getEvents();
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
			EngineEvents events = data.engine.getEvents();
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
	
	public EngineSettings getActiveServerSettings() {
		if( transaction != null ) {
			if( transaction.settings != null )
				return( transaction.settings );
		}
		return( data.getEngineSettings() );
	}

	public EngineContext getActiveServerContext() {
		EngineSettings settings = getActiveServerSettings();
		return( settings.getServerContext() );
	}

	public EngineMirrors getActiveMirrors() {
		if( transaction != null ) {
			if( transaction.mirrors != null )
				return( transaction.mirrors );
		}
		
		EngineRegistry registry = data.getRegistry();
		return( registry.mirrors );
	}
	
	public EngineResources getActiveResources() {
		if( transaction != null ) {
			if( transaction.resources != null )
				return( transaction.resources );
		}
		
		EngineRegistry registry = data.getRegistry();
		return( registry.resources );
	}

	public EngineBuilders getActiveBuilders() {
		if( transaction != null ) {
			if( transaction.builders != null )
				return( transaction.builders );
		}
		
		EngineRegistry registry = data.getRegistry();
		return( registry.builders );
	}
	
	public EngineDirectory getActiveDirectory() {
		if( transaction != null ) {
			if( transaction.directory != null )
				return( transaction.directory );
		}
		
		EngineDirectory directory = data.getDirectory();
		return( directory );
	}
	
	public EngineInfrastructure getActiveInfrastructure() {
		return( data.getInfrastructure() );
	}
	
	public EngineLifecycles getActiveReleaseLifecycles() {
		if( transaction != null ) {
			if( transaction.lifecycles != null )
				return( transaction.lifecycles );
		}
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
	
	public EngineAuth getServerAuth() {
		return( engine.getAuth() );
	}
	
	public EngineStatus getServerStatus() {
		return( engine.getStatus() );
	}
	
	public EngineScheduler getServerScheduler() {
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

	public Meta createProductMetadata( TransactionBase transaction , AppProduct product ) throws Exception {
		return( data.createProductMetadata( transaction , product ) );
	}

	public void releaseProductMetadata( TransactionBase transaction , Meta sessionMeta ) throws Exception {
		data.releaseSessionProductMetadata( transaction.action , sessionMeta , false );
	}

	public Meta reloadProductMetadata( String productName ) throws Exception {
		return( data.getSessionProductMetadata( this , productName , true ) );
	}

}
