package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.engine.ServerSession;
import org.urm.engine.TransactionBase;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerAuth;
import org.urm.meta.engine.ServerBase;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.engine.ServerContext;
import org.urm.meta.engine.ServerDirectory;
import org.urm.meta.engine.ServerInfrastructure;
import org.urm.meta.engine.ServerMirrors;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.engine.ServerProduct;
import org.urm.meta.engine.ServerRegistry;
import org.urm.meta.engine.ServerResources;
import org.urm.meta.engine.ServerSettings;
import org.urm.meta.product.Meta;

public class ActionInit extends ActionBase {

	public CommandAction commandAction;
	public String actionName;
	private ServerLoader loader;
	
	protected TransactionBase transaction;
	private boolean memoryOnly;

	public ActionInit( ServerLoader loader , ServerSession session , Artefactory artefactory , CommandExecutor executor , CommandOutput output , CommandAction commandAction , String actionName , boolean memoryOnly ) {
		super( session , artefactory , executor , output );
		this.actionInit = this;
		this.commandAction = commandAction;
		this.actionName = actionName;
		this.loader = loader;
		this.memoryOnly = memoryOnly;
	}

	@Override
	protected void runBefore() throws Exception {
		Common.exitUnexpected();
	}
	
	@Override
	protected void runBefore( ActionScope scope ) throws Exception {
		Common.exitUnexpected();
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

	public ServerSettings getActiveServerSettings() {
		if( transaction != null ) {
			if( transaction.settings != null )
				return( transaction.settings );
		}
		return( loader.getServerSettings() );
	}

	public ServerContext getActiveServerContext() {
		ServerSettings settings = getActiveServerSettings();
		return( settings.getServerContext() );
	}

	public LocalFolder getServerHomeFolder() throws Exception {
		return( loader.getServerHomeFolder( this ) );
	}
	
	public LocalFolder getServerSettingsFolder() throws Exception {
		return( loader.getServerSettingsFolder( this ) );
	}
	
	public void setServerSettings( TransactionBase transaction , ServerSettings settings ) throws Exception {
		loader.setServerSettings( transaction , settings );
	}
	
	public ServerMirrors getActiveMirrors() {
		ServerRegistry registry = loader.getRegistry();
		return( registry.mirrors );
	}
	
	public void saveInfrastructure( TransactionBase transaction ) throws Exception {
		loader.saveInfrastructure( transaction );
	}
	
	public void saveBase( TransactionBase transaction ) throws Exception {
		loader.saveBase( transaction );
	}
	
	public void saveMirrors( TransactionBase transaction ) throws Exception {
		loader.saveMirrors( transaction );
	}
	
	public void saveMonitoring( TransactionBase transaction ) throws Exception {
		loader.saveMonitoring( transaction );
	}
	
	public ServerResources getActiveResources() {
		if( transaction != null ) {
			if( transaction.resources != null )
				return( transaction.resources );
		}
		
		ServerRegistry registry = loader.getRegistry();
		return( registry.resources );
	}

	public void setResources( TransactionBase transaction , ServerResources resources ) throws Exception {
		loader.setResources( transaction , resources );
	}
	
	public ServerBuilders getActiveBuilders() {
		if( transaction != null ) {
			if( transaction.builders != null )
				return( transaction.builders );
		}
		
		ServerRegistry registry = loader.getRegistry();
		return( registry.builders );
	}
	
	public void setBuilders( TransactionBase transaction , ServerBuilders builders ) throws Exception {
		loader.setBuilders( transaction , builders );
	}
	
	public ServerDirectory getActiveDirectory() {
		if( transaction != null ) {
			if( transaction.directory != null )
				return( transaction.directory );
		}
		
		ServerRegistry registry = loader.getRegistry();
		return( registry.directory );
	}
	
	public ServerInfrastructure getActiveInfrastructure() {
		return( loader.getInfrastructure() );
	}
	
	public ServerMonitoring getActiveMonitoring() {
		return( loader.getMonitoring() );
	}
	
	public ServerBase getServerBase() {
		return( loader.getServerBase() );
	}
	
	public ServerInfrastructure getServerInfrastructure() {
		return( loader.getInfrastructure() );
	}
	
	public ServerMonitoring getServerMonitoring() {
		return( loader.getMonitoring() );
	}
	
	public ServerAuth getServerAuth() {
		return( engine.getAuth() );
	}
	
	public void setDirectory( TransactionBase transaction , ServerDirectory directory ) throws Exception {
		loader.setDirectory( transaction , directory );
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
	
	public void setProductMetadata( TransactionBase transaction , ServerProductMeta storage ) throws Exception {
		loader.setProductMetadata( transaction , storage );
	}

	public void deleteProductMetadata( TransactionBase transaction , ServerProductMeta storage ) throws Exception {
		loader.deleteProductMetadata( transaction , storage );
	}

	public Meta createProductMetadata( TransactionBase transaction , ServerDirectory directory , ServerProduct product ) throws Exception {
		ServerProductMeta storage = loader.createProductMetadata( transaction , directory , product );
		return( loader.createSessionProductMetadata( transaction.action , storage ) );
	}

	public void releaseProductMetadata( TransactionBase transaction , Meta sessionMeta ) throws Exception {
		loader.releaseSessionProductMetadata( transaction.action , sessionMeta );
	}

	public void reloadCoreMetadata() throws Exception {
		loader.reloadCore();
	}
	
	public Meta reloadProductMetadata( String productName ) throws Exception {
		loader.reloadProduct( productName );
		return( loader.getSessionProductMetadata( this , productName , true ) );
	}
	
}
