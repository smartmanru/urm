package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.Engine;
import org.urm.engine.DataService;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.session.EngineSession;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.loader.Types.*;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.system.AppProduct;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Meta extends EngineObject {

	public String name;
	public Engine engine;
	public EngineSession session;
	public EngineProduct ep;
	
	private ProductMeta storage;

	private MetaProductSettings settings;
	private MetaUnits units;
	private MetaDatabase database;
	private MetaDocs docs;
	private MetaDistr distr;
	private MetaSources sources;
	
	static String[] configurableExtensions = {
		"cmd" , "sh" , "xml" , "txt" , "properties" , "conf" , "config" , "xconf" , "groovy" , "sql" , "yml" 
	};

	static String S_REDIST_ARCHIVE_TYPE_DIRECT = "direct";
	static String S_REDIST_ARCHIVE_TYPE_CHILD = "child";
	static String S_REDIST_ARCHIVE_TYPE_SUBDIR = "subdir";
	
	private static String configurableExtensionsFindOptions = createConfigurableExtensions();

	public static String PROPERTY_NAME = "name";
	
	public Meta( Engine engine , EngineProduct ep , ProductMeta storage , EngineSession session ) {
		super( null );
		this.ep = ep;
		this.storage = storage;
		this.session = session;
		this.engine = ep.engine;
		name = storage.NAME;
		
		if( session != null )
			engine.trace( "new run session meta object, id=" + super.objectId + ", session=" + session.objectId );
		else
			engine.trace( "new run revision meta object, id=" + super.objectId );
	}
	
	@Override
	public String getName() {
		return( name );
	}

	public Integer getId() {
		return( storage.ID );
	}
	
	public EngineProduct getEngineProduct() {
		return( ep );
	}
	
	public AppProduct getProduct() throws Exception {
		return( ep.getProduct() );
	}

	public AppProduct findProduct() {
		return( ep.findProduct() );
	}

	public boolean isPrimary() {
		return( storage.isPrimary() );
	}

	public boolean isDraft() {
		return( storage.isDraft() );
	}
	
	public String getRevision() {
		return( storage.REVISION );
	}
	
	public void setStorage( ProductMeta storage ) {
		// clear old refs
		settings = null;
		units = null;
		database = null;
		docs = null;
		distr = null;
		sources = null;
		
		this.storage = storage;
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
	
	public void setSettings( MetaProductSettings settings ) {
		this.settings = settings;
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
	
	public void setSources( MetaSources sources ) {
		this.sources = sources;
	}

	public synchronized ProductMeta getStorage() {
		return( storage );
	}

	public synchronized MetaProductCoreSettings getProductCoreSettings() {
		MetaProductSettings settings = getProductSettings();
		return( settings.core );
	}
	
	public synchronized MetaProductSettings getProductSettings() {
		if( settings == null )
			settings = storage.getSettings();
		return( settings );
	}
	
	public synchronized MetaUnits getUnits() {
		if( units == null )
			units = storage.getUnits();
		return( units );
	}

	public synchronized MetaDatabase getDatabase() {
		if( database == null )
			database = storage.getDatabase();
		return( database );
	}

	public synchronized MetaDocs getDocs() {
		if( docs == null )
			docs = storage.getDocs();
		return( docs );
	}

	public synchronized MetaDistr getDistr() {
		if( distr == null )
			distr = storage.getDistr();
		return( distr );
	}

	public synchronized MetaSources getSources() {
		if( sources == null )
			sources = storage.getSources();
		return( sources );
	}

	public ProductEnvs getEnviroments() {
		return( storage.getEnviroments() );
	}
	
	public ReleaseRepository getReleases() {
		return( storage.getReleaseRepository() );
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

	public static String getMask( ActionBase action , EnumNameType nameType ) throws Exception {
    	String mask = null;
    	if( nameType == EnumNameType.ALPHANUM )
    		mask = "[0-9a-zA-Z_]+";
    	else
    	if( nameType == EnumNameType.ALPHANUMDOT )
    		mask = "[0-9a-zA-Z_.]+";
    	else
    	if( nameType == EnumNameType.ALPHANUMDOTDASH )
    		mask = "[0-9a-zA-Z_.-]+";
    	else
    		action.exitUnexpectedState();
    	return( mask );
	}
	
    public static String getNameAttr( ActionBase action , Node node , EnumNameType nameType ) throws Exception {
    	String name = ConfReader.getRequiredAttrValue( node , PROPERTY_NAME );
    	if( nameType == EnumNameType.ANY )
    		return( name );
    	
    	String mask = getMask( action , nameType );
    	if( !name.matches( mask ) )
    		action.exit1( _Error.WrongNameAttribute1 , "name attribute should contain only alphanumeric or dot characters, value=" + name , name );
    	return( name );	
    }

    public static void setNameAttr( ActionBase action , Document doc , Element element , EnumNameType nameType , String value ) throws Exception {
    	if( nameType != EnumNameType.ANY ) {
        	String mask = getMask( action , nameType );
        	if( !value.matches( mask ) )
        		action.exit1( _Error.WrongNameAttribute1 , "name attribute should contain only alphanumeric or dot characters, value=" + value , value );
    	}
    	
    	Common.xmlSetElementAttr( doc , element , PROPERTY_NAME , value );
    }

    public MetaEnv findEnv( String name ) {
    	ProductEnvs envs = storage.getEnviroments();
    	return( envs.findMetaEnv( name ) );
    }

    public MetaEnv findEnv( int id ) {
    	ProductEnvs envs = storage.getEnviroments();
    	return( envs.findMetaEnv( id ) );
    }

	public static Integer getObject( MetaDistrBinaryItem item ) {
		if( item == null )
			return( null );
		return( item.ID );
	}
	
	public static Integer getObject( MetaDistrConfItem item ) {
		if( item == null )
			return( null );
		return( item.ID );
	}
	
	public static Integer getObject( MetaDatabaseSchema schema ) {
		if( schema == null )
			return( null );
		return( schema.ID );
	}

	public DataService getEngineData() {
		return( engine.getData() );
	}
	
}
