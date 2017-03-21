package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistItemInfo;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.ReleaseSchedule;
import org.urm.engine.dist.ReleaseSchedulePhase;
import org.urm.engine.dist.ReleaseSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.engine.storage.FileSet;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;

public class ActionPrintReleaseStatus extends ActionBase {

	Dist dist;
	
	public ActionPrintReleaseStatus( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Print release status=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		Release release = dist.release;
		ReleaseSchedule schedule = release.schedule;
		
		FileSet files = dist.getFiles( this );
		String hashStatus = dist.checkHash( this )? "OK" : "not matched";
		
		MetaProductSettings product = dist.meta.getProductSettings( this );
		info( "RELEASE " + dist.RELEASEDIR + " STATUS:" );
		info( "\tlocation: " + product.CONFIG_DISTR_HOSTLOGIN + ":" + dist.getDistPath( this ) );
		info( "\tversion: " + release.RELEASEVER );
		info( "\tstate: " + dist.getState().name() );
		info( "\tsignature: " + hashStatus );
		info( "PROPERTIES:" );
		info( "\tproperty=prod: " + Common.getBooleanValue( release.PROPERTY_PROD ) );
		info( "\tproperty=buildmode: " + Common.getEnumLower( release.PROPERTY_BUILDMODE ) );
		info( "\tproperty=obsolete: " + Common.getBooleanValue( release.PROPERTY_OBSOLETE ) );
		info( "\tproperty=over: " + release.PROPERTY_COMPATIBILITY );
		info( "\tproperty=cumulative: " + Common.getBooleanValue( release.isCumulative() ) );
		
		info( "SCHEDULE:" );
		if( !dist.isFullProd() ) {
			info( "\trelease lifecycle: " + schedule.LIFECYCLE );
			info( "\trelease date: " + Common.getDateValue( schedule.releaseDate ) );
			
			ReleaseSchedulePhase phase = schedule.getCurrentPhase();
			if( phase != null ) {
				info( "\trelease phase: " + phase.name );
				info( "\tphase deadline: " + Common.getDateValue( phase.getDeadlineFinish() ) );
			}
			
			if( context.CTX_ALL ) {
				info( "\tphase schedule: " );
				for( int k = 0; k < schedule.getPhaseCount(); k++ ) {
					phase = schedule.getPhase( k );
					Date started = ( phase.isStarted() )? phase.getStartDate() : phase.getDeadlineStart();
					Date finished = ( phase.isFinished() )? phase.getFinishDate() : phase.getDeadlineFinish();
					String status = ( phase.isStarted() )? ( ( phase.isFinished() )? "finished" : "started" ) : "expected";
					
					info( "\t\t" + (k+1) + ": " + phase.name + " - start=" + Common.getDateValue( started ) +
						", finish=" + Common.getDateValue( finished ) + " (" + status + ")" );
				}
			}
		}
		else
			info( "\t(not applicable for master production distributive)" );
		
		if( release.isEmpty() ) {
			info( "(scope is empty)" );
			return( SCOPESTATE.NotRun );
		}
		
		if( !dist.isFullProd() ) {
			for( String set : release.getSourceSetNames() )
				printReleaseSourceSetStatus( dist , files , release.getSourceSet( this , set ) );
			
			for( VarCATEGORY CATEGORY : Meta.getAllReleaseCategories() ) {
				ReleaseSet set = release.findCategorySet( CATEGORY );
				if( set != null )
					printReleaseCategorySetStatus( dist , files , set );
			}

			info( "DELIVERIES:" );
			for( String s : release.getDeliveryNames() )
				info( "\tdelivery=" + s );
		}
		else {
			MetaSource sources = dist.meta.getSources( this );
			for( String set : sources.getSetNames() )
				printProdSourceSetStatus( dist , files , sources.getProjectSet( this , set ) );

			printProdManualStatus( dist , files );

			info( "DELIVERIES:" );
			MetaDistr distr = dist.meta.getDistr( this );
			for( String s : distr.getDeliveryNames() )
				info( "\tdelivery=" + s );
		}
	
		return( SCOPESTATE.RunSuccess );
	}

