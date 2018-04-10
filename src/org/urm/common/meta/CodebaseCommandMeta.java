package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMethodMeta.ACTION_ACCESS;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;
import org.urm.common.action.CommandMethodMeta.SecurityAction;

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
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_BUILDTAGS , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , false , "build from tag" , cmdOpts , "<TAG> [set [projects]]" ) );
		
		cmdOpts = "OPT_GET,OPT_DIST,OPT_CHECK,OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_BUILDRELEASE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , false , "build release" , cmdOpts , "<RELEASELABEL> [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newInfo( this , METHOD_CHECKSET , ACTION_ACCESS.PRODUCT , true , SecurityAction.ACTION_CONFIGURE , false , "check configuration variables" , "" , "" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_CUSTOM , ACTION_ACCESS.PRODUCT , true , SecurityAction.ACTION_CONFIGURE , false , "run any custom operation in build scope" , cmdOpts , "[set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_GETBUILD , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , false , "download build items" , cmdOpts , "[set [projects]]" ) );

		cmdOpts = "OPT_DIST,OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_GETRELEASE , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , false , "download release build items" , cmdOpts , "<RELEASELABEL> [set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_CHECKOUT , ACTION_ACCESS.PRODUCT , true , SecurityAction.ACTION_CODEBASE , true , "checkout sources to update" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_COMMIT , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "commit sources after updates" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_COPYBRANCHES , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "copy tag or branch to branch" , cmdOpts , "<SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_COPYBRANCHTOTAG , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "copy tag or branch to branch" , cmdOpts , "<SRCBRANCH> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_COPYNEWTAGS , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "copy tag to tag, do not delete old tags" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_COPYTAGS , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "copy tag to tag, delete old tags" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_COPYTAGTOBRANCH , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "copy tag to new branch" , cmdOpts , "<SRCTAG> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DROPBRANCH , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "drop branches" , cmdOpts , "<BRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DROPTAGS , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "drop tags" , cmdOpts , "<TAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_EXPORT , ACTION_ACCESS.PRODUCT , true , SecurityAction.ACTION_CODEBASE , true , "codebase export" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_RENAMEBRANCH , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "rename branch" , cmdOpts , "<SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_RENAMETAGS , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "rename tag" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_SETVERSION , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "change version in pom.xml using maven" , cmdOpts , "<VERSION> [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_UPLOADDIST , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "upload thirdparty final binaries to nexus from release" , cmdOpts , "<RELEASELABEL>" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_UPLOADLIB , ACTION_ACCESS.PRODUCT , false , SecurityAction.ACTION_CODEBASE , true , "upload thirdparty build binaries to nexus" , cmdOpts , "<groupid> <file> [<artefactid> [<version> [classifier]]]" ) );
	}
	
}
