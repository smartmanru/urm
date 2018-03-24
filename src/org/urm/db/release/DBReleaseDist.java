package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.db.DBConnection;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;

public class DBReleaseDist {

	public static void exportxml( EngineLoader loader , ReleaseDist releaseDist , String filePath ) {
	}
	
	public static ReleaseDist createReleaseDist( EngineMethod method , ActionBase action , Release release , String variant ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		ReleaseDist releaseDist = new ReleaseDist( meta , repo );
		release.createNormal( action , RELEASEVER , releaseDate , lc );
		
		modifyRelease( c , repo , release , true );
		repo.addRelease( release );
		
		return( release );
	}
	
}
