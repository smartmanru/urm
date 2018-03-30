package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseScope;

public class DBReleaseScope {

	public static void dropChildBuildTargets( EngineMethod method , ActionBase action , Release release , ReleaseBuildTarget target ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		
		for( ReleaseBuildTarget childTarget : scope.getChildTargets( target ) ) {
			if( childTarget != target ) {
				DBReleaseBuildTarget.deleteBuildTarget( c , release , childTarget );
				scope.removeBuildTarget( childTarget );
			}
		}
	}

	public static void addAllSource( EngineMethod method , ActionBase action , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		
		ReleaseBuildTarget target = scope.findBuildAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseBuildTarget.createSourceTarget( c , release , scope , true );
		scope.addBuildTarget( target );
		
		dropChildBuildTargets( method , action , release , target );
	}
	
	public static void addAllSourceSet( EngineMethod method , ActionBase action , Release release , MetaSourceProjectSet set ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		
		ReleaseBuildTarget target = scope.findBuildAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findBuildProjectSetTarget( set );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseBuildTarget.createSourceSetTarget( c , release , scope , set , true );
		scope.addBuildTarget( target );
		
		dropChildBuildTargets( method , action , release , target );
	}
	
	public static void addAllProjectItems( EngineMethod method , ActionBase action , Release release , MetaSourceProject project ) throws Exception {
		addProjectItems( method , action , release , project , true );
	}
	
	public static void addProjectItems( EngineMethod method , ActionBase action , Release release , MetaSourceProject project , boolean allItems ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		
		ReleaseBuildTarget target = scope.findBuildAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findBuildProjectSetTarget( project.set );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findBuildProjectTarget( project );
		if( target == null ) {
			target = DBReleaseBuildTarget.createSourceProjectTarget( c , release , scope , project , allItems );
			scope.addBuildTarget( target );
		}
		
		if( allItems ) {
			for( MetaSourceProjectItem item : project.getItems() )
				addProjectItem( method , action , release , project , item ); 
		}
	}

	public static void addProjectItem( EngineMethod method , ActionBase action , Release release , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		ReleaseScope scope = release.getScope();
		
		ReleaseBuildTarget target = scope.findBuildProjectTarget( project );
		if( target == null )
			addProjectItems( method , action , release , project , false );
		
		if( !item.isInternal() )
			addBinaryItem( method , action , release , item.distItem );
	}

	public static void addConfItem( EngineMethod method , ActionBase action , Release release , MetaDistrConfItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryConfTarget( item.delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistConfItemTarget( item );
		if( target != null )
			return;
		
		target = DBReleaseDistTarget.createConfItemTarget( c , release , scope , item );
		scope.addDistTarget( target );
	}

	public static boolean addManualItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}

	public static boolean addDerivedItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}

	public static boolean addBinaryItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}
	
	public static boolean addDeliveryAllDatabaseSchemes( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		return( false );
	}

	public static boolean addDeliveryAllDocs( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		return( false );
	}

	public static boolean addDeliveryDatabaseSchema( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		return( false );
	}
	
	public static boolean addDeliveryDoc( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		return( false );
	}
	
	public static boolean addDatabaseAll( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

	public static boolean addDocAll( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

	public static boolean addAllCategory( EngineMethod method , ActionBase action , Release release , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		return( false );
	}
	
	public static boolean descopeAll( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

	public static boolean descopeSet( EngineMethod method , ActionBase action , Release release , ReleaseBuildScopeSet set ) throws Exception {
		return( false );
	}

	public static boolean descopeSet( EngineMethod method , ActionBase action , Release release , ReleaseDistScopeSet set ) throws Exception {
		return( false );
	}

	public static boolean descopeAllSource( EngineMethod method , ActionBase action , Release release ) throws Exception {
		return( false );
	}

	public static void copyScope( EngineMethod method , ActionBase action , ReleaseRepository repo , Release release , Release dst ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		ReleaseScope scopeDst = dst.getScope();
		if( !scopeDst.isEmpty() )
			DBReleaseScope.descopeAll( method , action , release );
		
		ReleaseScope scopeSrc = release.getScope();
		
		for( ReleaseBuildTarget targetSrc : scopeSrc.getBuildTargets() ) {
			ReleaseBuildTarget targetDst = targetSrc.copy( scopeDst );
			DBReleaseBuildTarget.modifyReleaseBuildTarget( c , dst , targetDst , true );
		}
		
		for( ReleaseDistTarget targetSrc : scopeSrc.getDistTargets() ) {
			ReleaseDistTarget targetDst = targetSrc.copy( scopeDst );
			DBReleaseDistTarget.modifyReleaseDistTarget( c , dst , targetDst , true );
		}
	}
	
}
