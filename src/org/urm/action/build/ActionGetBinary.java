package org.urm.action.build;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.NexusDownloadInfo;
import org.urm.engine.storage.NexusStorage;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.Types.*;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaSourceProjectItem;

public class ActionGetBinary extends ActionBase {

	LocalFolder downloadFolder;
	boolean copyDist;
	Dist targetRelease;
	
	Dist useProdRelease;
	Dist useRefRelease;
	
	static String C_RELEASENEXUSGROUPID = "release";
	
	public ActionGetBinary( ActionBase action , String stream , boolean copyDist , Dist targetRelease , LocalFolder downloadFolder ) {
		super( action , stream , "Get binary files, " + 
				( ( targetRelease == null )? "default built" : "release=" + targetRelease.RELEASEDIR ) + 
				", change distr=" + copyDist );
		this.copyDist = copyDist;
		this.targetRelease = targetRelease;
		this.downloadFolder = downloadFolder;
	}

	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		if( target.isBuildableProject() )
			downloadCoreProject( target );
		else 
		if( target.isPrebuiltProject() )
			downloadPrebuiltProject( target );
		else
			this.exitUnexpectedCategory( target.CATEGORY );
		return( SCOPESTATE.RunSuccess );
	}
	
	private void downloadCoreProject( ActionScopeTarget scopeProject ) throws Exception {
		ServerProjectBuilder builder = super.getBuilder( scopeProject.sourceProject.BUILDER );
		for( ActionScopeTargetItem scopeItem : scopeProject.getItems( this ) )
			downloadBuiltItem( builder , scopeProject , scopeItem );
	}
	
	private void downloadBuiltItem( ServerProjectBuilder builder , ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
		MetaSourceProjectItem item = scopeItem.sourceItem;
		if( item.isInternal() ) {
			debug( "skip internal project item " + scopeItem.sourceItem.ITEMNAME + " ..." );
			return;
		}
		
		// get dist item details
		info( "get project item " + scopeItem.sourceItem.ITEMNAME + " ..." );

		// compare with release information
		if( builder.isTargetLocal() )
			downloadLocalItem( builder , scopeProject , scopeItem );
		else
		if( builder.isTargetNexus() ) {
			if( scopeItem.sourceItem.itemSrcType == VarITEMSRCTYPE.STATICWAR )
				downloadNexusItem( builder.TARGETNEXUS , "staticwar" , scopeProject , scopeItem );
			else
				downloadNexusItem( builder.TARGETNEXUS , "nexus" , scopeProject , scopeItem );
		}
		else
		if( builder.isTargetNuget() )
			downloadNugetItem( builder , scopeProject , scopeItem );
		else
			exitUnexpectedState();
	}

	private void downloadLocalItem( ServerProjectBuilder builder , ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
	}
	
	private void downloadNexusItem( String nexusResource , String type , ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
		String ARTEFACTID = scopeItem.sourceItem.ITEMBASENAME;
		String EXT = scopeItem.sourceItem.ITEMEXTENSION; 
		
		String CLASSIFIER = "";
		String PACKAGING = "";

		// get source item details
		String GROUPID = scopeItem.sourceItem.ITEMPATH.replace( '/' , '.' );
		
		if( EXT.isEmpty() ) {
			CLASSIFIER = "webstatic";
		}
		else if( EXT.startsWith( "-" ) ) {
			CLASSIFIER = Common.getPartBeforeFirst( EXT , "." );
			CLASSIFIER = CLASSIFIER.substring( 1 );
			PACKAGING = Common.getPartAfterFirst( EXT , "." );
		}
		else if( EXT.startsWith( "." ) )
			PACKAGING = EXT.substring( 1 );
		else
			exitUnexpectedState();

		String BUILDVERSION = scopeItem.getProjectItemBuildVersion( this );
		boolean copyDistr = context.CTX_DIST;
		if( scopeItem.sourceItem.isInternal() )
			copyDistr = false;

		if( type.equals( "staticwar" ) ) {
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , nexusResource , scopeProject.meta , downloadFolder );
			NexusDownloadInfo WAR = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "war" , "" , scopeItem.distItem );
			NexusDownloadInfo STATIC = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "tar.gz" , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , WAR.BASENAME, WAR.EXT );
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , STATIC.BASENAME, WAR.EXT );
			}
		}
		else 
		if( type.equals( "thirdparty" ) ) {
			NexusStorage nexusStorage = artefactory.getThirdpartyNexusStorage( this , nexusResource , scopeProject.meta , downloadFolder );
			NexusDownloadInfo BINARY = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , PACKAGING , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , BINARY.DOWNLOAD_FILENAME , BINARY.BASENAME, BINARY.EXT );
			}
		}
		else if( type.equals( "nexus" ) ) {
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , nexusResource , scopeProject.meta , downloadFolder );
			NexusDownloadInfo BINARY = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , PACKAGING , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , BINARY.DOWNLOAD_FILENAME , BINARY.BASENAME, BINARY.EXT );
			}
		}
	}

	private void downloadNugetItem( ServerProjectBuilder builder , ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
		String ARTEFACTID = scopeItem.sourceItem.ITEMPATH;
		String BUILDVERSION = scopeItem.getProjectItemBuildVersion( this );
		boolean copyDistr = context.CTX_DIST;
		if( scopeItem.sourceItem.isInternal() )
			copyDistr = false;

		NexusStorage nexusStorage = artefactory.getDefaultNugetStorage( this , builder.TARGETNEXUS , scopeProject.meta , downloadFolder );
		NexusDownloadInfo BINARY = nexusStorage.downloadNuget( this , ARTEFACTID , BUILDVERSION , scopeItem.distItem );

		String FILENAME = "";
		String BASENAME = "";
		String EXT = "";
		if( builder.isTargetNuget() ) {
			if( builder.TARGETNUGETPLATFORM.isEmpty() ) {
				FILENAME = BINARY.DOWNLOAD_FILENAME;
				BASENAME = BINARY.BASENAME;
				EXT = BINARY.EXT;
			}
			else {
				// repack given item
				FILENAME = nexusStorage.repackageNugetPlatform( this , BINARY , scopeItem.sourceItem );
				BASENAME = scopeItem.distItem.DISTBASENAME;
				EXT = scopeItem.sourceItem.ITEMEXTENSION;
			}
		}
		
		if( copyDistr ) {
			Dist releaseStorage = targetRelease;
			releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , FILENAME , BASENAME, EXT );
		}
	}

	private void downloadPrebuiltProject( ActionScopeTarget project ) throws Exception {
		String BUILDVERSION = project.getProjectBuildVersion( this );
		
		// get project items
		List<ActionScopeTargetItem> PROJECT_ITEMS = project.getItems( this );
		for( ActionScopeTargetItem item : PROJECT_ITEMS )
			downloadPrebuiltItem( project , item , BUILDVERSION );
	}
	
	private void downloadPrebuiltItem( ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem , String BUILDVERSION ) throws Exception {
		// compare with release information
		boolean copyDistr = context.CTX_DIST;
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , scopeProject.meta , downloadFolder );
		
		if( scopeProject.sourceProject.isPrebuiltVCS() ) {
			String ITEMPATH = scopeItem.sourceItem.ITEMPATH;
			String DISTFOLDER = scopeItem.distItem.delivery.FOLDER;
			ITEMPATH = Common.replace( ITEMPATH , "@BUILDVERSION@" , BUILDVERSION ); 
			sourceStorage.downloadThirdpartyItemFromVCS( this , ITEMPATH , DISTFOLDER );
			
			if( copyDistr ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , scopeItem.distItem.delivery.FOLDER + "/" + Common.getBaseName( ITEMPATH ) , 
						scopeItem.distItem.DISTBASENAME , scopeItem.distItem.EXT );
			}
		}
		else
		if( scopeProject.sourceProject.isPrebuiltNexus() ) {
			downloadNexusItem( scopeProject.sourceProject.RESOURCE , "thirdparty" , scopeProject , scopeItem );
		}
		else
			exitUnexpectedState();
	}

}
