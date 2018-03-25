package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;

public class DBReleaseDist {

	public static void exportxml( EngineLoader loader , ReleaseDist releaseDist , String filePath ) {
	}
	
	public static ReleaseDist createReleaseDist( EngineMethod method , ActionBase action , Release release , String variant ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}
	
}
