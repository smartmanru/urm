package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerLoader;
import org.urm.engine.ServerProductMeta;
import org.urm.engine.SessionContext;
import org.urm.engine.meta.MetaBase.VarBASESRCFORMAT;
import org.urm.engine.meta.MetaBase.VarBASESRCTYPE;
import org.urm.engine.meta.MetaDesign.VarELEMENTTYPE;
import org.urm.engine.meta.MetaDesign.VarLINKTYPE;
import org.w3c.dom.Node;

public class Meta {

	public String name;
	
	private ServerLoader loader;
	private SessionContext session;
	private ServerProductMeta storage;

	private MetaProductVersion version;
	private MetaProductSettings product;
	private MetaDatabase database;
	private MetaDistr distr;
	private MetaSource sources;
	
	static String[] configurableExtensions = {
		"cmd" , "sh" , "xml" , "txt" , "properties" , "conf" , "config" , "xconf" , "groovy" , "sql" , "yml" 
	};

	static String S_REDIST_ARCHIVE_TYPE_DIRECT = "direct";
	static String S_REDIST_ARCHIVE_TYPE_CHILD = "child";
	static String S_REDIST_ARCHIVE_TYPE_SUBDIR = "subdir";
	
	public enum VarCATEGORY {
		UNKNOWN ,
		BUILD ,
		MANUAL , 
		PREBUILT ,
		CONFIG ,
		DB ,
		ENV
	};

	public enum VarITEMVERSION {
		UNKNOWN ,
		NONE ,
		IGNORE ,
		MIDDASH ,
		MIDPOUND ,
		PREFIX
	};
	
	public enum VarITEMSRCTYPE {
		UNKNOWN ,
		NEXUS ,
		NUGET ,
		NUGET_PLATFORM ,
		STATICWAR ,
		SVN ,
		SVNOLD ,
		SVNNEW ,
		GENERATED
	};
	
	public enum VarBUILDMODE {
		UNKNOWN ,
		BRANCH ,
		MAJORBRANCH ,
		TRUNK ,
		DEVBRANCH ,
		DEVTRUNK
	};
	
	public enum VarSERVERTYPE {
		UNKNOWN ,
		SERVICE ,
		SERVICE_DATABASE ,
		GENERIC_WEB ,
		GENERIC_SERVER ,
		GENERIC_COMMAND ,
		GENERIC_NOSSH ,
		GENERIC_DATABASE ,
		OFFLINE
	};

	public enum VarNODETYPE {
		UNKNOWN ,
		SELF ,
		ADMIN ,
		SLAVE
	};
	
	public enum VarDEPLOYTYPE {
		UNKNOWN ,
		NONE , 
		MANUAL , 
		COLD , 
		HOT ,
		LINKS_SINGLEDIR ,
		LINKS_MULTIDIR ,
		COPYONLY
	};
	
	public enum VarDBMSTYPE {
		UNKNOWN ,
		ORACLE ,
		POSTGRESQL ,
		FIREBIRD
	};

	public enum VarSESSIONTYPE {
		UNKNOWN ,
		UNIXLOCAL ,
		UNIXREMOTE ,
		UNIXFROMWINDOWS ,
		WINDOWSLOCAL ,
		WINDOWSREMOTE ,
		WINDOWSFROMUNIX
	};

	public enum VarDISTITEMTYPE {
		UNKNOWN ,
		BINARY ,
		DOTNETPKG ,
		WAR ,
		ARCHIVE_DIRECT ,	// deploydir = archive/content
		ARCHIVE_CHILD ,		// deploydir/archivename = archive/archivename/fullcontent
		ARCHIVE_SUBDIR		// deploydir/archivename = archive/content/fullcontent
	};
	
	public enum VarDISTITEMSOURCE {
		UNKNOWN ,
		MANUAL ,
		BUILD ,
		DISTITEM
	};
	
	public enum VarPROCESSMODE {
		UNKNOWN ,
		STARTED ,
		STARTING ,
		ERRORS ,
		STOPPED
	};

	public enum VarCONFITEMTYPE {
		UNKNOWN ,
		FILES ,
		DIR
	};
	
	public enum VarCONTENTTYPE {
		BINARYCOLDDEPLOY ,
		BINARYHOTDEPLOY ,
		BINARYCOPYONLY ,
		CONFCOLDDEPLOY ,
		CONFHOTDEPLOY ,
		CONFCOPYONLY
	};

