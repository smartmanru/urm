package org.urm.meta;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.w3c.dom.Node;

public class Types {

	public enum VarELEMENTTYPE {
		UNKNOWN ,
		EXTERNAL ,
		GENERIC ,
		SERVER ,
		DATABASE ,
		LIBRARY ,
		GROUP
	};
	
	public enum VarLINKTYPE {
		UNKNOWN ,
		GENERIC ,
		MSG
	};
	
	public enum VarBUILDERTYPE {
		UNKNOWN ,
		ANT ,
		MAVEN ,
		GRADLE ,
		MSBUILD
	};
	
	public enum VarBUILDERTARGET {
		UNKNOWN ,
		LOCALPATH ,
		NEXUS
	};
	
	public enum VarCATEGORY {
		UNKNOWN ,
		PROJECT ,
		CONFIG ,
		MANUAL ,
		DB ,
		ENV ,
		BUILDABLE ,
		PREBUILT
	};

	public enum VarENVTYPE {
		UNKNOWN ,
		PRODUCTION ,
		UAT ,
		DEVELOPMENT
	};
	
	public enum VarDEPLOYITEMTYPE {
		UNKNOWN ,
		BINARY ,
		CONF ,
		SCHEMA ,
		COMP
	};
	
	public enum VarCOMPITEMTYPE {
		UNKNOWN ,
		BINARY ,
		CONF ,
		SCHEMA ,
		WSDL
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
	
	public enum VarSERVERACCESSTYPE {
		UNKNOWN ,
		SERVICE ,
		PACEMAKER ,
		DOCKER ,
		GENERIC ,
		MANUAL
	};

	public enum VarSERVERRUNTYPE {
		UNKNOWN ,
		DATABASE ,
		APP ,
		WEBUI ,
		WEBAPP ,
		COMMAND
	};

	public enum VarNODETYPE {
		UNKNOWN ,
		SELF ,
		ADMIN ,
		SLAVE
	};
	
	public enum VarDEPLOYMODE {
		UNKNOWN ,
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
		STATICWAR ,
		ARCHIVE_DIRECT ,	// deploydir = archive/content
		ARCHIVE_CHILD ,		// deploydir/archivename = archive/archivename/fullcontent
		ARCHIVE_SUBDIR		// deploydir/archivename = archive/content/fullcontent
	};
	
	public enum VarDISTITEMORIGIN {
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
		UNKNOWN ,
		BINARYCOLDDEPLOY ,
		BINARYHOTDEPLOY ,
		BINARYCOPYONLY ,
		CONFCOLDDEPLOY ,
		CONFHOTDEPLOY ,
		CONFCOPYONLY
	};

	public enum VarNAMETYPE {
		UNKNOWN ,
		ANY ,
		ALPHANUM ,
		ALPHANUMDOT ,
		ALPHANUMDOTDASH
	};
	
	public enum VarARCHIVETYPE {
		UNKNOWN ,
		TARGZ ,
		TAR ,
		ZIP
	};
	
	public enum VarBASESRCTYPE {
		UNKNOWN ,
		ARCHIVE_LINK ,
		ARCHIVE_DIRECT ,
		NODIST ,
		INSTALLER
	};
	
	public enum VarBASESRCFORMAT {
		UNKNOWN ,
		TARGZ_SINGLEDIR ,
		ZIP_SINGLEDIR ,
		SINGLEFILE
	};
	
