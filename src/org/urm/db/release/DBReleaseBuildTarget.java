package org.urm.db.release;

import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseScope;

public class DBReleaseBuildTarget {

	public static void modifyReleaseBuildTarget( DBConnection c , Release release , ReleaseBuildTarget target , boolean insert ) throws Exception {
		if( insert )
			target.ID = c.getNextSequenceValue();
		
		target.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseBuildTarget , target.ID , target.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getBoolean( target.isScopeTarget() ) ,
				EngineDB.getEnum( target.TYPE ) ,
				EngineDB.getBoolean( target.ALL ) ,
				EngineDB.getMatchId( target.SRCSET ) ,
				EngineDB.getMatchName( target.SRCSET ) ,
				EngineDB.getMatchId( target.PROJECT ) ,
				EngineDB.getMatchName( target.PROJECT ) ,
				EngineDB.getString( target.BUILD_BRANCH ) ,
				EngineDB.getString( target.BUILD_TAG ) ,
				EngineDB.getString( target.BUILD_VERSION )
				} , insert );
	}

	public static void deleteBuildTarget( DBConnection c , Release release , ReleaseBuildTarget target ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextReleaseVersion( release );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseBuildTarget , target.ID , version );
	}
	
	public static ReleaseBuildTarget createSourceTarget( DBConnection c , Release release , ReleaseScope scope , boolean all ) throws Exception {
		ReleaseBuildTarget target = new ReleaseBuildTarget( scope );
		target.create( all );
		DBReleaseBuildTarget.modifyReleaseBuildTarget( c , release , target , true );
		return( target );
	}
	
	public static ReleaseBuildTarget createSourceSetTarget( DBConnection c , Release release , ReleaseScope scope , MetaSourceProjectSet set , boolean all ) throws Exception {
		ReleaseBuildTarget target = new ReleaseBuildTarget( scope );
		target.create( set , all );
		DBReleaseBuildTarget.modifyReleaseBuildTarget( c , release , target , true );
		return( target );
	}
	
	public static ReleaseBuildTarget createSourceProjectTarget( DBConnection c , Release release , ReleaseScope scope , MetaSourceProject project , boolean all ) throws Exception {
		ReleaseBuildTarget target = new ReleaseBuildTarget( scope );
		target.create( project , all );
		DBReleaseBuildTarget.modifyReleaseBuildTarget( c , release , target , true );
		return( target );
	}

}
