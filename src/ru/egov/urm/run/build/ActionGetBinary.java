package ru.egov.urm.run.build;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.NexusDownloadInfo;
import ru.egov.urm.storage.NexusStorage;
import ru.egov.urm.storage.SourceStorage;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.meta.Metadata.VarITEMSRCTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;

public class ActionGetBinary extends ActionBase {

	LocalFolder downloadFolder;
	boolean copyDist;
	DistStorage targetRelease;
	
	DistStorage useProdRelease;
	DistStorage useRefRelease;
	
	static String C_RELEASENEXUSGROUPID = "release";
	
	public ActionGetBinary( ActionBase action , String stream , boolean copyDist , DistStorage targetRelease , LocalFolder downloadFolder ) {
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
		log( "get binary item " + distItem.KEY + " ..." );

		// compare with release information
		VarITEMSRCTYPE itemtype = scopeItem.sourceItem.ITEMSRCTYPE;
		if( itemtype == VarITEMSRCTYPE.NEXUS )
			downloadNexusItem( "nexus" , scopeProject , scopeItem );
		else
		if( itemtype == VarITEMSRCTYPE.STATICWAR )
			downloadNexusItem( "staticwar" , scopeProject , scopeItem );
		else
			exit( "unexpected ITEMTYPE=" + Common.getEnumLower( scopeItem.sourceItem.ITEMSRCTYPE ) );
	}
	
	private void downloadNexusItem( String type , ActionScopeTarget scopeProject , ActionScopeTargetItem scopeItem ) throws Exception {
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
			exit( "unexpected extension=" + EXT );

		String BUILDVERSION = scopeItem.getProjectItemBuildVersion( this );
		boolean copyDistr = options.OPT_DIST;
		if( scopeItem.sourceItem.INTERNAL )
			copyDistr = false;

		if( type.equals( "war" ) ) {
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , downloadFolder );
			NexusDownloadInfo WAR = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "war" , "" , scopeItem.distItem );
			NexusDownloadInfo STATIC = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , "tar.gz" , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr && scopeItem.sourceItem.ITEMSRCTYPE == VarITEMSRCTYPE.NEXUS ) {
				DistStorage releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , WAR.BASENAME, WAR.EXT );
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , STATIC.BASENAME, WAR.EXT );
			}
		}
		else 
		if( type.equals( "thirdparty" ) ) {
			NexusStorage nexusStorage = artefactory.getThirdpartyNexusStorage( this , downloadFolder );
			NexusDownloadInfo BINARY = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , PACKAGING , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr ) {
				DistStorage releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , BINARY.DOWNLOAD_FILENAME , BINARY.BASENAME, BINARY.EXT );
			}
		}
		else if( type.equals( "nexus" ) ) {
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( this , downloadFolder );
			NexusDownloadInfo BINARY = nexusStorage.downloadNexus( this , GROUPID , ARTEFACTID , BUILDVERSION , PACKAGING , CLASSIFIER , scopeItem.distItem );
			
			if( copyDistr ) {
				DistStorage releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , BINARY.DOWNLOAD_FILENAME , BINARY.BASENAME, BINARY.EXT );
			}
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
		boolean copyDistr = options.OPT_DIST;
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , downloadFolder );
		
		if( scopeItem.sourceItem.isStoredInSvn( this ) ) {
			String ITEMPATH = scopeItem.sourceItem.ITEMPATH;
			String DISTFOLDER = scopeItem.distItem.delivery.FOLDERPATH;
			ITEMPATH = Common.replace( ITEMPATH , "@BUILDVERSION@" , BUILDVERSION ); 
			sourceStorage.downloadThirdpartyItemFromVCS( this , ITEMPATH , DISTFOLDER );
			
			if( copyDistr ) {
				DistStorage releaseStorage = targetRelease;
				releaseStorage.copyVFileToDistr( this , scopeItem.distItem , downloadFolder , scopeItem.distItem.delivery.FOLDERPATH + "/" + Common.getBaseName( ITEMPATH ) , 
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
