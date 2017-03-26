package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerSession;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Node;

public class Meta extends ServerObject {

	public String name;
	public ServerSession session;
	
	private ServerLoader loader;
	private ServerProductMeta storage;

	private MetaProductVersion version;
	private MetaProductSettings product;
	private MetaDatabase database;
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
	
	public Meta( ServerProductMeta storage , ServerSession session ) {
		super( null );
		this.storage = storage;
		this.loader = storage.loader;
		this.session = session;
		name = storage.name;
	}
	
	@Override
	public String getName() {
		return( name );
	}
	
	public void replaceStorage( ActionBase action , ServerProductMeta storage ) throws Exception {
		loader.releaseSessionProductMetadata( action , this , false );
		
		// clear old refs
		version = null;
		product = null;
		database = null;
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
	
	public void setDistr( MetaDistr distr ) {
		this.distr = distr;
	}
	
	public void setDatabase( MetaDatabase database ) {
		this.database = database;
	}
	
	public void setSources( MetaSource sources ) {
		this.sources = sources;
	}

	public synchronized ServerProductMeta getStorage( ActionBase action ) throws Exception {
		return( storage );
	}

	public synchronized MetaProductVersion getVersion( ActionBase action ) throws Exception {
		if( version == null )
			version = loader.loadVersion( action.actionInit , storage );
		return( version );
	}

	public synchronized MetaProductCoreSettings getProductCoreSettings( ActionBase action ) throws Exception {
		MetaProductSettings settings = getProductSettings( action );
		return( settings.core );
	}
	
	public synchronized MetaProductSettings getProductSettings( ActionBase action ) throws Exception {
		if( product == null )
			product = loader.loadProduct( action.actionInit , storage );
		return( product );
	}
	
	public synchronized MetaDatabase getDatabase( ActionBase action ) throws Exception {
		if( database == null )
			database = loader.loadDatabase( action.actionInit , storage );
		return( database );
	}

	public synchronized MetaDistr getDistr( ActionBase action ) throws Exception {
		if( distr == null )
			distr = loader.loadDistr( action.actionInit , storage );
		return( distr );
	}

	public synchronized MetaSource getSources( ActionBase action ) throws Exception {
		if( sources == null )
			sources = loader.loadSources( action.actionInit , storage );
		return( sources );
	}

	public synchronized MetaMonitoring getMonitoring( ActionBase action ) throws Exception {
		if( monitoring == null )
			monitoring = loader.loadMonitoring( action.actionInit , storage );
		return( monitoring );
	}
	
	public synchronized MetaDesign getDesignData( ActionBase action , String fileName ) throws Exception {
		return( loader.loadDesignData( action.actionInit , storage , fileName ) );
	}
	
	public String[] getEnvNames() {
		return( storage.getEnvironmentNames() );
	}
	
	public MetaEnv[] getEnvironments() {
		return( storage.getEnvironments() );
	}
	
	public synchronized MetaEnv getEnvData( ActionBase action , String envFile , boolean loadProps ) throws Exception {
		return( loader.loadEnvData( action.actionInit , storage , envFile , loadProps ) );
	}
	
	public MetaEnv findEnv( String envId ) {
		return( storage.findEnvironment( envId ) );
	}
	
	public synchronized MetaEnv getEnv( ActionBase action , String envId ) throws Exception {
		getStorage( action );
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

	public static VarCATEGORY readCategoryAttr( Node node ) throws Exception {
		String value = ConfReader.getAttrValue( node , "category" );
		return( Types.getCategory( value , true ) );
	}
	
	public static boolean isSourceCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.PROJECT )
			return( true );
		return( false );
	}
	
