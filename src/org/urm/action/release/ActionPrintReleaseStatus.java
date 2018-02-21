package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseScriptFile;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistItemInfo;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseMasterItem;
import org.urm.engine.dist.ReleaseSchedule;
import org.urm.engine.dist.ReleaseSchedulePhase;
import org.urm.engine.dist.ReleaseSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileSet;
import org.urm.meta.Types;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.Types.*;

public class ActionPrintReleaseStatus extends ActionBase {

	Dist dist;
	
	public ActionPrintReleaseStatus( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Print release status=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		Release release = dist.release;
		ReleaseSchedule schedule = release.schedule;
	
		dist.gatherFiles( this );
		FileSet files = dist.getFiles( this );
		String hashStatus = dist.checkHash( this )? "OK" : "not matched";
		
		info( "RELEASE " + dist.RELEASEDIR + " STATUS:" );
		info( "\tlocation: " + dist.getDistPath( this ) );
		info( "\tversion: " + release.RELEASEVER );
		info( "\tstate: " + dist.getState().name() );
		info( "\tsignature: " + hashStatus );
		info( "PROPERTIES:" );
		info( "\tproperty=master: " + Common.getBooleanValue( release.MASTER ) );
		if( !release.MASTER ) {
			info( "\tproperty=mode: " + Common.getEnumLower( release.BUILDMODE ) );
			info( "\tproperty=over: " + release.COMPATIBILITY );
			info( "\tproperty=cumulative: " + Common.getBooleanValue( release.isCumulative() ) );
		}
		
		if( !dist.isMaster() ) {
			info( "SCHEDULE:" );
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
		
			if( release.isEmpty() ) {
				info( "(scope is empty)" );
				return( SCOPESTATE.NotRun );
			}
		
			for( String set : release.getSourceSetNames() )
				printReleaseSourceSetStatus( dist , files , release.getSourceSet( this , set ) );
			
			for( EnumScopeCategory CATEGORY : Types.getAllReleaseCategories() ) {
				ReleaseSet set = release.findCategorySet( CATEGORY );
				if( set != null )
					printReleaseCategorySetStatus( dist , files , set );
			}

			info( "DELIVERIES:" );
			for( String s : release.getDeliveryNames() ) {
				ReleaseDelivery delivery = release.findDelivery( s );
				info( "\tdelivery=" + s + " (folder=" + delivery.distDelivery.FOLDER + ")" );
			}
		}
		else {
			info( "DELIVERIES:" );
			MetaDistr distr = dist.meta.getDistr();
			for( String s : distr.getDeliveryNames() ) {
				MetaDistrDelivery delivery = distr.findDelivery( s );
				info( "\tdelivery=" + s + " (folder=" + delivery.FOLDER + ")" + ":" );
				printProdDeliveryStatus( dist , files , delivery );
			}
		}
	
		return( SCOPESTATE.RunSuccess );
	}

