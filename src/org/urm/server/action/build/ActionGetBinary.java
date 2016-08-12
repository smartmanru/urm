package org.urm.server.action.build;

import java.util.List;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.dist.Dist;
import org.urm.server.meta.MetaDistrBinaryItem;
import org.urm.server.meta.Meta.VarCATEGORY;
import org.urm.server.meta.Meta.VarITEMSRCTYPE;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.NexusDownloadInfo;
import org.urm.server.storage.NexusStorage;
import org.urm.server.storage.SourceStorage;

public class ActionGetBinary extends ActionBase {

	LocalFolder downloadFolder;
	boolean copyDist;
	Dist targetRelease;
	
	Dist useProdRelease;
	Dist useRefRelease;
	
	static String C_RELEASENEXUSGROUPID = "release";
	
	public ActionGetBinary( ActionBase action , String stream , boolean copyDist , Dist targetRelease , LocalFolder downloadFolder ) {
		super( action , stream );
		this.copyDist = copyDist;
		this.targetRelease = targetRelease;
		this.downloadFolder = downloadFolder;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		if( target.CATEGORY == VarCATEGORY.BUILD )
			downloadCoreProject( target );
		else 
		if( target.CATEGORY == VarCATEGORY.PREBUILT )
			downloadPrebuiltProject( target );
		else
			this.exitUnexpectedCategory( target.CATEGORY );
		return( true );
	}
	
	private void downloadCoreProject( ActionScopeTarget scopeProject ) throws Exception {
		for( ActionScopeTargetItem scopeItem : scopeProject.getItems( this ) )
			downloadBuiltItem( scopeProject , scopeItem );
	}
	
	private void downloadBuiltItem( ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
		// get dist item details
		MetaDistrBinaryItem distItem = scopeItem.distItem;
		info( "get binary item " + distItem.KEY + " ..." );

		// compare with release information
		if( scopeItem.sourceItem.isStoredInNexus( this ) ) {
			if( scopeItem.sourceItem.ITEMSRCTYPE == VarITEMSRCTYPE.STATICWAR )
				downloadNexusItem( "staticwar" , scopeProject , scopeItem );
			else
				downloadNexusItem( "nexus" , scopeProject , scopeItem );
		}
		else
		if( scopeItem.sourceItem.isStoredInNuget( this ) )
			downloadNugetItem( scopeProject , scopeItem );
		else
			exit( "unexpected ITEMTYPE=" + Common.getEnumLower( scopeItem.sourceItem.ITEMSRCTYPE ) );
	}
	
	private void downloadNexusItem( String type , ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
		String ARTEFACTID = scopeItem.sourceItem.ITEMBASENAME;
		String EXT = scopeItem.sourceItem.ITEMEXTENSION; 
		
		String CLASSIFIER = "";
		String PACKAGING = "";

		// get source item details
		String GROUPID = scopeItem.sourceItem.NEXUS_ITEMPATH.replace( '/' , '.' );
		
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
			exit( "unexpected extension=" + EXT );

		String BUILDVERSION = scopeItem.getProjectItemBuildVersion( this );
		boolean copyDistr = context.CTX_DIST;
		if( scopeItem.sourceItem.INTERNAL )
			copyDistr = false;

		if( type.equals( "war" ) ) {
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , downloadFolder );
			NexusDownloadInfo WAR = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "war" , "" , scopeItem.distItem );
			NexusDownloadInfo STATIC = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "tar.gz" , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr && scopeItem.sourceItem.ITEMSRCTYPE == VarITEMSRCTYPE.NEXUS ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , WAR.BASENAME, WAR.EXT );
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , STATIC.BASENAME, WAR.EXT );
			}
		}
		else 
		if( type.equals( "thirdparty" ) ) {
			NexusStorage nexusStorage = artefactory.getThirdpartyNexusStorage( this , downloadFolder );
			NexusDownloadInfo BINARY = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , PACKAGING , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , BINARY.DOWNLOAD_FILENAME , BINARY.BASENAME, BINARY.EXT );
			}
		}
		else if( type.equals( "nexus" ) ) {
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , downloadFolder );
			NexusDownloadInfo BINARY = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , PACKAGING , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , BINARY.DOWNLOAD_FILENAME , BINARY.BASENAME, BINARY.EXT );
			}
		}
	}

	private void downloadNugetItem( ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
		String ARTEFACTID = scopeItem.sourceItem.NUGET_ITEMPATH;
		String BUILDVERSION = scopeItem.getProjectItemBuildVersion( this );
		boolean copyDistr = context.CTX_DIST;
		if( scopeItem.sourceItem.INTERNAL )
			copyDistr = false;

		NexusStorage nexusStorage = artefactory.getDefaultNugetStorage( this , downloadFolder );
		NexusDownloadInfo BINARY = nexusStorage.downloadNuget( this , ARTEFACTID , BUILDVERSION , scopeItem.distItem );

		String FILENAME = "";
		String BASENAME = "";
		String EXT = "";
		if( scopeItem.sourceItem.ITEMSRCTYPE == VarITEMSRCTYPE.NUGET ) {
			FILENAME = BINARY.DOWNLOAD_FILENAME;
			BASENAME = BINARY.BASENAME;
			EXT = BINARY.EXT;
		}
		else
		if( scopeItem.sourceItem.ITEMSRCTYPE == VarITEMSRCTYPE.NUGET_PLATFORM ) {
			// repack given item
			FILENAME = nexusStorage.repackageNugetPlatform( this , BINARY , scopeItem.sourceItem );
			BASENAME = scopeItem.sourceItem.distItem.DISTBASENAME;
			EXT = scopeItem.sourceItem.ITEMEXTENSION;
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
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , downloadFolder );
		
		if( scopeItem.sourceItem.isStoredInSvn( this ) ) {
			String ITEMPATH = scopeItem.sourceItem.SVN_ITEMPATH;
			String DISTFOLDER = scopeItem.distItem.delivery.FOLDER;
			ITEMPATH = Common.replace( ITEMPATH , "@BUILDVERSION@" , BUILDVERSION ); 
			sourceStorage.downloadThirdpartyItemFromVCS( this , ITEMPATH , DISTFOLDER );
			
			if( copyDistr ) {
				Dist releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , scopeItem.distItem.delivery.FOLDER + "/" + Common.getBaseName( ITEMPATH ) , 
						scopeItem.distItem.DISTBASENAME , scopeItem.distItem.EXT );
			}
		}
		else if( scopeItem.sourceItem.isStoredInNexus( this ) ) {
			downloadNexusItem( "thirdparty" , scopeProject , scopeItem );
		}
		else
			exit( "downloadPrebuiltItem: unsupported prebuilt type=" + Common.getEnumLower( scopeItem.sourceItem.ITEMSRCTYPE ) );
	}

}