	public static VarCATEGORY[] getAllReleaseCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.PROJECT , VarCATEGORY.CONFIG , VarCATEGORY.DB , VarCATEGORY.MANUAL };
		return( categories );
	}

	public static VarCATEGORY[] getAllSourceCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.PROJECT };
		return( categories );
	}

	public static boolean checkCategoryProperty( VarCATEGORY part , VarCATEGORY property ) {
		if( part == property )
			return( true );
		if( property == VarCATEGORY.BUILDABLE ) {
			if( part == VarCATEGORY.PROJECT )
				return( true );
		}
		return( false );
	}
	
	public static String getVersionPattern( ActionBase action , VarITEMVERSION version , String basename , String ext ) throws Exception {
		String value = "";
		if( version == VarITEMVERSION.NONE || version == VarITEMVERSION.IGNORE )
			value = basename + ext;
		else if( version == VarITEMVERSION.MIDPOUND )
			value = Common.getLiteral( basename ) + "##[0-9.]+" + Common.getLiteral( ext );
		else if( version == VarITEMVERSION.MIDDASH )
			value = basename + "-[0-9.]+" + ext;
		else if( version == VarITEMVERSION.PREFIX )
			value = "[0-9.]+-" + Common.getLiteral( basename + ext );
		else
			action.exitUnexpectedState();
		
		return( value );
	}

	public static String[] getVersionPatterns( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		String basename = distItem.DISTBASENAME;
		String ext = distItem.EXT;
		if( distItem.deployVersion == VarITEMVERSION.IGNORE ) {
			String[] values = new String[1];
			values[0] = getVersionPattern( action , distItem.deployVersion , basename , ext );
			return( values );
		}

		String[] values = new String[4];
		values[0] = getVersionPattern( action , VarITEMVERSION.NONE , basename , ext );
		values[1] = getVersionPattern( action , VarITEMVERSION.MIDPOUND , basename , ext );
		values[2] = getVersionPattern( action , VarITEMVERSION.MIDDASH , basename , ext );
		values[3] = getVersionPattern( action , VarITEMVERSION.PREFIX , basename , ext );
		return( values );
	}
	
	public static boolean isBinaryContent( ActionBase action , VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.BINARYCOLDDEPLOY || c == VarCONTENTTYPE.BINARYCOPYONLY || c == VarCONTENTTYPE.BINARYHOTDEPLOY )
			return( true );
		return( false );
	}
	
	public static boolean isConfContent( ActionBase action , VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.CONFCOLDDEPLOY || c == VarCONTENTTYPE.CONFCOPYONLY || c == VarCONTENTTYPE.CONFHOTDEPLOY )
			return( true );
		return( false );
	}
	
    public static String getNameAttr( ActionBase action , Node node , VarNAMETYPE nameType ) throws Exception {
    	String name = ConfReader.getRequiredAttrValue( node , "name" );
    	if( nameType == VarNAMETYPE.ANY )
    		return( name );
    	
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
    		
    	if( !name.matches( mask ) )
    		action.exit1( _Error.WrongNameAttribute1 , "name attribute should contain only alphanumeric or dot characters, value=" + name , name );
    	return( name );	
    }

    public static boolean isArchive( VarDISTITEMTYPE distItemType ) {
		if( distItemType == VarDISTITEMTYPE.ARCHIVE_CHILD || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_DIRECT || 
			distItemType == VarDISTITEMTYPE.ARCHIVE_SUBDIR )
			return( true );
		return( false );
    }

    public MetaEnv findMetaEnv( MetaEnv env ) {
    	if( env == null )
    		return( null );
    	return( findEnv( env.ID ) );
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

	public void deleteBinaryItemFromEnvironments( ServerTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteBinaryItem( transaction , item );
	}

	public void deleteConfItemFromEnvironments( ServerTransaction transaction , MetaDistrConfItem item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteConfItem( transaction , item );
	}

	public void deleteComponentFromEnvironments( ServerTransaction transaction , MetaDistrComponent item ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteComponent( transaction , item );
	}

	public void deleteDatabaseSchemaFromEnvironments( ServerTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnv env : storage.getEnvironments() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteSchema( transaction , schema );
	}

}
