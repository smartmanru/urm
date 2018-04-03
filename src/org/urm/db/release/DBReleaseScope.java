package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseScope;

public class DBReleaseScope {

	private static void dropChildBuildTargets( EngineMethod method , ActionBase action , Release release , ReleaseBuildTarget target ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		
		for( ReleaseBuildTarget childTarget : scope.getChildBuildTargets( target ) ) {
			if( childTarget != target ) {
				DBReleaseBuildTarget.deleteBuildTarget( c , release , childTarget );
				scope.removeBuildTarget( childTarget );
			}
		}
	}

	private static void dropChildDistTargets( EngineMethod method , ActionBase action , Release release , ReleaseDistTarget target ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		
		for( ReleaseDistTarget childTarget : scope.getChildDistTargets( target ) ) {
			if( childTarget != target ) {
				DBReleaseDistTarget.deleteDistTarget( c , release , childTarget );
				scope.removeDistTarget( childTarget );
			}
		}
	}

	public static void addAllSource( EngineMethod method , ActionBase action , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
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
	
	private static void addAllBinaries( EngineMethod method , ActionBase action , Release release ) throws Exception {
		ReleaseScope scope = release.getScope();
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		for( MetaDistrDelivery delivery : distr.getDeliveries() )
			addDeliveryAllBinaries( method , action , release , delivery );
	}
	
	public static void addAllConf( EngineMethod method , ActionBase action , Release release ) throws Exception {
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		for( MetaDistrDelivery delivery : distr.getDeliveries() )
			addDeliveryAllConfItems( method , action , release , delivery );
	}
	
	public static void addAllSourceSet( EngineMethod method , ActionBase action , Release release , MetaSourceProjectSet set ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
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
		method.checkUpdateRelease( release );
		
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
		method.checkUpdateRelease( release );
		
		ReleaseBuildTarget target = scope.findBuildProjectTarget( project );
		if( target == null )
			addProjectItems( method , action , release , project , false );
		
		if( !item.isInternal() )
			addBinaryItem( method , action , release , item.distItem );
	}

	public static void addConfItem( EngineMethod method , ActionBase action , Release release , MetaDistrConfItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
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

	public static void addManualItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		addBinaryItem( method , action , release , item );
	}

	public static void addDerivedItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		addBinaryItem( method , action , release , item );
	}

	public static void addBinaryItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryBinaryTarget( item.delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistBinaryItemTarget( item );
		if( target != null )
			return;
		
		if( item.isProjectItem() )
			addProjectItems( method , action , release , item.sourceProjectItem.project , false );
		
		target = DBReleaseDistTarget.createBinaryItemTarget( c , release , scope , item );
		scope.addDistTarget( target );
	}
	
