package org.urm.meta.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist.VersionInfo;
import org.urm.meta.product.Meta;

public class ReleaseRepository {

	public static String MASTER_NAME_PRIMARY = "primary";
	
	public enum ReleaseOperation {
		CREATE ,
		DROP ,
		FINISH ,
		REOPEN ,
		COMPLETE ,
		PHASE ,
		MODIFY ,
		BUILD ,
		PUT ,
		ARCHIVE ,
		STATUS
	};
	
	public Meta meta;
	
	public int ID;
	public String NAME;
	public String DESC;
	
	private Map<Integer,Release> mapReleasesById;
	private Map<String,Release> mapReleasesNormal;
	private Map<String,Release> mapReleasesMaster;
	
	private boolean modifyState;
	
	public ReleaseRepository( Meta meta ) {
		this.meta = meta;
		
		mapReleasesById = new HashMap<Integer,Release>();
		mapReleasesNormal = new HashMap<String,Release>();
		mapReleasesMaster = new HashMap<String,Release>();
		modifyState = false;
	}

	public ReleaseRepository copy( Meta rmeta ) throws Exception {
		ReleaseRepository r = new ReleaseRepository( rmeta );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		
		for( Release release : mapReleasesById.values() ) {
			Release rrelease = release.copy( r );
			r.addRelease( rrelease );
		}
		return( r );
	}
	
	public synchronized void modify( boolean done ) throws Exception {
		if( !done ) {
			if( modifyState )
				Common.exitUnexpected();
			modifyState = true;
		}
		else {
			if( !modifyState )
				Common.exitUnexpected();
			modifyState = false;
		}
	}
	
	public Release[] getReleases() {
		return( mapReleasesById.values().toArray( new Release[0] ) );
	}
	
	public void createRepository( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}

	public Release findReleaseByLabel( ActionBase action , String RELEASELABEL ) {
		try {
			ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , meta, RELEASELABEL );
			return( mapReleasesNormal.get( info.RELEASEVER ) );
		}
		catch( Throwable e ) {
			return( null );
		}
	}

	public Release findRelease( int id ) {
		return( mapReleasesById.get( id ) ); 
	}
	
	public Release findReleaseByFullVersion( String RELEASEVER ) {
		return( mapReleasesById.get( RELEASEVER ) ); 
	}
	
	public Release findDefaultMaster() {
		return( findMaster( ReleaseRepository.MASTER_NAME_PRIMARY ) );
	}
	
	public Release findMaster( String name ) {
		Release release = mapReleasesMaster.get( name );
		if( release != null )
			return( release );
		return( null );
	}
	
	public synchronized String[] getActiveVersions() {
		return( VersionInfo.orderVersions( Common.getSortedKeys( mapReleasesNormal ) ) );
	}

	public synchronized void addRelease( Release release ) {
		if( release.isArchived() )
			return;
		
		if( release.MASTER )
			mapReleasesMaster.put( release.NAME , release );
		else
			mapReleasesNormal.put( release.RELEASEVER , release );
		mapReleasesById.put( release.ID , release );
	}

	public synchronized void removeRelease( Release release ) {
		if( release.isMaster() )
			mapReleasesMaster.remove( release.NAME );
		else
			mapReleasesNormal.remove( release.RELEASEVER );
		mapReleasesById.remove( release.ID );
	}

	public void replaceRelease( Release release ) throws Exception {
		if( !mapReleasesById.containsKey( release.ID ) )
			Common.exitUnexpected();
		
		addRelease( release );
	}
	
	public void archiveRelease( Release release ) throws Exception {
		if( !mapReleasesById.containsKey( release.ID ) )
			Common.exitUnexpected();
		
		if( release.isMaster() )
			mapReleasesMaster.remove( release.NAME );
		else
			mapReleasesNormal.remove( release.RELEASEVER );
	}
	
	public Release getRelease( int id ) throws Exception {
		return( mapReleasesById.get( id ) );
	}

}
