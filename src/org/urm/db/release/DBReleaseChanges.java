package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumTicketType;
import org.urm.meta.release.ReleaseTicketSet;

public class DBReleaseChanges {

	public void createSet( String code , String name , String comments ) throws Exception {
		Common.exitUnexpected();
	}
	
	public void createTicket( ActionBase action , ReleaseTicketSet set , DBEnumTicketType type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		Common.exitUnexpected();
	}
	
	public void modifySet( ActionBase action , ReleaseTicketSet set , String code , String name , String comments ) throws Exception {
		Common.exitUnexpected();
	}
	
	public void dropSet( ActionBase action , ReleaseTicketSet set , boolean descope ) throws Exception {
		Common.exitUnexpected();
	}

}
