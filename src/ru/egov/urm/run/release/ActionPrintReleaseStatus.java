package ru.egov.urm.run.release;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.meta.MetaReleaseSet;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionBase;
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
		comment( "\tproperty::buildMode: " + Common.getEnumLower( release.PROPERTY_BUILDMODE ) );
		comment( "\tproperty::obsolete: " + Common.getBooleanValue( release.PROPERTY_OBSOLETE ) );
		
		if( release.isEmpty( this ) ) {
			comment( "scope is empty" );
			return( true );
		}
		
		comment( "DELIVERIES: " + Common.getList( release.getDeliveries( this ).keySet().toArray( new String[0] ) , ", " ) );
		comment( "SCOPE:" );

		for( String set : Common.getSortedKeys( release.getSourceSets( this ) ) )
			printReleaseSourceSetStatus( dist , files , release.getSourceSet( this , set ) );
		for( VarCATEGORY CATEGORY : meta.getAllReleaseCategories( this ) ) {
			MetaReleaseSet set = release.findCategorySet( this , CATEGORY );
			if( set != null )
				printReleaseCategorySetStatus( dist , files , set );
		}

		return( true );
	}

	private void printReleaseSourceSetStatus( DistStorage dist , FileSet files , MetaReleaseSet set ) throws Exception {
		if( set.isEmpty( this ) )
			return;
		
		String specifics = set.getSpecifics( this );
		comment( "\tSET=" + set.NAME + " CATEGORY=" + Common.getEnumLower( set.CATEGORY ) + Common.getCommentIfAny( specifics ) + ":" );
		if( set.getTargets( this ).isEmpty() )
			comment( "\t\t(no items)" );
			
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
				comment( "\tbuild PROJECT=" + project.sourceProject.PROJECT + " (internal)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( project.isEmpty( this ) ) {
				comment( "\tbuild PROJECT=" + project.sourceProject.PROJECT + " (no items added)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( !project.isEmpty( this ) )
				comment( "\tbuild PROJECT=" + project.sourceProject.PROJECT + Common.getCommentIfAny( specifics ) + ":" );
			else
				comment( "\tbuild PROJECT=" + project.sourceProject.PROJECT + Common.getCommentIfAny( specifics ) + " (no items)" );
		}
		else
		if( set.CATEGORY == VarCATEGORY.PREBUILT ) {
			if( project.isEmpty( this ) )
				return;
			
			comment( "\tget PROJECT=" + project.sourceProject.PROJECT + Common.getCommentIfAny( specifics ) + ":" );
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
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ")" : "missing";
		
		comment( "\t" + distItem.KEY + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseConfStatus( DistStorage dist , FileSet files , MetaReleaseTarget conf ) throws Exception {
		String specifics = conf.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , conf.distConfItem );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ")" : "missing";
		
		comment( "\t" + conf.distConfItem.KEY + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseManualStatus( DistStorage dist , FileSet files , MetaReleaseTarget conf ) throws Exception {
		String specifics = conf.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , conf.distManualItem , false );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ")" : "missing";
		
		comment( "\t" + conf.distManualItem.KEY + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseDatabaseStatus( DistStorage dist , FileSet files , MetaReleaseTarget db ) throws Exception {
		MetaDistrDelivery delivery = db.distDatabaseItem;
			
		String folder = dist.getDeliveryDatabaseFolder( this , delivery );
		FileSet dbset = files.getDirByPath( this , folder );
		String status = ( dbset == null || dbset.isEmpty() )? "missing" : "OK";
		comment( "\t" + delivery.NAME + ": " + status + Common.getCommentIfAny( folder ) );
	}

}