	public static void addDeliveryAllBinaries( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryBinaryTarget( delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseDistTarget.createBinaryDeliveryTarget( c , release , scope , delivery );
		scope.addDistTarget( target );
		
		for( MetaDistrBinaryItem item : delivery.getBinaryItems() ) {
			if( item.isProjectItem() )
				addProjectItems( method , action , release , item.sourceProjectItem.project , false );
		}
		
		dropChildDistTargets( method , action , release , target );
	}

	public static void addDeliveryAllConfItems( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryConfTarget( delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseDistTarget.createConfDeliveryTarget( c , release , scope , delivery );
		scope.addDistTarget( target );
		
		dropChildDistTargets( method , action , release , target );
	}

	public static void addDeliveryAllDatabaseSchemes( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryDatabaseTarget( delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseDistTarget.createDatabaseDeliveryTarget( c , release , scope , delivery );
		scope.addDistTarget( target );
		
		dropChildDistTargets( method , action , release , target );
	}

	public static void addDeliveryAllDocs( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryDocTarget( delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseDistTarget.createDocDeliveryTarget( c , release , scope , delivery );
		scope.addDistTarget( target );
		
		dropChildDistTargets( method , action , release , target );
	}

	public static void addDeliveryDatabaseSchema( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryDatabaseTarget( delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliverySchemaTarget( delivery , schema );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseDistTarget.createDeliverySchemaTarget( c , release , scope , delivery , schema );
		scope.addDistTarget( target );
	}
	
	public static void addDeliveryDoc( EngineMethod method , ActionBase action , Release release , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryDocTarget( delivery );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = scope.findDistDeliveryDocTarget( delivery , doc );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			return;
		}
		
		target = DBReleaseDistTarget.createDeliveryDocTarget( c , release , scope , delivery , doc );
		scope.addDistTarget( target );
	}
	
	public static void addAllDatabase( EngineMethod method , ActionBase action , Release release ) throws Exception {
		method.checkUpdateRelease( release );
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		for( MetaDistrDelivery delivery : distr.getDeliveries() )
			addDeliveryAllDatabaseSchemes( method , action , release , delivery );
	}

	public static void addAllDoc( EngineMethod method , ActionBase action , Release release ) throws Exception {
		method.checkUpdateRelease( release );
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		for( MetaDistrDelivery delivery : distr.getDeliveries() )
			addDeliveryAllDocs( method , action , release , delivery );
	}

	public static void addAllCategory( EngineMethod method , ActionBase action , Release release , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		method.checkUpdateRelease( release );
		
		if( CATEGORY == DBEnumScopeCategoryType.PROJECT )
			addAllSource( method , action , release );
		else
		if( CATEGORY == DBEnumScopeCategoryType.BINARY )
			addAllBinaries( method , action , release );
		else
		if( CATEGORY == DBEnumScopeCategoryType.CONFIG )
			addAllConf( method , action , release );
		else
		if( CATEGORY == DBEnumScopeCategoryType.DB )
			addAllDatabase( method , action , release );
		else
		if( CATEGORY == DBEnumScopeCategoryType.DOC )
			addAllDoc( method , action , release );
	}
	
	public static void descopeAll( EngineMethod method , ActionBase action , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		EngineEntities entities = c.getEntities();
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		scope.clear();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseBuildTarget , DBQueries.FILTER_REL_SCOPERELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDistTarget , DBQueries.FILTER_REL_SCOPERELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
	}

	private static void descopeBuildTargetOnly( DBConnection c , Release release , ReleaseScope scope , ReleaseBuildTarget target ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		scope.removeBuildTarget( target );
		int version = c.getNextReleaseVersion( release );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseBuildTarget , target.ID , version );
	}

	private static void descopeDistTargetOnly( DBConnection c , Release release , ReleaseScope scope , ReleaseDistTarget target ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		scope.removeDistTarget( target );
		int version = c.getNextReleaseVersion( release );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseDistTarget , target.ID , version );
	}

	private static void descopeAllBinaries( DBConnection c , Release release , ReleaseScope scope ) throws Exception {
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		
		for( MetaSourceProjectSet set : sources.getSets() )
			descopeSetBinaries( c , release , scope , set );
	}

	private static void descopeSetBinaries( DBConnection c , Release release , ReleaseScope scope , MetaSourceProjectSet set ) throws Exception {
		for( MetaSourceProject project : set.getProjects() )
			descopeProjectBinaries( c , release , scope , project );
	}

	private static void descopeProjectBinaries( DBConnection c , Release release , ReleaseScope scope , MetaSourceProject project ) throws Exception {
		for( MetaSourceProjectItem item : project.getItems() ) {
			if( !item.isInternal() ) {
				ReleaseDistTarget target = scope.findDistBinaryItemTarget( item.distItem );
				if( target != null )
					descopeDistTargetOnly( c , release , scope , target );
			}
		}
	}

	public static void descopeBinaryItem( EngineMethod method , ActionBase action , Release release , MetaDistrBinaryItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistBinaryItemTarget( item );
		if( target != null ) {
			descopeDistTargetOnly( c , release , scope , target );
			return;
		}
	}
	
	public static void descopeConfItem( EngineMethod method , ActionBase action , Release release , MetaDistrConfItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseDistTarget target = scope.findDistConfItemTarget( item );
		if( target != null ) {
			descopeDistTargetOnly( c , release , scope , target );
			return;
		}
	}
	
	private static void descopeDeliveryBinaries( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDistrBinaryItem item : delivery.getBinaryItems() ) {
			ReleaseDistTarget target = scope.findDistBinaryItemTarget( item );
			if( target != null )
				descopeDistTargetOnly( c , release , scope , target );
		}
	}

	private static void descopeDeliveryConfs( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDistrConfItem item : delivery.getConfItems() ) {
			ReleaseDistTarget target = scope.findDistConfItemTarget( item );
			if( target != null )
				descopeDistTargetOnly( c , release , scope , target );
		}
	}