	public enum VarNAMETYPE {
		ANY ,
		ALPHANUM ,
		ALPHANUMDOT ,
		ALPHANUMDOTDASH
	};
	
	public enum VarARCHIVETYPE {
		TARGZ ,
		TAR ,
		ZIP
	};
	
	private static String configurableExtensionsFindOptions = createConfigurableExtensions();
	
	public Meta( ServerProductMeta storage , SessionContext session ) {
		this.storage = storage;
		this.loader = storage.loader;
		this.session = session;
		name = storage.name;
	}
	
	public Meta( ServerLoader loader , SessionContext session , String productName ) {
		this.loader = loader;
		this.session = session;
		name = productName;
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
	
	public void clearAll() {
		this.product = null;
		this.distr = null;
		this.database = null;
		this.sources = null;
	}

	public SessionContext getSession() {
		return( session );
	}
	
	public synchronized ServerProductMeta getStorage( ActionBase action ) throws Exception {
		if( storage == null )
			storage = loader.getMetaStorage( action.actionInit , name );
		return( storage );
	}

	public synchronized MetaProductVersion getVersion( ActionBase action ) throws Exception {
		if( version == null ) {
			getStorage( action );
			version = loader.loadVersion( action.actionInit , storage );
		}
		return( version );
	}
	
	public synchronized MetaProductSettings getProduct( ActionBase action ) throws Exception {
		if( product == null ) {
			getStorage( action );
			product = loader.loadProduct( action.actionInit , storage );
		}
		return( product );
	}
	
	public synchronized MetaDatabase getDatabase( ActionBase action ) throws Exception {
		if( database == null ) {
			getStorage( action );
			database = loader.loadDatabase( action.actionInit , storage );
		}
		return( database );
	}

	public synchronized MetaDistr getDistr( ActionBase action ) throws Exception {
		if( distr == null ) {
			getStorage( action );
			distr = loader.loadDistr( action.actionInit , storage );
		}
		return( distr );
	}

	public synchronized MetaSource getSources( ActionBase action ) throws Exception {
		if( sources == null ) {
			getStorage( action );
			sources = loader.loadSources( action.actionInit , storage );
		}
		return( sources );
	}

	public synchronized MetaMonitoring getMonitoring( ActionBase action ) throws Exception {
		getStorage( action );
		return( loader.loadMonitoring( action.actionInit , storage ) );
	}
	
	public synchronized MetaDesign getDesignData( ActionBase action , String fileName ) throws Exception {
		getStorage( action );
		return( loader.loadDesignData( action.actionInit , storage , fileName ) );
	}
	
	public synchronized MetaEnv getEnvData( ActionBase action , String envFile , boolean loadProps ) throws Exception {
		getStorage( action );
		return( loader.loadEnvData( action.actionInit , storage , envFile , loadProps ) );
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

	public static VarOSTYPE getOSType( String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingOSType0 , "missing operating system type" );
		
		VarOSTYPE value = null;
		try {
			value = VarOSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidOSType1 , "invalid OS type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarCATEGORY getCategory( String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingCategory0 , "missing category" );

		VarCATEGORY value = null;
		try {
			value = VarCATEGORY.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidCategory1 , "invalid category=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarSERVERTYPE getServerType( String ID ) throws Exception {
		if( ID.isEmpty() )
			return( VarSERVERTYPE.UNKNOWN );
		
		VarSERVERTYPE value = null;
		try {
			value = VarSERVERTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidServerType1 , "invalid server type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarNODETYPE getNodeType( String ID , VarNODETYPE defValue ) throws Exception {
		if( ID.isEmpty() )
			return( defValue );
		
		VarNODETYPE value = null;
		try {
			value = VarNODETYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidNodeType1 , "invalid node type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDEPLOYTYPE getDeployType( String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingDeployType0 , "missing deploy type" );
		
		VarDEPLOYTYPE value = null;
		try {
			value = VarDEPLOYTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDeployType1 , "invalid deploytype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarITEMSRCTYPE getItemSrcType( String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingSourceItemType0 , "missing source item type" );
		
		VarITEMSRCTYPE value = null;
		try {
			value = VarITEMSRCTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidSourceItemType1 , "invalid source item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDISTITEMTYPE getItemDistType( String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingDistributiveItemType0 , "missing distributive item type" );
		
		VarDISTITEMTYPE value = null;
		try {
			value = VarDISTITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistributiveItemType1 , "invalid distributive item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDISTITEMSOURCE getItemDistSource( String ID ) throws Exception {
		VarDISTITEMSOURCE value = null;
		try {
			value = VarDISTITEMSOURCE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistributiveItemSource1 , "invalid distributive item source=" + ID , ID );
		}
		
		if( value == null )
			Common.exit0( _Error.MissingDistributiveItemSource0 , "missing distributive item source" );
		
		return( value );
	}
	
	public static VarBUILDMODE getBuildMode( String ID ) throws Exception {
		if( ID.isEmpty() )
			return( VarBUILDMODE.UNKNOWN );
		
		VarBUILDMODE value = null;
		try {
			value = VarBUILDMODE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBuildMode1 , "invalid build mode=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDBMSTYPE getDbmsType( String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingDbmsType0 , "missing DBMS type" );
		
		VarDBMSTYPE value = null;		
		try {
			value = VarDBMSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDbmsType1 , "invalid dbmstype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarCONFITEMTYPE getConfItemType( String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingConfItemType0 , "missing configuration item type" );
		
		VarCONFITEMTYPE value = null;		
		try {
			value = VarCONFITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidConfItemType1 , "invalid confitemtype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarBASESRCTYPE getBaseSrcType( ActionBase action , String TYPE ) throws Exception {
		if( TYPE.isEmpty() )
			Common.exit0( _Error.MissingBaseSrcType0 , "missing base srctype" );
		
		VarBASESRCTYPE value = null;		
		try {
			value = VarBASESRCTYPE.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit1( _Error.InvalidBaseSrcType1 , "invalid base srctype=" + TYPE , TYPE );
		}
		
		return( value );
	}

	public static VarBASESRCFORMAT getBaseSrcFormat( ActionBase action , String TYPE ) throws Exception {
		if( TYPE.isEmpty() )
			Common.exit0( _Error.MissingBaseSrcType0 , "missing base srcformat" );
		
		VarBASESRCFORMAT value = null;		
		try {
			value = VarBASESRCFORMAT.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit1( _Error.InvalidBaseSrcFormat1 , "invalid base srcformat=" + TYPE , TYPE );
		}
		
		return( value );
	}

	public static VarITEMVERSION readItemVersionAttr( Node node , String attrName ) throws Exception {
		String ID = ConfReader.getAttrValue( node , attrName , "default" );
		if( ID.equals( "default" ) )
			return( VarITEMVERSION.PREFIX );
		
		VarITEMVERSION value = null;
		try {
			value = VarITEMVERSION.valueOf( ID.toUpperCase() );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidItemVersionType1 , "invalid item version type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarELEMENTTYPE getDesignElementType( ActionBase action , String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingDesignElementType0 , "missing design element type" );
		
		VarELEMENTTYPE value = null;		
		try {
			value = VarELEMENTTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit1( _Error.InvalidDesignElementType1 , "invalid design element type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarLINKTYPE getDesignLinkType( ActionBase action , String ID ) throws Exception {
		if( ID.isEmpty() )
			Common.exit0( _Error.MissingDesignLinkType0 , "missing design link type" );
		
		VarLINKTYPE value = null;		
		try {
			value = VarLINKTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit1( _Error.InvalidDesignLinkType1 , "invalid design link type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarCATEGORY readCategoryAttr( Node node ) throws Exception {
		String value = ConfReader.getRequiredAttrValue( node , "category" );
		return( getCategory( value ) );
	}
	
	public static boolean isSourceCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.BUILD || value == VarCATEGORY.PREBUILT )
			return( true );
		return( false );
	}
	
	public static boolean isBuildableCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.BUILD )
			return( true );
		return( false );
	}
	
	public static VarCATEGORY[] getAllCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public static VarCATEGORY[] getAllReleaseCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public static VarCATEGORY[] getAllSourceCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.PREBUILT };
		return( categories );
	}

	public static VarCATEGORY[] getAllBuildableCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD };
		return( categories );
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
		if( distItem.DEPLOYVERSION == VarITEMVERSION.IGNORE ) {
			String[] values = new String[1];
			values[0] = getVersionPattern( action , distItem.DEPLOYVERSION , basename , ext );
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

}
