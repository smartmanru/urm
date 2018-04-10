package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.dist.DistItemInfo;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseDistItem;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBReleaseDistTarget {

	public static void modifyReleaseDistTarget( DBConnection c , Release release , ReleaseDistTarget target , boolean insert ) throws Exception {
		if( insert )
			target.ID = c.getNextSequenceValue();
		
		target.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseDistTarget , target.ID , target.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getBoolean( target.isScopeTarget() ) ,
				EngineDB.getEnum( target.TYPE ) ,
				EngineDB.getBoolean( target.ALL ) ,
				EngineDB.getMatchId( target.DELIVERY ) ,
				EngineDB.getMatchName( target.DELIVERY ) ,
				EngineDB.getMatchId( target.BINARY ) ,
				EngineDB.getMatchName( target.BINARY ) ,
				EngineDB.getMatchId( target.CONF ) ,
				EngineDB.getMatchName( target.CONF ) ,
				EngineDB.getMatchId( target.SCHEMA ) ,
				EngineDB.getMatchName( target.SCHEMA ) ,
				EngineDB.getMatchId( target.DOC ) ,
				EngineDB.getMatchName( target.DOC ) ,
				} , insert );
	}

	public static void modifyReleaseDistItem( DBConnection c , Release release , ReleaseDistItem item , boolean insert ) throws Exception {
		if( insert )
			item.ID = c.getNextSequenceValue();
		
		item.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseDistItem , item.ID , item.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getObject( item.releaseDist.ID ) ,
				EngineDB.getObject( item.DISTTARGET_ID ) ,
				EngineDB.getString( item.TARGETFILE ) ,
				EngineDB.getString( item.TARGETFILE_FOLDER ) ,
				EngineDB.getString( item.TARGETFILE_HASH ) ,
				EngineDB.getLong( item.TARGETFILE_SIZE ) ,
				EngineDB.getDate( item.TARGETFILE_TIME ) ,
				EngineDB.getString( item.SOURCE_RELEASEDIR ) ,
				EngineDB.getString( item.SOURCE_RELEASETIME )
				} , insert );
	}

	public static void loaddbReleaseDistTarget( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseScope scope , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDistTarget;

		ReleaseDistTarget target = null;
		boolean scopetarget = entity.loaddbBoolean( rs , ReleaseBuildTarget.PROPERTY_SCOPETARGET );
		if( scopetarget )
			target = new ReleaseDistTarget( scope );
		else
			target = new ReleaseDistTarget( changes );
		
		target.ID = entity.loaddbId( rs );
		target.RV = entity.loaddbVersion( rs );
		target.create(
				DBEnumDistTargetType.getValue( entity.loaddbEnum( rs , ReleaseDistTarget.PROPERTY_TARGETTYPE ) , true ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_DISTTARGET_DELIVERY_ID , ReleaseDistTarget.PROPERTY_DELIVERY ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_DISTTARGET_BINARY_ID , ReleaseDistTarget.PROPERTY_BINARY ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_DISTTARGET_CONF_ID , ReleaseDistTarget.PROPERTY_CONF ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_DISTTARGET_SCHEMA_ID , ReleaseDistTarget.PROPERTY_SCHEMA ) ,
				entity.loaddbMatchItem( rs , DBReleaseData.FIELD_DISTTARGET_DOC_ID , ReleaseDistTarget.PROPERTY_DOC ) ,
				entity.loaddbBoolean( rs , ReleaseDistTarget.PROPERTY_ALL )
				);
		
		if( scopetarget )
			scope.addDistTarget( target );
		else
			changes.addDistTarget( target );
	}
	
	public static void exportxmlDistTarget( EngineLoader loader , Release release , ReleaseDistTarget target , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDistTarget;
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		MetaDatabase db = meta.getDatabase();
		MetaDocs docs = meta.getDocs();
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlEnum( target.TYPE ) ,
				entity.exportxmlBoolean( target.ALL ) ,
				entity.exportxmlString( distr.getDeliveryName( target.DELIVERY ) ) ,
				entity.exportxmlString( distr.getBinaryItemName( target.BINARY ) ) ,
				entity.exportxmlString( distr.getConfItemName( target.CONF ) ) ,
				entity.exportxmlString( db.getSchemaName( target.SCHEMA ) ) ,
				entity.exportxmlString( docs.getDocName( target.DOC ) ) ,
		} , true );
	}
	
	public static ReleaseDistTarget importxmlDistTarget( EngineLoader loader , Release release , ReleaseChanges changes , ReleaseScope scope , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDistTarget;
		
		ReleaseDistTarget distTarget = ( changes != null )? new ReleaseDistTarget( changes ) : new ReleaseDistTarget( scope );
		
		Meta meta = release.getMeta();
		MetaDistr distr = meta.getDistr();
		MetaDatabase db = meta.getDatabase();
		MetaDocs docs = meta.getDocs();
		distTarget.create(
				DBEnumDistTargetType.getValue( entity.importxmlEnumAttr( root , ReleaseDistTarget.PROPERTY_TARGETTYPE ) , true ) ,
				distr.getDeliveryMatchItem( null , entity.importxmlStringAttr( root , ReleaseDistTarget.PROPERTY_DELIVERY ) ) ,
				distr.getBinaryMatchItem( null , entity.importxmlStringAttr( root , ReleaseDistTarget.PROPERTY_BINARY ) ) ,
				distr.getConfMatchItem( null , entity.importxmlStringAttr( root , ReleaseDistTarget.PROPERTY_CONF ) ) ,
				db.getSchemaMatchItem( null , entity.importxmlStringAttr( root , ReleaseDistTarget.PROPERTY_SCHEMA ) ) ,
				docs.getDocMatchItem( null , entity.importxmlStringAttr( root , ReleaseDistTarget.PROPERTY_DOC ) ) ,
				entity.importxmlBooleanAttr( root , ReleaseDistTarget.PROPERTY_ALL , false )
				);
		modifyReleaseDistTarget( c , release , distTarget , true );
		return( distTarget );
	}
	
	public static void deleteDistTarget( DBConnection c , Release release , ReleaseDistTarget target ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextReleaseVersion( release );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseDistTarget , target.ID , version );
	}
	
	public static ReleaseDistTarget createConfItemTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrConfItem item ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( item );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createBinaryItemTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrBinaryItem item ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( item );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createBinaryDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYBINARIES );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDatabaseDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYDATABASE );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createConfDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYCONFS );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDocDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYDOC );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDeliverySchemaTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , schema );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDeliveryDocTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , doc );
		modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static void dropAllDistItems( DBConnection c , Release release ) throws Exception {
		EngineEntities entities = c.getEntities();
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleaseDistTarget , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
	}

	public static ReleaseDistItem createDistItem( EngineMethod method , ActionBase action , Release release , ReleaseDistTarget target , ReleaseDist releaseDist , DistItemInfo info ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		ReleaseDistItem item = new ReleaseDistItem( release , releaseDist );
		item.create( target , info );
		modifyReleaseDistItem( c , release , item , true );
		return( item );
	}
	
}
