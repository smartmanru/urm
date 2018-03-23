package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductBuildSettings;

public class ReleaseLabelInfo {

	public static String LABEL_DEFAULT = "default";
	public static String LABEL_MASTER = "master";
	
	Meta meta;
	
	public String RELEASEPATH = "";
	public String RELEASEVER = "";
	public String RELEASEDIR = "";
	public String VARIANT = "";
	public boolean master;
	
	public ReleaseLabelInfo( Meta meta ) {
		this.meta = meta;
		this.master = false;
	}
	
	public static ReleaseLabelInfo getLabelInfo( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = new ReleaseLabelInfo( meta );
		info.createLabelInfo( action , RELEASELABEL );
		return( info );
	}
	
	public void createLabelInfo( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		if( RELEASELABEL.equals( LABEL_DEFAULT ) && !action.context.CTX_DISTPATH.isEmpty() ) {
			RELEASEVER = "default";
			RELEASEDIR = ".";
			RELEASEPATH = ".";
			VARIANT = "";
			return;
		}
		
		if( RELEASELABEL.equals( LABEL_MASTER ) ) {
			RELEASEVER = "(master)";
			RELEASEDIR = Dist.MASTER_DIR;
			RELEASEPATH = RELEASEDIR;
			VARIANT = "";
			master = true;
		}
		else
		if( RELEASELABEL.indexOf( "-" ) > 0 ) {
			RELEASEVER = getReleaseVerByDir( RELEASELABEL );
			RELEASEDIR = RELEASELABEL;
			RELEASEPATH = "releases/" + RELEASEDIR;
			VARIANT = Common.getPartAfterFirst( RELEASEDIR , "-" );
		}
		else {
			RELEASEVER = getReleaseVerByLabel( action , RELEASELABEL );
			RELEASEDIR = getReleaseDirByVer( RELEASEVER );
			RELEASEPATH = "releases/" + RELEASEDIR;
			VARIANT = "";
		}
	}
	
	public DBEnumLifecycleType getLifecycleType() {
		return( VersionInfo.getLifecycleTypeByShortVersion( RELEASEVER ) );
	}
	
	private String getReleaseVerByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		MetaProductBuildSettings build = action.getBuildSettings( meta );
		
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
			RELEASEVER = VersionInfo.normalizeReleaseVer( RELEASELABEL );
			return( RELEASEVER );
		}

		Common.exit1( _Error.UnexpectedReleaseLabel1 , "unexpected release label=" + RELEASELABEL , RELEASELABEL );
		return( null );
	}
	
	private static String getReleaseDirByVer( String RELEASEVER ) throws Exception {
		RELEASEVER = VersionInfo.normalizeReleaseVer( RELEASEVER );
		String[] items = Common.splitDotted( RELEASEVER );
		if( items[3].equals( "0" ) ) {
			if( items[2].equals( "0" ) )
				return( items[0] + "." + items[1] );
			return( items[0] + "." + items[1] + "." + items[2] );
		}
		return( RELEASEVER );
	}
	
	public static String getReleaseVerByDir( String RELEASEDIR ) throws Exception {
		String RELEASEVER = Common.getPartBeforeFirst( RELEASEDIR , "-" );
		RELEASEVER = VersionInfo.normalizeReleaseVer( RELEASEVER );
		return( RELEASEVER );
	}
	
	public static String getReleaseFolder( String RELEASEDIR ) throws Exception {
		return( "releases/" + RELEASEDIR );
	}
	
	public static String getArchivedReleaseFolder( String RELEASEDIR ) {
		return( "archive/" + RELEASEDIR );
	}
	
	public static String getArchiveFolder() {
		return( "archive" );
	}
	
}
