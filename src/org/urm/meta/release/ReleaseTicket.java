package org.urm.meta.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

	public Meta meta;
	public ReleaseTicketSet set;

	public int POS;
	public String CODE;
	public String NAME;
	public String LINK;
	public String OWNER;
	public String DEV;
	public String QA;
	public String COMMENTS;
	public DBEnumTicketType type;
	public DBEnumTicketStatusType status;
	public boolean active;
	public boolean accepted;
	public boolean descoped;
	
	public ReleaseTicket( Meta meta , ReleaseTicketSet set , int pos ) {
		this.meta = meta; 
		this.set = set;
		this.POS = pos;
	}

	public ReleaseTicket copy( ActionBase action , Meta meta , ReleaseTicketSet set ) throws Exception {
		ReleaseTicket r = new ReleaseTicket( meta , set , POS );

		r.CODE = CODE;
		r.NAME = NAME;
		r.LINK = LINK;
		r.OWNER = OWNER;
		r.DEV = DEV;
		r.QA = QA;
		r.COMMENTS = COMMENTS;
		r.type = type;
		r.status = status;
		r.active = active;
		r.accepted = accepted;
		r.descoped = descoped;
		
		return( r );
	}

	public ReleaseTicket copyNew( ActionBase action , Meta meta , ReleaseTicketSet set ) throws Exception {
		ReleaseTicket ticket = copy( action , meta , set );
		ticket.active = false;
		ticket.accepted = false;
		ticket.descoped = false;
		return( ticket );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		CODE = ConfReader.getRequiredAttrValue( root , PROPERTY_CODE );
		NAME = Meta.getNameAttr( action , root , EnumNameType.ANY );
		LINK = ConfReader.getAttrValue( root , PROPERTY_LINK );
		OWNER = ConfReader.getAttrValue( root , PROPERTY_OWNER );
		DEV = ConfReader.getAttrValue( root , PROPERTY_DEV );
		QA = ConfReader.getAttrValue( root , PROPERTY_QA );
		COMMENTS = ConfReader.getAttrValue( root , PROPERTY_DESC );
		String TYPE = ConfReader.getAttrValue( root , PROPERTY_TYPE );
		type = DBEnumTicketType.getValue( TYPE , false );
		String STATUS = ConfReader.getAttrValue( root , PROPERTY_STATUS );
		status = DBEnumTicketStatusType.getValue( STATUS , true );
		active = ConfReader.getBooleanAttrValue( root , PROPERTY_ACTIVE , false );
		accepted = ConfReader.getBooleanAttrValue( root , PROPERTY_ACCEPTED , false );
		descoped = ConfReader.getBooleanAttrValue( root , PROPERTY_DESCOPED , false );
		if( accepted )
			active = true;
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , PROPERTY_CODE , CODE );
		Meta.setNameAttr( action , doc , root , EnumNameType.ANY , NAME );
		Common.xmlSetElementAttr( doc , root , PROPERTY_LINK , LINK );
		Common.xmlSetElementAttr( doc , root , PROPERTY_OWNER , OWNER );
		Common.xmlSetElementAttr( doc , root , PROPERTY_DEV , DEV );
		Common.xmlSetElementAttr( doc , root , PROPERTY_QA , QA );
		Common.xmlSetElementAttr( doc , root , PROPERTY_DESC , COMMENTS );
		Common.xmlSetElementAttr( doc , root , PROPERTY_TYPE , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , PROPERTY_STATUS , Common.getEnumLower( status ) );
		Common.xmlSetElementAttr( doc , root , PROPERTY_ACTIVE , Common.getBooleanValue( active ) );
		Common.xmlSetElementAttr( doc , root , PROPERTY_ACCEPTED , Common.getBooleanValue( accepted ) );
		Common.xmlSetElementAttr( doc , root , PROPERTY_DESCOPED , Common.getBooleanValue( descoped ) );
	}

	public void accept( ActionBase action ) throws Exception {
		accepted = true;
		active = true;
	}

	public void descope( ActionBase action ) throws Exception {
		if( !descoped ) {
			if( set.isActive() )
				accepted = false;
			
			descoped = true;
		}
	}

	public void create( ActionBase action , DBEnumTicketType type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		this.type = type;
		this.CODE = code;
		this.NAME = name;
		this.LINK = link;
		this.COMMENTS = comments;
		type = DBEnumTicketType.CHANGE;
		this.OWNER = owner;
		this.QA = "";
		this.active = false;
		this.accepted = false;
		this.descoped = false;
		if( devdone ) {
			this.DEV = owner;
			status = DBEnumTicketStatusType.DEVDONE;
		}
		else {
			this.DEV = "";
			status = DBEnumTicketStatusType.NEW;
		}
	}
	
	public void modify( ActionBase action , DBEnumTicketType type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		this.type = type;
		this.CODE = code;
		this.NAME = name;
		this.LINK = link;
		this.COMMENTS = comments;
		this.OWNER = owner;
		
		if( devdone ) {
			this.DEV = owner;
			status = DBEnumTicketStatusType.DEVDONE;
		}
		else {
			this.DEV = "";
			status = DBEnumTicketStatusType.NEW;
		}
	}

	public void setPos( ActionBase action , int pos ) throws Exception {
		this.POS = pos;
	}

	public boolean isAccepted() {
		return( accepted );
	}

	public boolean isDescoped() {
		return( descoped );
	}

	public boolean isCompleted() {
		if( accepted && ( status == DBEnumTicketStatusType.QADONE || descoped ) )
			return( true );
		return( false );
	}

	public boolean isNew() {
		if( status == DBEnumTicketStatusType.NEW )
			return( true );
		return( false );
	}

	public boolean isDevDone() {
		if( status == DBEnumTicketStatusType.DEVDONE || status == DBEnumTicketStatusType.QADONE )
			return( true );
		return( false );
	}

	public boolean isQaDone() {
		if( status == DBEnumTicketStatusType.QADONE )
			return( true );
		return( false );
	}

	public boolean isRunning() {
		if( active )
			return( true );
		return( false );
	}

	public void setDevDone( ActionBase action ) throws Exception {
		if( isRunning() && isNew() ) {
			status = DBEnumTicketStatusType.DEVDONE;
			DEV = action.getUserName();
		}
	}
	
	public void setVerified( ActionBase action ) throws Exception {
		if( isRunning() && isDevDone() ) {
			status = DBEnumTicketStatusType.QADONE;
			QA = action.getUserName();
		}
	}
	
}
