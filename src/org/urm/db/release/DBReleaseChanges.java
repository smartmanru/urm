package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.AuthService;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.MatchItem;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseTicket;
import org.urm.meta.release.ReleaseTicketSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBReleaseChanges {

	public static ReleaseTicketSet importxmlReleaseChangeSet( EngineLoader loader , Release release , ReleaseChanges changes , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketSet;

		ReleaseTicketSet set = new ReleaseTicketSet( release , changes );
		set.create(
				entity.importxmlStringAttr( root , ReleaseTicketSet.PROPERTY_CODE ) ,
				entity.importxmlStringAttr( root , ReleaseTicketSet.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , ReleaseTicketSet.PROPERTY_DESC ) ,
				DBEnumTicketSetStatusType.getValue( entity.importxmlEnumAttr( root , ReleaseTicketSet.PROPERTY_STATUS ) , true )
				);
		modifyTicketSet( c , release , changes , set , true );
		return( set );
	}
	
	private static void modifyTicketSet( DBConnection c , Release release , ReleaseChanges changes , ReleaseTicketSet set , boolean insert ) throws Exception {
		if( insert )
			set.ID = c.getNextSequenceValue();
		
		set.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseTicketSet , set.ID , set.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getString( set.CODE ) ,
				EngineDB.getString( set.NAME ) ,
				EngineDB.getString( set.DESC ) ,
				EngineDB.getEnum( set.STATUS )
				} , insert );
	}
	
	public static void loaddbReleaseTicketSet( EngineLoader loader , Release release , ReleaseChanges changes , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketSet;

		ReleaseTicketSet set = new ReleaseTicketSet( release , changes );
		set.ID = entity.loaddbId( rs );
		set.RV = entity.loaddbVersion( rs );
		set.create(
				entity.loaddbString( rs , ReleaseTicketSet.PROPERTY_CODE ) ,
				entity.loaddbString( rs , ReleaseTicketSet.PROPERTY_NAME ) ,
				entity.loaddbString( rs , ReleaseTicketSet.PROPERTY_DESC ) ,
				DBEnumTicketSetStatusType.getValue( entity.loaddbEnum( rs , ReleaseTicketSet.PROPERTY_STATUS ) , true )
				);
		changes.addSet( set );
	}
	
	public static void loaddbReleaseTicket( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseTicketSet set , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicket;

		ReleaseTicket ticket = new ReleaseTicket( release , set );
		ticket.ID = entity.loaddbId( rs );
		ticket.RV = entity.loaddbVersion( rs );
		ticket.create(
				entity.loaddbInt( rs , ReleaseTicket.PROPERTY_POS ) ,
				entity.loaddbString( rs , ReleaseTicket.PROPERTY_CODE ) ,
				entity.loaddbString( rs , ReleaseTicket.PROPERTY_NAME ) ,
				entity.loaddbString( rs , ReleaseTicket.PROPERTY_DESC ) ,
				entity.loaddbString( rs , ReleaseTicket.PROPERTY_LINK ) ,
				DBEnumTicketType.getValue( entity.loaddbEnum( rs , ReleaseTicket.PROPERTY_TYPE ) , true ) ,
				DBEnumTicketStatusType.getValue( entity.loaddbEnum( rs , ReleaseTicket.PROPERTY_STATUS ) , true ) ,
				entity.loaddbBoolean( rs , ReleaseTicket.PROPERTY_ACTIVE ) ,
				entity.loaddbBoolean( rs , ReleaseTicket.PROPERTY_ACCEPTED ) ,
				entity.loaddbBoolean( rs , ReleaseTicket.PROPERTY_DESCOPED ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_TICKET_OWNER_ID , ReleaseTicket.PROPERTY_OWNER ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_TICKET_DEVUSER_ID , ReleaseTicket.PROPERTY_DEV ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_TICKET_QAUSER_ID , ReleaseTicket.PROPERTY_QA )
				);
		set.addTicket( ticket );
	}
	
	public static ReleaseTicket importxmlReleaseChangeTicket( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseTicketSet set , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicket;

		AuthService auth = loader.getAuth();
		MatchItem owner = auth.getUserMatchItem( entity.importxmlStringAttr( root , ReleaseTicket.PROPERTY_OWNER ) );
		MatchItem dev = auth.getUserMatchItem( entity.importxmlStringAttr( root , ReleaseTicket.PROPERTY_DEV ) );
		MatchItem qa = auth.getUserMatchItem( entity.importxmlStringAttr( root , ReleaseTicket.PROPERTY_QA ) );
		
		int pos = entity.importxmlIntAttr( root , ReleaseTicket.PROPERTY_POS );
		if( pos <= 0 )
			pos = set.getLastTicketPos() + 1;
		
		ReleaseTicket ticket = new ReleaseTicket( release , set );
		ticket.create(
				pos ,
				entity.importxmlStringAttr( root , ReleaseTicket.PROPERTY_CODE ) ,
				entity.importxmlStringAttr( root , ReleaseTicket.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , ReleaseTicket.PROPERTY_DESC ) ,
				entity.importxmlStringAttr( root , ReleaseTicket.PROPERTY_LINK ) ,
				DBEnumTicketType.getValue( entity.importxmlEnumAttr( root , ReleaseTicket.PROPERTY_TYPE ) , true ) ,
				DBEnumTicketStatusType.getValue( entity.importxmlEnumAttr( root , ReleaseTicket.PROPERTY_STATUS ) , true ) ,
				entity.importxmlBooleanAttr( root , ReleaseTicket.PROPERTY_ACTIVE , false ) ,
				entity.importxmlBooleanAttr( root , ReleaseTicket.PROPERTY_ACCEPTED , false ) ,
				entity.importxmlBooleanAttr( root , ReleaseTicket.PROPERTY_DESCOPED , false ) ,
				owner ,
				dev ,
				qa
				);
		modifyTicket( c , release , changes , set , ticket , true );
		return( ticket );
	}
	
	private static void modifyTicket( DBConnection c , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket , boolean insert ) throws Exception {
		if( insert )
			ticket.ID = c.getNextSequenceValue();
		
		ticket.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseTicket , ticket.ID , ticket.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getObject( set.ID ) ,
				EngineDB.getInteger( ticket.POS ) ,
				EngineDB.getString( ticket.CODE ) ,
				EngineDB.getString( ticket.NAME ) ,
				EngineDB.getString( ticket.DESC ) ,
				EngineDB.getString( ticket.LINK ) ,
				EngineDB.getEnum( ticket.TYPE ) ,
				EngineDB.getEnum( ticket.TICKETSTATUS ) ,
				EngineDB.getBoolean( ticket.ACTIVE ) ,
				EngineDB.getBoolean( ticket.ACCEPTED ) ,
				EngineDB.getBoolean( ticket.DESCOPED ) ,
				EngineDB.getMatchId( ticket.OWNER ) ,
				EngineDB.getMatchName( ticket.OWNER ) ,
				EngineDB.getMatchId( ticket.DEV ) ,
				EngineDB.getMatchName( ticket.DEV ) ,
				EngineDB.getMatchId( ticket.QA ) ,
				EngineDB.getMatchName( ticket.QA )
				} , insert );
	}
	
	public static void exportxmlChangeSet( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseTicketSet set , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicketSet;
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( set.CODE ) ,
				entity.exportxmlString( set.NAME ) ,
				entity.exportxmlString( set.DESC ) ,
				entity.exportxmlEnum( set.STATUS )
		} , true );
	}
	
	public static void exportxmlChangeTicket( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseTicket;
		
		AuthService auth = loader.getAuth();
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlInt( ticket.POS ) ,
				entity.exportxmlString( ticket.CODE ) ,
				entity.exportxmlString( ticket.NAME ) ,
				entity.exportxmlString( ticket.DESC ) ,
				entity.exportxmlString( ticket.LINK ) ,
				entity.exportxmlEnum( ticket.TYPE ) ,
				entity.exportxmlEnum( ticket.TICKETSTATUS ) ,
				entity.exportxmlBoolean( ticket.ACTIVE ) ,
				entity.exportxmlBoolean( ticket.ACCEPTED ) ,
				entity.exportxmlBoolean( ticket.DESCOPED ) ,
				entity.exportxmlString( auth.getUserName( ticket.OWNER ) ) ,
				entity.exportxmlString( auth.getUserName( ticket.DEV ) ) ,
				entity.exportxmlString( auth.getUserName( ticket.QA ) )
		} , true );
	}
	
	public static void createSet( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , String code , String name , String comments ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		
		if( changes.findSet( code ) != null )
			Common.exitUnexpected();
		
		ReleaseTicketSet set = new ReleaseTicketSet( release , changes );
		set.create( code , name , comments );
		modifyTicketSet( c , release , changes , set , true );
		changes.addSet( set );
	}
	
	public static void modifySet( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , String code , String name , String comments ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );

		set.modify( code , name , comments );
		modifyTicketSet( c , release , changes , set , false );
		changes.updateSet( set );
	}
	
	public static void dropSet( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , boolean descope ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		EngineEntities entities = c.getEntities();
		
		if( descope ) {
			if( !set.isDescoped() ) {
				set.descope();
				modifyTicketSet( c , release , changes , set , false );
				
				for( ReleaseTicket ticket : set.getTickets() ) {
					if( !ticket.isDescoped() ) {
						ticket.descope();
						modifyTicket( c , release , changes , set , ticket , false );
					}
				}
			}
		}
		else {
			if( !set.isNew() )
				action.exitUnexpectedState();
			
			DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseBuildTarget , DBQueries.FILTER_REL_BUILDTARGET_TICKETSET1 , new String[] { EngineDB.getInteger( set.ID ) } );
			DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDistTarget , DBQueries.FILTER_REL_DISTTARGET_TICKETSET1 , new String[] { EngineDB.getInteger( set.ID ) } );
			DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketTarget , DBQueries.FILTER_REL_TICKETSET1 , new String[] { EngineDB.getInteger( set.ID ) } );
			DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicket , DBQueries.FILTER_REL_TICKETSET1 , new String[] { EngineDB.getInteger( set.ID ) } );
			DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseTicketSet , DBQueries.FILTER_REL_TICKETSET1 , new String[] { EngineDB.getInteger( set.ID ) } );
			changes.removeSet( set );
		}
	}

	public static void createTicket( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , DBEnumTicketType type , String code , String name , String link , String comments , Integer owner , boolean devdone ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		
		if( set.findTicket( code ) != null )
			Common.exitUnexpected();
		
		ReleaseTicket ticket = new ReleaseTicket( release , set );
		int pos = set.getLastTicketPos() + 1;
		ticket.create( pos , type , code , name , link , comments , owner , devdone );
		modifyTicket( c , release , changes , set , ticket , true );
		set.addTicket( ticket );
	}
	
	public static void modifyTicket( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket , DBEnumTicketType type , String code , String name , String link , String comments , Integer owner , boolean devdone ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		
		if( set.findTicket( code ) == null )
			Common.exitUnexpected();
		
		ticket.modify( type , code , name , link , comments , owner , devdone );
		modifyTicket( c , release , changes , set , ticket , false );
		set.updateTicket( ticket );
	}
	
	public static void dropTicket( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket , boolean descope ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		EngineEntities entities = c.getEntities();
		
		if( descope ) {
			if( !ticket.isDescoped() ) {
				ticket.descope();
				modifyTicket( c , release , changes , set , ticket , false );
			}
		}
		else {
			set.removeTicket( ticket );

			int version = c.getNextReleaseVersion( release );
			DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseTicket , ticket.ID , version );
			
			set.reorderTickets();
			for( ReleaseTicket orderTicket : set.getTickets() )
				modifyTicket( c , release , changes , set , orderTicket , false );
		}
	}
	
	public static void setDevDone( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket , Integer dev ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		
		ticket.setDevDone( dev );
		modifyTicket( c , release , changes , set , ticket , false );
	}
	
	public static void setVerified( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket , Integer qa ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		
		ticket.setVerified( qa );
		modifyTicket( c , release , changes , set , ticket , false );
	}
	
	public static void copyTicket( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket src ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		
		ReleaseTicket ticket = set.copyTicket( src );
		modifyTicket( c , release , changes , set , ticket , true );
		set.addTicket( ticket );
	}
	
	public static void moveTicket( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket , ReleaseTicketSet setNew ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );

		if( setNew.isDescoped() )
			Common.exitUnexpected();
		
		set.moveTicket( ticket , setNew );
		modifyTicket( c , release , changes , setNew , ticket , false );
	}
	
	public static void activateTicketSet( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );

		set.activate();
		modifyTicketSet( c , release , changes , set , false );
	}
	
	public static void acceptTicket( EngineMethod method , ActionBase action , Release release , ReleaseChanges changes , ReleaseTicketSet set , ReleaseTicket ticket ) throws Exception {
		method.checkUpdateRelease( release );
		DBConnection c = method.getMethodConnection( action );
		
		ticket.accept();
		modifyTicket( c , release , changes , set , ticket , false );
	}
	
}
