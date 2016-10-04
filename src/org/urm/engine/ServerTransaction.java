package org.urm.engine;

import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.action.ActionInit;
import org.urm.engine.registry.ServerAuthResource;
import org.urm.engine.registry.ServerMirrorRepository;
import org.urm.engine.registry.ServerMirrors;
import org.urm.engine.registry.ServerProduct;
import org.urm.engine.registry.ServerProjectBuilder;
import org.urm.engine.registry.ServerSystem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.Meta.VarBUILDMODE;
import org.urm.meta.product.Meta.VarNODETYPE;
import org.urm.meta.product.Meta.VarSERVERACCESSTYPE;
import org.urm.meta.product.Meta.VarSERVERRUNTYPE;

public class ServerTransaction extends TransactionBase {

	public ServerTransaction( ServerEngine engine , ActionInit action ) {
		super( engine , action );
	}

	// transactional operations
	public void createMirrorRepository( ServerMirrorRepository repo , String resource , String reponame , String reporoot , String dataroot , String repobranch , boolean push ) throws Exception {
		repo.createMirrorRepository( this , resource , reponame  , reporoot , dataroot , repobranch , push );
		action.saveMirrors( this );
	}

	public void pushMirror( ServerMirrorRepository repo ) throws Exception {
		repo.pushMirror( this );
		action.saveMirrors( this );
	}

	public void refreshMirror( ServerMirrorRepository repo ) throws Exception {
		repo.refreshMirror( this );
		action.saveMirrors( this );
	}

	public void dropMirror( ServerMirrorRepository repo ) throws Exception {
		repo.dropMirror( this );
		repo.deleteObject();
		action.saveMirrors( this );
	}

	public void createResource( ServerAuthResource res ) throws Exception {
		checkTransactionResources();
		resources.createResource( this , res );
	}
	
	public void updateResource( ServerAuthResource res , ServerAuthResource resNew ) throws Exception {
		checkTransactionResources();
		res.updateResource( this , resNew );
	}
	
	public void deleteResource( ServerAuthResource res ) throws Exception {
		checkTransactionResources();
		resources.deleteResource( this , res );
		res.deleteObject();
	}
	
	public void createBuilder( ServerProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		builders.createBuilder( this , builder );
	}
	
	public void updateBuilder( ServerProjectBuilder builder , ServerProjectBuilder builderNew ) throws Exception {
		checkTransactionBuilders();
		builder.updateBuilder( this , builderNew );
	}
	
	public void deleteBuilder( ServerProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		builders.deleteBuilder( this , builder );
		builder.deleteObject();
	}
	
	public void createSystem( ServerSystem system ) throws Exception {
		checkTransactionDirectory();
		directory.addSystem( this , system );
	}
	
	public void modifySystem( ServerSystem system ) throws Exception {
		checkTransactionDirectory();
		system.modifySystem( this );
	}

	public void deleteSystem( ServerSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		
		ServerMirrors mirrors = action.getMirrors();
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
			mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		directory.deleteSystem( this , system );
		system.deleteObject();
	}
	
	public void createProduct( ServerProduct product , boolean forceClear ) throws Exception {
		checkTransactionDirectory();
		
		ServerMirrors mirrors = action.getMirrors();
		mirrors.addProductMirrors( this , product , forceClear );
		
		createMetadata = true;
		directory.createProduct( this , product );
		sessionMeta = action.createProductMetadata( this , directory , product );
		metadata = sessionMeta.getStorage( action );
	}
	
	public void modifyProduct( ServerProduct product ) throws Exception {
		checkTransactionDirectory();
		product.modifyProduct( this );
	}

	public void deleteProduct( ServerProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		checkTransactionMetadata( metadataOld );
		
		ServerMirrors mirrors = action.getMirrors();
		mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		directory.deleteProduct( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		metadata = null;
		action.saveMirrors( this );
		product.deleteObject();
	}

	public void setServerProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setServerProperties( this , props );
	}
	
