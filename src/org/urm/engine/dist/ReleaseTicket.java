package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTicket {

	public Meta meta;
	public ReleaseTicketSet set;

	public int POS;
	public String CODE;
	public String NAME;
	public String LINK;
	public String OWNER;
	public String QA;
	public String COMMENTS;
	public VarTICKETTYPE type;
	public VarTICKETSTATUS status;
	public boolean accepted;
	
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
		r.QA = QA;
		r.COMMENTS = COMMENTS;
		r.type = type;
		r.status = status;
		r.accepted = accepted;
		
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		CODE = ConfReader.getRequiredAttrValue( root , Release.PROPERTY_TICKETCODE );
		NAME = Meta.getNameAttr( action , root , VarNAMETYPE.ANY );
		LINK = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETLINK );
		OWNER = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETOWNER );
		QA = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETQA );
		COMMENTS = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETCOMMENTS );
		String TYPE = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETTYPE );
		type = Types.getTicketType( TYPE , false );
		String STATUS = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETSTATUS );
		status = Types.getTicketStatus( STATUS , true );
		accepted = ConfReader.getBooleanAttrValue( root , Release.PROPERTY_TICKETACCEPTED , true );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETCODE , CODE );
		Meta.setNameAttr( action , doc , root , VarNAMETYPE.ANY , NAME );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETLINK , LINK );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETOWNER , OWNER );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETQA , QA );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETCOMMENTS , COMMENTS );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTYPE , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETSTATUS , Common.getEnumLower( status ) );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETACCEPTED , Common.getBooleanValue( accepted ) );
	}

	public void setDescoped( ActionBase action ) throws Exception {
		status = VarTICKETSTATUS.DESCOPED;
		accepted = false;
	}

	public boolean isDescoped() {
		if( status == VarTICKETSTATUS.DESCOPED )
			return( true );
		return( false );
	}

	public void create( ActionBase action , VarTICKETTYPE type , String code , String name , String link , String comments ) throws Exception {
		this.type = type;
		this.CODE = code;
		this.NAME = name;
		this.LINK = link;
		this.COMMENTS = comments;
		type = VarTICKETTYPE.CHANGE;
		status = VarTICKETSTATUS.NEW;
		this.OWNER = "";
		this.QA = "";
		this.accepted = false;
	}
	
	public void modify( ActionBase action , VarTICKETTYPE type , String code , String name , String link , String comments ) throws Exception {
		this.type = type;
		this.CODE = code;
		this.NAME = name;
		this.LINK = link;
		this.COMMENTS = comments;
	}

	public void setPos( ActionBase action , int pos ) throws Exception {
		this.POS = pos;
	}

	public boolean isCompleted() {
		if( !accepted )
			return( false );
		
		if( status == VarTICKETSTATUS.QADONE || status == VarTICKETSTATUS.DESCOPED )
			return( true );
			
		return( false );
	}

	public boolean isAccepted() {
		return( accepted );
	}

	public boolean isRunning() {
		if( status == VarTICKETSTATUS.NEW )
			return( false );
		return( true );
	}
	
}
