package org.urm.engine.dist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTicketSet {

	public Meta meta;
	public ReleaseChanges changes;

	public String CODE;
	public String NAME;
	public String COMMENTS;
	public VarTICKETSETSTATUS status;
	
	private List<ReleaseTicket> items;
	private Map<String,ReleaseTicket> map;
	private List<ReleaseTicketSetTarget> targets;
	
	public ReleaseTicketSet( Meta meta , ReleaseChanges changes ) {
		this.meta = meta; 
		this.changes = changes;
		items = new LinkedList<ReleaseTicket>();
		map = new HashMap<String,ReleaseTicket>();
		targets = new LinkedList<ReleaseTicketSetTarget>();
	}

	public ReleaseTicketSet copy( ActionBase action , Meta meta , ReleaseChanges changes ) throws Exception {
		ReleaseTicketSet r = new ReleaseTicketSet( meta , changes );

		r.CODE = CODE;
		r.NAME = NAME;
		r.COMMENTS = COMMENTS;
		r.status = status;
		
		for( ReleaseTicket ticket : items ) {
			ReleaseTicket rticket = ticket.copy( action , meta , r );
			r.addTicket( rticket );
		}
		
		for( ReleaseTicketSetTarget target : targets ) {
			ReleaseTicketSetTarget rtarget = target.copy( action , meta , r );
			r.addTarget( rtarget );
		}
		
		return( r );
	}

	private void addTicket( ReleaseTicket ticket ) {
		items.add( ticket );
		map.put( ticket.CODE , ticket );
	}
	
	private void removeTicket( ReleaseTicket ticket ) {
		map.remove( ticket );
		items.remove( ticket );
	}

	private void reorderTickets( ActionBase action ) throws Exception {
		int pos = 1;
		for( ReleaseTicket ticketUpdate : items ) {
			ticketUpdate.setPos( action , pos );
			pos++;
		}
	}
	
	private void addTarget( ReleaseTicketSetTarget target ) {
		targets.add( target );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		items.clear();
		map.clear();
		targets.clear();
		
		CODE = ConfReader.getRequiredAttrValue( root , Release.PROPERTY_TICKETSETCODE );
		NAME = Meta.getNameAttr( action , root , VarNAMETYPE.ANY );
		COMMENTS = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETSETCOMMENTS );
		String STATUS = ConfReader.getAttrValue( root , Release.PROPERTY_TICKETSETSTATUS );
		status = Types.getTicketSetStatus( STATUS , true );
		
		Node[] items = ConfReader.xmlGetChildren( root , Release.ELEMENT_TICKET );
		if( items != null ) {
			int pos = 1;
			for( Node ticketNode : items ) {
				ReleaseTicket ticket = new ReleaseTicket( meta , this , pos );
				ticket.load( action , ticketNode );
				addTicket( ticket );
				pos++;
			}
		}
		
		items = ConfReader.xmlGetChildren( root , Release.ELEMENT_TICKETSETTARGET );
		if( items != null ) {
			for( Node targetNode : items ) {
				ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this );
				target.load( action , targetNode );
				addTarget( target );
			}
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETSETCODE , CODE );
		Meta.setNameAttr( action , doc , root , VarNAMETYPE.ANY , NAME );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETSETCOMMENTS , COMMENTS );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETSETSTATUS , Common.getEnumLower( status ) );
		
		for( ReleaseTicket ticket : items ) {
			Element ticketElement = Common.xmlCreateElement( doc , root , Release.ELEMENT_TICKET );
			ticket.save( action , doc , ticketElement );
		}

		for( ReleaseTicketSetTarget target : targets ) {
			Element targetElement = Common.xmlCreateElement( doc , root , Release.ELEMENT_TICKETSETTARGET );
			target.save( action , doc , targetElement );
		}
	}

	public void create( ActionBase action , String code , String name , String comments ) throws Exception {
		this.CODE = code;
		this.NAME = name;
		this.COMMENTS = comments;
		status = VarTICKETSETSTATUS.NEW;
	}
	
	public void createTicket( ActionBase action , VarTICKETTYPE type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		ReleaseTicket ticket = new ReleaseTicket( meta , this , items.size() + 1 );
		ticket.create( action , type , code , name , link , comments , owner , devdone );
		addTicket( ticket );
	}
	
	public void modify( ActionBase action , String code , String name , String comments ) throws Exception {
		this.CODE = code;
		this.NAME = name;
		this.COMMENTS = comments;
	}

	public void descope( ActionBase action ) throws Exception {
		for( ReleaseTicket ticket : items )
			ticket.descope( action );
		status = VarTICKETSETSTATUS.DESCOPED;
	}

	public ReleaseTicket[] getTickets() {
		if( items.isEmpty() )
			return( new ReleaseTicket[0] );
		return( items.toArray( new ReleaseTicket[0] ) );
	}

	public ReleaseTicketSetTarget[] getTargets() {
		if( targets.isEmpty() )
			return( new ReleaseTicketSetTarget[0] );
		return( targets.toArray( new ReleaseTicketSetTarget[0] ) );
	}

	public ReleaseTicket[] getActiveTickets() {
		List<ReleaseTicket> tickets = new LinkedList<ReleaseTicket>();
		for( ReleaseTicket ticket : items ) {
			if( !ticket.isDescoped() )
				tickets.add( ticket );
		}
		return( tickets.toArray( new ReleaseTicket[0] ) );
	}
	
	public ReleaseTicket findTicket( String code ) {
		return( map.get( code ) );
	}

	public ReleaseTicket getTicket( ActionBase action , int POS ) throws Exception {
		if( POS < 1 || POS > items.size() )
			action.exitUnexpectedState();
		return( items.get( POS - 1 ) );
	}

	public void modifyTicket( ActionBase action , ReleaseTicket ticket , VarTICKETTYPE type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		map.remove( ticket.CODE );
		ticket.modify( action , type , code , name , link , comments , owner , devdone );
		map.put( ticket.CODE , ticket );
	}

	public void dropTicket( ActionBase action , int ticketPos , boolean descope ) throws Exception {
		ReleaseTicket ticket = getTicket( action , ticketPos );
			
		if( descope )
			ticket.descope( action );
		else {
			removeTicket( ticket );
			reorderTickets( action );
		}
	}
	
	public void moveTicket( ActionBase action , ReleaseTicket ticket , ReleaseTicketSet newSet ) throws Exception {
		removeTicket( ticket );
		reorderTickets( action );
		
		newSet.addTicket( ticket );
		newSet.reorderTickets( action );
	}

	public boolean isNew() {
		if( status == VarTICKETSETSTATUS.NEW )
			return( true );
		return( false );
	}
	
	public boolean isActive() {
		if( status == VarTICKETSETSTATUS.ACTIVE )
			return( true );
		return( false );
	}
	
	public boolean isDescoped() {
		if( status == VarTICKETSETSTATUS.DESCOPED )
			return( true );
		return( false );
	}

	public boolean isRunning() {
		if( status != VarTICKETSETSTATUS.NEW )
			return( true );
		
		for( ReleaseTicket ticket : items ) {
			if( ticket.isRunning() )
				return( true );
		}
		return( false );
	}
	
	public boolean isCompleted() {
		for( ReleaseTicket ticket : items ) {
			if( !ticket.isCompleted() )
				return( false );
		}
		return( true );
	}
	
	public boolean isAccepted() {
		for( ReleaseTicket ticket : items ) {
			if( !ticket.isAccepted() )
				return( false );
		}
		return( true );
	}

	public void acceptAll( ActionBase action ) throws Exception {
		if( status == VarTICKETSETSTATUS.NEW )
			status = VarTICKETSETSTATUS.ACTIVE;
		
		for( ReleaseTicket ticket : items ) {
			if( !ticket.isAccepted() )
				ticket.accept( action );
		}
	}

	public void setDevDone( ActionBase action , ReleaseTicket ticket ) throws Exception {
		ticket.setDevDone( action );
	}
	
	public void setTicketVerified( ActionBase action , ReleaseTicket ticket ) throws Exception {
		ticket.setVerified( action );
	}
	
	public void createTarget( ActionBase action , MetaSourceProjectSet projectSet ) throws Exception {
		ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this );
		target.create( action , projectSet );
		addTarget( target );
	}
	
	public void createTarget( ActionBase action , MetaSourceProject project , String[] items ) throws Exception {
		if( items == null ) {
			ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this );
			target.create( action , project , true );
			addTarget( target );
		}
		else
		if( items.length == 0 ) {
			ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this );
			target.create( action , project , false );
			addTarget( target );
		}
		else {
			for( String item : items ) {
				MetaSourceProjectItem projectItem = project.getItem( action , item );
				if( projectItem.distItem == null )
					continue;
				
				ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this );
				target.create( action , projectItem.distItem );
				addTarget( target );
			}
		}
	}
	
}
