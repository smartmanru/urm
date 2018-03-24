package org.urm.meta.release;

import org.urm.action.ActionBase;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.MatchItem;

public class ReleaseTicket {

	public static String PROPERTY_CODE = "code";
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_LINK = "link";
	public static String PROPERTY_OWNER = "owner";
	public static String PROPERTY_DEV = "dev";
	public static String PROPERTY_QA = "qa";
	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_STATUS = "status";
	public static String PROPERTY_ACTIVE = "active";
	public static String PROPERTY_ACCEPTED = "accepted";
	public static String PROPERTY_DESCOPED = "descoped";

	public Release release;
	public ReleaseTicketSet set;

	public int ID;
	public int POS;
	public String CODE;
	public String NAME;
	public String DESC;
	public String LINK;
	public DBEnumTicketType TYPE;
	public DBEnumTicketStatusType TICKETSTATUS;
	public boolean ACTIVE;
	public boolean ACCEPTED;
	public boolean DESCOPED;
	public MatchItem OWNER;
	public MatchItem DEV;
	public MatchItem QA;
	public int RV;
	
	public ReleaseTicket( Release release , ReleaseTicketSet set ) {
		this.release = release; 
		this.set = set;
	}

	public ReleaseTicket copy( Release release , ReleaseTicketSet set ) throws Exception {
		ReleaseTicket r = new ReleaseTicket( release , set );

		r.ID = ID;
		r.POS = POS;
		r.CODE = CODE;
		r.NAME = NAME;
		r.DESC = DESC;
		r.LINK = LINK;
		r.TYPE = TYPE;
		r.TICKETSTATUS = TICKETSTATUS;
		r.ACTIVE = ACTIVE;
		r.ACCEPTED = ACCEPTED;
		r.DESCOPED = DESCOPED;
		r.OWNER = OWNER;
		r.DEV = DEV;
		r.QA = QA;
		r.RV = RV;
		
		return( r );
	}

	public ReleaseTicket copyNew( Release release , ReleaseTicketSet set ) throws Exception {
		ReleaseTicket ticket = copy( release , set );
		ticket.ACTIVE = false;
		ticket.ACCEPTED = false;
		ticket.DESCOPED = false;
		return( ticket );
	}
	
	public void accept( ActionBase action ) throws Exception {
		ACCEPTED = true;
		ACTIVE = true;
	}

	public void descope( ActionBase action ) throws Exception {
		if( !DESCOPED ) {
			if( set.isActive() )
				ACCEPTED = false;
			
			DESCOPED = true;
		}
	}

	public void create( DBEnumTicketType type , String code , String name , String link , String comments , Integer owner , boolean devdone ) throws Exception {
		this.TYPE = type;
		this.CODE = code;
		this.NAME = name;
		this.LINK = link;
		this.DESC = comments;
		type = DBEnumTicketType.CHANGE;
		this.OWNER = MatchItem.create( owner );
		this.QA = null;
		this.ACTIVE = false;
		this.ACCEPTED = false;
		this.DESCOPED = false;
		
		if( devdone ) {
			this.DEV = this.OWNER;
			TICKETSTATUS = DBEnumTicketStatusType.DEVDONE;
		}
		else {
			this.DEV = null;
			TICKETSTATUS = DBEnumTicketStatusType.NEW;
		}
	}
	
	public void modify( DBEnumTicketType type , String code , String name , String link , String comments , Integer owner , boolean devdone ) throws Exception {
		this.TYPE = type;
		this.CODE = code;
		this.NAME = name;
		this.LINK = link;
		this.DESC = comments;
		this.OWNER = MatchItem.create( owner );
		
		if( devdone ) {
			this.DEV = this.OWNER;
			TICKETSTATUS = DBEnumTicketStatusType.DEVDONE;
		}
		else {
			this.DEV = null;
			TICKETSTATUS = DBEnumTicketStatusType.NEW;
		}
	}

	public void setPos( int pos ) throws Exception {
		this.POS = pos;
	}

	public boolean isAccepted() {
		return( ACCEPTED );
	}

	public boolean isDescoped() {
		return( DESCOPED );
	}

	public boolean isCompleted() {
		if( ACCEPTED && ( TICKETSTATUS == DBEnumTicketStatusType.QADONE || DESCOPED ) )
			return( true );
		return( false );
	}

	public boolean isNew() {
		if( TICKETSTATUS == DBEnumTicketStatusType.NEW )
			return( true );
		return( false );
	}

	public boolean isDevDone() {
		if( TICKETSTATUS == DBEnumTicketStatusType.DEVDONE || TICKETSTATUS == DBEnumTicketStatusType.QADONE )
			return( true );
		return( false );
	}

	public boolean isQaDone() {
		if( TICKETSTATUS == DBEnumTicketStatusType.QADONE )
			return( true );
		return( false );
	}

	public boolean isRunning() {
		if( ACTIVE )
			return( true );
		return( false );
	}

	public void setDevDone( ActionBase action ) throws Exception {
		if( isRunning() && isNew() ) {
			TICKETSTATUS = DBEnumTicketStatusType.DEVDONE;
			DEV = MatchItem.create( action.getUserId() );
		}
	}
	
	public void setVerified( ActionBase action ) throws Exception {
		if( isRunning() && isDevDone() ) {
			TICKETSTATUS = DBEnumTicketStatusType.QADONE;
			QA = MatchItem.create( action.getUserId() );
		}
	}
	
}