	public static VarOSTYPE getOSType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingOSType0 , "missing operating system type" );
			return( VarOSTYPE.UNKNOWN );
		}
		
		VarOSTYPE value = null;
		try {
			value = VarOSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidOSType1 , "invalid OS type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarCATEGORY getCategory( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingCategory0 , "missing category" );
			return( VarCATEGORY.UNKNOWN );
		}

		VarCATEGORY value = null;
		try {
			value = VarCATEGORY.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidCategory1 , "invalid category=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarSERVERACCESSTYPE getServerAccessType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingServerAccessType0 , "missing server access type" );
			return( VarSERVERACCESSTYPE.UNKNOWN );
		}
		
		VarSERVERACCESSTYPE value = null;
		try {
			value = VarSERVERACCESSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidServerAccessType1 , "invalid server access type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarSERVERRUNTYPE getServerRunType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingServerRunType0 , "missing server run type" );
			return( VarSERVERRUNTYPE.UNKNOWN );
		}
		
		VarSERVERRUNTYPE value = null;
		try {
			value = VarSERVERRUNTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidServerRunType1 , "invalid server run type=" + ID , ID );
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
	
	public static VarDEPLOYMODE getDeployMode( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDeployType0 , "missing deploy type" );
			return( VarDEPLOYMODE.UNKNOWN );
		}
		
		VarDEPLOYMODE value = null;
		try {
			value = VarDEPLOYMODE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDeployType1 , "invalid deploytype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarITEMSRCTYPE getItemSrcType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingSourceItemType0 , "missing source item type" );
			return( VarITEMSRCTYPE.UNKNOWN );
		}
		
		VarITEMSRCTYPE value = null;
		try {
			value = VarITEMSRCTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidSourceItemType1 , "invalid source item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDISTITEMTYPE getItemDistType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDistributiveItemType0 , "missing distributive item type" );
			return( VarDISTITEMTYPE.UNKNOWN );
		}
		
		VarDISTITEMTYPE value = null;
		try {
			value = VarDISTITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistributiveItemType1 , "invalid distributive item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDISTITEMORIGIN getItemDistOrigin( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDistItemSource0 , "missing distributive item source" );
			return( VarDISTITEMORIGIN.UNKNOWN );
		}
		
		VarDISTITEMORIGIN value = null;
		try {
			value = VarDISTITEMORIGIN.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistributiveItemSource1 , "invalid distributive item source=" + ID , ID );
		}
		
		if( value == null )
			Common.exit0( _Error.MissingDistributiveItemSource0 , "missing distributive item source" );
		
		return( value );
	}
	
	public static VarENVTYPE getEnvType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingEnvType0 , "missing deploy item type" );
			return( VarENVTYPE.UNKNOWN );
		}
		
		VarENVTYPE value = null;
		try {
			value = VarENVTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidEnvType1 , "invalid environment type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDEPLOYITEMTYPE getDeployItemType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDeployItemType0 , "missing deploy item type" );
			return( VarDEPLOYITEMTYPE.UNKNOWN );
		}
		
		VarDEPLOYITEMTYPE value = null;
		try {
			value = VarDEPLOYITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDeployItemType1 , "invalid deploy item type=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarBUILDMODE getBuildMode( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBuildMode0 , "missing build mode" );
			return( VarBUILDMODE.UNKNOWN );
		}
		
		VarBUILDMODE value = null;
		try {
			value = VarBUILDMODE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBuildMode1 , "invalid build mode=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarDBMSTYPE getDbmsType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDbmsType0 , "missing DBMS type" );
			return( VarDBMSTYPE.UNKNOWN );
		}
		
		VarDBMSTYPE value = null;		
		try {
			value = VarDBMSTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDbmsType1 , "invalid dbmstype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarCONFITEMTYPE getConfItemType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingConfItemType0 , "missing configuration item type" );
			return( VarCONFITEMTYPE.UNKNOWN );
		}
		
		VarCONFITEMTYPE value = null;		
		try {
			value = VarCONFITEMTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidConfItemType1 , "invalid confitemtype=" + ID , ID );
		}
		
		return( value );
	}
	
	public static VarBASESRCTYPE getBaseSrcType( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBaseSrcType0 , "missing base srctype" );
			return( VarBASESRCTYPE.UNKNOWN );
		}
		
		VarBASESRCTYPE value = null;		
		try {
			value = VarBASESRCTYPE.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBaseSrcType1 , "invalid base srctype=" + TYPE , TYPE );
		}
		
		return( value );
	}

	public static VarBASESRCFORMAT getBaseSrcFormat( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBaseSrcType0 , "missing base srcformat" );
			return( VarBASESRCFORMAT.UNKNOWN );
		}
		
		VarBASESRCFORMAT value = null;		
		try {
			value = VarBASESRCFORMAT.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBaseSrcFormat1 , "invalid base srcformat=" + TYPE , TYPE );
		}
		
		return( value );
	}

	public static VarITEMVERSION readItemVersionAttr( Node node , String attrName ) throws Exception {
		String ID = ConfReader.getAttrValue( node , attrName , "default" );
		return( getItemVersionType( ID ) );
	}

	public static VarITEMVERSION getItemVersionType( String ID ) throws Exception {
		if( ID.isEmpty() || ID.equals( "default" ) )
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
	
	public static VarELEMENTTYPE getDesignElementType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignElementType0 , "missing design element type" );
			return( VarELEMENTTYPE.UNKNOWN );
		}
		
		VarELEMENTTYPE value = null;		
		try {
			value = VarELEMENTTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignElementType1 , "invalid design element type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarLINKTYPE getDesignLinkType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignLinkType0 , "missing design link type" );
			return( VarLINKTYPE.UNKNOWN );
		}
		
		VarLINKTYPE value = null;		
		try {
			value = VarLINKTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignLinkType1 , "invalid design link type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarBUILDERTYPE getBuilderType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBuilderType0 , "missing builder type" );
			return( VarBUILDERTYPE.UNKNOWN );
		}
		
		VarBUILDERTYPE value = null;		
		try {
			value = VarBUILDERTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBuilderType1 , "invalid builder type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarBUILDERTARGET getBuilderTarget( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingBuilderTarget0 , "missing builder target" );
			return( VarBUILDERTARGET.UNKNOWN );
		}
		
		VarBUILDERTARGET value = null;		
		try {
			value = VarBUILDERTARGET.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidBuilderTarget1 , "invalid builder target=" + ID , ID );
		}
		
		return( value );
	}

}