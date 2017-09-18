package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;

public class CodebaseCommandMeta extends CommandMeta {

	public static String METHOD_BUILDTAGS = "buildtags";
	public static String METHOD_BUILDRELEASE = "buildrelease";
	public static String METHOD_CHECKSET = "checkset";
	public static String METHOD_CUSTOM = "custom";
	public static String METHOD_GETBUILD = "getbuild";
	public static String METHOD_GETRELEASE = "getrelease";
	public static String METHOD_CHECKOUT = "checkout";
	public static String METHOD_COMMIT = "commit";
	public static String METHOD_COPYBRANCHES = "copybranches";
	public static String METHOD_COPYBRANCHTOTAG = "copybranchtotag";
	public static String METHOD_COPYNEWTAGS = "copynewtags";
	public static String METHOD_COPYTAGS = "copytags";
	public static String METHOD_COPYTAGTOBRANCH = "copytagtobranch";
	public static String METHOD_DROPBRANCH = "dropbranch";
	public static String METHOD_DROPTAGS = "droptags";
	public static String METHOD_EXPORT = "export";
	public static String METHOD_RENAMEBRANCH = "renamebranch";
	public static String METHOD_RENAMETAGS = "renametags";
	public static String METHOD_SETVERSION = "setversion";
	public static String METHOD_UPLOADDIST = "uploaddist";
	public static String METHOD_UPLOADLIB = "uploadlib";

	public static String NAME = "codebase";
	public static String DESC = "operations to manage source code, build binaries and copy files to distributives";
	
	public CodebaseCommandMeta( OptionsMeta options ) {
		super( options , NAME , DESC );
		
		String cmdOpts;
		cmdOpts = "OPT_GET,OPT_CHECK,OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_BUILDTAGS , false , "build from tag" , cmdOpts , "<TAG> [set [projects]]" ) );
		
		cmdOpts = "OPT_GET,OPT_DIST,OPT_CHECK,OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_BUILDRELEASE , false , "build release" , cmdOpts , "[set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newInfo( this , METHOD_CHECKSET , false , "check configuration variables" , "" , "" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_CUSTOM , false , "run any custom operation in build scope" , cmdOpts , "[set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_GETBUILD , false , "download build items" , cmdOpts , "[set [projects]]" ) );

		cmdOpts = "OPT_DIST,OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_GETRELEASE , false , "download release build items" , cmdOpts , "[set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_CHECKOUT , true , "checkout sources to update" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_COMMIT , true , "commit sources after updates" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_COPYBRANCHES , true , "copy tag or branch to branch" , cmdOpts , "<SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_COPYBRANCHTOTAG , true , "copy tag or branch to branch" , cmdOpts , "<SRCBRANCH> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_COPYNEWTAGS , true , "copy tag to tag, do not delete old tags" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_COPYTAGS , true , "copy tag to tag, delete old tags" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_COPYTAGTOBRANCH , true , "copy tag to new branch" , cmdOpts , "<SRCTAG> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DROPBRANCH , true , "drop branches" , cmdOpts , "<BRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DROPTAGS , true , "drop tags" , cmdOpts , "<TAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_EXPORT , true , "codebase export" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_RENAMEBRANCH , true , "rename branch" , cmdOpts , "<SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_RENAMETAGS , true , "rename tag" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_SETVERSION , true , "change version in pom.xml using maven" , cmdOpts , "<VERSION> [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_UPLOADDIST , true , "upload thirdparty final binaries to nexus from release" , cmdOpts , "<RELEASELABEL>" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_UPLOADLIB , true , "upload thirdparty build binaries to nexus" , cmdOpts , "<groupid> <file> [<artefactid> [<version> [classifier]]]" ) );
	}
	
}
