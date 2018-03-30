package org.urm.db.release;

import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseScope;

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

	public static void deleteDistTarget( DBConnection c , Release release , ReleaseDistTarget target ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextReleaseVersion( release );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseDistTarget , target.ID , version );
	}
	
	public static ReleaseDistTarget createConfItemTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrConfItem item ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( item );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createBinaryItemTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrBinaryItem item ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( item );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createBinaryDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYBINARIES );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDatabaseDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYDATABASE );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createConfDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYCONFS );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDocDeliveryTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , DBEnumDistTargetType.DELIVERYDOC );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDeliverySchemaTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , schema );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

	public static ReleaseDistTarget createDeliveryDocTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( delivery , doc );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

}
