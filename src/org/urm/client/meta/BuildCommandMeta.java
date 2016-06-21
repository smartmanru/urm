package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class BuildCommandMeta extends CommandMeta {

	public static String NAME = "build";

	public BuildCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts;
		cmdOpts = "OPT_GET,OPT_CHECK,OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethod.newNormal( "buildall-tags" , false , "build from tag" , cmdOpts , "./buildall-tags.sh [OPTIONS] <TAG> [set [projects]]" ) );
		
		cmdOpts = "OPT_ALL,OPT_GET,OPT_DIST,OPT_CHECK,OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethod.newNormal( "buildall-release" , false , "build release" , cmdOpts , "./buildall-release.sh [OPTIONS] [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethod.newInfo( "checkset" , false , "check configuration variables" , "" , "./checkset.sh [OPTIONS]" ) );
		super.defineAction( CommandMethod.newNormal( "custom" , false , "run any custom operation in build scope" , cmdOpts , "./custom.sh [OPTIONS] [set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethod.newNormal( "getall" , false , "download build items" , cmdOpts , "./getall.sh [OPTIONS] [set [projects]]" ) );

		cmdOpts = "OPT_ALL,OPT_DIST,OPT_RELEASE,OPT_TAG,OPT_DATE,OPT_GROUP,OPT_VERSION";
		super.defineAction( CommandMethod.newNormal( "getall-release" , false , "download release build items" , cmdOpts , "./getall-release.sh [OPTIONS] [set [projects]]" ) );
		
		cmdOpts = "OPT_RELEASE,OPT_BRANCH,OPT_TAG,OPT_DATE,OPT_GROUP";
		super.defineAction( CommandMethod.newNormal( "codebase-checkout" , true , "checkout sources to update" , cmdOpts , "./codebase-checkout.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethod.newCritical( "codebase-commit" , true , "commit sources after updates" , cmdOpts , "./codebase-commit.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethod.newCritical( "codebase-copybranches" , true , "copy tag or branch to branch" , cmdOpts , "./codebase-copybranch.sh [OPTIONS] <SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newNormal( "codebase-copybranchtotag" , true , "copy tag or branch to branch" , cmdOpts , "./codebase-copybranch.sh [OPTIONS] <SRCBRANCH> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newNormal( "codebase-copynewtags" , true , "copy tag to tag, do not delete old tags" , cmdOpts , "./codebase-copynewtags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newNormal( "codebase-copytags" , true , "copy tag to tag, delete old tags" , cmdOpts , "./codebase-copytags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newCritical( "codebase-copytagtobranch" , true , "copy tag to new branch" , cmdOpts , "./codebase-copytagtobranch.sh [OPTIONS] <SRCTAG> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newCritical( "codebase-dropbranch" , true , "drop branches" , cmdOpts , "./codebase-dropbranch.sh [OPTIONS] <BRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newCritical( "codebase-droptags" , true , "drop tags" , cmdOpts , "./codebase-droptags.sh [OPTIONS] <TAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newNormal( "codebase-export" , true , "codebase export" , cmdOpts , "./codebase-export.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethod.newCritical( "codebase-renamebranch" , true , "rename branch" , cmdOpts , "./codebase-renamebranch.sh [OPTIONS] <SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newNormal( "codebase-renametags" , true , "rename tag" , cmdOpts , "./codebase-renametags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newCritical( "codebase-setversion" , true , "change version in pom.xml using maven" , cmdOpts , "./codebase-setversion.sh [OPTIONS] <VERSION> [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethod.newCritical( "uploaddist" , true , "upload thirdparty final binaries to nexus from release" , cmdOpts , "./uploaddist.sh [OPTIONS] <RELEASELABEL>" ) );
		super.defineAction( CommandMethod.newCritical( "uploadlib" , true , "upload thirdparty build binaries to nexus" , cmdOpts , "./uploadlib.sh <groupid> <file> [<artefactid> [<version> [classifier]]] [OPTIONS] " ) );
	}
	
}
