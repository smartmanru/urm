package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;

public class ReleaseCommandMeta extends CommandMeta {

	public static String NAME = "release";
	public static String DESC = "create, populate and manage lifecycle of releases";
	
	public ReleaseCommandMeta() {
		super( NAME , DESC );
		
		String releaseOpts = "OPT_BUILDMODE,OPT_OBSOLETE,OPT_COMPATIBILITY,OPT_CUMULATIVE";
		defineAction( CommandMethodMeta.newNormal( this , "create" , true , "create release" , releaseOpts , "./create.sh [OPTIONS] <RELEASELABEL> [<RELEASEDATE> [<LIFECYCLE>]]" ) );
		releaseOpts = "OPT_ALL,OPT_BUILDMODE,OPT_OBSOLETE,OPT_COMPATIBILITY";
		defineAction( CommandMethodMeta.newNormal( this , "modify" , true , "set release properties" , releaseOpts , "./modify.sh [OPTIONS] <RELEASELABEL> [<RELEASEDATE> [<LIFECYCLE>]]" ) );
		defineAction( CommandMethodMeta.newNormal( this , "phase" , true , "set phase properties" , releaseOpts , "./phase.sh [OPTIONS] <RELEASELABEL> {next|deadline <PHASE> <DEADLINEDATE>|days <PHASE> {any|<DURATIONDAYS>}}" ) );
		releaseOpts = "";
		defineAction( CommandMethodMeta.newCritical( this , "drop" , true , "delete release" , releaseOpts , "./drop.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newStatus( this , "status" , true , "get release status" , releaseOpts , "./status.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , "close" , true , "close release" , releaseOpts , "./close.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , "copy" , true , "copy release" , releaseOpts , "./close.sh [OPTIONS] <RELEASESRC> <RELEASEDST> <RELEASEDATE>" ) );
		defineAction( CommandMethodMeta.newNormal( this , "finish" , true , "finalize and disable distributive updates" , releaseOpts , "./finish.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , "complete" , true , "mark all release operations as completed" , releaseOpts , "./complete.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , "reopen" , true , "reopen release" , releaseOpts , "./reopen.sh [OPTIONS] <RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newCritical( this , "prod" , true , "create master distributive from predefined set" , releaseOpts , "./prod.sh [OPTIONS] {create <initial version>|copy <RELEASELABEL>|drop}" ) );
		String addOpts = "OPT_BRANCH,OPT_TAG,OPT_VERSION,OPT_REPLACE";
		defineAction( CommandMethodMeta.newNormal( this , "scope" , true , "add projects to build (except for prebuilt) and use all its binary items" , addOpts , "./scope.sh [OPTIONS] <RELEASELABEL> <set> [target1 target2 ...]" ) );
		defineAction( CommandMethodMeta.newNormal( this , "scopeitems" , true , "add specified binary items to built (if not prebuilt) and get" , addOpts , "./scopeitems.sh [OPTIONS] <RELEASELABEL> item1 [item2 ...]" ) );
		String addDbOpts = "OPT_ALL";
		defineAction( CommandMethodMeta.newNormal( this , "scopedb" , true , "add database changes to release deliveries" , addDbOpts , "./scopedb.sh [OPTIONS] <RELEASELABEL> delivery1 [delivery2 ...]" ) );
		String addConfOpts = "OPT_REPLACE";
		defineAction( CommandMethodMeta.newNormal( this , "scopeconf" , true , "add configuration items to release" , addConfOpts , "./scopeconf.sh [OPTIONS] <RELEASELABEL> [component1 component2 ...]" ) );
		String buildReleaseOpts = "OPT_DIST,OPT_GET,OPT_CHECK";
		defineAction( CommandMethodMeta.newNormal( this , "build" , true , "build release and (with -get) " , buildReleaseOpts , "./build.sh [OPTIONS] <RELEASELABEL> {all|set [projects]}" ) );
		String getReleaseOpts = "OPT_DIST,OPT_MOVE_ERRORS";
		defineAction( CommandMethodMeta.newNormal( this , "getdist" , true , "download ready and/or built release items" , getReleaseOpts , "./getdist.sh [OPTIONS] <RELEASELABEL> {all|set [projects]}" ) );
		String getDescopeOpts = "";
		defineAction( CommandMethodMeta.newNormal( this , "descope" , true , "descope release elements" , getDescopeOpts , "./descope.sh [OPTIONS] <RELEASELABEL> set [project [project items]|configuration components|database deliveries]" ) );
	}	
	
}
