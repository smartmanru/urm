package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBEnumTypes.*;

public class VersionInfo {

	private int v1; 
	private int v2; 
	private int v3; 
	private int v4;
	private String variant;
	
	public VersionInfo() {
		v1 = 1;
		v2 = 0;
		v3 = 0;
		v4 = 0;
		variant = "";
	}
	
	public VersionInfo copy() {
		VersionInfo r = new VersionInfo();
		r.v1 = v1;
		r.v2 = v2;
		r.v3 = v3;
		r.v4 = v4;
		r.variant = variant;
		return( r );
	}

	public static VersionInfo getDistVersion( ActionBase action , Dist dist ) throws Exception {
		VersionInfo vi = new VersionInfo();
		vi.setDistData( action , dist );
		return( vi );
	}
	
	public static VersionInfo getFileVersion( ActionBase action , String version ) throws Exception {
		VersionInfo vi = new VersionInfo();
		vi.setVersion( action , version );
		return( vi );
	}
	
	public static VersionInfo getReleaseVersion( ActionBase action , String releaseDir ) throws Exception {
		VersionInfo vi = new VersionInfo();
		vi.setReleaseDir( action , releaseDir );
		return( vi );
	}

	public static String getReleaseVersion( String RELEASEDIR ) {
		return( Common.getPartBeforeLast( RELEASEDIR , "-" ) );
	}

	public static String getReleaseVariant( String RELEASEDIR ) {
		return( Common.getPartAfterLast( RELEASEDIR , "-" ) );
	}

	public void setVersion( ActionBase action , String version ) throws Exception {
		int[] vn = new int[4];
		parseVersion( action , version , vn );
		v1 = vn[0]; 
		v2 = vn[1]; 
		v3 = vn[2]; 
		v4 = vn[3]; 
		variant = "";
	}

	public void setReleaseDir( ActionBase action , String releaseDir ) throws Exception {
		String version = Common.getPartBeforeLast( releaseDir , "-" );
		setVersion( action , version );
		variant = Common.getPartAfterLast( releaseDir , "-" );
	}
	
	public void setDistData( ActionBase action , Dist dist ) throws Exception {
		int[] vn = new int[4];
		parseVersion( action , dist.release.RELEASEVER , vn );
		
		if( !dist.isMaster() )
			checkDistVersion( action , vn , Common.getPartBeforeLast( dist.RELEASEDIR , "-" ) );
		
		v1 = vn[0]; 
		v2 = vn[1]; 
		v3 = vn[2]; 
		v4 = vn[3]; 
		variant = Common.getPartAfterLast( dist.RELEASEDIR , "-" );
	}
	
	private void parseVersion( ActionBase action , String version , int[] vn ) throws Exception {
		if( !version.matches( "[0-9.]*" ) )
			action.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		
		String items[] = Common.splitDotted( version );
		if( items.length == 0 || items.length > 4 )
			action.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		
		for( String item : items ) {
			if( item.isEmpty() )
				action.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		}

		vn[0] = parseNumber( action , items[0] );
		vn[1] = ( items.length > 1 )? parseNumber( action , items[1] ) : 0;
		vn[2] = ( items.length > 2 )? parseNumber( action , items[2] ) : 0;
		vn[3] = ( items.length > 3 )? parseNumber( action , items[3] ) : 0;
	}
	
	private void checkDistVersion( ActionBase action , int[] cvn , String version ) throws Exception {
		int[] vn = new int[4];
		parseVersion( action , version , vn );
		for( int k = 0; k < 4; k++ ) {
			if( cvn[k] != vn[k] )
				action.exit1( _Error.InvalidReleaseVersion1 , "Invalid release version=" + version , version );
		}
	}
	
	private int parseNumber( ActionBase action , String number ) throws Exception {
		return( Integer.parseInt( number ) );
	}
	
	public String getFileVersion() {
		return( getReleaseVersion() );
	}
	
	public String getMajorVersion() {
		return( v1 + "." + v2 );
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
		if( v4 != 0 )
			return( DBEnumLifecycleType.URGENT );
		if( v3 != 0 )
			return( DBEnumLifecycleType.MINOR );
		return( DBEnumLifecycleType.MAJOR );
	}
	
	public static DBEnumLifecycleType getLifecycleType( String RELEASEVER ) {
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
	
}
