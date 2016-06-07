package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class ReleaseCommandMeta extends CommandMeta {

	public static String NAME = "release";
	
	public ReleaseCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String releaseOpts = "GETOPT_BUILDMODE,GETOPT_OBSOLETE,GETOPT_COMPATIBILITY,GETOPT_CUMULATIVE";
		defineAction( CommandMethod.newAction( "create" , true , "create release" , releaseOpts , "./create.sh [OPTIONS] <RELEASELABEL>" ) );
		releaseOpts = "GETOPT_ALL,GETOPT_BUILDMODE,GETOPT_OBSOLETE,GETOPT_COMPATIBILITY";
		defineAction( CommandMethod.newAction( "modify" , true , "set release properties" , releaseOpts , "./modify.sh [OPTIONS] <RELEASELABEL>" ) );
		releaseOpts = "";
		defineAction( CommandMethod.newAction( "drop" , true , "delete release" , releaseOpts , "./drop.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newAction( "status" , true , "get release status" , releaseOpts , "./status.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newAction( "close" , true , "close release" , releaseOpts , "./close.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newAction( "copy" , true , "copy release" , releaseOpts , "./close.sh [OPTIONS] <RELEASESRC> <RELEASEDST>" ) );
		defineAction( CommandMethod.newAction( "finish" , true , "finish release" , releaseOpts , "./finish.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newAction( "reopen" , true , "reopen release" , releaseOpts , "./reopen.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethod.newAction( "prod" , true , "create master distributive from predefined set" , releaseOpts , "./prod.sh [OPTIONS] create <initial version>" ) );
		String addOpts = "GETOPT_BRANCH,GETOPT_TAG,GETOPT_VERSION,GETOPT_REPLACE";
		defineAction( CommandMethod.newAction( "scope" , true , "add projects to build (except for prebuilt) and use all its binary items" , addOpts , "./scope.sh [OPTIONS] <RELEASELABEL> <set> [target1 target2 ...]" ) );
		defineAction( CommandMethod.newAction( "scopeitems" , true , "add specified binary items to built (if not prebuilt) and get" , addOpts , "./scopeitems.sh [OPTIONS] <RELEASELABEL> item1 [item2 ...]" ) );
		String addDbOpts = "GETOPT_ALL";
		defineAction( CommandMethod.newAction( "scopedb" , true , "add database changes to release deliveries" , addDbOpts , "./scopedb.sh [OPTIONS] <RELEASELABEL> delivery1 [delivery2 ...]" ) );
		String addConfOpts = "GETOPT_REPLACE";
		defineAction( CommandMethod.newAction( "scopeconf" , true , "add configuration items to release" , addConfOpts , "./scopeconf.sh [OPTIONS] <RELEASELABEL> [component1 component2 ...]" ) );
		String buildReleaseOpts = "GETOPT_DIST,GETOPT_CHECK";
		defineAction( CommandMethod.newAction( "build" , true , "build release and (with -get) " , buildReleaseOpts , "./build.sh [OPTIONS] <RELEASELABEL> [set [projects]]" ) );
		String getReleaseOpts = "GETOPT_DIST,GETOPT_MOVE_ERRORS";
		defineAction( CommandMethod.newAction( "getdist" , true , "download ready and/or built release items" , getReleaseOpts , "./getdist.sh [OPTIONS] <RELEASELABEL> [set [projects]]" ) );
		String getDescopeOpts = "";
		defineAction( CommandMethod.newAction( "descope" , true , "descope release elements" , getDescopeOpts , "./descope.sh [OPTIONS] <RELEASELABEL> set [project [project items]|configuration components|database deliveries]" ) );
	}	
	
}
