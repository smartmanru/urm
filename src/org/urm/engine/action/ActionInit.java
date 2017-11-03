package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.engine.EngineSession;
import org.urm.engine.TransactionBase;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.schedule.EngineScheduler;
import org.urm.engine.status.EngineStatus;
import org.urm.engine.status.ScopeState;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineReleaseLifecycles;
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
	private EngineLoader loader;
	
	protected TransactionBase transaction;
	private boolean memoryOnly;
	private EngineEventsApp eventsApp;

	public ActionInit( EngineSession session , Artefactory artefactory , CommandExecutor executor , CommandOutput output , String actionInfo ) {
		super( session , artefactory , executor , output , actionInfo );
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

	public void create( RootActionType type , EngineLoader loader , CommandMethod commandAction , String actionName , boolean memoryOnly ) throws Exception {
		this.type = type;
		this.commandAction = commandAction;
		this.actionName = actionName;
		this.loader = loader;
		this.memoryOnly = memoryOnly;
		
		EngineEvents events = loader.engine.getEvents();
		eventsApp = events.createApp( "session-" + super.session.sessionId );
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
		EngineEvents events = loader.engine.getEvents();
		events.deleteApp( eventsApp );
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
		return( loader.getServerSettings() );
	}

	public EngineContext getActiveServerContext() {
		EngineSettings settings = getActiveServerSettings();
		return( settings.getServerContext() );
	}

	public LocalFolder getServerHomeFolder() throws Exception {
		return( loader.getServerHomeFolder( this ) );
	}
	
	public LocalFolder getServerSettingsFolder() throws Exception {
		return( loader.getServerSettingsFolder( this ) );
	}
	
	public void setServerSettings( TransactionBase transaction , EngineSettings settings ) throws Exception {
		loader.setServerSettings( transaction , settings );
	}
	
	public EngineMirrors getActiveMirrors() {
		if( transaction != null ) {
			if( transaction.mirrors != null )
				return( transaction.mirrors );
		}
		
		EngineRegistry registry = loader.getRegistry();
		return( registry.mirrors );
	}
	
	public void saveInfrastructure( TransactionBase transaction ) throws Exception {
		loader.saveInfrastructure( transaction );
	}
	
	public void saveReleaseLifecycles( TransactionBase transaction ) throws Exception {
		loader.saveReleaseLifecycles( transaction );
	}
	
	public void saveBase( TransactionBase transaction ) throws Exception {
		loader.saveBase( transaction );
	}
	
	public void saveMonitoring( TransactionBase transaction ) throws Exception {
		loader.saveMonitoring( transaction );
	}
	
	public EngineResources getActiveResources() {
		if( transaction != null ) {
			if( transaction.resources != null )
				return( transaction.resources );
		}
		
		EngineRegistry registry = loader.getRegistry();
		return( registry.resources );
	}

	public void setResources( TransactionBase transaction , EngineResources resources ) throws Exception {
		loader.setResources( transaction , resources );
	}
	
	public EngineBuilders getActiveBuilders() {
		if( transaction != null ) {
			if( transaction.builders != null )
				return( transaction.builders );
		}
		
		EngineRegistry registry = loader.getRegistry();
		return( registry.builders );
	}
	
	public void setBuilders( TransactionBase transaction , EngineBuilders builders ) throws Exception {
		loader.setBuilders( transaction , builders );
	}
	
	public EngineDirectory getActiveDirectory() {
		if( transaction != null ) {
			if( transaction.directory != null )
				return( transaction.directory );
		}
		
		EngineRegistry registry = loader.getRegistry();
		return( registry.directory );
	}
	
	public EngineInfrastructure getActiveInfrastructure() {
		return( loader.getInfrastructure() );
	}
	
	public EngineReleaseLifecycles getActiveReleaseLifecycles() {
		if( transaction != null ) {
			if( transaction.lifecycles != null )
				return( transaction.lifecycles );
		}
		return( loader.getReleaseLifecycles() );
	}
	
	public EngineMonitoring getActiveMonitoring() {
		return( loader.getMonitoring() );
	}
	
	public EngineBase getServerBase() {
		return( loader.getServerBase() );
	}

	public EngineReleaseLifecycles getServerReleaseLifecycles() {
		return( loader.getReleaseLifecycles() );
	}
	
	public EngineInfrastructure getServerInfrastructure() {
		return( loader.getInfrastructure() );
	}
	
	public EngineMonitoring getServerMonitoring() {
		return( loader.getMonitoring() );
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
	
	public void setDirectory( TransactionBase transaction , EngineDirectory directory ) throws Exception {
		loader.setDirectory( transaction , directory );
	}

	public void setMirrors( TransactionBase transaction , EngineMirrors mirrors ) throws Exception {
		loader.setMirrors( transaction , mirrors );
	}

	public void saveRegistry( TransactionBase transaction ) throws Exception {
		loader.saveRegistry( transaction );
	}

	public Meta getActiveProductMetadata( String productName ) throws Exception {
		if( transaction != null ) {
			Meta meta = transaction.findTransactionSessionProductMetadata( productName );
			if( meta != null )
				return( meta );
		}
		return( loader.getSessionProductMetadata( this , productName , false ) );
	}

	public LocalFolder getActiveProductHomeFolder( String productName ) throws Exception {
		if( transaction != null ) {
			Meta meta = transaction.findTransactionSessionProductMetadata( productName );
			if( meta != null ) {
				MetadataStorage storageMeta = artefactory.getMetadataStorage( this , meta );
				return( storageMeta.getHomeFolder( this ) );
			}
		}
		return( loader.getProductHomeFolder( this , productName ) );
	}
	
	public boolean isActiveProductBroken( String productName ) {
		if( transaction != null ) {
			Meta meta = transaction.findTransactionSessionProductMetadata( productName );
			if( meta != null )
				return( meta.isCorrect() );
		}
		
		return( loader.isProductBroken( productName ) );
	}
	
	public void setProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		loader.setProductMetadata( transaction , storage );
	}

	public void deleteProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		loader.deleteProductMetadata( transaction , storage );
	}

	public Meta createProductMetadata( TransactionBase transaction , EngineDirectory directory , Product product ) throws Exception {
		ProductMeta storage = loader.createProductMetadata( transaction , directory , product );
		return( loader.createSessionProductMetadata( transaction.action , storage ) );
	}

	public void releaseProductMetadata( TransactionBase transaction , Meta sessionMeta ) throws Exception {
		loader.releaseSessionProductMetadata( transaction.action , sessionMeta );
	}

	public Meta reloadProductMetadata( String productName ) throws Exception {
		return( loader.getSessionProductMetadata( this , productName , true ) );
	}

}
