package org.urm.db.release;

import java.sql.ResultSet;
import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.BlotterService;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistItemInfo;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseDistItem;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;
import org.urm.meta.release.ReleaseScope;
import org.urm.meta.release.ReleaseTicket;
import org.urm.meta.release.ReleaseTicketSet;
import org.urm.meta.release.ReleaseTicketTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBRelease {

	public static String ELEMENT_RELEASEPROPS = "version";
	public static String ELEMENT_RELEASEDISTPROPS = "dist";
	public static String ELEMENT_SCHEDULE = "schedule";
	public static String ELEMENT_PHASE = "phase";
	public static String ELEMENT_CHANGES = "changes";
	public static String ELEMENT_TICKETSET = "ticketset";
	public static String ELEMENT_TICKET = "ticket";
	public static String ELEMENT_TICKETTARGET = "target";
	public static String ELEMENT_BUILDTARGET = "buildtarget";
	public static String ELEMENT_DISTTARGET = "disttarget";
	
	public static void modifyRelease( DBConnection c , ReleaseRepository repo , Release release , boolean insert ) throws Exception {
		if( insert )
			release.ID = c.getNextSequenceValue();
		
		release.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		VersionInfo info = VersionInfo.getReleaseDirInfo( release.RELEASEVER );
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseMain , release.ID , release.RV , new String[] {
				EngineDB.getObject( repo.ID ) ,
				EngineDB.getString( release.NAME ) ,
				EngineDB.getString( release.DESC ) ,
				EngineDB.getBoolean( release.MASTER ) ,
				EngineDB.getEnum( release.TYPE ) ,
				EngineDB.getInteger( info.v1 ) ,
				EngineDB.getInteger( info.v2 ) ,
				EngineDB.getInteger( info.v3 ) ,
				EngineDB.getInteger( info.v4 ) ,
				EngineDB.getString( info.getFullVersion() ) ,
				EngineDB.getEnum( release.BUILDMODE ) ,
				EngineDB.getString( release.COMPATIBILITY ) ,
				EngineDB.getBoolean( release.CUMULATIVE ) ,
				EngineDB.getBoolean( release.ARCHIVED ) ,
				EngineDB.getBoolean( release.CANCELLED )
				} , insert );
	}

	public static Release loaddbRelease( EngineLoader loader , ReleaseRepository repo , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseMain;
		
		Release release = new Release( repo );
		release.ID = entity.loaddbId( rs );
		release.RV = entity.loaddbVersion( rs );
		release.create(
				entity.loaddbString( rs , Release.PROPERTY_NAME ) ,
				entity.loaddbString( rs , Release.PROPERTY_DESC ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_MASTER ) ,
				DBEnumLifecycleType.getValue( entity.loaddbEnum( rs , Release.PROPERTY_LIFECYCLETYPE ) , true ) ,
				entity.loaddbString( rs , Release.PROPERTY_VERSION ) ,
				DBEnumBuildModeType.getValue( entity.loaddbEnum( rs , Release.PROPERTY_BUILDMODE ) , false ) ,
				entity.loaddbString( rs , Release.PROPERTY_COMPATIBILITY ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_CUMULATIVE ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_ARCHIVED ) ,
				entity.loaddbBoolean( rs , Release.PROPERTY_CANCELLED )
				);
		return( release );
	}
	
	public static void exportxml( EngineLoader loader , Release release , ReleaseDist releaseDist , Document doc , Element root ) throws Exception {
		exportxmlReleaseProperties( loader , release , doc , root );
		DBReleaseDist.exportxmlReleaseDistProperties( loader , releaseDist , doc , root );
		exportxmlReleaseSchedule( loader , release , doc , root );
		exportxmlReleaseChanges( loader , release , doc , root );
		exportxmlReleaseScope( loader , release , doc , root );
	}
	
	private static void exportxmlReleaseSchedule( EngineLoader loader , Release release , Document doc , Element root ) throws Exception {
		Element node = Common.xmlCreateElement( doc , root , ELEMENT_SCHEDULE );
		ReleaseSchedule schedule = release.getSchedule();
		DBReleaseSchedule.exportxmlReleaseSchedule( loader , release , schedule , doc , node );
		
		for( ReleaseSchedulePhase phase : schedule.getPhases() ) {
			Element nodePhase = Common.xmlCreateElement( doc , node , ELEMENT_PHASE );
			DBReleaseSchedulePhase.exportxmlReleaseSchedulePhase( loader , release , phase , doc , nodePhase );
		}
	}
	
	private static void exportxmlReleaseChanges( EngineLoader loader , Release release , Document doc , Element root ) throws Exception {
		Element node = Common.xmlCreateElement( doc , root , ELEMENT_CHANGES );
		ReleaseChanges changes = release.getChanges();
		
		for( String setCode : changes.getSetCodes() ) {
			ReleaseTicketSet set = changes.getSet( setCode );
			Element nodeSet = Common.xmlCreateElement( doc , node , ELEMENT_TICKETSET );
			DBReleaseChanges.exportxmlChangeSet( loader , release , changes , set , doc , nodeSet );
			
			for( ReleaseTicket ticket : set.getTickets() ) {
				Element nodeTicket = Common.xmlCreateElement( doc , nodeSet , ELEMENT_TICKET );
				DBReleaseChanges.exportxmlChangeTicket( loader , release , changes , set , ticket , doc , nodeTicket );
			}
			
			for( ReleaseTicketTarget target : set.getTargets() ) {
				Element nodeTarget = Common.xmlCreateElement( doc , nodeSet , ELEMENT_TICKETTARGET );
				DBReleaseTicketTarget.exportxmlChangeTicketTarget( loader , release , changes , set , target , doc , nodeTarget );
				
				if( target.isBuildTarget() ) {
					Element nodeBuildTarget = Common.xmlCreateElement( doc , nodeTarget , ELEMENT_BUILDTARGET );
					ReleaseBuildTarget buildTarget = target.getBuildTarget();
					DBReleaseBuildTarget.exportxmlBuildTarget( loader , release , buildTarget , doc , nodeBuildTarget );
				}
				else
				if( target.isDistTarget() ) {
					Element nodeDistTarget = Common.xmlCreateElement( doc , nodeTarget , ELEMENT_DISTTARGET );
					ReleaseDistTarget distTarget = target.getDistTarget();
					DBReleaseDistTarget.exportxmlDistTarget( loader , release , distTarget , doc , nodeDistTarget );
				}
			}
		}
	}
	
	private static void exportxmlReleaseScope( EngineLoader loader , Release release , Document doc , Element root ) {
	}

	public static void importxml( EngineLoader loader , Release release , ReleaseDist releaseDist , Dist dist , Node root ) throws Exception {
		importxmlReleaseProperties( loader , release , root );
		DBReleaseDist.importxmlReleaseDistProperties( loader , releaseDist , dist , root );
		importxmlReleaseSchedule( loader , release , root );
		importxmlReleaseChanges( loader , release , root );
		importxmlReleaseScope( loader , release , root );
	}

	public static void exportxmlReleaseProperties( EngineLoader loader , Release release , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseMain;
		
		Element node = Common.xmlCreateElement( doc , root , ELEMENT_RELEASEPROPS );
		DBEngineEntities.exportxmlAppObject( doc , node , entity , new String[] {
				entity.exportxmlString( release.NAME ) ,
				entity.exportxmlString( release.DESC ) ,
				entity.exportxmlBoolean( release.MASTER ) ,
				entity.exportxmlEnum( release.TYPE ) ,
				entity.exportxmlString( release.RELEASEVER ) ,
				entity.exportxmlEnum( release.BUILDMODE ) ,
				entity.exportxmlString( release.COMPATIBILITY ) ,
				entity.exportxmlBoolean( release.CUMULATIVE ) ,
				entity.exportxmlBoolean( release.ARCHIVED ) ,
				entity.exportxmlBoolean( release.CANCELLED )
		} , false );
	}
	
	public static void importxmlReleaseProperties( EngineLoader loader , Release release , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseMain;

		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_RELEASEPROPS );
		if( node == null )
			Common.exitUnexpected();
		
		release.create( 
				entity.importxmlStringProperty( node , Release.PROPERTY_NAME ) ,
				entity.importxmlStringProperty( node , Release.PROPERTY_DESC ) ,
				entity.importxmlBooleanProperty( node , Release.PROPERTY_MASTER , false ) ,
				DBEnumLifecycleType.getValue( entity.importxmlEnumProperty( node , Release.PROPERTY_LIFECYCLETYPE ) , true ) ,
				release.RELEASEVER ,
				DBEnumBuildModeType.getValue( entity.importxmlEnumProperty( node , Release.PROPERTY_BUILDMODE ) , false ) ,
				entity.importxmlStringProperty( node , Release.PROPERTY_COMPATIBILITY ) ,
				entity.importxmlBooleanProperty( node , Release.PROPERTY_CUMULATIVE , false ) ,
				entity.importxmlBooleanProperty( node , Release.PROPERTY_ARCHIVED , false ) ,
				entity.importxmlBooleanProperty( node , Release.PROPERTY_CANCELLED , false )
				);
		modifyRelease( c , release.repo , release , false );
	}
	
	public static void importxmlReleaseSchedule( EngineLoader loader , Release release , Node root ) throws Exception {
		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_SCHEDULE );
		if( node == null )
			Common.exitUnexpected();

		ReleaseSchedule schedule = release.getSchedule();
		DBReleaseSchedule.importxmlReleaseSchedule( loader , release , schedule , node );

		Node[] nodeItems = ConfReader.xmlGetChildren( node , ELEMENT_PHASE );
		if( nodeItems != null ) {
			for( Node nodePhase : nodeItems )
				DBReleaseSchedulePhase.importxmlReleaseSchedulePhase( loader , release , schedule , nodePhase );
			schedule.sortPhases();
		}
		
		schedule.setDeadlines();
	}
	
	private static void importxmlReleaseChanges( EngineLoader loader , Release release , Node root ) throws Exception {
		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_CHANGES );
		if( node == null )
			return;

		ReleaseChanges changes = release.getChanges();
		Node[] nodeItems = ConfReader.xmlGetChildren( node , ELEMENT_TICKETSET );
		if( nodeItems != null ) {
			for( Node nodeSet : nodeItems )
				importxmlReleaseChangeSet( loader , release , changes , nodeSet );
		}
	}

	private static void importxmlReleaseChangeSet( EngineLoader loader , Release release , ReleaseChanges changes , Node root ) throws Exception {
		ReleaseTicketSet set = DBReleaseChanges.importxmlReleaseChangeSet( loader , release , changes , root );
		changes.addSet( set );
		
		Node[] nodeItems = ConfReader.xmlGetChildren( root , ELEMENT_TICKET );
		if( nodeItems != null ) {
			for( Node nodeTicket : nodeItems ) {
				ReleaseTicket ticket = DBReleaseChanges.importxmlReleaseChangeTicket( loader , release , changes , set , nodeTicket );
				set.addTicket( ticket );
			}
			
			set.sortTickets();
		}
		
		nodeItems = ConfReader.xmlGetChildren( root , ELEMENT_TICKETTARGET );
		if( nodeItems != null ) {
			for( Node nodeTarget : nodeItems ) {
				ReleaseTicketTarget target = importxmlReleaseChangeTarget( loader , release , changes , set , nodeTarget );
				set.addTarget( target );
			}
			
			set.sortTargets();
		}
	}

	private static ReleaseTicketTarget importxmlReleaseChangeTarget( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseTicketSet set , Node root ) throws Exception {
		Node nodeBuildTarget = ConfReader.xmlGetFirstChild( root , ELEMENT_BUILDTARGET );
		if( nodeBuildTarget != null ) {
			ReleaseBuildTarget buildTarget = DBReleaseBuildTarget.importxmlBuildTarget( loader , release , changes , null , nodeBuildTarget );
			ReleaseTicketTarget target = DBReleaseTicketTarget.importxmlChangeTicketTarget( loader , release , changes , set , buildTarget , null , root );
			set.addTarget( target );
			return( target );
		}
		
		Node nodeDistTarget = ConfReader.xmlGetFirstChild( root , ELEMENT_DISTTARGET );
		if( nodeDistTarget != null ) {
			ReleaseDistTarget distTarget = DBReleaseDistTarget.importxmlDistTarget( loader , release , changes , null , nodeDistTarget );
			ReleaseTicketTarget target = DBReleaseTicketTarget.importxmlChangeTicketTarget( loader , release , changes , set , null , distTarget , root );
			set.addTarget( target );
			return( target );
		}
		
		Common.exitUnexpected();
		return( null );
	}
	
	private static void importxmlReleaseScope( EngineLoader loader , Release release , Node root ) throws Exception {
		ReleaseScope scope = release.getScope();
		
		Node[] nodeItems = ConfReader.xmlGetChildren( root , ELEMENT_BUILDTARGET );
		if( nodeItems != null ) {
			for( Node nodeBuildTarget : nodeItems ) {
				ReleaseBuildTarget buildTarget = DBReleaseBuildTarget.importxmlBuildTarget( loader , release , null , scope , nodeBuildTarget );
				scope.addBuildTarget( buildTarget );
			}
		}
		
		nodeItems = ConfReader.xmlGetChildren( root , ELEMENT_DISTTARGET );
		if( nodeItems != null ) {
			for( Node nodeDistTarget : nodeItems ) {
				ReleaseDistTarget distTarget = DBReleaseDistTarget.importxmlDistTarget( loader , release , null , scope , nodeDistTarget );
				scope.addDistTarget( distTarget );
			}
		}
	}

	public static Release createNormalRelease( EngineMethod method , ActionBase action , ReleaseRepository repo , String RELEASEVER , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		Release release = new Release( repo );
		release.createNormal( action , RELEASEVER , releaseDate , lc );

		modifyRelease( c , repo , release , true );
		DBReleaseSchedule.createReleaseSchedule( method , action , release );
		
		method.createRelease( repo , release );
		return( release );
	}

	public static Release createMasterRelease( EngineMethod method , ActionBase action , ReleaseRepository repo , String RELEASEVER ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		Release release = new Release( repo );
		release.createMaster( action , RELEASEVER );

		modifyRelease( c , repo , release , true );
		
		method.createRelease( repo , release );
		return( release );
	}

	public static void setProperties( EngineMethod method , ActionBase action , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		release.setProperties( action );
		modifyRelease( c , release.repo , release , false );
	}
	
	public static void reopen( EngineMethod method , ActionBase action , Release release ) throws Exception {
		method.checkUpdateRelease( release );
		
		ReleaseSchedule schedule = release.getSchedule();
		DBReleaseSchedule.reopen( method , action , release , schedule );
	}
	
	public static void setMasterVersion( EngineMethod method , ActionBase action , Release release , String RELEASEVER ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		release.setReleaseVer( action , RELEASEVER );
		modifyRelease( c , release.repo , release , false );
	}

	public static void finish( EngineMethod method , ActionBase action , Release release , Dist dist ) throws Exception {
		method.checkUpdateRelease( release );
		
		if( release.isFinalized() )
			Common.exitUnexpected();
		
		// finish schedule
		ReleaseSchedule schedule = release.getSchedule();
		DBReleaseSchedule.finish( method , action , release , schedule );
		
		// replace group scope with items
		ReleaseScope scope = release.getScope();
		DBReleaseScope.finish( method , action , release , scope );
		
		// create file information records
		createFileRecords( method , action , release , dist );
	}
	
	public static void complete( EngineMethod method , ActionBase action , Release release ) throws Exception {
		method.checkUpdateRelease( release );
		
		if( !release.isFinalized() )
			Common.exitUnexpected();
		
		if( release.isCompleted() )
			Common.exitUnexpected();
		
		ReleaseSchedule schedule = release.getSchedule();
		DBReleaseSchedule.complete( method , action , release , schedule );
	}
	
	public static void finishStatus( EngineMethod method , ActionBase action , Release release ) throws Exception {
		BlotterService blotter = action.getServerBlotter();
		blotter.runReleaseStatus( action , release );
	}

	private static void createFileRecords( EngineMethod method , ActionBase action , Release release , Dist dist ) throws Exception {
		/*
		ReleaseScope scope = release.getScope();
		DBReleaseDistTarget.dropAllTargets( method , action , release , dist.releaseDist );
		
		for( ReleaseDistTarget target : scope.getDistTargets() ) {
			if( !target.isDistItem() )
				Common.exitUnexpected();
			
			if( target.isBinaryItem() ) {
				MetaDistrBinaryItem item = target.getBinaryItem();
				DistItemInfo info = dist.getDistItemInfo( action , true , true );
				ReleaseDistItem item = DBReleaseDistTarget.createDistItem( method , action , release , target , info );
				scope.addDistItem( item );
			}
			else
			if( target.isBinaryItem() ) {
				ReleaseDistItem item = DBReleaseDistTarget.createDistItem( method , action , release , dist , target );
				scope.addDistItem( item );
			}
		}
		*/
	}
	
}
