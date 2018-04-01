package org.urm.meta.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;

public class ReleaseChanges {

	public Release release;

	private Map<String,ReleaseTicketSet> sets;
	private Map<Integer,ReleaseTicketSet> setsById;
	
	public ReleaseChanges( Release release ) {
		this.release = release;
		sets = new HashMap<String,ReleaseTicketSet>();
		setsById = new HashMap<Integer,ReleaseTicketSet>();
	}

	public ReleaseChanges copy( Release rrelease ) throws Exception {
		ReleaseChanges r = new ReleaseChanges( rrelease );
		
		for( ReleaseTicketSet set : sets.values() ) {
			ReleaseTicketSet rset = set.copy( rrelease , r );
			r.addSet( rset );
		}
		
		return( r );
	}

	public void addSet( ReleaseTicketSet set ) {
		sets.put( set.CODE , set );
		setsById.put( set.ID , set );
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
	
}
