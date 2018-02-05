package org.urm.meta;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class Types {

	public enum EnumResourceCategory {
		ANY ,
		VCS ,
		SSH ,
		CREDENTIALS ,
		NEXUS ,
		SOURCE;
	};
	
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
	
	public enum VarCATEGORY {
		UNKNOWN ,
		PROJECT ,
		CONFIG ,
		MANUAL ,
		DERIVED ,
		DB ,
		ENV ,
		BUILDABLE ,
		PREBUILT
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

	public enum VarPROCESSMODE {
		UNKNOWN ,
		STARTED ,
		STARTING ,
		ERRORS ,
		STOPPED ,
		UNREACHABLE
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
	
	public enum VarTICKETSETSTATUS {
		UNKNOWN ,
		NEW ,
		ACTIVE ,
		DESCOPED
	};

	public enum VarTICKETTYPE {
		UNKNOWN ,
		FEATURE ,
		CHANGE ,
		DOCUMENT ,
		BUGFIX
	};

	public enum VarTICKETSTATUS {
		UNKNOWN ,
		NEW ,
		DEVDONE ,
		QADONE
	};

	public enum VarTICKETSETTARGETTYPE {
		UNKNOWN ,
		PROJECTSET ,
		PROJECTALLITEMS ,
		PROJECTNOITEMS ,
		DISTITEM ,
		CONFITEM ,
		SCHEMA ,
		DELIVERYBINARIES ,
		DELIVERYCONFS ,
		DELIVERYDATABASE
	};

	public enum VarPACKAGEEXTENSION {
		UNKNOWN ,
		NUPKG ,
		RPM ,
		DEB
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

	public static VarTICKETSETSTATUS getTicketSetStatus( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketSetStatus0 , "missing ticket set status" );
			return( VarTICKETSETSTATUS.UNKNOWN );
		}
		
		VarTICKETSETSTATUS value = null;
		try {
			value = VarTICKETSETSTATUS.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketSetStatus1 , "invalid ticket set status=" + ID , ID );
		}
		
		return( value );
	}

	public static VarTICKETTYPE getTicketType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketType0 , "missing ticket type" );
			return( VarTICKETTYPE.UNKNOWN );
		}
		
		VarTICKETTYPE value = null;
		try {
			value = VarTICKETTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketType1 , "invalid ticket type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarTICKETSTATUS getTicketStatus( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketStatus0 , "missing ticket status" );
			return( VarTICKETSTATUS.UNKNOWN );
		}
		
		VarTICKETSTATUS value = null;
		try {
			value = VarTICKETSTATUS.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketStatus1 , "invalid ticket status=" + ID , ID );
		}
		
		return( value );
	}

	public static VarTICKETSETTARGETTYPE getTicketSetTargetType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingTicketSetTargetType0 , "missing ticket set target type" );
			return( VarTICKETSETTARGETTYPE.UNKNOWN );
		}
		
		VarTICKETSETTARGETTYPE value = null;
		try {
			value = VarTICKETSETTARGETTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidTicketSetTargetType1 , "invalid ticket set target type=" + ID , ID );
		}
		
		return( value );
	}

	public static VarPACKAGEEXTENSION getPackageExtension( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingPackageExtension0 , "missing package extension" );
			return( VarPACKAGEEXTENSION.UNKNOWN );
		}
		
		VarPACKAGEEXTENSION value = null;
		try {
			value = VarPACKAGEEXTENSION.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidPackageExtension1 , "invalid package extension=" + ID , ID );
		}
		
		return( value );
	}

	public static boolean isBinaryContent( VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.BINARYCOLDDEPLOY || c == VarCONTENTTYPE.BINARYCOPYONLY || c == VarCONTENTTYPE.BINARYHOTDEPLOY )
			return( true );
		return( false );
	}
	
	public static boolean isConfContent( VarCONTENTTYPE c ) throws Exception {
		if( c == VarCONTENTTYPE.CONFCOLDDEPLOY || c == VarCONTENTTYPE.CONFCOPYONLY || c == VarCONTENTTYPE.CONFHOTDEPLOY )
			return( true );
		return( false );
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
		VarCATEGORY[] categories = { VarCATEGORY.PROJECT , VarCATEGORY.CONFIG , VarCATEGORY.DB , VarCATEGORY.MANUAL , VarCATEGORY.DERIVED };
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
	
	public static boolean isPackageExtension( String ext ) {
		try {
			if( !ext.startsWith( "." ) )
				return( false );
			
			getPackageExtension( ext.substring( 1 ) , true );
		}
		catch( Throwable e ) {
			return( false );
		}
		return( true );
	}
	
	
}
