package org.urm.db.release;

import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.meta.product.MetaDistrConfItem;
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

	public static ReleaseDistTarget createConfItemTarget( DBConnection c , Release release , ReleaseScope scope , MetaDistrConfItem item ) throws Exception {
		ReleaseDistTarget target = new ReleaseDistTarget( scope );
		target.create( item );
		DBReleaseDistTarget.modifyReleaseDistTarget( c , release , target , true );
		return( target );
	}

}
