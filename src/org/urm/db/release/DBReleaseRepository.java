package org.urm.db.release;

import java.sql.ResultSet;
import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist._Error;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductPolicy;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseRepository.ReleaseOperation;

public class DBReleaseRepository {

	public static Release createReleaseNormal( EngineMethod method , ActionBase action , ReleaseRepository repo , ReleaseLabelInfo info , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		Meta meta = repo.meta;

		if( repo.findRelease( info.RELEASEVER ) != null )
			action.exit1( _Error.ReleaseAlreadyExists1 , "release version=" + info.RELEASEVER + " already exists" , info.RELEASEVER );
		
		lc = getLifecycle( action , meta , lc , info.getLifecycleType() );
		releaseDate = getReleaseDate( action , meta , info.RELEASEVER , releaseDate , lc );
		if( releaseDate == null )
			action.exit1( _Error.MissingReleaseDate1 , "unable to create release version=" + info.RELEASEVER + " due to missing release date" , info.RELEASEVER );

		action.debug( "create normal release: version=" + info.RELEASEVER + ", version=" + info.RELEASEVER + ", date=" + Common.getDateValue( releaseDate ) + " ..." );
		
		// create meta item
		Release release = DBRelease.createRelease( method , action , repo , info.RELEASEVER , releaseDate , lc );
		repo.addRelease( release );
		
		return( release );
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

	public static void loaddbReleases( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		loaddbReleasesMain( loader , repo );
		loaddbReleasesDist( loader , repo );
	}
	
	public static void loaddbReleasesMain( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseMain;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPOACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				Release release = DBRelease.loaddbRelease( loader , repo , rs );
				repo.addRelease( release );
				break;
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void loaddbReleasesDist( EngineLoader loader , ReleaseRepository repo ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseDist;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_REL_REPORELEASEACTIVE1 , 
				new String[] { EngineDB.getInteger( repo.ID ) 
				} );
		try {
			while( rs.next() ) {
				int releaseId = entity.loaddbObject( rs , DBReleaseData.FIELD_RELEASE_ID );
				Release release = repo.getRelease( releaseId );
				ReleaseDist releaseDist = DBReleaseDist.loaddbReleaseDist( loader , release , rs );
				if( releaseDist.isDefault() )
					release.setDefaultDist( releaseDist );
				else
					release.addDist( releaseDist );
				break;
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void dropRelease( EngineMethod method , ActionBase action , ReleaseRepository repo , Release release ) throws Exception {
	}

	public static void archiveRelease( EngineMethod method , ActionBase action , ReleaseRepository repo , Release release ) throws Exception {
	}
	
	public static void copyScope( EngineMethod method , ActionBase action , ReleaseRepository repo , Release release , Release dst ) throws Exception {
	}
	
	public static void addDistAction( EngineMethod method , Release release , ReleaseDist releaseDist , boolean success , ReleaseOperation op , String msg ) throws Exception {
	}
	
}
