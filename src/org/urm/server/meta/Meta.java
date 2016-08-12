package org.urm.server.meta;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.ExitException;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.ServerLoader;
import org.urm.server.ServerMetaSet;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class Meta {
	
	public ServerLoader loader;
	public SessionContext session;
	public ServerMetaSet storage;
	
	public MetaProduct product;
	public MetaDatabase database;
	public MetaDistr distr;
	public MetaSource sources;
	
	private String configurableExtensionsFindOptions = "";
	
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
	
	public Meta( ServerMetaSet storage , SessionContext session ) {
		this.storage = storage;
		this.loader = storage.loader;
		this.session = session;
	}
	
	public Meta( ServerLoader loader , SessionContext session ) {
		this.loader = loader;
		this.session = session;
		
		configurableExtensionsFindOptions = "";
		for( int k = 0; k < configurableExtensions.length; k++ ) {
			if( k > 0 )
				configurableExtensionsFindOptions += " -o ";
			configurableExtensionsFindOptions += "-name \"*." + configurableExtensions[ k ] + "\"";
		}
	}
	
	public void setProduct( MetaProduct product ) {
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
	
	public String getConfigurableExtensionsFindOptions( ActionBase action ) throws Exception {
		return( configurableExtensionsFindOptions );
	}

	private synchronized void getStorage( ActionBase action ) throws Exception {
		if( storage == null )
			storage = loader.getMetaStorage( action.actionInit );
	}

	public void loadProduct( ActionBase action ) throws Exception {
		getStorage( action );
		product = loader.loadProduct( action.actionInit , storage );
	}
	
	public void loadDistr( ActionBase action ) throws Exception {
		getStorage( action );
		distr = loader.loadDistr( action.actionInit , storage );
		database = loader.loadDatabase( action.actionInit , storage );
	}

	public void loadSources( ActionBase action ) throws Exception {
		getStorage( action );
		sources = loader.loadSources( action.actionInit , storage );
	}

	public MetaMonitoring loadMonitoring( ActionBase action ) throws Exception {
		getStorage( action );
		return( loader.loadMonitoring( action.actionInit , storage ) );
	}
	
	public MetaDesign loadDesignData( ActionBase action , String fileName ) throws Exception {
		getStorage( action );
		return( loader.loadDesignData( action.actionInit , storage , fileName ) );
	}
	
	public MetaEnv loadEnvData( ActionBase action , String envFile , boolean loadProps ) throws Exception {
		getStorage( action );
		return( loader.loadEnvData( action.actionInit , storage , envFile , loadProps ) );
	}
	
	public boolean isConfigurableFile( ActionBase action , String filePath ) throws Exception {
		String ext = Common.getFileExtension( filePath );
		for( int k = 0; k < configurableExtensions.length; k++ )
			if( ext.equals( configurableExtensions[ k ] ) )
				return( true );
		return( false );
	}

	public String[] getConfigurableExtensions( ActionBase action ) throws Exception {
		return( configurableExtensions );
	}

	public VarOSTYPE getOSType( String ID ) throws Exception {
		VarOSTYPE value = null;
		try {
			value = VarOSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid operation system type=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "missing operating system type" );
		
		return( value );
	}
	
	public VarCATEGORY getCategory( String ID ) throws Exception {
		VarCATEGORY value = null;
		try {
			value = VarCATEGORY.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid category=" + ID );
		}
		if( value == null )
			throw new ExitException( "missing category" );
		
		return( value );
	}
	
	public VarSERVERTYPE getServerType( String ID ) throws Exception {
		if( ID == null || ID.isEmpty() )
			return( VarSERVERTYPE.UNKNOWN );
		
		VarSERVERTYPE value = null;
		try {
			value = VarSERVERTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid server type=" + ID );
		}
		
		return( value );
	}
	
	public VarNODETYPE getNodeType( String ID , VarNODETYPE defValue ) throws Exception {
		if( ID == null || ID.isEmpty() )
			return( defValue );
		
		VarNODETYPE value = null;
		try {
			value = VarNODETYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid node type=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "missing server type" );
		
		return( value );
	}
	
	public VarDEPLOYTYPE getDeployType( String ID ) throws Exception {
		VarDEPLOYTYPE value = null;
		try {
			value = VarDEPLOYTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid deploytype=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "unknown deploy type=" + ID );
		
		return( value );
	}
	
	public VarITEMSRCTYPE getItemSrcType( String ID ) throws Exception {
		VarITEMSRCTYPE value = null;
		try {
			value = VarITEMSRCTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid source item type=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "unknown source item type=" + ID );
		
		return( value );
	}
	
	public VarDISTITEMTYPE getItemDistType( String ID ) throws Exception {
		VarDISTITEMTYPE value = null;
		try {
			value = VarDISTITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid distributive item type=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "unknown distributive item type=" + ID );
		
		return( value );
	}
	
	public VarDISTITEMSOURCE getItemDistSource( String ID ) throws Exception {
		VarDISTITEMSOURCE value = null;
		try {
			value = VarDISTITEMSOURCE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid distributive item source=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "unknown distributive item source=" + ID );
		
		return( value );
	}
	
	public VarBUILDMODE getBuildMode( String ID ) throws Exception {
		if( ID == null || ID.isEmpty() )
			return( VarBUILDMODE.UNKNOWN );
		
		VarBUILDMODE value = null;
		try {
			value = VarBUILDMODE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid build mode=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "unknown build mode=" + ID );
		
		return( value );
	}
	
	public VarDBMSTYPE getDbmsType( String ID ) throws Exception {
		VarDBMSTYPE value = null;		
		
		try {
			value = VarDBMSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid dbmstype=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "unknown DBMS type=" + ID );
		
		return( value );
	}
	
	public VarCONFITEMTYPE getConfItemType( String ID ) throws Exception {
		VarCONFITEMTYPE value = null;		
		
		try {
			value = VarCONFITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			throw new ExitException( "invalid confitemtype=" + ID );
		}
		
		if( value == null )
			throw new ExitException( "unknown configuration item type=" + ID );
		
		return( value );
	}
	
	public VarCATEGORY readCategoryAttr( Node node ) throws Exception {
		String value = ConfReader.getRequiredAttrValue( node , "category" );
		return( getCategory( value ) );
	}
	
	public VarITEMVERSION readItemVersionAttr( Node node , String attrName ) throws Exception {
		String ID = ConfReader.getAttrValue( node , attrName , "default" );
		if( ID.equals( "default" ) )
			return( VarITEMVERSION.PREFIX );
		
		VarITEMVERSION value = VarITEMVERSION.valueOf( ID.toUpperCase() );
		if( value == null )
			throw new ExitException( "unknown version type=" + ID );
		
		return( value );
	}
	
	public boolean isSourceCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.BUILD || value == VarCATEGORY.PREBUILT )
			return( true );
		return( false );
	}
	
	public boolean isBuildableCategory( VarCATEGORY value ) {
		if( value == VarCATEGORY.BUILD )
			return( true );
		return( false );
	}
	
	public VarCATEGORY[] getAllCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public VarCATEGORY[] getAllReleaseCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.MANUAL , VarCATEGORY.PREBUILT , VarCATEGORY.CONFIG , VarCATEGORY.DB };
		return( categories );
	}

	public VarCATEGORY[] getAllSourceCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD , VarCATEGORY.PREBUILT };
		return( categories );
	}

	public VarCATEGORY[] getAllBuildableCategories() {
		VarCATEGORY[] categories = { VarCATEGORY.BUILD };
		return( categories );
	}
	
	public String getVersionPattern( ActionBase action , VarITEMVERSION version , String basename , String ext ) throws Exception {
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
			action.exit( "unknown version type=" + Common.getEnumLower( version ) );
		
		return( value );
	}
	
	public String[] getVersionPatterns( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
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
	
	public void updateProduct( ActionBase action ) throws Exception {
		product.updateProperties( action );
	}

	public boolean isBinaryContent( ActionBase action , VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.BINARYCOLDDEPLOY || c == VarCONTENTTYPE.BINARYCOPYONLY || c == VarCONTENTTYPE.BINARYHOTDEPLOY )
			return( true );
		return( false );
	}
	
	public boolean isConfContent( ActionBase action , VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.CONFCOLDDEPLOY || c == VarCONTENTTYPE.CONFCOPYONLY || c == VarCONTENTTYPE.CONFHOTDEPLOY )
			return( true );
		return( false );
	}
	
    public String getNameAttr( ActionBase action , Node node , VarNAMETYPE nameType ) throws Exception {
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
    		throw new ExitException( "unexpected state" );
    		
    	if( !name.matches( mask ) )
    		action.exit( "name attribute should contain only alphanumeric or dot characters, value=" + name );
    	return( name );	
    }
    
}