	public void setServerProductDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductDefaultsProperties( this , props );
	}
	
	public void setServerProductBuildCommonDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildCommonDefaultsProperties( this , props );
	}
	
	public void setServerProductBuildModeDefaultsProperties( VarBUILDMODE mode , PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildModeDefaultsProperties( this , mode , props );
	}
	
	public void setProductVersion( MetaProductVersion version ) throws Exception {
		checkTransactionMetadata( version.meta.getStorage( action ) );
		metadata.setVersion( this , version );
	}

	public void setProductProperties( PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( metadata );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setProperties( this , props , system );
	}
	
	public void setProductBuildCommonProperties( PropertySet props ) throws Exception {
		checkTransactionMetadata( metadata );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setBuildCommonProperties( this , props );
	}
	
	public void setProductBuildModeProperties( VarBUILDMODE mode , PropertySet props ) throws Exception {
		checkTransactionMetadata( metadata );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setBuildModeProperties( this , mode , props );
	}

	public MetaProductVersion createProductVersion( Meta meta , int majorFirstNumber , int majorSecondNumber , int majorNextFirstNumber , int majorNextSecondNumber , int lastProdTag , int nextProdTag ) throws Exception {
		checkTransactionMetadata( meta.getStorage( action ) );
		MetaProductVersion version = new MetaProductVersion( metadata , metadata.meta );
		version.createVersion( this , majorFirstNumber , majorSecondNumber , majorNextFirstNumber , majorNextSecondNumber , lastProdTag , nextProdTag );
		metadata.setVersion( this , version );
		return( version );
	}
	
	public MetaEnv createMetaEnv( Meta meta , String name , boolean prod ) throws Exception {
		checkTransactionMetadata( meta.getStorage( action ) );
		MetaEnv env = new MetaEnv( metadata , metadata.meta );
		env.createEnv( getAction() , name , prod );
		metadata.addEnv( this , env );
		return( env );
	}
	
	public void deleteMetaEnv( MetaEnv env ) throws Exception {
		checkTransactionMetadata( env.meta.getStorage( action ) );
		metadata.deleteEnv( this , env );
		env.deleteObject();
	}

	public void setMetaEnvProperties( MetaEnv env , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( env.meta.getStorage( action ) );
		env.setProperties( this , props , system );
	}
	
	public void updateMetaEnv( MetaEnv env ) throws Exception {
		checkTransactionMetadata( env.meta.getStorage( action ) );
		env.updateProperties( this );
	}
	
	public void updateMetaEnvDC( MetaEnvDC dc ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		dc.updateProperties( this );
	}
	
	public void createMetaEnvDC( MetaEnvDC dc ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		dc.env.createDC( this , dc );
	}
	
	public void deleteMetaEnvDC( MetaEnvDC dc ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		dc.env.deleteDC( this , dc );
		dc.deleteObject();
	}

	public MetaEnvServer createMetaEnvServer( MetaEnvDC dc , String name , VarOSTYPE osType , VarSERVERRUNTYPE runType , VarSERVERACCESSTYPE accessType , String service ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		MetaEnvServer server = new MetaEnvServer( getMeta() , dc );
		server.createServer( action , name , osType , runType , accessType , service );
		dc.createServer( this , server );
		return( server );
	}
	
	public void deleteMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.dc.deleteServer( this , server );
		server.deleteObject();
	}

	public void updateMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.updateProperties( this );
	}

	public MetaEnvServerNode createMetaEnvServerNode( MetaEnvServer server , int pos , VarNODETYPE nodeType , String account ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		MetaEnvServerNode node = new MetaEnvServerNode( getMeta() , server , pos );
		node.createNode( action , nodeType , account );
		server.createNode( this , node );
		return( node );
	}
	
	public void updateMetaEnvServerNode( MetaEnvServerNode node ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage( action ) );
		node.updateProperties( this );
	}

	public void deleteMetaEnvServerNode( MetaEnvServerNode node ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage( action ) );
		node.server.deleteNode( this , node );
		node.deleteObject();
	}

}
