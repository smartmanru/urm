package org.urm.meta.release;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist._Error;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTicketSet {

	public static String PROPERTY_CODE = "code";
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_STATUS = "status";
	
	public Meta meta;
	public ReleaseChanges changes;

	public String CODE;
	public String NAME;
	public String COMMENTS;
	public DBEnumTicketSetStatusType status;
	
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
		map.remove( ticket.CODE );
		items.remove( ticket );
	}

	private void removeTarget( ReleaseTicketSetTarget target ) {
		targets.remove( target );
	}

	private void reorderTickets( ActionBase action ) throws Exception {
		int pos = 1;
		for( ReleaseTicket ticketUpdate : items ) {
			ticketUpdate.setPos( action , pos );
			pos++;
		}
	}
	
	private void reorderTargets( ActionBase action ) throws Exception {
		int pos = 1;
		for( ReleaseTicketSetTarget targetUpdate : targets ) {
			targetUpdate.setPos( action , pos );
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
		
		CODE = ConfReader.getRequiredAttrValue( root , PROPERTY_CODE );
		NAME = Meta.getNameAttr( action , root , EnumNameType.ANY );
		COMMENTS = ConfReader.getAttrValue( root , PROPERTY_DESC );
		String STATUS = ConfReader.getAttrValue( root , PROPERTY_STATUS );
		status = DBEnumTicketSetStatusType.getValue( STATUS , true );
		
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
			int pos = 1;
			for( Node targetNode : items ) {
				ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this , pos );
				target.load( action , targetNode );
				addTarget( target );
				pos++;
			}
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , PROPERTY_CODE , CODE );
		Meta.setNameAttr( action , doc , root , EnumNameType.ANY , NAME );
		Common.xmlSetElementAttr( doc , root , PROPERTY_DESC , COMMENTS );
		Common.xmlSetElementAttr( doc , root , PROPERTY_STATUS , Common.getEnumLower( status ) );
		
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
		status = DBEnumTicketSetStatusType.NEW;
	}
	
	public void createTicket( ActionBase action , DBEnumTicketType type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
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
		status = DBEnumTicketSetStatusType.DESCOPED;
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

	public ReleaseTicket findTicket( String code ) {
		return( map.get( code ) );
	}

	public ReleaseTicket getTicket( ActionBase action , int POS ) throws Exception {
		if( POS < 1 || POS > items.size() )
			action.exitUnexpectedState();
		return( items.get( POS - 1 ) );
	}

	public ReleaseTicketSetTarget getTarget( ActionBase action , int POS ) throws Exception {
		if( POS < 1 || POS > targets.size() )
			action.exitUnexpectedState();
		return( targets.get( POS - 1 ) );
	}

	public void modifyTicket( ActionBase action , ReleaseTicket ticket , DBEnumTicketType type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
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
	
	public void dropTarget( ActionBase action , int ticketPos , boolean descope ) throws Exception {
		ReleaseTicketSetTarget target = getTarget( action , ticketPos );
			
		if( descope )
			target.descope( action );
		else {
			removeTarget( target );
			reorderTargets( action );
		}
	}
	
	public void moveTicket( ActionBase action , ReleaseTicket ticket , ReleaseTicketSet newSet ) throws Exception {
		removeTicket( ticket );
		reorderTickets( action );
		
		newSet.addTicket( ticket );
		newSet.reorderTickets( action );
	}

	public void copyTicket( ActionBase action , ReleaseTicket ticket , ReleaseTicketSet newSet ) throws Exception {
		if( newSet.findTicket( ticket.CODE ) != null ) {
			String release = newSet.changes.release.RELEASEVER;
			action.exit3( _Error.DuplicateReleaseTicket3 , "Duplicate ticket release=" + release + ", set=" + newSet.CODE + ", ticket=" + ticket.CODE , release , newSet.CODE , ticket.CODE );
		}
		
		ReleaseTicket ticketNew = ticket.copyNew( action , meta , newSet ); 
		newSet.addTicket( ticketNew );
		newSet.reorderTickets( action );
	}

	public boolean isNew() {
		if( status == DBEnumTicketSetStatusType.NEW )
			return( true );
		return( false );
	}
	
	public boolean isActive() {
		if( status == DBEnumTicketSetStatusType.ACTIVE )
			return( true );
		return( false );
	}
	
	public boolean isDescoped() {
		if( status == DBEnumTicketSetStatusType.DESCOPED )
			return( true );
		return( false );
	}

	public boolean isRunning() {
		if( status != DBEnumTicketSetStatusType.NEW )
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
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isAccepted() )
				return( false );
		}
		return( true );
	}

	public void activate( ActionBase action ) throws Exception {
		if( status == DBEnumTicketSetStatusType.NEW )
			status = DBEnumTicketSetStatusType.ACTIVE;
	}

	public void setDevDone( ActionBase action , ReleaseTicket ticket ) throws Exception {
		ticket.setDevDone( action );
	}
	
	public void setTicketVerified( ActionBase action , ReleaseTicket ticket ) throws Exception {
		ticket.setVerified( action );
	}
	
	public void createTarget( ActionBase action , MetaSourceProjectSet projectSet ) throws Exception {
		int pos = targets.size() + 1;
		ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this , pos );
		target.create( action , projectSet );
		addTarget( target );
	}
	
	public void createTarget( ActionBase action , MetaSourceProject project , String[] items ) throws Exception {
		int pos = targets.size() + 1;
		if( items == null ) {
			ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this , pos );
			target.create( action , project , true );
			addTarget( target );
		}
		else
		if( items.length == 0 ) {
			ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this , pos );
			target.create( action , project , false );
			addTarget( target );
		}
		else {
			for( String item : items ) {
				MetaSourceProjectItem projectItem = project.getItem( item );
				if( projectItem.distItem == null )
					continue;
				
				ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this , pos );
				target.create( action , projectItem.distItem );
				addTarget( target );
			}
		}
	}

	public void createTarget( ActionBase action , MetaDistrDelivery delivery , DBEnumReleaseTargetType type , String[] items ) throws Exception {
		int pos = targets.size() + 1;
		if( items == null ) {
			ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this , pos );
			if( type == DBEnumReleaseTargetType.DISTITEM )
				target.create( action , delivery , DBEnumReleaseTargetType.DELIVERYBINARIES );
			else
			if( type == DBEnumReleaseTargetType.CONFITEM )
				target.create( action , delivery , DBEnumReleaseTargetType.DELIVERYCONFS );
			else
			if( type == DBEnumReleaseTargetType.SCHEMA )
				target.create( action , delivery , DBEnumReleaseTargetType.DELIVERYDATABASE );
			else
			if( type == DBEnumReleaseTargetType.DOC )
				target.create( action , delivery , DBEnumReleaseTargetType.DELIVERYDOC );
			else
				Common.exitUnexpected();
			addTarget( target );
		}
		else {
			for( String item : items ) {
				ReleaseTicketSetTarget target = new ReleaseTicketSetTarget( meta , this , pos );
				if( type == DBEnumReleaseTargetType.DISTITEM ) {
					MetaDistrBinaryItem binaryItem = delivery.getBinaryItem( item );
					target.create( action , binaryItem );
				}
				else
				if( type == DBEnumReleaseTargetType.CONFITEM ) {
					MetaDistrConfItem confItem = delivery.getConfItem( item );
					target.create( action , confItem );
				}
				else
				if( type == DBEnumReleaseTargetType.SCHEMA ) {
					MetaDatabaseSchema schemaItem = delivery.getSchema( item );
					target.create( action , delivery , schemaItem );
				}
				else
				if( type == DBEnumReleaseTargetType.DOC ) {
					MetaProductDoc docItem = delivery.getDoc( item );
					target.create( action , delivery , docItem );
				}
				addTarget( target );
			}
		}
	}

	public boolean references( MetaSourceProjectSet set ) {
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.descoped && target.accepted )
				continue;
			if( target.isEqualTo( set ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaSourceProject project ) {
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.descoped && target.accepted )
				continue;
			if( target.isEqualTo( project ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaSourceProjectItem item ) {
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.descoped && target.accepted )
				continue;
			if( target.isEqualTo( item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrBinaryItem item ) {
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.descoped && target.accepted )
				continue;
			if( target.references( item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrConfItem item ) {
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.descoped && target.accepted )
				continue;
			if( target.references( item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrDelivery delivery , MetaDatabaseSchema item ) {
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.descoped && target.accepted )
				continue;
			if( target.references( delivery , item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrDelivery delivery , MetaProductDoc doc ) {
		for( ReleaseTicketSetTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.descoped && target.accepted )
				continue;
			if( target.references( delivery , doc ) )
				return( true );
		}
		return( false );
	}
	
}