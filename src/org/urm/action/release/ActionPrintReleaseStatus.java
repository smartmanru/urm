package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileSet;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseDistTarget;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;
import org.urm.meta.release.ReleaseScope;

public class ActionPrintReleaseStatus extends ActionBase {

	Release release;
	
	public ActionPrintReleaseStatus( ActionBase action , String stream , Release release ) {
		super( action , stream , "Print release status=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		ReleaseSchedule schedule = release.getSchedule();
	
		Meta meta = release.getMeta();
		DistRepository repo = meta.getDistRepository();
		Dist dist = repo.findDefaultMasterDist();
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
			info( "\trelease date: " + Common.getDateValue( schedule.RELEASE_DATE ) );
			
			ReleaseSchedulePhase phase = schedule.getCurrentPhase();
			if( phase != null ) {
				info( "\trelease phase: " + phase.NAME );
				info( "\tphase deadline: " + Common.getDateValue( phase.getDeadlineFinish() ) );
			}
			
			if( context.CTX_ALL ) {
				info( "\tphase schedule: " );
				for( int k = 0; k < schedule.getPhaseCount(); k++ ) {
					phase = schedule.getPhase( k );
					Date started = ( phase.isStarted() )? phase.getStartDate() : phase.getDeadlineStart();
					Date finished = ( phase.isFinished() )? phase.getFinishDate() : phase.getDeadlineFinish();
					String status = ( phase.isStarted() )? ( ( phase.isFinished() )? "finished" : "started" ) : "expected";
					
					info( "\t\t" + (k+1) + ": " + phase.NAME + " - start=" + Common.getDateValue( started ) +
						", finish=" + Common.getDateValue( finished ) + " (" + status + ")" );
				}
			}
		
			showScope();
		}
		else {
			info( "DELIVERIES:" );
			MetaDistr distr = dist.meta.getDistr();
			for( String s : distr.getDeliveryNames() ) {
				MetaDistrDelivery delivery = distr.findDelivery( s );
				info( "\tdelivery=" + s + " (folder=" + delivery.FOLDER + ")" + ":" );
				printMasterDeliveryStatus( dist , files , delivery );
			}
		}
	
		return( SCOPESTATE.RunSuccess );
	}

	private void showScope() {
		info( "SCOPE:" );
		ReleaseScope scope = release.getScope();
		ReleaseBuildTarget[] buildTargets = scope.getBuildTargets();
		ReleaseDistTarget[] distTargets = scope.getDistTargets();
		if( buildTargets.length == 0 && distTargets.length == 0 ) {
			info( "\t(empty)" );
			return;
		}
		
		if( buildTargets.length > 0 ) {
			for( ReleaseBuildTarget target : buildTargets ) {
				if( target.isBuildAll() ) {
					String extra = getBuildExtra( target );
					info( "\tbuild all" + extra );
				}
				else
				if( target.isBuildSet() ) {
					MetaSourceProjectSet set = target.getProjectSet();
					String extra = "";
					info( "\tbuild project set=" + set.NAME + extra );
				}
				else
				if( target.isBuildProject() ) {
					MetaSourceProject project = target.getProject();
					String extra = "";
					info( "\tbuild project=" + project.NAME + extra );
				}
			}
		}
		
		if( distTargets.length > 0 ) {
			for( ReleaseDistTarget target : distTargets ) {
				if( target.isDistAll() )
					info( "\t(all distributive items)" );
				else
				if( target.isDelivery() ) {
					MetaDistrDelivery delivery = target.getDelivery();
					if( target.isDeliveryBinaries() )
						info( "\tdelivery=" + delivery.NAME + " (all items)" );
					else
					if( target.isDeliveryConfs() )
						info( "\tdelivery=" + delivery.NAME + " (all configuration)" );
					else
					if( target.isDeliveryDatabase() )
						info( "\tdelivery=" + delivery.NAME + " (all database)" );
					else
					if( target.isDeliveryDocs() )
						info( "\tdelivery=" + delivery.NAME + " (all documents)" );
				}
				else
				if( target.isBinaryItem() ) {
					MetaDistrBinaryItem item = target.getBinaryItem();
					info( "\tdelivery=" + item.delivery.NAME + ", binary item=" + item.NAME );
				}
				else
				if( target.isConfItem() ) {
					MetaDistrConfItem item = target.getConfItem();
					info( "\tdelivery=" + item.delivery.NAME + ", binary item=" + item.NAME );
				}
				else
				if( target.isSchema() ) {
					MetaDistrDelivery delivery = target.getDelivery();
					MetaDatabaseSchema schema = target.getSchema();
					info( "\tdelivery=" + delivery.NAME + ", database schema=" + schema.NAME );
				}
				else
				if( target.isDoc() ) {
					MetaDistrDelivery delivery = target.getDelivery();
					MetaProductDoc doc = target.getDoc();
					info( "\tdelivery=" + delivery.NAME + ", document type=" + doc.NAME );
				}
			}
		}
	}

	private String getBuildExtra( ReleaseBuildTarget target ) {
		String extra = "";
		if( !target.BUILD_BRANCH.isEmpty() )
			extra = Common.addToList( extra , "branch=" + target.BUILD_BRANCH , ", " );
		if( !target.BUILD_TAG.isEmpty() )
			extra = Common.addToList( extra , "tag=" + target.BUILD_TAG , ", " );
		if( !target.BUILD_VERSION.isEmpty() )
			extra = Common.addToList( extra , "version=" + target.BUILD_VERSION , ", " );
		if( !extra.isEmpty() )
			extra = " (" + extra + ")";
		return( extra );
	}
	
	private void printMasterDeliveryStatus( Dist dist , FileSet files , MetaDistrDelivery delivery ) throws Exception {
		if( delivery.isEmpty() ) {
			info( "\t\t(no items)" );
		}
			
		for( String key : delivery.getBinaryItemNames() ) {
			MetaDistrBinaryItem item = delivery.findBinaryItem( key );
			printProdBinaryStatus( dist , files , item );
		}
	}
	
	private void printProdBinaryStatus( Dist dist , FileSet files , MetaDistrBinaryItem distItem ) throws Exception {
	}

}