	private void printProdSourceSetStatus( Dist dist , FileSet files , MetaSourceProjectSet set ) throws Exception {
		if( set.isEmpty() )
			return;
		
		info( "SCOPE SET=" + set.NAME + " CATEGORY=" + Common.getEnumLower( VarCATEGORY.PROJECT ) + ":" );
		if( set.isEmpty() )
			info( "\t(no items)" );
			
		for( String key : set.getProjectNames() ) {
			MetaSourceProject project = set.findProject( key );
			printProdBuildSetProjectStatus( dist , files , set , project );
		}
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

	private void printProdManualStatus( Dist dist , FileSet files ) throws Exception {
		MetaDistr distr = dist.meta.getDistr( this );
		String[] manualNames = distr.getManualItemNames();
		if( manualNames.length == 0 )
			return;
		
		// configuration
		info( "SCOPE SET=" + Common.getEnumLower( VarCATEGORY.MANUAL ) + ":" );
		
		for( String key : manualNames ) {
			MetaDistrBinaryItem item = distr.findBinaryItem( key );
			printProdManualStatus( dist , files , item );
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

	private void printProdBuildSetProjectStatus( Dist dist , FileSet files , MetaSourceProjectSet set , MetaSourceProject project ) throws Exception {
		if( project.isBuildable() ) {
			if( project.isEmpty( this ) ) {
				info( "\tbuild project=" + project.NAME + " (internal)" );
				return;
			}
			
			if( project.isEmpty( this ) ) {
				info( "\tbuild project=" + project.NAME + " (no items added)" );
				return;
			}
			
			if( !project.isEmpty( this ) )
				info( "\tbuild project=" + project.NAME + ":" );
			else
				info( "\tbuild project=" + project.NAME + " (no items)" );
		}
		else {
			if( project.isEmpty( this ) )
				return;
			
			info( "\tprebuilt project=" + project.NAME + ":" );
		}
		
		for( String key : project.getItemNames() ) {
			MetaSourceProjectItem item = project.findItem( key );
			if( !item.isInternal() )
				printProdBuildSetProjectItemStatus( dist , files , set , project , item );
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

	private void printProdBuildSetProjectItemStatus( Dist dist , FileSet files , MetaSourceProjectSet set , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		MetaDistrBinaryItem distItem = item.distItem;
		DistItemInfo info = dist.getDistItemInfo( this , distItem , false , true );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ", " + 
				Common.getRefDate( info.timestamp ) + ")" : "missing (" + info.subPath + ")";
		
		info( "\t\tdistitem=" + distItem.KEY + ": " + status );
	}
	
	private void printReleaseBuildSetProjectItemStatus( Dist dist , FileSet files , ReleaseSet set , ReleaseTarget project , ReleaseTargetItem item ) throws Exception {
		String specifics = item.getSpecifics( this );
		MetaDistrBinaryItem distItem = item.distItem;
		DistItemInfo info = dist.getDistItemInfo( this , distItem , false , true );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ", " + 
				Common.getRefDate( info.timestamp ) + ")" : "missing (" + info.subPath + ")";
		
		info( "\t\tdistitem=" + distItem.KEY + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseConfStatus( Dist dist , FileSet files , ReleaseTarget conf ) throws Exception {
		String specifics = conf.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , conf.distConfItem );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		info( "\t\tconfitem=" + conf.distConfItem.KEY + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printProdManualStatus( Dist dist , FileSet files , MetaDistrBinaryItem manual ) throws Exception {
		DistItemInfo info = dist.getDistItemInfo( this , manual , false , true );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		info( "\t\tdistitem=" + manual.KEY + ": " + status + " (" + folder + ", " + 
				Common.getRefDate( info.timestamp ) + ")" );
	}

	private void printReleaseManualStatus( Dist dist , FileSet files , ReleaseTarget manual ) throws Exception {
		String specifics = manual.getSpecifics( this );
		DistItemInfo info = dist.getDistItemInfo( this , manual.distManualItem , false , true );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		info( "\t\tdistitem=" + manual.distManualItem.KEY + ": " + status + " (" + folder + ", " + 
				Common.getRefDate( info.timestamp ) + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseDatabaseStatus( Dist dist , FileSet files , ReleaseTarget db ) throws Exception {
		MetaDistrDelivery delivery = db.distDatabaseItem;

		if( dist.release.isCumulative() ) {
			String[] versions = dist.release.getCumulativeVersions();
			
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
