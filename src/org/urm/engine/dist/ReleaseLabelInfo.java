package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductBuildSettings;

public class ReleaseLabelInfo {

	public static String LABEL_DEFAULT = "default";
	public static String LABEL_MASTER = "master";
	public static String LABEL_ARCHIVED = "archived";
	
	Meta meta;
	
	public String DISTPATH = "";
	public String RELEASEVER = "";
	public String RELEASEDIR = "";
	public String VARIANT = "";
	
	public boolean local;
	public boolean master;
	public boolean normal;
	public boolean archived;
	public boolean primary;
	
	public ReleaseLabelInfo( Meta meta ) {
		this.meta = meta;
		this.master = false;
		this.normal = false;
		this.archived = false;
		this.local = false;
		this.primary = false;
	}
	
	public static ReleaseLabelInfo getLabelInfo( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = new ReleaseLabelInfo( meta );
		boolean archived = false;
		if( RELEASELABEL.startsWith( LABEL_ARCHIVED + "-" ) ) {
			archived = true;
			RELEASELABEL = Common.getPartAfterFirst( RELEASELABEL , "-" );
		}
		
		info.createLabelInfo( action , RELEASELABEL , archived );
		return( info );
	}
	
	public void createLabelInfo( ActionBase action , String RELEASELABEL , boolean archived ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		if( RELEASELABEL.equals( LABEL_DEFAULT ) && !action.context.CTX_DISTPATH.isEmpty() ) {
			RELEASEVER = "default";
			RELEASEDIR = ".";
			VARIANT = "";
			local = true;
			primary = true;
			return;
		}
		
		if( RELEASELABEL.equals( LABEL_MASTER ) ) {
			RELEASEVER = "(master)";
			RELEASEDIR = DistRepository.REPO_FOLDER_RELEASES_MASTER;
			DISTPATH = RELEASEDIR;
			VARIANT = "";
			master = true;
			primary = true;
		}
		else
		if( RELEASELABEL.indexOf( "-" ) > 0 ) {
			RELEASEVER = getReleaseVerByDir( RELEASELABEL );
			RELEASEDIR = RELEASELABEL;
			VARIANT = Common.getPartAfterFirst( RELEASEDIR , "-" );
			this.primary = false;
			this.archived = archived;
		}
		else {
			RELEASEVER = getReleaseVerByLabel( action , RELEASELABEL );
			RELEASEDIR = getReleaseDirByVer( RELEASEVER );
			DISTPATH = "releases/" + RELEASEDIR;
			VARIANT = "";
			this.primary = true;
			this.archived = archived;
		}
	}
	
	public void setRepositoryPath( String path ) {
		this.DISTPATH = path;
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
	
}
