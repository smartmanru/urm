package org.urm.engine;

import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaEnv;
import org.urm.engine.meta.MetaEnvDC;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.MetaProductSettings;
import org.urm.engine.meta.MetaProductVersion;
import org.urm.engine.meta.Meta.VarBUILDMODE;
import org.urm.engine.meta.Meta.VarSERVERACCESSTYPE;
import org.urm.engine.meta.Meta.VarSERVERRUNTYPE;
import org.urm.engine.registry.ServerAuthResource;
import org.urm.engine.registry.ServerMirrorRepository;
import org.urm.engine.registry.ServerMirrors;
import org.urm.engine.registry.ServerProduct;
import org.urm.engine.registry.ServerProjectBuilder;
import org.urm.engine.registry.ServerSystem;

public class ServerTransaction extends TransactionBase {

	public ServerTransaction( ServerEngine engine ) {
		super( engine );
	}

	// transactional operations
	public void createMirrorRepository( ServerMirrorRepository repo , String resource , String reponame , String reporoot , String dataroot , String repobranch , boolean push ) throws Exception {
		repo.createMirrorRepository( this , resource , reponame  , reporoot , dataroot , repobranch , push );
		loader.saveMirrors( this );
	}

	public void pushMirror( ServerMirrorRepository repo ) throws Exception {
		repo.pushMirror( this );
		loader.saveMirrors( this );
	}

	public void refreshMirror( ServerMirrorRepository repo ) throws Exception {
		repo.refreshMirror( this );
		loader.saveMirrors( this );
	}

	public void dropMirror( ServerMirrorRepository repo ) throws Exception {
		repo.dropMirror( this );
		repo.deleteObject();
		loader.saveMirrors( this );
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
		
		ServerMirrors mirrors = engine.getMirrors();
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
			mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		directory.deleteSystem( this , system );
		system.deleteObject();
	}
	
	public void createProduct( ServerProduct product , boolean forceClear ) throws Exception {
		checkTransactionDirectory();
		
		ServerMirrors mirrors = engine.getMirrors();
		mirrors.addProductMirrors( this , product , forceClear );
		
		createMetadata = true;
		directory.createProduct( this , product );
		metadata = loader.createMetadata( this , directory , product );
	}
	
	public void modifyProduct( ServerProduct product ) throws Exception {
		checkTransactionDirectory();
		product.modifyProduct( this );
	}

	public void deleteProduct( ServerProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		checkTransactionMetadata( metadata );
		ServerMirrors mirrors = engine.getMirrors();
		mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		directory.deleteProduct( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		metadata = null;
		loader.saveMirrors( this );
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
		version.createVersion( getAction() , majorFirstNumber , majorSecondNumber , majorNextFirstNumber , majorNextSecondNumber , lastProdTag , nextProdTag );
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

	public MetaEnvServer createMetaEnvServer( MetaEnvDC dc , String name , VarOSTYPE osType , VarSERVERRUNTYPE runType , VarSERVERACCESSTYPE accessType ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		MetaEnvServer server = new MetaEnvServer( getMeta() , dc );
		server.createServer( getAction() , name , osType , runType , accessType );
		dc.createServer( this , server );
		return( server );
	}
	
	public void deleteMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.dc.deleteServer( this , server );
		server.deleteObject();
	}

	public void setMetaEnvServerStatus( MetaEnvServer server , boolean OFFLINE ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.setOfflineStatus( this , OFFLINE );
	}

}
