package org.urm.meta.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist._Error;

public class ReleaseChanges {

	public Release release;

	private Map<String,ReleaseTicketSet> sets;
	
	public ReleaseChanges( Release release ) {
		this.release = release;
		sets = new HashMap<String,ReleaseTicketSet>();
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
	}
	
	public void removeSet( ReleaseTicketSet set ) {
		sets.remove( set.CODE );
	}
	
	public String[] getSetCodes() {
		return( Common.getSortedKeys( sets ) );
	}
	
	public ReleaseTicketSet getSet( ActionBase action , String code ) throws Exception {
		ReleaseTicketSet set = sets.get( code );
		if( set == null )
			action.exit2( _Error.UnknownReleaseTicketSet2 , "Unknown set=" + code + " in release=" + release.RELEASEVER , code , release.RELEASEVER );
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

	public void setDevDone( ActionBase action , ReleaseTicket ticket ) throws Exception {
		ReleaseTicketSet set = ticket.set;
		set.setDevDone( action , ticket );
	}
	
}
