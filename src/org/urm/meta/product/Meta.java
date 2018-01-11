package org.urm.meta.product;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineSession;
import org.urm.engine.EngineTransaction;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.Types.*;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.HostAccount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Meta extends EngineObject {

	public String name;
	public EngineSession session;
	
	private EngineProducts products;
	private ProductMeta storage;

	private MetaProductVersion version;
	private MetaProductSettings product;
	private MetaUnits units;
	private MetaDatabase database;
	private MetaDocs docs;
	private MetaDistr distr;
	private MetaSource sources;
	private MetaMonitoring monitoring;
	
	static String[] configurableExtensions = {
		"cmd" , "sh" , "xml" , "txt" , "properties" , "conf" , "config" , "xconf" , "groovy" , "sql" , "yml" 
	};

	static String S_REDIST_ARCHIVE_TYPE_DIRECT = "direct";
	static String S_REDIST_ARCHIVE_TYPE_CHILD = "child";
	static String S_REDIST_ARCHIVE_TYPE_SUBDIR = "subdir";
	
	private static String configurableExtensionsFindOptions = createConfigurableExtensions();

	public static String PROPERTY_NAME = "name";
	
	public Meta( ProductMeta storage , EngineSession session ) {
		super( null );
		this.storage = storage;
		this.products = storage.products;
		this.session = session;
		name = storage.name;
	}
	
	@Override
	public String getName() {
		return( name );
	}

	public void trace( String s ) {
		products.engine.trace( s );
	}
	
	public void replaceStorage( ActionBase action , ProductMeta storage ) throws Exception {
		products.releaseSessionProductMetadata( action , this );
		
		// clear old refs
		version = null;
		product = null;
		units = null;
		database = null;
		docs = null;
		distr = null;
		sources = null;
		monitoring = null;
		
		this.storage = storage;
		storage.addSessionMeta( this );
		session.addProductMeta( this );
	}

	public boolean isCorrect() {
		if( storage.loadFailed )
			return( false );
		return( true );
	}
	
	private static String createConfigurableExtensions() {
		String configurableExtensionsFindOptions = "";
		for( int k = 0; k < configurableExtensions.length; k++ ) {
			if( k > 0 )
				configurableExtensionsFindOptions += " -o ";
			configurableExtensionsFindOptions += "-name \"*." + configurableExtensions[ k ] + "\"";
		}
		return( configurableExtensionsFindOptions );
	}
	
	public void setVersion( MetaProductVersion version ) {
		this.version = version;
	}
	
	public void setProduct( MetaProductSettings product ) {
		this.product = product;
	}
	
	public void setUnits( MetaUnits units ) {
		this.units = units;
	}
	
	public void setDocs( MetaDocs docs ) {
		this.docs = docs;
	}
	
	public void setDistr( MetaDistr distr ) {
		this.distr = distr;
	}
	
	public void setDatabase( MetaDatabase database ) {
		this.database = database;
	}
	
	public void setSources( MetaSource sources ) {
		this.sources = sources;
	}

	public synchronized ProductMeta getStorage() {
		return( storage );
	}

	public synchronized MetaProductVersion getVersion( ActionBase action ) throws Exception {
		if( version == null )
			version = storage.getVersion();
		return( version );
	}

	public synchronized MetaProductCoreSettings getProductCoreSettings( ActionBase action ) throws Exception {
		MetaProductSettings settings = getProductSettings( action );
		return( settings.core );
	}
	
	public synchronized MetaProductSettings getProductSettings( ActionBase action ) throws Exception {
		if( product == null )
			product = storage.getProductSettings();
		return( product );
	}
	
	public synchronized MetaUnits getUnits( ActionBase action ) throws Exception {
		if( units == null )
			units = storage.getUnits();
		return( units );
	}

	public synchronized MetaDatabase getDatabase( ActionBase action ) throws Exception {
		if( database == null )
			database = storage.getDatabase();
		return( database );
	}

	public synchronized MetaDocs getDocs( ActionBase action ) throws Exception {
		if( docs == null )
			docs = storage.getDocs();
		return( docs );
	}

	public synchronized MetaDistr getDistr( ActionBase action ) throws Exception {
		if( distr == null )
			distr = storage.getDistr();
		return( distr );
	}

	public synchronized MetaSource getSources( ActionBase action ) throws Exception {
		if( sources == null )
			sources = storage.getSources();
		return( sources );
	}

	public synchronized MetaMonitoring getMonitoring( ActionBase action ) throws Exception {
		if( monitoring == null )
			monitoring = storage.getMonitoring();
		return( monitoring );
	}
	
	public synchronized MetaDesignDiagram getDesignData( ActionBase action , String fileName ) throws Exception {
		EngineLoader loader = action.engine.createLoader();
		return( products.loadDesignData( loader , storage , fileName ) );
	}
	
	public String[] getEnvNames() {
		return( storage.getEnvironmentNames() );
	}
	
	public MetaEnv[] getEnvironments() {
		return( storage.getEnvironments() );
	}
	
	public DistRepository getDistRepository() {
		return( storage.getDistRepository() );
	}
	
	public synchronized MetaEnv getEnvData( ActionBase action , String envName , boolean loadProps ) throws Exception {
		return( storage.findEnvironment( envName ) );
	}
	
	public MetaEnv findEnv( String envId ) {
		return( storage.findEnvironment( envId ) );
	}
	
	public synchronized MetaEnv getEnv( ActionBase action , String envId ) throws Exception {
		return( storage.findEnvironment( envId ) );
	}
	
	public static String getConfigurableExtensionsFindOptions( ActionBase action ) throws Exception {
		return( configurableExtensionsFindOptions );
	}

	public static boolean isConfigurableFile( ActionBase action , String filePath ) throws Exception {
		String ext = Common.getFileExtension( filePath );
		for( int k = 0; k < configurableExtensions.length; k++ )
			if( ext.equals( configurableExtensions[ k ] ) )
				return( true );
		return( false );
	}

	public static String[] getConfigurableExtensions( ActionBase action ) throws Exception {
		return( configurableExtensions );
	}

	public static String getMask( ActionBase action , VarNAMETYPE nameType ) throws Exception {
    	String mask = null;
    	if( nameType == VarNAMETYPE.ALPHANUM )
    		mask = "[0-9a-zA-Z_]+";
    	else
    	if( nameType == VarNAMETYPE.ALPHANUMDOT )
    		mask = "[0-9a-zA-Z_.]+";
    	else
    	if( nameType == VarNAMETYPE.ALPHANUMDOTDASH )
    		mask = "[0-9a-zA-Z_.-]+";
    	else
    		action.exitUnexpectedState();
    	return( mask );
	}
	
    public static String getNameAttr( ActionBase action , Node node , VarNAMETYPE nameType ) throws Exception {
    	String name = ConfReader.getRequiredAttrValue( node , PROPERTY_NAME );
    	if( nameType == VarNAMETYPE.ANY )
    		return( name );
    	
    	String mask = getMask( action , nameType );
    	if( !name.matches( mask ) )
    		action.exit1( _Error.WrongNameAttribute1 , "name attribute should contain only alphanumeric or dot characters, value=" + name , name );
    	return( name );	
    }

    public static void setNameAttr( ActionBase action , Document doc , Element element , VarNAMETYPE nameType , String value ) throws Exception {
    	if( nameType != VarNAMETYPE.ANY ) {
        	String mask = getMask( action , nameType );
        	if( !value.matches( mask ) )
        		action.exit1( _Error.WrongNameAttribute1 , "name attribute should contain only alphanumeric or dot characters, value=" + value , value );
    	}
    	
    	Common.xmlSetElementAttr( doc , element , PROPERTY_NAME , value );
    }
    
    public MetaEnv findMetaEnv( MetaEnv env ) {
    	if( env == null )
    		return( null );
    	return( findEnv( env.NAME ) );
    }
    
    public MetaEnvSegment findMetaEnvSegment( MetaEnvSegment sg ) {
    	if( sg == null )
    		return( null );
    	MetaEnv env = findMetaEnv( sg.env );
    	if( env == null )
    		return( null );
    	return( env.findSegment( sg.NAME ) );
    }
    
    public MetaEnvServer findMetaEnvServer( MetaEnvServer server ) {
    	if( server == null )
    		return( null );
    	MetaEnvSegment sg = findMetaEnvSegment( server.sg );
    	if( sg == null )
    		return( null );
    	return( sg.findServer( server.NAME ) );
    }

    public MetaEnvServerNode getMetaEnvServerNode( MetaEnvServerNode node ) {
    	if( node == null )
    		return( null );
    	MetaEnvServer server = findMetaEnvServer( node.server );
    	if( server == null )
    		return( null );
    	return( server.findNode( node.POS ) );
    }

	public void deleteBinaryItemFromEnvironments( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteBinaryItem( transaction , item );
	}

	public void deleteConfItemFromEnvironments( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteConfItem( transaction , item );
	}

	public void deleteComponentFromEnvironments( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteComponent( transaction , item );
	}

	public void deleteDatabaseSchemaFromEnvironments( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteSchema( transaction , schema );
	}

	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		for( MetaEnv env : storage.getEnvironments() )
			env.getApplicationReferences( account , refs );
	}

}
