package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class BuildCommandMeta extends CommandMeta {

	public static String NAME = "build";

	public BuildCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts;
		cmdOpts = "GETOPT_GET,GETOPT_CHECK,GETOPT_RELEASE,GETOPT_BRANCH,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandMethod.newAction( "buildall-tags" , false , "build from tag" , cmdOpts , "./buildall-tags.sh [OPTIONS] <TAG> [set [projects]]" ) );
		
		cmdOpts = "GETOPT_ALL,GETOPT_GET,GETOPT_DIST,GETOPT_CHECK,GETOPT_RELEASE,GETOPT_BRANCH,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandMethod.newAction( "buildall-release" , false , "build release" , cmdOpts , "./buildall-release.sh [OPTIONS] [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethod.newAction( "checkset" , false , "check configuration variables" , "" , "./checkset.sh [OPTIONS]" ) );
		super.defineAction( CommandMethod.newAction( "custom" , false , "run any custom operation in build scope" , cmdOpts , "./custom.sh [OPTIONS] [set [projects]]" ) );
		
		cmdOpts = "GETOPT_RELEASE,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandMethod.newAction( "getall" , false , "download build items" , cmdOpts , "./getall.sh [OPTIONS] [set [projects]]" ) );

		cmdOpts = "GETOPT_ALL,GETOPT_DIST,GETOPT_RELEASE,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP,GETOPT_VERSION";
		super.defineAction( CommandMethod.newAction( "getall-release" , false , "download release build items" , cmdOpts , "./getall-release.sh [OPTIONS] [set [projects]]" ) );
		
		cmdOpts = "GETOPT_RELEASE,GETOPT_BRANCH,GETOPT_TAG,GETOPT_DATE,GETOPT_GROUP";
		super.defineAction( CommandMethod.newAction( "codebase-checkout" , true , "checkout sources to update" , cmdOpts , "./codebase-checkout.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-commit" , true , "commit sources after updates" , cmdOpts , "./codebase-commit.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-copybranches" , true , "copy tag or branch to branch" , cmdOpts , "./codebase-copybranch.sh [OPTIONS] <SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-copybranchtotag" , true , "copy tag or branch to branch" , cmdOpts , "./codebase-copybranch.sh [OPTIONS] <SRCBRANCH> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-copynewtags" , true , "copy tag to tag, do not delete old tags" , cmdOpts , "./codebase-copynewtags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-copytags" , true , "copy tag to tag, delete old tags" , cmdOpts , "./codebase-copytags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-copytagtobranch" , true , "copy tag to new branch" , cmdOpts , "./codebase-copytagtobranch.sh [OPTIONS] <SRCTAG> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-dropbranch" , true , "drop branches" , cmdOpts , "./codebase-dropbranch.sh [OPTIONS] <BRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-droptags" , true , "drop tags" , cmdOpts , "./codebase-droptags.sh [OPTIONS] <TAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-export" , true , "codebase export" , cmdOpts , "./codebase-export.sh [OPTIONS] <CODIR> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-renamebranch" , true , "rename branch" , cmdOpts , "./codebase-renamebranch.sh [OPTIONS] <SRCBRANCH> <DSTBRANCH> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-renametags" , true , "rename tag" , cmdOpts , "./codebase-renametags.sh [OPTIONS] <SRCTAG> <DSTTAG> [set [projects]]" ) );
		super.defineAction( CommandMethod.newAction( "codebase-setversion" , true , "change version in pom.xml using maven" , cmdOpts , "./codebase-setversion.sh [OPTIONS] <VERSION> [set [projects]]" ) );

		cmdOpts = "";
		super.defineAction( CommandMethod.newAction( "uploaddist" , true , "upload thirdparty final binaries to nexus from release" , cmdOpts , "./uploaddist.sh [OPTIONS] <RELEASELABEL>" ) );
		super.defineAction( CommandMethod.newAction( "uploadlib" , true , "upload thirdparty build binaries to nexus" , cmdOpts , "./uploadlib.sh <groupid> <file> [<artefactid> [<version> [classifier]]] [OPTIONS] " ) );
	}
	
}
