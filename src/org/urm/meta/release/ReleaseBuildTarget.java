package org.urm.meta.release;

import org.urm.db.core.DBEnums.*;
import org.urm.meta.MatchItem;
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
	
	public Release release;
	public ReleaseScope scope;
	
	public int ID;
	public DBEnumBuildTargetType TYPE;
	public MatchItem SRCSET;
	public MatchItem PROJECT;
	public String BUILD_BRANCH;
	public String BUILD_TAG;
	public String BUILD_VERSION;
	public boolean ALL;
	public int RV;
	
	public ReleaseBuildTarget( Release release ) {
		this.release = release;
	}

	public ReleaseBuildTarget( ReleaseScope scope ) {
		this.release = scope.release;
		this.scope = scope;
	}

	public ReleaseBuildTarget copy( ReleaseScope rscope ) {
		ReleaseBuildTarget r = new ReleaseBuildTarget( rscope );
		
		r.ID = ID;
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
	
	public void create( boolean all ) {
		TYPE = DBEnumBuildTargetType.BUILDALL;
		this.ALL = all;
	}
	
	public void create( MetaSourceProjectSet projectSet , boolean all ) {
		TYPE = DBEnumBuildTargetType.PROJECTSET;
		SRCSET = MatchItem.create( projectSet.ID );
		this.ALL = all;
	}
	
	public void create( MetaSourceProject project , boolean all ) {
		TYPE = ( all )? DBEnumBuildTargetType.PROJECTALLITEMS : DBEnumBuildTargetType.PROJECTNOITEMS;
		PROJECT = MatchItem.create( project.ID );
		this.ALL = all;
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
	
}
