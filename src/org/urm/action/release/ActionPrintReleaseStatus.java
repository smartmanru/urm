package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistItemInfo;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.ReleaseSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.engine.storage.FileSet;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.Types.*;

public class ActionPrintReleaseStatus extends ActionBase {

	Dist dist;
	
	public ActionPrintReleaseStatus( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Print release status=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		Release release = dist.release;
		
		FileSet files = dist.getFiles( this );
		String hashStatus = dist.checkHash( this )? "OK" : "not matched";
		
		MetaProductSettings product = dist.meta.getProductSettings( this );
		info( "RELEASE " + dist.RELEASEDIR + " STATUS:" );
		info( "\tlocation: " + product.CONFIG_DISTR_HOSTLOGIN + ":" + dist.getDistPath( this ) );
		info( "\tversion: " + release.RELEASEVER );
		info( "\tstate: " + dist.getState().name() );
		info( "\tsignature: " + hashStatus );
		info( "PROPERTIES:" );
		info( "\tproperty=buildmode: " + Common.getEnumLower( release.PROPERTY_BUILDMODE ) );
		info( "\tproperty=obsolete: " + Common.getBooleanValue( release.PROPERTY_OBSOLETE ) );
		info( "\tproperty=over: " + release.PROPERTY_COMPATIBILITY );
		info( "\tproperty=cumulative: " + Common.getBooleanValue( release.isCumulative() ) );
		
		if( release.isEmpty( this ) ) {
			info( "(scope is empty)" );
			return( SCOPESTATE.NotRun );
		}
		
		for( String set : Common.getSortedKeys( release.getSourceSets( this ) ) )
			printReleaseSourceSetStatus( dist , files , release.getSourceSet( this , set ) );
		for( VarCATEGORY CATEGORY : Meta.getAllReleaseCategories() ) {
			ReleaseSet set = release.findCategorySet( this , CATEGORY );
			if( set != null )
				printReleaseCategorySetStatus( dist , files , set );
		}

		info( "DELIVERIES:" );
		for( String s : Common.getSortedKeys( release.getDeliveries( this ) ) )
			info( "\tdelivery=" + s );
	
		return( SCOPESTATE.RunSuccess );
	}

	private void printReleaseSourceSetStatus( Dist dist , FileSet files , ReleaseSet set ) throws Exception {
		if( set.isEmpty() )
			return;
		
		String specifics = set.getSpecifics( this );
		info( "SCOPE SET=" + set.NAME + " CATEGORY=" + Common.getEnumLower( set.CATEGORY ) + Common.getCommentIfAny( specifics ) + ":" );
		if( set.isEmpty() )
			info( "\t(no items)" );
			
		for( String key : set.getTargetNames() ) {
			ReleaseTarget project = set.findTarget( key );
			printReleaseBuildSetProjectStatus( dist , files , set , project );
		}
	}

	private void printReleaseCategorySetStatus( Dist dist , FileSet files , ReleaseSet set ) throws Exception {
		if( set.isEmpty() )
			return;
		
		// configuration
		info( "SCOPE SET=" + Common.getEnumLower( set.CATEGORY ) + ":" );
		
		for( String key : set.getTargetNames() ) {
			ReleaseTarget target = set.findTarget( key );
			
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
		if( project.isBuildableProject() ) {
			if( project.sourceProject.isEmpty( this ) ) {
				info( "\tbuild project=" + project.sourceProject.NAME + " (internal)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( project.isEmpty( this ) ) {
				info( "\tbuild project=" + project.sourceProject.NAME + " (no items added)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( !project.isEmpty( this ) )
				info( "\tbuild project=" + project.sourceProject.NAME + Common.getCommentIfAny( specifics ) + ":" );
			else
				info( "\tbuild project=" + project.sourceProject.NAME + Common.getCommentIfAny( specifics ) + " (no items)" );
		}
		else
		if( project.isPrebuiltProject() ) {
			if( project.isEmpty( this ) )
				return;
			
			info( "\tprebuilt project=" + project.sourceProject.NAME + Common.getCommentIfAny( specifics ) + ":" );
		}
		else
			exitUnexpectedCategory( set.CATEGORY );
		
		for( String key : project.getItemNames() ) {
			ReleaseTargetItem item = project.findItem( key );
			printReleaseBuildSetProjectItemStatus( dist , files , set , project , item );
		}
	}

	private void printReleaseBuildSetProjectItemStatus( Dist dist , FileSet files , ReleaseSet set , ReleaseTarget project , ReleaseTargetItem item ) throws Exception {
		String specifics = item.getSpecifics( this );
		MetaDistrBinaryItem distItem = item.distItem;
		DistItemInfo info = dist.getDistItemInfo( this , distItem , false );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ")" : "missing (" + info.subPath + ")";
		
		info( "\tdistitem=" + distItem.KEY + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseConfStatus( Dist dist , FileSet files , ReleaseTarget conf ) throws Exception {
		String specifics = conf.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , conf.distConfItem );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		info( "\tconfitem=" + conf.distConfItem.KEY + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseManualStatus( Dist dist , FileSet files , ReleaseTarget manual ) throws Exception {
		String specifics = manual.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , manual.distManualItem , false );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		info( "\tdistitem=" + manual.distManualItem.KEY + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
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
				
				info( "\tdelivery=" + delivery.NAME + ", version=" + version + ": OK (" + folder + ")" + Common.getCommentIfAny( folder ) );
			}
		}
		else {
			String folder = dist.getDeliveryDatabaseFolder( this , delivery , dist.release.RELEASEVER );
			FileSet dbset = files.getDirByPath( this , folder );
			String status = ( dbset == null || dbset.isEmpty() )? "missing/empty" : "OK";
			info( "\tdelivery=" + delivery.NAME + ": " + status + Common.getCommentIfAny( folder ) );
		}
	}

}
