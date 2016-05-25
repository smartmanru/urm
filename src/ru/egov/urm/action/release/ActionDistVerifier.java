package ru.egov.urm.action.release;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.dist.DistItemInfo;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.storage.FileSet;

public class ActionDistVerifier extends ActionBase {

	public ActionDistVerifier( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeScope( ActionScope scope ) throws Exception {
		Dist dist = scope.release;
		FileSet set = dist.getFiles( this );
		MetaRelease info = dist.info;
		
		// delete empty top folders, top directories are deliveries only
		for( String dirPath : set.dirList ) {
			FileSet dir = set.getDirByPath( this , dirPath );
			if( dir == null )
				exit( "unknown dir " + dirPath );
			
			// find delivery
			if( !dir.isEmpty() ) {
				MetaReleaseDelivery delivery = info.getDeliveryByFolder( this , dir.dirName );
				if( delivery.isEmpty() ) {
					log( "distributive folder=" + dir.dirName + " is expected to be empty" );
					super.setFailed();
 					return( true );
				}
			}
		}

		// check all deliveries are stored in distributive
		for( MetaReleaseDelivery delivery : info.getDeliveries( this ).values() ) {
			if( !delivery.isEmpty() ) {
				FileSet dir = set.getDirByPath( this , delivery.distDelivery.FOLDER );
				if( dir == null ) {
					log( "delivery folder=" + delivery.distDelivery.FOLDER + " is missing" );
					return( false );
				}
				
				if( dir.isEmpty() && !delivery.isEmpty() ) {
					log( "delivery folder=" + delivery.distDelivery.FOLDER + " is empty" );
					super.setFailed();
 					return( true );
				}
				
				if( !checkFinalizeItems( dist , delivery ) )
					return( true );
				
				if( !checkFinalizeDatabase( dist , delivery ) )
					return( true );

				if( !checkFinalizeConfiguration( dist , delivery ) )
					return( true );
			}
		}
		
		return( true );
	}

	private boolean checkFinalizeItems( Dist dist , MetaReleaseDelivery delivery ) throws Exception {
		FileSet set = dist.getFiles( this );
		Map<String,MetaDistrBinaryItem> items = new HashMap<String,MetaDistrBinaryItem>(); 
		
		// find distributive files
		for( MetaReleaseTargetItem item : delivery.getProjectItems( this ).values() ) {
			MetaDistrBinaryItem distItem = item.distItem;
			DistItemInfo info = dist.getDistItemInfo( this , distItem , false );
			if( !info.found ) {
				log( "unable to find in delivery=" + delivery.distDelivery.NAME + " - item=" + distItem.KEY );
				return( false );
			}
			
			items.put( info.fileName , distItem );
		}
		
		// check all distributive files have been matched
		for( String fileName : set.files.keySet() ) {
			if( !items.containsKey( fileName ) ) {
				log( "unexpected distributive file=" + fileName + " in folder=" + delivery.distDelivery.FOLDER );
				return( false );
			}
		}
		
		return( true );
	}
	
	private boolean checkFinalizeDatabase( Dist dist , MetaReleaseDelivery delivery ) throws Exception {
		return( false );
	}
	
	private boolean checkFinalizeConfiguration( Dist dist , MetaReleaseDelivery delivery ) throws Exception {
		return( false );
	}

}
