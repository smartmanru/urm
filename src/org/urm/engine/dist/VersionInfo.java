package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;

public class VersionInfo {

	DBEnumLifecycleType type;
	public int v1; 
	public int v2; 
	public int v3; 
	public int v4;
	public String variant;
	
	public VersionInfo() {
		v1 = 1;
		v2 = 0;
		v3 = 0;
		v4 = 0;
		variant = "";
	}
	
	public VersionInfo copy() {
		VersionInfo r = new VersionInfo();
		r.type = type;
		r.v1 = v1;
		r.v2 = v2;
		r.v3 = v3;
		r.v4 = v4;
		r.variant = variant;
		return( r );
	}

	public static VersionInfo getDistVersion( Dist dist ) throws Exception {
		VersionInfo vi = new VersionInfo();
		vi.setDistData( dist );
		return( vi );
	}
	
	public static VersionInfo getFileVersion( String version ) throws Exception {
		VersionInfo vi = new VersionInfo();
		vi.setVersion( version );
		return( vi );
	}
	
	public static VersionInfo getReleaseDirInfo( String releaseDir ) throws Exception {
		VersionInfo vi = new VersionInfo();
		vi.setReleaseDir( releaseDir );
		return( vi );
	}

	public static String getReleaseDirVersion( String RELEASEDIR ) {
		return( Common.getPartBeforeLast( RELEASEDIR , "-" ) );
	}

	public static String getReleaseVariant( String RELEASEDIR ) {
		return( Common.getPartAfterLast( RELEASEDIR , "-" ) );
	}

	public static DBEnumLifecycleType getLifecycleTypeByShortVersion( String RELEASEVER ) {
		String[] parts = Common.splitDotted( RELEASEVER );
		if( parts.length == 2 )
			return( DBEnumLifecycleType.MAJOR );
		if( parts.length == 3 )
			return( DBEnumLifecycleType.MINOR );
		if( parts.length == 4 )
			return( DBEnumLifecycleType.URGENT );
		return( DBEnumLifecycleType.UNKNOWN );
	}
	
	public void setVersion( String version ) throws Exception {
		int[] vn = new int[4];
		parseVersion( version , vn );
		setVersion( vn );
	}

	public void setVersion( int[] vn ) throws Exception {
		v1 = vn[0]; 
		v2 = vn[1]; 
		v3 = vn[2]; 
		v4 = vn[3]; 
		variant = "";
		
		type = DBEnumLifecycleType.UNKNOWN;
		if( v4 != 0 )
			type = DBEnumLifecycleType.URGENT;
		else
		if( v3 != 0 )
			type = DBEnumLifecycleType.MINOR;
		else
			type = DBEnumLifecycleType.MAJOR;
	}
	
	public void setReleaseDir( String releaseDir ) throws Exception {
		String version = Common.getPartBeforeLast( releaseDir , "-" );
		setVersion( version );
		variant = Common.getPartAfterLast( releaseDir , "-" );
	}
	
	public void setDistData( Dist dist ) throws Exception {
		int[] vn = new int[4];
		parseVersion( dist.release.RELEASEVER , vn );
		
		if( !dist.isMaster() )
			checkDistVersion( vn , Common.getPartBeforeLast( dist.RELEASEDIR , "-" ) );

		setVersion( vn );
		variant = Common.getPartAfterLast( dist.RELEASEDIR , "-" );
	}
	
	private void parseVersion( String version , int[] vn ) throws Exception {
		if( !version.matches( "[0-9.]*" ) )
			Common.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		
		String items[] = Common.splitDotted( version );
		if( items.length == 0 || items.length > 4 )
			Common.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		
		for( String item : items ) {
			if( item.isEmpty() )
				Common.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		}

		vn[0] = parseNumber( items[0] );
		vn[1] = ( items.length > 1 )? parseNumber( items[1] ) : 0;
		vn[2] = ( items.length > 2 )? parseNumber( items[2] ) : 0;
		vn[3] = ( items.length > 3 )? parseNumber( items[3] ) : 0;
	}
	
	private void checkDistVersion( int[] cvn , String version ) throws Exception {
		int[] vn = new int[4];
		parseVersion( version , vn );
		for( int k = 0; k < 4; k++ ) {
			if( cvn[k] != vn[k] )
				Common.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		}
	}

	public boolean isMajor() {
		if( type == DBEnumLifecycleType.MAJOR )
			return( true );
		return( false );
	}
	
	public boolean isMinor() {
		if( type == DBEnumLifecycleType.MINOR )
			return( true );
		return( false );
	}
	
	public boolean isUrgent() {
		if( type == DBEnumLifecycleType.URGENT )
			return( true );
		return( false );
	}
	
