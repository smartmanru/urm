package org.urm.db.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist._Error;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductPolicy;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class DBReleaseRepository {

	public static Release createReleaseNormal( EngineMethod method , ActionBase action , Meta meta , ReleaseRepository repo , String RELEASELABEL , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , meta , RELEASELABEL );
		
		lc = getLifecycle( action , meta , lc , info.getLifecycleType() );
		releaseDate = getReleaseDate( action , meta , info.RELEASEVER , releaseDate , lc );
		if( releaseDate == null )
			action.exit1( _Error.MissingReleaseDate1 , "unable to create release label=" + RELEASELABEL + " due to missing release date" , RELEASELABEL );

		action.debug( "create normal release: label=" + RELEASELABEL + ", version=" + info.RELEASEVER + ", date=" + Common.getDateValue( releaseDate ) + " ..." );
		
		// create meta item
		Release release = DBRelease.createRelease( method , action , meta , repo , info.RELEASEVER , releaseDate , lc );
		ReleaseDist releaseDist = DBReleaseDist.createReleaseDist( method , action , release , info.VARIANT );
		
		// create distributive
		DistRepository distrepo = meta.getDistRepository();
		DistRepositoryItem item = distrepo.findItem( info.RELEASEVER );
		if( item != null )
			action.exit1( _Error.ReleaseAlreadyExists1 , "release label=" + RELEASELABEL + " already exists" , RELEASELABEL );
		
		item = distrepo.createRepositoryItem( action , RELEASELABEL );
		Dist dist = item.createDistNormal( action , releaseDist );
		
		return( dist.release );
	}
	
	public static ReleaseLifecycle getLifecycle( ActionBase action , Meta meta , ReleaseLifecycle lc , DBEnumLifecycleType type ) throws Exception {
		MetaProductPolicy policy = meta.getPolicy();
		
		if( type == DBEnumLifecycleType.MAJOR ) {
			Integer expected = policy.getMajorId( action );
			if( expected == null ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					if( expected != lc.ID )
						action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" + lc.NAME , lc.NAME );
					return( lc );
				}
				
				EngineLifecycles lifecycles = action.getServerReleaseLifecycles();
				return( lifecycles.getLifecycle( expected ) );
			}
		}
		else
		if( type == DBEnumLifecycleType.MINOR ) {
			Integer expected = policy.getMinorId( action );
			if( expected == null ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					if( expected != lc.ID )
						action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" + lc.NAME , lc.NAME );
					return( lc );
				}
				
				EngineLifecycles lifecycles = action.getServerReleaseLifecycles();
				return( lifecycles.getLifecycle( expected ) );
			}
		}
		else
		if( type == DBEnumLifecycleType.URGENT ) {
			MatchItem[] expected = policy.LC_URGENT_LIST;
			if( expected.length == 0 ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					for( int k = 0; k < expected.length; k++ ) {
						if( expected[ k ].FKID == lc.ID )
							return( lc );
					}
					action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" + lc.NAME , lc.NAME );
				}
				
				action.exit0( _Error.MissingReleasecycleType0 , "Missing release cycle type" );
			}
		}
		
		return( null );
	}
	
	private static Date getReleaseDate( ActionBase action , Meta meta , String RELEASEVER , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		if( releaseDate != null )
			return( releaseDate );
		
		Date date = ReleaseLifecycle.findReleaseDate( action , RELEASEVER , meta , lc );
		if( date == null )
			action.exit0( _Error.MissingReleaseDate0 , "Missing release date" );
		return( null );
	}

}
