package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.MetaProductBuildSettings;

public class DistLabelInfo {

	DistRepository repo;
	
	String RELEASEPATH = "";
	String RELEASEVER = "";
	String RELEASEDIR = "";
	boolean prod;
	
	public DistLabelInfo( DistRepository repo ) {
		this.repo = repo;
		this.prod = false;
	}
	
	public void createLabelInfo( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		if( RELEASELABEL.equals( "default" ) && !action.context.CTX_DISTPATH.isEmpty() ) {
			RELEASEVER = "default";
			RELEASEDIR = ".";
			RELEASEPATH = ".";
			return;
		}
		
		if( RELEASELABEL.equals( "prod" ) ) {
			RELEASEVER = "(prod)";
			RELEASEDIR = "prod";
			RELEASEPATH = "prod";
			prod = true;
		}
		else
		if( RELEASELABEL.indexOf( "-" ) > 0 ) {
			RELEASEVER = getReleaseVerByDir( action , RELEASELABEL );
			RELEASEDIR = RELEASELABEL;
			RELEASEPATH = "releases/" + RELEASEDIR;
		}
		else {
			RELEASEVER = getReleaseVerByLabel( action , RELEASELABEL );
			RELEASEDIR = getReleaseDirByVer( action , RELEASEVER );
			RELEASEPATH = "releases/" + RELEASEDIR;
		}
		
		action.debug( "release directory=" + RELEASEPATH + " by label=" + RELEASELABEL + " (RELEASEVER=" + RELEASEVER + ")" );
	}
	
	private String getReleaseVerByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		MetaProductBuildSettings build = action.getBuildSettings( repo.meta );
		
		String RELEASEVER = "";
		if( RELEASELABEL.equals( "last" ) ) {
			RELEASEVER = build.CONFIG_RELEASE_LASTMINOR;
			if( RELEASEVER.isEmpty() )
				action.exit0( _Error.LastMinorVersionNotSet0 , "Last minor release version is not set in product settings" );

			return( RELEASEVER );
		}
		
		if( RELEASELABEL.equals( "next" ) ) {
			RELEASEVER = build.CONFIG_RELEASE_NEXTMINOR;
			if( RELEASEVER.isEmpty() )
				action.exit0( _Error.NextMinorVersionNotSet0 , "Next minor release version is not set in product settings" );

			return( RELEASEVER );
		}
		
		if( RELEASELABEL.indexOf( "-" ) < 0 ) {
			RELEASEVER = normalizeReleaseVer( action , RELEASELABEL );
			return( RELEASEVER );
		}

		action.exit1( _Error.UnexpectedReleaseLabel1 , "unexpected release label=" + RELEASELABEL , RELEASELABEL );
		return( null );
	}
	
	private String getReleaseDirByVer( ActionBase action , String RELEASEVER ) throws Exception {
		RELEASEVER = normalizeReleaseVer( action , RELEASEVER );
		String[] items = Common.splitDotted( RELEASEVER );
		if( items[3].equals( "0" ) ) {
			if( items[2].equals( "0" ) )
				return( items[0] + "." + items[1] );
			return( items[0] + "." + items[1] + "." + items[2] );
		}
		return( RELEASEVER );
	}
	
	public static String getReleaseVerByDir( ActionBase action , String RELEASEDIR ) throws Exception {
		String RELEASEVER = Common.getPartBeforeFirst( RELEASEDIR , "-" );
		RELEASEVER = normalizeReleaseVer( action , RELEASEVER );
		return( RELEASEVER );
	}
	
	public static String getReleaseFolder( ActionBase action , Dist dist ) throws Exception {
		return( "releases/" + dist.RELEASEDIR );
	}
	
	public static String getArchivedReleaseFolder( ActionBase action , Dist dist ) throws Exception {
		return( "archive/" + dist.RELEASEDIR );
	}
	
	public static String getArchiveFolder( ActionBase action ) throws Exception {
		return( "archive" );
	}
	
	public static String normalizeReleaseVer( ActionBase action , String RELEASEVER ) throws Exception {
		String[] items = Common.splitDotted( RELEASEVER );
		if( items.length < 2 && items.length > 4 )
			action.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
		
		String value = "";
		for( int k = 0; k < 4; k++ ) {
			if( k > 0 )
				value += ".";
			if( k >= items.length )
				value += "0";
			else {
				if( !items[k].matches( "[0-9]+" ) )
					action.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
				if( items[k].length() > 3 )
					action.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
				value += items[k];
			}
		}
		
		return( value );
	}

}