	public String getNextVersion() {
		if( isMajor() )
			return( v1 + "." + (v2+1) );
		if( isMinor() )
			return( v1 + "." + v2 + "." + (v3+1) );
		if( isUrgent() )
			return( v1 + "." + v2 + "." + v3 + "." + (v4+1) );
		return( "" );
	}
	
	private int parseNumber( String number ) throws Exception {
		return( Integer.parseInt( number ) );
	}
	
	public String getFileVersion() {
		return( getReleaseVersion() );
	}
	
	public String getMajorVersion() {
		return( v1 + "." + v2 );
	}
	
	public static String getReleaseShortVersion( String RELEASEDIR ) {
		String RELEASEVER = Common.getPartBeforeFirst( RELEASEDIR , "-" );
		String[] parts = Common.splitDotted( RELEASEVER );
		if( parts.length <= 2 )
			return( RELEASEVER );
		if( parts.length == 3 ) {
			if( parts[2].equals( "0" ) )
				return( parts[0] + "." + parts[1] );
			return( RELEASEVER );
		}
		if( parts[3].equals( "0" ) ) {
			if( parts[2].equals( "0" ) )
				return( parts[0] + "." + parts[1] );
			return( parts[0] + "." + parts[1] + "." + parts[2] );
		}
		
		return( RELEASEVER );
	}
	
	public String getReleaseVersion() {
		if( v4 == 0 ) {
			if( v3 == 0 )
				return( v1 + "." + v2 );
			return( v1 + "." + v2 + "." + v3 );
		}
		return( v1 + "."  + v2 + "." + v3 + "." + v4 );
	}
	
	public String getReleaseName() {
		String name = getReleaseVersion();
		if( !variant.isEmpty() )
			name += "-" + variant;
		return( name );
	}
	
	public String getFullVersion() {
		return( v1 + "."  + v2 + "." + v3 + "." + v4 );
	}

	public String getSortVersion() {
		String x1 = Common.getZeroPadded( v1 , 10 );
		String x2 = Common.getZeroPadded( v2 , 10 );
		String x3 = Common.getZeroPadded( v3 , 10 );
		String x4 = Common.getZeroPadded( v4 , 10 );
		return( x1 + "." + x2 + "." + x3 + "." + x4 );
	}
	
	public String getPreviousVersion() {
		if( v4 != 0 )
			return( v1 + "."  + v2 + "." + v3 + "." + (v4-1) );
		if( v3 != 0 )
			return( v1 + "."  + v2 + "." + (v3-1) + ".0" );
		if( v2 != 0 )
			return( v1 + "."  + (v2-1) + ".0.0" );
		return( (v1-1) + ".0.0.0" );
	}

	public DBEnumLifecycleType getLifecycleType() {
		return( type );
	}
	
	public static DBEnumLifecycleType getLifecycleTypeByFullVersion( String RELEASEVER ) {
		String[] items = Common.splitDotted( RELEASEVER );
		if( items.length != 4 )
			return( DBEnumLifecycleType.UNKNOWN );
		
		if( !items[3].equals( "0" ) )
			return( DBEnumLifecycleType.URGENT );
		
		if( !items[2].equals( "0" ) )
			return( DBEnumLifecycleType.MINOR );
		
		return( DBEnumLifecycleType.MAJOR );
	}

	public static String[] orderVersions( String[] list ) {
		Map<String,String> tosort = new HashMap<String,String>();
		
		for( String version : list ) {
			String variant = Common.getPartAfterFirst( version , "-" );
			version = Common.getPartBeforeFirst( version , "-" );
			String[] items = Common.splitDotted( version );
			String padded = "";
			for( int k = 0; k < items.length; k++ )
				padded += "0000000000".substring( items[k].length() ) + items[k];
			
			if( !variant.isEmpty() )
				version += "-" + variant ;
			tosort.put( padded , version );
		}
		
		String[] sorted = Common.getSortedKeys( tosort );
		for( int k = 0; k < sorted.length; k++ )
			sorted[k] = tosort.get( sorted[k] );
		
		return( sorted );
	}
	
	public static String normalizeReleaseVer( String RELEASEVER ) throws Exception {
		String[] items = Common.splitDotted( RELEASEVER );
		if( items.length < 2 && items.length > 4 )
			Common.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
		
		String value = "";
		for( int k = 0; k < 4; k++ ) {
			if( k > 0 )
				value += ".";
			if( k >= items.length )
				value += "0";
			else {
				if( !items[k].matches( "[0-9]+" ) )
					Common.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
				if( items[k].length() > 3 )
					Common.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
				value += items[k];
			}
		}
		
		return( value );
	}

	public static String getNextVersion( String RELEASEVER ) {
		try {
			VersionInfo info = VersionInfo.getReleaseDirInfo( RELEASEVER );
			return( info.getNextVersion() );
		}
		catch( Throwable e ) {
			return( "" );
		}
	}
	
}
