package org.urm.meta.release;

import org.urm.db.core.DBEnums.*;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;

public class ReleaseBuildTarget {

	public static String PROPERTY_SCOPETARGET = "scopetarget";
	public static String PROPERTY_TARGETTYPE = "targettype";
	public static String PROPERTY_SRCSET = "srcset";
	public static String PROPERTY_PROJECT = "project";
	public static String PROPERTY_BUILDBRANCH = "buildbranch";
	public static String PROPERTY_BUILDTAG = "buildtag";
	public static String PROPERTY_BUILDVERSION = "buildversion";
	public static String PROPERTY_ALL = "all";

	public static String CLEANUP_VALUE = ".";
	
	public Release release;
	public ReleaseChanges changes;
	public ReleaseScope scope;
	
	public int ID;
	public boolean SCOPETARGET;
	public DBEnumBuildTargetType TYPE;
	public MatchItem SRCSET;
	public MatchItem PROJECT;
	public String BUILD_BRANCH;
	public String BUILD_TAG;
	public String BUILD_VERSION;
	public boolean ALL;
	public int RV;
	
	public ReleaseBuildTarget( ReleaseChanges changes ) {
		this.changes = changes;
		this.release = changes.release;
		this.SCOPETARGET = false;
	}

	public ReleaseBuildTarget( ReleaseScope scope ) {
		this.release = scope.release;
		this.scope = scope;
		this.SCOPETARGET = true;
	}

	public ReleaseBuildTarget copy( ReleaseChanges rchanges , ReleaseScope rscope ) {
		ReleaseBuildTarget r = ( rscope != null )? new ReleaseBuildTarget( rscope ) : new ReleaseBuildTarget( rchanges );
		
		r.ID = ID;
		r.SCOPETARGET = SCOPETARGET;
		r.TYPE = TYPE;
		r.SRCSET = MatchItem.copy( SRCSET );
		r.PROJECT = MatchItem.copy( PROJECT );
		r.BUILD_BRANCH = BUILD_BRANCH;
		r.BUILD_TAG = BUILD_TAG;
		r.BUILD_VERSION = BUILD_VERSION;
		r.ALL = ALL;
		r.RV = RV;
		
		return( r );
	}

	public void create( DBEnumBuildTargetType TYPE , MatchItem SRCSET , MatchItem PROJECT , String BUILD_BRANCH , String BUILD_TAG , String BUILD_VERSION , boolean ALL ) {
		this.TYPE = TYPE;
		this.SRCSET = MatchItem.copy( SRCSET );
		this.PROJECT = MatchItem.copy( PROJECT );
		this.BUILD_BRANCH = BUILD_BRANCH;
		this.BUILD_TAG = BUILD_TAG;
		this.BUILD_VERSION = BUILD_VERSION;
		this.ALL = ALL;
	}
	
	public void create( boolean all ) {
		TYPE = DBEnumBuildTargetType.BUILDALL;
		this.ALL = all;
		BUILD_BRANCH = "";
		BUILD_TAG = "";
		BUILD_VERSION = "";
	}
	
	public void create( MetaSourceProjectSet projectSet , boolean all ) {
		TYPE = DBEnumBuildTargetType.PROJECTSET;
		SRCSET = MatchItem.create( projectSet.ID );
		this.ALL = all;
		BUILD_BRANCH = "";
		BUILD_TAG = "";
		BUILD_VERSION = "";
	}
	
	public void create( MetaSourceProject project , boolean all ) {
		TYPE = ( all )? DBEnumBuildTargetType.PROJECTALLITEMS : DBEnumBuildTargetType.PROJECTNOITEMS;
		PROJECT = MatchItem.create( project.ID );
		this.ALL = all;
		BUILD_BRANCH = "";
		BUILD_TAG = "";
		BUILD_VERSION = "";
	}

	public boolean isScopeTarget() {
		if( scope != null )
			return( true );
		return( false );
	}
	
	public boolean isBuildAll() {
		return( TYPE == DBEnumBuildTargetType.BUILDALL );
	}

	public boolean isBuildSet() {
		return( TYPE == DBEnumBuildTargetType.PROJECTSET );
	}
	
	public boolean isBuildProject() {
		return( TYPE == DBEnumBuildTargetType.PROJECTALLITEMS || TYPE == DBEnumBuildTargetType.PROJECTNOITEMS );
	}

	public boolean isBuildProjectAllItems() {
		return( TYPE == DBEnumBuildTargetType.PROJECTALLITEMS );
	}

	public boolean isBuildProjectNoItems() {
		return( TYPE == DBEnumBuildTargetType.PROJECTNOITEMS );
	}

	public boolean isParentOf( ReleaseBuildTarget targetCheck ) {
		if( targetCheck.isBuildProject() ) {
			if( isBuildAll() )
				return( true );
			
			MetaSourceProject project = targetCheck.getProject();
					
			if( isBuildSet() ) {
				if( MatchItem.equals( SRCSET , project.ID ) )
					return( true );
				return( false );
			}
			
			return( false );
		}
		
		if( targetCheck.isBuildSet() ) {
			if( isBuildAll() )
				return( true );
			
			return( false );
		}
		
		return( false );
	}

	public MetaSourceProjectSet getProjectSet() {
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		return( sources.findProjectSet( SRCSET ) );
	}
		
	public MetaSourceProject getProject() {
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		return( sources.findProject( PROJECT ) );
	}

	public void setSpecifics( String branch , String tag , String version ) throws Exception {
		if( !branch.isEmpty() ) {
			if( branch.equals( CLEANUP_VALUE ) )
				BUILD_BRANCH = "";
			else
				BUILD_BRANCH = branch;
		}
		if( !tag.isEmpty() ) {
			if( tag.equals( CLEANUP_VALUE ) )
				BUILD_TAG = "";
			else
				BUILD_TAG = tag;
		}
		if( !version.isEmpty() ) {
			if( version.equals( CLEANUP_VALUE ) )
				BUILD_VERSION = "";
			else
				BUILD_VERSION = version;
		}
	}
	
}
