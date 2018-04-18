package org.urm.meta.release;

import org.urm.db.core.DBEnums.*;
import org.urm.engine.AuthService;
import org.urm.meta.engine.AuthUser;
import org.urm.meta.loader.MatchItem;

public class ReleaseTicket {

	public static String PROPERTY_POS = "pos";
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
	
	public void setSet( ReleaseTicketSet set ) {
		this.set = set;
	}
	
	public void accept() throws Exception {
		ACCEPTED = true;
		ACTIVE = true;
	}

	public void descope() throws Exception {
		if( !DESCOPED ) {
			if( set.isActive() )
				ACCEPTED = false;
			
			DESCOPED = true;
		}
	}

	public void create( int pos , String code , String name , String comments , String link , DBEnumTicketType type , DBEnumTicketStatusType status ,  
			boolean active , boolean accepted , boolean descoped , MatchItem owner , MatchItem dev , MatchItem qa ) throws Exception {
		this.POS = pos;
		this.CODE = code;
		this.NAME = name;
		this.DESC = comments;
		this.LINK = link;
		this.TYPE = type;
		this.TICKETSTATUS = status; 
		this.ACTIVE = active;
		this.ACCEPTED = accepted;
		this.DESCOPED = descoped;
		this.OWNER = MatchItem.copy( owner );
		this.DEV = MatchItem.copy( dev );
		this.QA = MatchItem.copy( qa );
	}
	
	public void create( int pos , DBEnumTicketType type , String code , String name , String link , String comments , Integer owner , boolean devdone ) throws Exception {
		this.POS = pos;
		this.TYPE = type;
		this.CODE = code;
		this.NAME = name;
		this.LINK = link;
		this.DESC = comments;
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

	public void setNew() {
		TICKETSTATUS = DBEnumTicketStatusType.NEW;
		ACTIVE = false;
		ACCEPTED = false;
		DESCOPED = false;
		DEV = null;
		QA = null;
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

	public void setDevDone( Integer userId ) throws Exception {
		if( isRunning() && isNew() ) {
			TICKETSTATUS = DBEnumTicketStatusType.DEVDONE;
			DEV = MatchItem.create( userId );
		}
	}
	
	public void setVerified( Integer userId ) throws Exception {
		if( isRunning() && isDevDone() ) {
			TICKETSTATUS = DBEnumTicketStatusType.QADONE;
			QA = MatchItem.create( userId );
		}
	}

	public AuthUser findOwner() {
		AuthService auth = release.repo.meta.engine.getAuth();
		return( auth.findUser( OWNER ) );
	}
	
	public AuthUser findDev() {
		AuthService auth = release.repo.meta.engine.getAuth();
		return( auth.findUser( DEV ) );
	}
	
	public AuthUser findQA() {
		AuthService auth = release.repo.meta.engine.getAuth();
		return( auth.findUser( QA ) );
	}
	
}
