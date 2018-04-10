package org.urm.meta.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist.VersionInfo;
import org.urm.meta.product.Meta;

public class ReleaseRepository {

	public static String MASTER_NAME_PRIMARY = "primary";
	
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PRODUCT = "product";
	
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
	public ProductReleases releases;
	
	public int ID;
	public String NAME;
	public String DESC;
	
	private Map<Integer,Release> mapReleasesById;
	private Map<String,Release> mapReleasesNormal;
	private Map<String,Release> mapReleasesMaster;
	private Map<Integer,Release> mapReleasesArchived;
	
	private boolean modifyState;
	
	public ReleaseRepository( Meta meta , ProductReleases releases ) {
		this.meta = meta;
		this.releases = releases;
		mapReleasesById = new HashMap<Integer,Release>();
		mapReleasesNormal = new HashMap<String,Release>();
		mapReleasesMaster = new HashMap<String,Release>();
		mapReleasesArchived = new HashMap<Integer,Release>();
		modifyState = false;
	}

	public ReleaseRepository copy( Meta rmeta , ProductReleases rreleases ) throws Exception {
		ReleaseRepository r = new ReleaseRepository( rmeta , rreleases );
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
	
	public Release findRelease( String RELEASEVER ) {
		try {
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			Release release = mapReleasesNormal.get( version );
			if( release != null )
				return( release );
			
			for( Release releaseArchived : mapReleasesArchived.values() ) {
				if( !releaseArchived.isMaster() ) {
					if( version.equals( releaseArchived.RELEASEVER ) )
						return( releaseArchived );
				}
			}
		}
		catch( Throwable e ) {
			meta.engine.log( "version" , e );
		}
		return( null );
	}

	public Release findDefaultMaster() {
		return( findMaster( ReleaseRepository.MASTER_NAME_PRIMARY ) );
	}
	
	public Release findMaster( String name ) {
		Release release = mapReleasesMaster.get( name );
		if( release != null )
			return( release );
		
		for( Release releaseArchived : mapReleasesArchived.values() ) {
			if( releaseArchived.isMaster() ) {
				if( name.equals( releaseArchived.NAME ) )
					return( releaseArchived );
			}
		}
		return( null );
	}
	
	public synchronized String[] getActiveVersions() {
		return( VersionInfo.orderVersions( Common.getSortedKeys( mapReleasesNormal ) ) );
	}

	public Release getNextRelease( String RELEASEVER ) {
		try {
			String[] versions = getActiveVersions();
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			int index = Common.getIndexOf( versions , version );
			if( index >= 0 && index < versions.length - 1 )
				return( mapReleasesNormal.get( versions[ index + 1 ] ) );
		}		
		catch( Throwable e ) {
		}
		return( null );	
	}

	public synchronized void addRelease( Release release ) {
		if( release.isArchived() )
			mapReleasesArchived.put( release.ID , release );
		else
		if( release.MASTER )
			mapReleasesMaster.put( release.NAME , release );
		else
			mapReleasesNormal.put( release.RELEASEVER , release );
		mapReleasesById.put( release.ID , release );
	}

	public synchronized void removeRelease( Release release ) {
		if( release.isArchived() )
			mapReleasesArchived.remove( release.ID );
		else
		if( release.isMaster() )
			mapReleasesMaster.remove( release.NAME );
		else
			mapReleasesNormal.remove( release.RELEASEVER );
		mapReleasesById.put( release.ID , release );
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
		mapReleasesArchived.put( release.ID , release );
	}
	
	public Release getRelease( int id ) throws Exception {
		return( mapReleasesById.get( id ) );
	}

	public ReleaseDist findReleaseDist( Dist dist ) {
		if( dist.isMaster() ) {
			Release release = mapReleasesMaster.get( dist.release.NAME );
			if( release == null )
				return( null );
			return( release.getDefaultReleaseDist() );
		}
		
		Release release = mapReleasesById.get( dist.release.ID );
		if( release == null )
			return( null );
		
		return( release.findDistVariant( dist.releaseDist.DIST_VARIANT ) ); 
	}
	
}
