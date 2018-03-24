package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumTicketType;
import org.urm.meta.release.ReleaseTicketSet;

public class DBReleaseChanges {

	public void createSet( String code , String name , String comments ) throws Exception {
		if( findSet( code ) != null )
			Common.exitUnexpected();
		
		ReleaseTicketSet set = new ReleaseTicketSet( release , this );
		set.create( code , name , comments );
		addSet( set );
	}
	
	public void createTicket( ActionBase action , ReleaseTicketSet set , DBEnumTicketType type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		set.createTicket( action , type , code , name , link , comments , owner , devdone );
	}
	
	public void modifySet( ActionBase action , ReleaseTicketSet set , String code , String name , String comments ) throws Exception {
		ReleaseTicketSet current = findSet( code );
		if( current != null && current != set )
			action.exitUnexpectedState();

		if( current == null )
			removeSet( set );
		set.modify( action , code , name , comments );
		if( current == null )
			addSet( set );
	}
	
	public void dropSet( ActionBase action , ReleaseTicketSet set , boolean descope ) throws Exception {
		if( descope )
			set.descope( action );
		else {
			if( !set.isNew() )
				action.exitUnexpectedState();
			removeSet( set );
		}
	}

}
