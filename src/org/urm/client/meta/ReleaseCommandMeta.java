package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class ReleaseCommandMeta extends CommandMeta {

	public static String NAME = "release";
	public static String DESC = "create, populate and manage lifecycle of releases";
	
	public ReleaseCommandMeta( CommandBuilder builder ) {
		super( builder , NAME , DESC );
		
		String releaseOpts = "OPT_BUILDMODE,OPT_OBSOLETE,OPT_COMPATIBILITY,OPT_CUMULATIVE";
		defineAction( CommandMethod.newNormal( "create" , true , "create release" , releaseOpts , "./create.sh [OPTIONS] <RELEASELABEL>" ) );
		releaseOpts = "OPT_ALL,OPT_BUILDMODE,OPT_OBSOLETE,OPT_COMPATIBILITY";
		defineAction( CommandMethod.newNormal( "modify" , true , "set release properties" , releaseOpts , "./modify.sh [OPTIONS] <RELEASELABEL>" ) );
		releaseOpts = "";
		defineAction( CommandMethod.newCritical( "drop" , true , "delete release" , releaseOpts , "./drop.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newStatus( "status" , true , "get release status" , releaseOpts , "./status.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newNormal( "close" , true , "close release" , releaseOpts , "./close.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newNormal( "copy" , true , "copy release" , releaseOpts , "./close.sh [OPTIONS] <RELEASESRC> <RELEASEDST>" ) );
		defineAction( CommandMethod.newNormal( "finish" , true , "finish release" , releaseOpts , "./finish.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newNormal( "reopen" , true , "reopen release" , releaseOpts , "./reopen.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newCritical( "prod" , true , "create master distributive from predefined set" , releaseOpts , "./prod.sh [OPTIONS] create <initial version>" ) );
		String addOpts = "OPT_BRANCH,OPT_TAG,OPT_VERSION,OPT_REPLACE";
		defineAction( CommandMethod.newNormal( "scope" , true , "add projects to build (except for prebuilt) and use all its binary items" , addOpts , "./scope.sh [OPTIONS] <RELEASELABEL> <set> [target1 target2 ...]" ) );
		defineAction( CommandMethod.newNormal( "scopeitems" , true , "add specified binary items to built (if not prebuilt) and get" , addOpts , "./scopeitems.sh [OPTIONS] <RELEASELABEL> item1 [item2 ...]" ) );
		String addDbOpts = "OPT_ALL";
		defineAction( CommandMethod.newNormal( "scopedb" , true , "add database changes to release deliveries" , addDbOpts , "./scopedb.sh [OPTIONS] <RELEASELABEL> delivery1 [delivery2 ...]" ) );
		String addConfOpts = "OPT_REPLACE";
		defineAction( CommandMethod.newNormal( "scopeconf" , true , "add configuration items to release" , addConfOpts , "./scopeconf.sh [OPTIONS] <RELEASELABEL> [component1 component2 ...]" ) );
		String buildReleaseOpts = "OPT_DIST,OPT_CHECK";
		defineAction( CommandMethod.newNormal( "build" , true , "build release and (with -get) " , buildReleaseOpts , "./build.sh [OPTIONS] <RELEASELABEL> [set [projects]]" ) );
		String getReleaseOpts = "OPT_DIST,OPT_MOVE_ERRORS";
		defineAction( CommandMethod.newNormal( "getdist" , true , "download ready and/or built release items" , getReleaseOpts , "./getdist.sh [OPTIONS] <RELEASELABEL> [set [projects]]" ) );
		String getDescopeOpts = "";
		defineAction( CommandMethod.newNormal( "descope" , true , "descope release elements" , getDescopeOpts , "./descope.sh [OPTIONS] <RELEASELABEL> set [project [project items]|configuration components|database deliveries]" ) );
	}	
	
}
