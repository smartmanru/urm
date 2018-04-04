package org.urm.meta.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;

public class ReleaseChanges {

	public Release release;

	private Map<String,ReleaseTicketSet> sets;
	private Map<Integer,ReleaseTicketSet> setsById;
	private Map<Integer,ReleaseBuildTarget> buildTargetMapById;
	private Map<Integer,ReleaseDistTarget> distTargetMapById;
	
	public ReleaseChanges( Release release ) {
		this.release = release;
		sets = new HashMap<String,ReleaseTicketSet>();
		setsById = new HashMap<Integer,ReleaseTicketSet>();
		buildTargetMapById = new HashMap<Integer,ReleaseBuildTarget>(); 
		distTargetMapById = new HashMap<Integer,ReleaseDistTarget>();
	}

	public void copy( Release rrelease , ReleaseChanges r ) throws Exception {
		for( ReleaseBuildTarget target : buildTargetMapById.values() ) {
			ReleaseBuildTarget rtarget = target.copy( r , null );
			r.addBuildTarget( rtarget );
		}
		
		for( ReleaseDistTarget target : distTargetMapById.values() ) {
			ReleaseDistTarget rtarget = target.copy( r , null );
			r.addDistTarget( rtarget );
		}
		
		for( ReleaseTicketSet set : sets.values() ) {
			ReleaseTicketSet rset = set.copy( rrelease , r );
			r.addSet( rset );
		}
	}

	public ReleaseTicketSet[] getSets() {
		return( setsById.values().toArray( new ReleaseTicketSet [0] ) );
	}
	
	public void addSet( ReleaseTicketSet set ) {
		sets.put( set.CODE , set );
		setsById.put( set.ID , set );
	}
	
	public void addBuildTarget( ReleaseBuildTarget target ) {
		buildTargetMapById.put( target.ID , target );
	}
	
	public void addDistTarget( ReleaseDistTarget target ) {
		distTargetMapById.put( target.ID , target );
	}
	
	public void removeBuildTarget( ReleaseBuildTarget target ) {
		buildTargetMapById.remove( target.ID );
	}
	
	public void removeDistTarget( ReleaseDistTarget target ) {
		distTargetMapById.remove( target.ID );
	}
	
	public void removeSet( ReleaseTicketSet set ) {
		sets.remove( set.CODE );
		setsById.remove( set.ID );
	}
	
	public String[] getSetCodes() {
		return( Common.getSortedKeys( sets ) );
	}
	
	public ReleaseTicketSet getSet( String code ) throws Exception {
		ReleaseTicketSet set = sets.get( code );
		if( set == null )
			Common.exit2( _Error.UnknownReleaseTicketSet2 , "Unknown set=" + code + " in release=" + release.RELEASEVER , code , release.RELEASEVER );
		return( set );
	}

	public ReleaseTicketSet getSet( int id ) throws Exception {
		ReleaseTicketSet set = setsById.get( id );
		if( set == null )
			Common.exit2( _Error.UnknownReleaseTicketSet2 , "Unknown set=" + id + " in release=" + release.RELEASEVER , "" + id , release.RELEASEVER );
		return( set );
	}

	public ReleaseTicketSet findSet( String code ) {
		return( sets.get( code ) );
	}

	public boolean isCompleted() {
		for( ReleaseTicketSet set : sets.values() ) {
			if( !set.isCompleted() )
				return( false );
		}
		return( true );
	}

	public void updateSet( ReleaseTicketSet set ) throws Exception {
		Common.changeMapKey( sets , set , set.CODE );
	}

	public ReleaseBuildTarget findBuildTarget( int id ) {
		return( buildTargetMapById.get( id ) );
	}
	
	public ReleaseDistTarget findDistTarget( int id ) {
		return( distTargetMapById.get( id ) );
	}
	
	public ReleaseBuildTarget getBuildTarget( int id ) throws Exception {
		ReleaseBuildTarget target = buildTargetMapById.get( id );
		if( target == null )
			Common.exit2( _Error.UnknownReleaseBuildTarget2 , "Unknown target=" + id + " in release=" + release.RELEASEVER , "" + id , release.RELEASEVER );
		return( target );
	}
	
	public ReleaseDistTarget getDistTarget( int id ) throws Exception {
		ReleaseDistTarget target = distTargetMapById.get( id );
		if( target == null )
			Common.exit2( _Error.UnknownReleaseDistTarget2 , "Unknown target=" + id + " in release=" + release.RELEASEVER , "" + id , release.RELEASEVER );
		return( target );
	}
	
}
