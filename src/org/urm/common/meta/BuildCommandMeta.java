package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;

public class BuildCommandMeta extends CommandMeta {

	public static String NAME = "build";
	public static String DESC = "operations to manage source code, build binaries and copy files to distributives";

	public BuildCommandMeta() {
		super( NAME , DESC );
		
		String cmdOpts;
		cmdOpts = "OPT_GET,OPT_CHECK,OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , "buildall-tags" , false , "build from tag" , cmdOpts , "<TAG> [set [projects]]" ) );
		
		cmdOpts = "OPT_ALL,OPT_GET,OPT_DIST,OPT_CHECK,OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , "buildall-release" , false , "build release" , cmdOpts , "[set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newInfo( this , "checkset" , false , "check configuration variables" , "" , "" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "custom" , false , "run any custom operation in build scope" , cmdOpts , "[set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , "getall" , false , "download build items" , cmdOpts , "[set [projects]]" ) );

		cmdOpts = "OPT_ALL,OPT_DIST,OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , "getall-release" , false , "download release build items" , cmdOpts , "[set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP";
		super.defineAction( CommandMethodMeta.newNormal( this , "codebase-checkout" , true , "checkout sources to update" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "codebase-commit" , true , "commit sources after updates" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "codebase-copybranches" , true , "copy tag or branch to branch" , cmdOpts , "<SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "codebase-copybranchtotag" , true , "copy tag or branch to branch" , cmdOpts , "<SRCBRANCH> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "codebase-copynewtags" , true , "copy tag to tag, do not delete old tags" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "codebase-copytags" , true , "copy tag to tag, delete old tags" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "codebase-copytagtobranch" , true , "copy tag to new branch" , cmdOpts , "<SRCTAG> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "codebase-dropbranch" , true , "drop branches" , cmdOpts , "<BRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "codebase-droptags" , true , "drop tags" , cmdOpts , "<TAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "codebase-export" , true , "codebase export" , cmdOpts , "<CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "codebase-renamebranch" , true , "rename branch" , cmdOpts , "<SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "codebase-renametags" , true , "rename tag" , cmdOpts , "<SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "codebase-setversion" , true , "change version in pom.xml using maven" , cmdOpts , "<VERSION> [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newCritical( this , "uploaddist" , true , "upload thirdparty final binaries to nexus from release" , cmdOpts , "<RELEASELABEL>" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "uploadlib" , true , "upload thirdparty build binaries to nexus" , cmdOpts , "<groupid> <file> [<artefactid> [<version> [classifier]]]" ) );
	}
	
}
