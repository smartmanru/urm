package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseMaster;

public class DBReleaseDist {

	public static void exportxml( EngineLoader loader , ReleaseDist releaseDist , String filePath ) {
	}
	
	public static void exportxml( EngineLoader loader , ReleaseMaster releaseMaster , String filePath ) {
	}

	public static ReleaseDist createReleaseDist( EngineMethod method , ActionBase action , Release release , String variant ) throws Exception {
		return( null );
	}
	
}