	private void printProdDeliveryStatus( Dist dist , FileSet files , MetaDistrDelivery delivery ) throws Exception {
		if( delivery.isEmpty() ) {
			info( "\t\t(no items)" );
		}
			
		for( String key : delivery.getBinaryItemNames() ) {
			MetaDistrBinaryItem item = delivery.findBinaryItem( key );
			printProdBinaryStatus( dist , files , item );
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

	private void printReleaseCategorySetStatus( Dist dist , FileSet files , ReleaseSet set ) throws Exception {
		if( set.isEmpty() )
			return;
		
		// configuration
		info( "SCOPE SET=" + Common.getEnumLower( set.CATEGORY ) + ":" );
		
		for( String key : set.getTargetNames() ) {
			ReleaseTarget target = set.findTarget( key );
			
			if( set.CATEGORY == EnumScopeCategory.CONFIG )
				printReleaseConfStatus( dist , files , target );
			else
			if( set.CATEGORY == EnumScopeCategory.DB )
				printReleaseDatabaseStatus( dist , files , target );
			else
			if( set.CATEGORY == EnumScopeCategory.MANUAL )
				printReleaseManualStatus( dist , files , target );
			else
			if( set.CATEGORY == EnumScopeCategory.DOC )
				printReleaseDocStatus( dist , files , target );
			else
				exitUnexpectedCategory( set.CATEGORY );
		}
	}

	private void printReleaseBuildSetProjectStatus( Dist dist , FileSet files , ReleaseSet set , ReleaseTarget project ) throws Exception {
		String specifics = project.getSpecifics();
		if( project.isBuildableProject() ) {
			if( project.sourceProject.isEmpty( this ) ) {
				info( "\tbuild project=" + project.sourceProject.NAME + " (internal)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( project.isEmpty() ) {
				info( "\tbuild project=" + project.sourceProject.NAME + " (no items added)" + Common.getCommentIfAny( specifics ) );
				return;
			}
			
			if( !project.isEmpty() )
				info( "\tbuild project=" + project.sourceProject.NAME + Common.getCommentIfAny( specifics ) + ":" );
			else
				info( "\tbuild project=" + project.sourceProject.NAME + Common.getCommentIfAny( specifics ) + " (no items)" );
		}
		else
		if( project.isPrebuiltProject() ) {
			if( project.isEmpty() )
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
		DistItemInfo info = dist.getDistItemInfo( this , distItem , false , true );
		String status = ( info.found )? "OK (" + Common.getPath( info.subPath , info.fileName ) + ", " + 
				Common.getRefDate( info.timestamp ) + ")" : "missing (" + info.subPath + ")";
		
		info( "\t\tdistitem=" + distItem.NAME + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseConfStatus( Dist dist , FileSet files , ReleaseTarget conf ) throws Exception {
		String specifics = conf.getSpecifics();
		DistItemInfo info = dist.getDistItemInfo( this , conf.distConfItem );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK" : "missing";
		
		info( "\t\tconfitem=" + conf.distConfItem.NAME + ": " + status + " (" + folder + ")" + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseManualStatus( Dist dist , FileSet files , ReleaseTarget manual ) throws Exception {
		String specifics = manual.getSpecifics();
		DistItemInfo info = dist.getDistItemInfo( this , manual.distManualItem , false , true );
		String folder = Common.getPath( info.subPath , info.fileName );
		String status = ( info.found )? "OK (" + folder + ", " + 
				Common.getRefDate( info.timestamp ) + ")" : "missing (" + info.subPath + ")";
		
		info( "\t\tdistitem=" + manual.distManualItem.NAME + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printReleaseDocStatus( Dist dist , FileSet files , ReleaseTarget items ) throws Exception {
		for( String name : items.getItemNames() ) {
			ReleaseTargetItem item = items.findItem( name );
			DistItemInfo info = dist.getDistItemInfo( this , items.distDelivery , item.doc , false , true );
			
			String folder = Common.getPath( info.subPath , info.fileName );
			String status = ( info.found )? "OK (" + folder + ", " + 
					Common.getRefDate( info.timestamp ) + ")" : "missing (" + info.subPath + ")";
			
			info( "\t\tdoc=" + item.doc.NAME + ": " + status );
		}
	}

	private void printReleaseDatabaseStatus( Dist dist , FileSet files , ReleaseTarget db ) throws Exception {
		MetaDistrDelivery delivery = db.distDelivery;

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
			
			for( String key : db.getItemNames() ) {
				ReleaseTargetItem item = db.findItem( key );
				printReleaseDatabaseSchemaStatus( dist , dbset , db , item );
			}
		}
	}

	private void printReleaseDatabaseSchemaStatus( Dist dist , FileSet files , ReleaseTarget project , ReleaseTargetItem item ) throws Exception {
		String specifics = item.getSpecifics( this );
		MetaDatabaseSchema schema = item.schema;
		boolean found = DatabaseScriptFile.checkDistHasSchemaFiles( files , schema );
		String status = ( found )? "found" : "missing";
		info( "\tschema=" + schema.NAME + ": " + status + Common.getCommentIfAny( specifics ) );
	}

	private void printProdBinaryStatus( Dist dist , FileSet files , MetaDistrBinaryItem distItem ) throws Exception {
		ReleaseMasterItem masterItem = dist.release.findMasterItem( distItem );
		DistItemInfo info = dist.getDistItemInfo( this , distItem , true , true );
		
		String status = "";
		if( masterItem == null ) {
			String folder = Common.getPath( distItem.delivery.FOLDER , Dist.BINARY_FOLDER );
			status = ( info.found )? "OK (" + Common.getPath( folder , info.fileName ) + ", new)" : 
				"missing (" + Common.getPath( folder , distItem.getBaseFile() ) + ")";
		}
		else {
			if( masterItem.FOLDER.equals( distItem.delivery.FOLDER ) ) { 
				String folder = Common.getPath( masterItem.FOLDER , Dist.BINARY_FOLDER );
				if( info.found ) {
					if( !info.md5value.equals( masterItem.MD5 ) )
						status = "OK (" + Common.getPath( folder , info.fileName ) + ", manual)";
					else
						status = "OK (" + Common.getPath( folder , info.fileName ) + ", " + masterItem.RELEASE + ")";
				}
				else					
					status = "missing (" + Common.getPath( folder , distItem.getBaseFile() ) + ")";
			}
			else {
				String folder = Common.getPath( distItem.delivery.FOLDER , Dist.BINARY_FOLDER );
				if( info.found )
					status = "OK (" + Common.getPath( folder , info.fileName ) + ", moved)";
				else
					status = "missing (" + Common.getPath( folder , distItem.getBaseFile() ) + ", obsolete)";
			}
		}
		
		info( "\t\tdistitem=" + distItem.NAME + ": " + status );
	}

}
