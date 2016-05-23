package ru.egov.urm.action.release;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.meta.MetaReleaseSet;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.storage.DistItemInfo;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileSet;

public class ActionPrintReleaseStatus extends ActionBase {

	DistStorage dist;
	
	public ActionPrintReleaseStatus( ActionBase action , String stream , DistStorage dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeSimple() throws Exception {
		MetaRelease release = dist.info;
		
		FileSet files = dist.getFiles( this );
		
		comment( "RELEASE " + dist.RELEASEDIR + " STATUS:" );
		comment( "\tlocation: " + meta.product.CONFIG_DISTR_HOSTLOGIN + ":" + dist.getDistPath( this ) );
		comment( "\tstate: " + dist.getState( this ) );
		comment( "\tversion: " + release.RELEASEVER );
		comment( "PROPERTIES:" );
		comment( "\tproperty=buildMode: " + Common.getEnumLower( release.PROPERTY_BUILDMODE ) );
		comment( "\tproperty=obsolete: " + Common.getBooleanValue( release.PROPERTY_OBSOLETE ) );
		
		if( release.isEmpty( this ) ) {
			comment( "(scope is empty)" );
			return( true );
		}
		
		for( String set : Common.getSortedKeys( release.getSourceSets( this ) ) )
			printReleaseSourceSetStatus( dist , files , release.getSourceSet( this , set ) );
		for( VarCATEGORY CATEGORY : meta.getAllReleaseCategories( this ) ) {
			MetaReleaseSet set = release.findCategorySet( this , CATEGORY );
			if( set != null )
				printReleaseCategorySetStatus( dist , files , set );
		}

		comment( "DELIVERABLES:" );
		for( String s : Common.getSortedKeys( release.getDeliveries( this ) ) )
			comment( "\tdelivery=" + s );
	
		return( true );
	}

	private void printReleaseSourceSetStatus( DistStorage dist , FileSet files , MetaReleaseSet set ) throws Exception {
		if( set.isEmpty( this ) )
			return;
		
		String specifics = set.getSpecifics( this );
		comment( "SCOPE SET=" + set.NAME + " CATEGORY=" + Common.getEnumLower( set.CATEGORY ) + Common.getCommentIfAny( specifics ) + ":" );
		if( set.getTargets( this ).isEmpty() )
			comment( "\t(no items)" );
			
		for( String key : Common.getSortedKeys( set.getTargets( this ) ) ) {
			MetaReleaseTarget project = set.getTarget( this , key );
			printReleaseBuildSetProjectStatus( dist , files , set , project );
		}
	}

	private void printReleaseCategorySetStatus( DistStorage dist , FileSet files , MetaReleaseSet set ) throws Exception {
		if( set.isEmpty( this ) )
			return;
		
		// configuration
		comment( "SET=" + Common.getEnumLower( set.CATEGORY ) + ":" );
		
		for( String key : Common.getSortedKeys( set.getTargets( this ) ) ) {
			MetaReleaseTarget target = set.getTarget( this , key );
			
			if( set.CATEGORY == VarCATEGORY.CONFIG )
				printReleaseConfStatus( dist , files , target );
			else
			if( set.CATEGORY == VarCATEGORY.DB )
				printReleaseDatabaseStatus( dist , files , target );
			else
			if( set.CATEGORY == VarCATEGORY.MANUAL )
				printReleaseManualStatus( dist , files , target );
			else
				exitUnexpectedCategory( set.CATEGORY );
		}
	}
		
	private void printReleaseBuildSetProjectStatus( DistStorage dist , FileSet files , MetaReleaseSet set , MetaReleaseTarget project ) throws Exception {
		String specifics = project.getSpecifics( this );
		if( meta.isBuildableCategory( this , set.CATEGORY ) ) {
			if( project.sourceProject.isEmpty( this ) ) {
				comment( "\tbuild project=" + project.sourceProject.PROJECT + " (internal)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( project.isEmpty( this ) ) {
				comment( "\tbuild project=" + project.sourceProject.PROJECT + " (no items added)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( !project.isEmpty( this ) )
				comment( "\tbuild project=" + project.sourceProject.PROJECT + Common.getCommentIfAny( specifics ) + ":" );
			else
				comment( "\tbuild project=" + project.sourceProject.PROJECT + Common.getCommentIfAny( specifics ) + " (no items)" );
		}
		else
		if( set.CATEGORY == VarCATEGORY.PREBUILT ) {
			if( project.isEmpty( this ) )
				return;
			
			comment( "\tprebuilt project=" + project.sourceProject.PROJECT + Common.getCommentIfAny( specifics ) + ":" );
		}
		else
			exitUnexpectedCategory( set.CATEGORY );
		
		for( String key : Common.getSortedKeys( project.getItems( this ) ) ) {
			MetaReleaseTargetItem item = project.getItems( this ).get( key );
			printReleaseBuildSetProjectItemStatus( dist , files , set , project , item );
		}
	}

	private void printReleaseBuildSetProjectItemStatus( DistStorage dist , FileSet files , MetaReleaseSet set , MetaReleaseTarget project , MetaReleaseTargetItem item ) throws Exception {
		String specifics = item.getSpecifics( this );
		MetaDistrBinaryItem distItem = item.distItem;
		DistItemInfo info = dist.getDistItemInfo( this , distItem , false );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ")" : "missing (" + info.subPath + ")";
		
		comment( "\tdistitem=" + distItem.KEY + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseConfStatus( DistStorage dist , FileSet files , MetaReleaseTarget conf ) throws Exception {
		String specifics = conf.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , conf.distConfItem );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		comment( "\tconfitem=" + conf.distConfItem.KEY + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseManualStatus( DistStorage dist , FileSet files , MetaReleaseTarget manual ) throws Exception {
		String specifics = manual.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , manual.distManualItem , false );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		comment( "\tdistitem=" + manual.distManualItem.KEY + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseDatabaseStatus( DistStorage dist , FileSet files , MetaReleaseTarget db ) throws Exception {
		MetaDistrDelivery delivery = db.distDatabaseItem;
			
		String folder = dist.getDeliveryDatabaseFolder( this , delivery );
		FileSet dbset = files.getDirByPath( this , folder );
		String status = ( dbset == null || dbset.isEmpty() )? "missing" : "OK";
		comment( "\tdelivery=" + delivery.NAME + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( folder ) );
	}

}
