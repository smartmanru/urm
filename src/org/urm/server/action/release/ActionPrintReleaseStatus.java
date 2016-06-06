package org.urm.server.action.release;

import org.urm.common.Common;
import org.urm.dist.Dist;
import org.urm.dist.DistItemInfo;
import org.urm.dist.Release;
import org.urm.dist.ReleaseSet;
import org.urm.dist.ReleaseTarget;
import org.urm.dist.ReleaseTargetItem;
import org.urm.meta.MetaDistrBinaryItem;
import org.urm.meta.MetaDistrDelivery;
import org.urm.meta.Metadata.VarCATEGORY;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.FileSet;

public class ActionPrintReleaseStatus extends ActionBase {

	Dist dist;
	
	public ActionPrintReleaseStatus( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeSimple() throws Exception {
		Release release = dist.release;
		
		FileSet files = dist.getFiles( this );
		String hashStatus = dist.checkHash( this )? "OK" : "not matched";
		
		comment( "RELEASE " + dist.RELEASEDIR + " STATUS:" );
		comment( "\tlocation: " + meta.product.CONFIG_DISTR_HOSTLOGIN + ":" + dist.getDistPath( this ) );
		comment( "\tversion: " + release.RELEASEVER );
		comment( "\tstate: " + dist.getState( this ) );
		comment( "\tsignature: " + hashStatus );
		comment( "PROPERTIES:" );
		comment( "\tproperty=buildmode: " + Common.getEnumLower( release.PROPERTY_BUILDMODE ) );
		comment( "\tproperty=obsolete: " + Common.getBooleanValue( release.PROPERTY_OBSOLETE ) );
		comment( "\tproperty=over: " + release.PROPERTY_COMPATIBILITY );
		comment( "\tproperty=cumulative: " + Common.getBooleanValue( release.isCumulative() ) );
		
		if( release.isEmpty( this ) ) {
			comment( "(scope is empty)" );
			return( true );
		}
		
		for( String set : Common.getSortedKeys( release.getSourceSets( this ) ) )
			printReleaseSourceSetStatus( dist , files , release.getSourceSet( this , set ) );
		for( VarCATEGORY CATEGORY : meta.getAllReleaseCategories( this ) ) {
			ReleaseSet set = release.findCategorySet( this , CATEGORY );
			if( set != null )
				printReleaseCategorySetStatus( dist , files , set );
		}

		comment( "DELIVERIES:" );
		for( String s : Common.getSortedKeys( release.getDeliveries( this ) ) )
			comment( "\tdelivery=" + s );
	
		return( true );
	}

	private void printReleaseSourceSetStatus( Dist dist , FileSet files , ReleaseSet set ) throws Exception {
		if( set.isEmpty( this ) )
			return;
		
		String specifics = set.getSpecifics( this );
		comment( "SCOPE SET=" + set.NAME + " CATEGORY=" + Common.getEnumLower( set.CATEGORY ) + Common.getCommentIfAny( specifics ) + ":" );
		if( set.getTargets( this ).isEmpty() )
			comment( "\t(no items)" );
			
		for( String key : Common.getSortedKeys( set.getTargets( this ) ) ) {
			ReleaseTarget project = set.getTarget( this , key );
			printReleaseBuildSetProjectStatus( dist , files , set , project );
		}
	}

	private void printReleaseCategorySetStatus( Dist dist , FileSet files , ReleaseSet set ) throws Exception {
		if( set.isEmpty( this ) )
			return;
		
		// configuration
		comment( "SCOPE SET=" + Common.getEnumLower( set.CATEGORY ) + ":" );
		
		for( String key : Common.getSortedKeys( set.getTargets( this ) ) ) {
			ReleaseTarget target = set.getTarget( this , key );
			
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
		
	private void printReleaseBuildSetProjectStatus( Dist dist , FileSet files , ReleaseSet set , ReleaseTarget project ) throws Exception {
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
			ReleaseTargetItem item = project.getItems( this ).get( key );
			printReleaseBuildSetProjectItemStatus( dist , files , set , project , item );
		}
	}

	private void printReleaseBuildSetProjectItemStatus( Dist dist , FileSet files , ReleaseSet set , ReleaseTarget project , ReleaseTargetItem item ) throws Exception {
		String specifics = item.getSpecifics( this );
		MetaDistrBinaryItem distItem = item.distItem;
		DistItemInfo info = dist.getDistItemInfo( this , distItem , false );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ")" : "missing (" + info.subPath + ")";
		
		comment( "\tdistitem=" + distItem.KEY + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseConfStatus( Dist dist , FileSet files , ReleaseTarget conf ) throws Exception {
		String specifics = conf.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , conf.distConfItem );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		comment( "\tconfitem=" + conf.distConfItem.KEY + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseManualStatus( Dist dist , FileSet files , ReleaseTarget manual ) throws Exception {
		String specifics = manual.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , manual.distManualItem , false );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		comment( "\tdistitem=" + manual.distManualItem.KEY + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseDatabaseStatus( Dist dist , FileSet files , ReleaseTarget db ) throws Exception {
		MetaDistrDelivery delivery = db.distDatabaseItem;

		if( dist.release.isCumulative() ) {
			String[] versions = dist.release.getCumulativeVersions( this );
			
			for( String version : versions ) {
				String folder = dist.getDeliveryDatabaseFolder( this , delivery , version );
				FileSet dbset = files.getDirByPath( this , folder );
				if( dbset == null || dbset.isEmpty() )
					continue;
				
				comment( "\tdelivery=" + delivery.NAME + ", version=" + version + ": OK (" + folder + ")" + Common.getCommentIfAny( folder ) );
			}
		}
		else {
			String folder = dist.getDeliveryDatabaseFolder( this , delivery , dist.release.RELEASEVER );
			FileSet dbset = files.getDirByPath( this , folder );
			String status = ( dbset == null || dbset.isEmpty() )? "missing/empty" : "OK";
			comment( "\tdelivery=" + delivery.NAME + ": " + status + Common.getCommentIfAny( folder ) );
		}
	}

}
