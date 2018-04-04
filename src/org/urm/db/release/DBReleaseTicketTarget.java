package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
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
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseTicketSet;
import org.urm.meta.release.ReleaseTicketTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBReleaseTicketTarget {

	public static void modifyReleaseTicketTarget( DBConnection c , Release release , ReleaseTicketSet set , ReleaseTicketTarget target , boolean insert ) throws Exception {
		if( insert )
			target.ID = c.getNextSequenceValue();
		
		target.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseTicketTarget , target.ID , target.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getObject( set.ID ) ,
				EngineDB.getInteger( target.POS ) ,
				EngineDB.getObject( target.BUILDTARGET_ID ) ,
				EngineDB.getObject( target.DISTTARGET_ID ) ,
				EngineDB.getBoolean( target.DESCOPED ) ,
				EngineDB.getBoolean( target.ACCEPTED )
				} , insert );
	}

	public static void loaddbReleaseTicketTarget( EngineLoader loader , Release release , ReleaseChanges changes , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketTarget;

		int setId = entity.loaddbObject( rs , DBReleaseData.FIELD_TICKETTARGET_TICKETSET_ID );
		ReleaseTicketSet set = changes.getSet( setId );
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , set );
		
		int pos = set.getLastTicketPos();
		target.ID = entity.loaddbId( rs );
		target.RV = entity.loaddbVersion( rs );
		target.create(
				pos ,
				entity.loaddbObject( rs , DBReleaseData.FIELD_TICKETTARGET_BUILDTARGET_ID ) ,
				entity.loaddbObject( rs , DBReleaseData.FIELD_TICKETTARGET_DISTTARGET_ID ) ,
				entity.loaddbBoolean( rs , ReleaseTicketTarget.PROPERTY_DESCOPED ) ,
				entity.loaddbBoolean( rs , ReleaseTicketTarget.PROPERTY_ACCEPTED )
				);
		
		set.addTarget( target );
	}
	
	public static void exportxmlChangeTicketTarget( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicketTarget target , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketTarget;
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlInt( target.POS ) ,
				entity.exportxmlBoolean( target.DESCOPED ) ,
				entity.exportxmlBoolean( target.ACCEPTED )
		} , true );
	}
	
	public static ReleaseTicketTarget importxmlChangeTicketTarget( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseTicketSet set , Node root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketTarget;
		
		ReleaseTicketTarget ticketTarget = new ReleaseTicketTarget( release , set );
		ticketTarget.create(
				entity.importxmlIntAttr( root , ReleaseTicketTarget.PROPERTY_POS ) ,
				null ,
				null ,
				entity.importxmlBooleanAttr( root , ReleaseTicketTarget.PROPERTY_DESCOPED , false ) ,
				entity.importxmlBooleanAttr( root , ReleaseTicketTarget.PROPERTY_ACCEPTED , false )
				);
		return( ticketTarget );
	}
	
	public static void createProjectSetTarget( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaSourceProjectSet projectSet ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		if( ticketSet.findTarget( projectSet ) != null )
			Common.exitUnexpected();
		
		// create build target
		ReleaseBuildTarget buildTarget = new ReleaseBuildTarget( changes );
		buildTarget.create( projectSet , false );
		DBReleaseBuildTarget.modifyReleaseBuildTarget( c , release , buildTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( buildTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addBuildTarget( buildTarget );
		ticketSet.addTarget( target );
	}
	
	public static void createProjectTarget( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaSourceProject project , boolean all ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		if( ticketSet.findTarget( project ) != null )
			Common.exitUnexpected();
		
		// create build target
		ReleaseBuildTarget buildTarget = new ReleaseBuildTarget( changes );
		buildTarget.create( project , all );
		DBReleaseBuildTarget.modifyReleaseBuildTarget( c , release , buildTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( buildTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addBuildTarget( buildTarget );
		ticketSet.addTarget( target );
		
		// add item if all project items
		if( all ) {
			for( MetaSourceProjectItem projectItem : project.getItems() ) {
				if( !projectItem.isInternal() )
					createProjectItemTarget( method , action , release , changes , ticketSet , projectItem );
			}
		}
	}

	public static void createProjectItemTarget( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaSourceProjectItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		if( item.isInternal() )
			Common.exitUnexpected();
		
		// create build target
		ReleaseDistTarget distTarget = new ReleaseDistTarget( changes );
		distTarget.create( item.distItem );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , distTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( distTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addDistTarget( distTarget );
		ticketSet.addTarget( target );
	}

	public static void createDeliveryTarget( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaDistrDelivery delivery , DBEnumDistTargetType type ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		// create build target
		ReleaseDistTarget distTarget = new ReleaseDistTarget( changes );
		distTarget.create( delivery , type );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , distTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( distTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addDistTarget( distTarget );
		ticketSet.addTarget( target );
	}

	public static void createDeliveryTargetItem( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaDistrDelivery delivery , MetaDistrBinaryItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		// create build target
		ReleaseDistTarget distTarget = new ReleaseDistTarget( changes );
		distTarget.create( item );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , distTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( distTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addDistTarget( distTarget );
		ticketSet.addTarget( target );
	}

	public static void createDeliveryTargetItem( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaDistrDelivery delivery , MetaDistrConfItem item ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		// create build target
		ReleaseDistTarget distTarget = new ReleaseDistTarget( changes );
		distTarget.create( item );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , distTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( distTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addDistTarget( distTarget );
		ticketSet.addTarget( target );
	}

	public static void createDeliveryTargetItem( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		// create build target
		ReleaseDistTarget distTarget = new ReleaseDistTarget( changes );
		distTarget.create( delivery , schema );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , distTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( distTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addDistTarget( distTarget );
		ticketSet.addTarget( target );
	}

	public static void createDeliveryTargetItem( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		// create build target
		ReleaseDistTarget distTarget = new ReleaseDistTarget( changes );
		distTarget.create( delivery , doc );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , distTarget , true );
		
		// create changes target
		int pos = ticketSet.getLastTicketPos() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , ticketSet );
		target.create( distTarget , pos );
		modifyReleaseTicketTarget( c , release , ticketSet , target , true );

		// add to sets
		changes.addDistTarget( distTarget );
		ticketSet.addTarget( target );
	}

	public static void deleteTarget( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , ReleaseTicketTarget target ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		EngineEntities entities = c.getEntities();
		method.checkUpdateRelease( release );
		
		ticketSet.removeTarget( target );
		int version = c.getNextReleaseVersion( release );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseTicketTarget , target.ID , version );
		
		if( target.isDistTarget() ) {
			changes.removeDistTarget( target.getDistTarget() );
			DBReleaseDistTarget.deleteDistTarget( c , release , target.getDistTarget() );
		}
		else
		if( target.isBuildTarget() ) {
			changes.removeBuildTarget( target.getBuildTarget() );
			DBReleaseBuildTarget.deleteBuildTarget( c , release , target.getBuildTarget() );
		}
	}	

	public static void descopeTarget( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , ReleaseTicketTarget target ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		target.descope();
		modifyReleaseTicketTarget( c , release , ticketSet , target , false );
	}
	
	public static void acceptTarget( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet ticketSet , ReleaseTicketTarget target ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		target.accept();
		modifyReleaseTicketTarget( c , release , ticketSet , target , false );
	}
		
}


