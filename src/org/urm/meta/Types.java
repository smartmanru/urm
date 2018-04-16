package org.urm.meta;

import org.urm.common.Common;

public class Types {

	public enum EnumModifyType {
		ORIGINAL ,
		NORMAL ,
		MATCH ,
		SET
	};
	
	public enum EnumResourceCategory {
		ANY ,
		VCS ,
		SSH ,
		CREDENTIALS ,
		NEXUS ,
		SOURCE;
	};
	
	public enum EnumAuthType {
		UNKNOWN ,
		PASSWORD ,
		CREDENTIALS ,
		KEYS ,
		ANONYMOUS , 
		CURRENTUSER
	};
	
	public enum EnumElementType {
		UNKNOWN ,
		EXTERNAL ,
		GENERIC ,
		SERVER ,
		DATABASE ,
		LIBRARY ,
		GROUP
	};
	
	public enum EnumLinkType {
		UNKNOWN ,
		GENERIC ,
		MSG
	};
	
	public enum EnumSessionType {
		UNKNOWN ,
		UNIXLOCAL ,
		UNIXREMOTE ,
		UNIXFROMWINDOWS ,
		WINDOWSLOCAL ,
		WINDOWSREMOTE ,
		WINDOWSFROMUNIX
	};

	public enum EnumProcessMode {
		UNKNOWN ,
		STARTED ,
		STARTING ,
		ERRORS ,
		STOPPED ,
		UNREACHABLE
	};

	public enum EnumContentType {
		UNKNOWN ,
		BINARYCOLDDEPLOY ,
		BINARYHOTDEPLOY ,
		BINARYCOPYONLY ,
		CONFCOLDDEPLOY ,
		CONFHOTDEPLOY ,
		CONFCOPYONLY
	};

	public enum EnumNameType {
		UNKNOWN ,
		ANY ,
		ALPHANUM ,
		ALPHANUMDOT ,
		ALPHANUMDOTDASH
	};
	
	public enum EnumArchiveType {
		UNKNOWN ,
		TARGZ ,
		TAR ,
		ZIP
	};
	
	public enum EnumPackageExtension {
		UNKNOWN ,
		NUPKG ,
		RPM ,
		DEB
	}
	
	public enum EnumDistItemType {
		UNKNOWN ,
		BINARIES ,
		CONFIGURATION ,
		DATABASE ,
		DOCUMENTATION
	}
	
	public static EnumElementType getDesignElementType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignElementType0 , "missing design element type" );
			return( EnumElementType.UNKNOWN );
		}
		
		EnumElementType value = null;		
		try {
			value = EnumElementType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignElementType1 , "invalid design element type=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumLinkType getDesignLinkType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDesignLinkType0 , "missing design link type" );
			return( EnumLinkType.UNKNOWN );
		}
		
		EnumLinkType value = null;		
		try {
			value = EnumLinkType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDesignLinkType1 , "invalid design link type=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumPackageExtension getPackageExtension( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingPackageExtension0 , "missing package extension" );
			return( EnumPackageExtension.UNKNOWN );
		}
		
		EnumPackageExtension value = null;
		try {
			value = EnumPackageExtension.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidPackageExtension1 , "invalid package extension=" + ID , ID );
		}
		
		return( value );
	}

	public static EnumDistItemType getDistItemType( String ID , boolean required ) throws Exception {
		if( ID.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingDistItemType0 , "missing dist item type" );
			return( EnumDistItemType.UNKNOWN );
		}
		
		EnumDistItemType value = null;
		try {
			value = EnumDistItemType.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidDistItemType1 , "invalid dist item type=" + ID , ID );
		}
		
		return( value );
	}

	public static boolean isBinaryContent( EnumContentType c ) throws Exception {
		if( c == EnumContentType.BINARYCOLDDEPLOY || c == EnumContentType.BINARYCOPYONLY || c == EnumContentType.BINARYHOTDEPLOY )
			return( true );
		return( false );
	}
	
	public static boolean isConfContent( EnumContentType c ) throws Exception {
		if( c == EnumContentType.CONFCOLDDEPLOY || c == EnumContentType.CONFCOPYONLY || c == EnumContentType.CONFHOTDEPLOY )
			return( true );
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
