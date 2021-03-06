package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;
import org.urm.common.action.CommandMethodMeta.ACTION_ACCESS;
import org.urm.common.action.CommandMethodMeta.SecurityAction;

public class ReleaseCommandMeta extends CommandMeta {

	public static String METHOD_CREATE = "create";
	public static String METHOD_MODIFY = "modify";
	public static String METHOD_PHASE = "phase";
	public static String METHOD_DROP = "drop";
	public static String METHOD_STATUS = "status";
	public static String METHOD_CLEANUP = "cleanup";
	public static String METHOD_COPY = "copy";
	public static String METHOD_IMPORT = "import";
	public static String METHOD_FINISH = "finish";
	public static String METHOD_COMPLETE = "complete";
	public static String METHOD_REOPEN = "reopen";
	public static String METHOD_MASTER = "master";
	public static String METHOD_ARCHIVE = "archive";
	public static String METHOD_TOUCH = "touch";
	public static String METHOD_SCOPEADD = "scopeadd";
	public static String METHOD_SCOPESPEC = "scopespec";
	public static String METHOD_SCOPEITEMS = "scopeitems";
	public static String METHOD_SCOPEDB = "scopedb";
	public static String METHOD_SCOPECONF = "scopeconf";
	public static String METHOD_BUILD = "build";
	public static String METHOD_GETDIST = "getdist";
	public static String METHOD_DESCOPE = "descope";
	public static String METHOD_SCHEDULE = "schedule";
	public static String METHOD_SCOPESET = "scopeset";
	public static String METHOD_TICKETS = "tickets";
	
	public static String NAME = "release";
	public static String DESC = "create, populate and manage lifecycle of releases";
	
	public ReleaseCommandMeta( OptionsMeta options ) {
		super( options , NAME , DESC );
		
		String releaseOpts = "OPT_BUILDMODE,OPT_COMPATIBILITY,OPT_CUMULATIVE";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_CREATE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "create release" , releaseOpts , "<RELEASELABEL> [<RELEASEDATE> [<LIFECYCLE>]]" ) );
		releaseOpts = "OPT_BUILDMODE,OPT_COMPATIBILITY";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_MODIFY , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "set release properties" , releaseOpts , "<RELEASELABEL> [<RELEASEDATE> [<LIFECYCLE>]]" ) );
		releaseOpts = "";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_PHASE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "set phase properties" , releaseOpts , "<RELEASELABEL> {next|deadline <PHASE> <DEADLINEDATE>|days <PHASE> <DURATIONDAYS>}" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_SCHEDULE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "schedule all phases" , releaseOpts , "<RELEASELABEL> {<phase1 date start> <phase1 date finish> <phase2 date start> ..." ) );
		defineAction( CommandMethodMeta.newCritical( this , METHOD_DROP , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "delete release" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newStatus( this , METHOD_STATUS , ACTION_ACCESS.PRODUCT , true , SecurityAction.ACTION_RELEASE , true , "get release status" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_CLEANUP , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "close release after failure" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_COPY , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "copy release" , releaseOpts , "<RELEASESRC> <RELEASEDST> <RELEASEDATE>" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_IMPORT , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "import release" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_FINISH , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "finalize and disable distributive updates" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_COMPLETE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "mark all release operations as completed" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_REOPEN , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "reopen release" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newCritical( this , METHOD_MASTER , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "master distributive operations" , releaseOpts , "{create <initial version>|copy <RELEASELABEL>|add <RELEASELABEL>|status|drop}" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_ARCHIVE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "archive release" , releaseOpts , "<RELEASELABEL>" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_TOUCH , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "reload release" , releaseOpts , "<RELEASELABEL>" ) );
		String addOpts = "OPT_BRANCH,OPT_TAG,OPT_VERSION,OPT_REPLACE";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_SCOPEADD , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "add projects to build (except for prebuilt) and use all its binary items" , addOpts , "<RELEASELABEL> {all|<set> {all|target1 target2 ...}}" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_SCOPESPEC , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "change source project specific attributes" , addOpts , "<RELEASELABEL> {all|<set> {all|target1 target2 ...}}" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_SCOPEITEMS , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "add specified binary items to built (if not prebuilt) and get" , addOpts , "<RELEASELABEL> item1 [item2 ...]" ) );
		String addDbOpts = "";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_SCOPEDB , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "add database changes to release deliveries" , addDbOpts , "<RELEASELABEL> delivery1 [delivery2 ...]" ) );
		String addConfOpts = "OPT_REPLACE";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_SCOPECONF , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "add configuration items to release" , addConfOpts , "<RELEASELABEL> [component1 component2 ...]" ) );
		String buildReleaseOpts = "OPT_DIST,OPT_GET,OPT_CHECK";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_BUILD , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "build release and (with -dist) get files into distributive" , buildReleaseOpts , "<RELEASELABEL> {all|set [projects]}" ) );
		String getReleaseOpts = "OPT_DIST,OPT_DBMOVE";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_GETDIST , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "download ready and/or built release items" , getReleaseOpts , "<RELEASELABEL> {all|set [projects]}" ) );
		String getDescopeOpts = "";
		defineAction( CommandMethodMeta.newNormal( this , METHOD_DESCOPE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "descope release elements" , getDescopeOpts , "<RELEASELABEL> set [project [project items]|configuration components|database deliveries]" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_SCOPESET , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "set scope elements by path list" , getDescopeOpts , "<RELEASELABEL> {source {set[/project[/item]] ...}|delivery {delivery[/item]} ...}" ) );
		defineAction( CommandMethodMeta.newNormal( this , METHOD_TICKETS , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_RELEASE , true , "change release tickets" , getDescopeOpts , "<RELEASELABEL> {createset|modifyset|dropset|acceptset|createticket|modifyticket|moveticket|copyticket|dropticket|setdevdone|createtarget|droptarget} <args>" ) );
	}	
	
}