	private static void descopeDeliveryDatabase( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() ) {
			ReleaseDistTarget target = scope.findDistDeliverySchemaTarget( delivery , schema );
			if( target != null )
				descopeDistTargetOnly( c , release , scope , target );
		}
	}

	private static void descopeDeliveryDocs( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		for( MetaProductDoc doc : delivery.getDocs() ) {
			ReleaseDistTarget target = scope.findDistDeliveryDocTarget( delivery , doc );
			if( target != null )
				descopeDistTargetOnly( c , release , scope , target );
		}
	}

	public static void descopeSet( EngineMethod method , ActionBase action , Release release , ReleaseBuildScopeSet set ) throws Exception {
		descopeSet( method , action , release , set.set );
	}
	
	public static void descopeSet( EngineMethod method , ActionBase action , Release release , MetaSourceProjectSet set ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseBuildTarget target = scope.findBuildAllTarget();
		if( target != null )
			return;
		
		target = scope.findBuildProjectSetTarget( set );
		if( target != null ) {
			if( !target.ALL )
				Common.exitUnexpected();
			
			descopeBuildTargetOnly( c , release , scope , target );
			descopeSetBinaries( c , release , scope , set );
			return;
		}
		
		for( MetaSourceProject project : set.getProjects() ) {
			target = scope.findBuildProjectTarget( project );
			if( target != null ) {
				descopeBuildTargetOnly( c , release , scope , target );
				descopeProjectBinaries( c , release , scope , project );
			}
		}
	}

	public static void descopeProject( EngineMethod method , ActionBase action , Release release , MetaSourceProjectSet set , MetaSourceProject project ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseBuildTarget target = scope.findBuildAllTarget();
		if( target != null )
			return;
		
		target = scope.findBuildProjectSetTarget( set );
		if( target != null )
			return;
		
		target = scope.findBuildProjectTarget( project );
		if( target != null ) {
			descopeBuildTargetOnly( c , release , scope , target );
			descopeProjectBinaries( c , release , scope , project );
			return;
		}
	}

	public static void descopeAllSource( EngineMethod method , ActionBase action , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );
		
		ReleaseBuildTarget target = scope.findBuildAllTarget();
		if( target != null ) {
			descopeBuildTargetOnly( c , release , scope , target );
			descopeAllBinaries( c , release , scope );
			return;
		}
		
		Meta meta = release.getMeta();
		MetaSources sources = meta.getSources();
		
		for( MetaSourceProjectSet set : sources.getSets() )
			descopeSet( method , action , release , set );
	}

	public static void descopeSet( EngineMethod method , ActionBase action , Release release , ReleaseDistScopeSet set ) throws Exception {
		method.checkUpdateRelease( release );
		
		for( ReleaseDistScopeDelivery delivery : set.getDeliveries() )
			descopeDelivery( method , action , release , delivery );
	}

	public static void descopeDelivery( EngineMethod method , ActionBase action , Release release , ReleaseDistScopeDelivery delivery ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseScope scope = release.getScope();
		method.checkUpdateRelease( release );

		ReleaseDistTarget target = scope.findDistAllTarget();
		if( target != null )
			return;
		
		if( delivery.CATEGORY == DBEnumScopeCategoryType.BINARY ) {
			target = scope.findDistDeliveryBinaryTarget( delivery.distDelivery );
			if( target != null ) {
				descopeDistTargetOnly( c , release , scope , target );
				return;
			}
				
			descopeDeliveryBinaries( c , release , scope , delivery.distDelivery );
			return;
		}
		
		if( delivery.CATEGORY == DBEnumScopeCategoryType.CONFIG ) {
			target = scope.findDistDeliveryConfTarget( delivery.distDelivery );
			if( target != null ) {
				descopeDistTargetOnly( c , release , scope , target );
				return;
			}
				
			descopeDeliveryConfs( c , release , scope , delivery.distDelivery );
			return;
		}
		
		if( delivery.CATEGORY == DBEnumScopeCategoryType.DB ) {
			target = scope.findDistDeliveryDatabaseTarget( delivery.distDelivery );
			if( target != null ) {
				descopeDistTargetOnly( c , release , scope , target );
				return;
			}
				
			descopeDeliveryDatabase( c , release , scope , delivery.distDelivery );
			return;
		}
		
		if( delivery.CATEGORY == DBEnumScopeCategoryType.DOC ) {
			target = scope.findDistDeliveryDocTarget( delivery.distDelivery );
			if( target != null ) {
				descopeDistTargetOnly( c , release , scope , target );
				return;
			}
				
			descopeDeliveryDocs( c , release , scope , delivery.distDelivery );
			return;
		}
	}
	
	public static void copyScope( EngineMethod method , ActionBase action , ReleaseRepository repo , Release release , Release dst ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		ReleaseScope scopeDst = dst.getScope();
		if( !scopeDst.isEmpty() )
			DBReleaseScope.descopeAll( method , action , release );
		
		ReleaseScope scopeSrc = release.getScope();
		
		for( ReleaseBuildTarget targetSrc : scopeSrc.getBuildTargets() ) {
			ReleaseBuildTarget targetDst = targetSrc.copy( null , scopeDst );
			DBReleaseBuildTarget.modifyReleaseBuildTarget( c , dst , targetDst , true );
		}
		
		for( ReleaseDistTarget targetSrc : scopeSrc.getDistTargets() ) {
			ReleaseDistTarget targetDst = targetSrc.copy( null , scopeDst );
			DBReleaseDistTarget.modifyReleaseDistTarget( c , dst , targetDst , true );
		}
	}

	public static void finish( EngineMethod method , ActionBase action , Release release , ReleaseScope scope ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		for( ReleaseDistTarget target : scope.getDistTargets() ) {
			// replace full scope
			if( target.isDistAll() ) {
				DBReleaseDistTarget.deleteDistTarget( c , release , target );
				scope.removeDistTarget( target );
				
				for( MetaDistrDelivery delivery : distr.getDeliveries() ) {
					for( MetaDistrBinaryItem binary : delivery.getBinaryItems() ) {
						target = DBReleaseDistTarget.createBinaryItemTarget( c , release , scope , binary );
						scope.addDistTarget( target );
					}
					for( MetaDistrConfItem conf : delivery.getConfItems() ) {
						target = DBReleaseDistTarget.createConfItemTarget( c , release , scope , conf );
						scope.addDistTarget( target );
					}
					for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() ) {
						target = DBReleaseDistTarget.createDeliverySchemaTarget( c , release , scope , delivery , schema );
						scope.addDistTarget( target );
					}
					for( MetaProductDoc doc : delivery.getDocs() ) {
						target = DBReleaseDistTarget.createDeliveryDocTarget( c , release , scope , delivery , doc );
						scope.addDistTarget( target );
					}
				}
			}
			
			// replace delivery scope
			if( target.isDelivery() ) {
				MetaDistrDelivery delivery = target.getDelivery();
				DBReleaseDistTarget.deleteDistTarget( c , release , target );
				scope.removeDistTarget( target );

				if( target.isDeliveryBinaries() ) {
					for( MetaDistrBinaryItem binary : delivery.getBinaryItems() ) {
						target = DBReleaseDistTarget.createBinaryItemTarget( c , release , scope , binary );
						scope.addDistTarget( target );
					}
				}
				else
				if( target.isDeliveryConfs() ) {
					for( MetaDistrConfItem conf : delivery.getConfItems() ) {
						target = DBReleaseDistTarget.createConfItemTarget( c , release , scope , conf );
						scope.addDistTarget( target );
					}
				}
				else
				if( target.isDeliveryDatabase() ) {
					for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() ) {
						target = DBReleaseDistTarget.createDeliverySchemaTarget( c , release , scope , delivery , schema );
						scope.addDistTarget( target );
					}
				}
				else
				if( target.isDeliveryDocs() ) {
					for( MetaProductDoc doc : delivery.getDocs() ) {
						target = DBReleaseDistTarget.createDeliveryDocTarget( c , release , scope , delivery , doc );
						scope.addDistTarget( target );
					}
				}
			}
		}
	}

}
