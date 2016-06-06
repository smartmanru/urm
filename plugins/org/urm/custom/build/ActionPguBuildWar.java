package org.urm.custom.build;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.custom.CommandCustom;
import org.urm.server.custom.ICustomBuild;
import org.urm.server.meta.MetaSourceProject;
import org.w3c.dom.Node;

public class ActionPguBuildWar implements ICustomBuild {
	
	String DISTLIBITEM;
	
	public void parseProject( ActionBase action , CommandCustom custom , MetaSourceProject project , Node node ) throws Exception {
		DISTLIBITEM = ConfReader.getAttrValue( action , node , "distlibitem" );
		if( DISTLIBITEM.isEmpty() )
			DISTLIBITEM = project.PROJECT + "-lib";
	}
	
	public void execute() {
		/*
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , downloadFolder );
		NexusDownloadInfo WAR = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "war" , "" , scopeItem.distItem );
		NexusDownloadInfo STATIC = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "tar.gz" , "webstatic" , scopeItem.distItem );
		nexusStorage.repackageStatic( this , scopeProject.sourceProject.PROJECT , BUILDVERSION , WAR.DOWNLOAD_FILENAME , STATIC.DOWNLOAD_FILENAME , "" , scopeItem.distItem );
		
		if( copyDistr ) {
			DistStorage releaseStorage = targetRelease;
			releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , WAR.BASENAME, WAR.EXT );
			releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , STATIC.BASENAME, WAR.EXT );
		}
		*/
	}

	public void downloadCustomProject( ActionScopeTarget scopeProject ) throws Exception {
		/*
		MetaDistrBinaryItem distItem = scopeProject.sourceProject.distItem;

		// get dist item details
		boolean ISOBSOLETE = distItem.OBSOLETE;

		// compare with release information
		if( scope.release != null && scope.release.info.PROPERTY_OBSOLETE == false && ISOBSOLETE == true )
			return;
		
		String BUILDVERSION = scopeProject.getProjectBuildVersion( this );
		
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , downloadFolder );
		LocalFolder artefactoryFolder = nexusStorage.artefactoryFolder;
		
		NexusDownloadInfo INFO = nexusStorage.downloadNexus( this , C_RELEASENEXUSGROUPID , scopeProject.sourceProject.PROJECT , BUILDVERSION , "txt" , "version" , scopeProject.sourceProject.distItem );
		String VERSION_TAGNAME = artefactoryFolder.readFile( this , INFO.DOWNLOAD_FILENAME ); 
		
		SpecificPGU pgu = new SpecificPGU( this , downloadFolder );
		boolean copyDistr = options.OPT_DIST;
		pgu.downloadWarCopyDistr( copyDistr , scope.release , VERSION_TAGNAME , scopeProject );
		*/
	}

	public void downloadAllWarApp() throws Exception {
		/*
		// handle war/ear setting
		SpecificPGU pgu = new SpecificPGU( this , downloadFolder ); 
		boolean USE_PROD_DISTR = true;
		if( options.OPT_ALL || context.buildMode != VarBUILDMODE.BRANCH )
			USE_PROD_DISTR = false;

		String VERSION = options.OPT_VERSION;
		if( VERSION.isEmpty() )
			VERSION = scope.meta.product.CONFIG_APPVERSION;
		
		if( VERSION.isEmpty() )
			VERSION = useRefRelease.info.RELEASEVER; 

		boolean copyDistr = options.OPT_DIST;
		DistStorage srcRelease = ( USE_PROD_DISTR )? useProdRelease : null;
		pgu.getAllWarApp( copyDistr , scope.release , USE_PROD_DISTR , VERSION , srcRelease , scope );
		*/
	}

}
