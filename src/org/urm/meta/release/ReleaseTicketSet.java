package org.urm.meta.release;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist._Error;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;

public class ReleaseTicketSet {

	public static String PROPERTY_CODE = "code";
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_STATUS = "status";
	
	public Release release;
	public ReleaseChanges changes;

	public int ID;
	public String CODE;
	public String NAME;
	public String DESC;
	public DBEnumTicketSetStatusType TYPE;
	public int RV;
	
	private List<ReleaseTicket> items;
	private Map<String,ReleaseTicket> map;
	private List<ReleaseTicketTarget> targets;
	
	public ReleaseTicketSet( Release release , ReleaseChanges changes ) {
		this.release = release; 
		this.changes = changes;
		
		items = new LinkedList<ReleaseTicket>();
		map = new HashMap<String,ReleaseTicket>();
		targets = new LinkedList<ReleaseTicketTarget>();
	}

	public ReleaseTicketSet copy( Release rrelease , ReleaseChanges rchanges ) throws Exception {
		ReleaseTicketSet r = new ReleaseTicketSet( rrelease , rchanges );

		r.ID = ID;
		r.CODE = CODE;
		r.NAME = NAME;
		r.DESC = DESC;
		r.TYPE = TYPE;
		r.RV = RV;
		
		for( ReleaseTicket ticket : items ) {
			ReleaseTicket rticket = ticket.copy( rrelease , r );
			r.addTicket( rticket );
		}
		
		for( ReleaseTicketTarget target : targets ) {
			ReleaseTicketTarget rtarget = target.copy( rrelease , r );
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

	private void removeTarget( ReleaseTicketTarget target ) {
		targets.remove( target );
	}

	private void reorderTickets( ActionBase action ) throws Exception {
		int pos = 1;
		for( ReleaseTicket ticketUpdate : items ) {
			ticketUpdate.setPos( pos );
			pos++;
		}
	}
	
	private void reorderTargets( ActionBase action ) throws Exception {
		int pos = 1;
		for( ReleaseTicketTarget targetUpdate : targets ) {
			targetUpdate.setPos( pos );
			pos++;
		}
	}
	
	private void addTarget( ReleaseTicketTarget target ) {
		targets.add( target );
	}

	public void create( String code , String name , String comments ) throws Exception {
		this.CODE = code;
		this.NAME = name;
		this.DESC = comments;
		TYPE = DBEnumTicketSetStatusType.NEW;
	}
	
	public void createTicket( ActionBase action , DBEnumTicketType type , String code , String name , String link , String desc , Integer owner , boolean devdone ) throws Exception {
		ReleaseTicket ticket = new ReleaseTicket( release , this );
		ticket.setPos( items.size() + 1 );
		ticket.create( type , code , name , link , desc , owner , devdone );
		addTicket( ticket );
	}
	
	public void modify( ActionBase action , String code , String name , String comments ) throws Exception {
		this.CODE = code;
		this.NAME = name;
		this.DESC = comments;
	}

	public void descope( ActionBase action ) throws Exception {
		for( ReleaseTicket ticket : items )
			ticket.descope( action );
		TYPE = DBEnumTicketSetStatusType.DESCOPED;
	}

	public ReleaseTicket[] getTickets() {
		if( items.isEmpty() )
			return( new ReleaseTicket[0] );
		return( items.toArray( new ReleaseTicket[0] ) );
	}

	public ReleaseTicketTarget[] getTargets() {
		if( targets.isEmpty() )
			return( new ReleaseTicketTarget[0] );
		return( targets.toArray( new ReleaseTicketTarget[0] ) );
	}

	public ReleaseTicket findTicket( String code ) {
		return( map.get( code ) );
	}

	public ReleaseTicket getTicket( ActionBase action , int POS ) throws Exception {
		if( POS < 1 || POS > items.size() )
			action.exitUnexpectedState();
		return( items.get( POS - 1 ) );
	}

	public ReleaseTicketTarget getTarget( ActionBase action , int POS ) throws Exception {
		if( POS < 1 || POS > targets.size() )
			action.exitUnexpectedState();
		return( targets.get( POS - 1 ) );
	}

	public void modifyTicket( ActionBase action , ReleaseTicket ticket , DBEnumTicketType type , String code , String name , String link , String desc , Integer owner , boolean devdone ) throws Exception {
		map.remove( ticket.CODE );
		ticket.modify( type , code , name , link , desc , owner , devdone );
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
		ReleaseTicketTarget target = getTarget( action , ticketPos );
			
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
		
		ReleaseTicket ticketNew = ticket.copyNew( release , newSet ); 
		newSet.addTicket( ticketNew );
		newSet.reorderTickets( action );
	}

	public boolean isNew() {
		if( TYPE == DBEnumTicketSetStatusType.NEW )
			return( true );
		return( false );
	}
	
	public boolean isActive() {
		if( TYPE == DBEnumTicketSetStatusType.ACTIVE )
			return( true );
		return( false );
	}
	
	public boolean isDescoped() {
		if( TYPE == DBEnumTicketSetStatusType.DESCOPED )
			return( true );
		return( false );
	}

	public boolean isRunning() {
		if( TYPE != DBEnumTicketSetStatusType.NEW )
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
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isAccepted() )
				return( false );
		}
		return( true );
	}

	public void activate( ActionBase action ) throws Exception {
		if( TYPE == DBEnumTicketSetStatusType.NEW )
			TYPE = DBEnumTicketSetStatusType.ACTIVE;
	}

	public void setDevDone( ActionBase action , ReleaseTicket ticket ) throws Exception {
		ticket.setDevDone( action );
	}
	
	public void setTicketVerified( ActionBase action , ReleaseTicket ticket ) throws Exception {
		ticket.setVerified( action );
	}
	
	public void createTarget( ActionBase action , MetaSourceProjectSet projectSet ) throws Exception {
		int pos = targets.size() + 1;
		ReleaseTicketTarget target = new ReleaseTicketTarget( release , this );
		ReleaseBuildTarget buildTarget = new ReleaseBuildTarget( release );
		buildTarget.create( projectSet , false );
		target.create( buildTarget , pos );
		addTarget( target );
	}
	
	public void createTarget( ActionBase action , MetaSourceProject project , String[] items ) throws Exception {
		int pos = targets.size() + 1;
		if( items == null ) {
			ReleaseTicketTarget target = new ReleaseTicketTarget( release , this );
			ReleaseBuildTarget buildTarget = new ReleaseBuildTarget( release );
			buildTarget.create( project , true );
			target.create( buildTarget , pos );
			addTarget( target );
		}
		else
		if( items.length == 0 ) {
			ReleaseTicketTarget target = new ReleaseTicketTarget( release , this );
			ReleaseBuildTarget buildTarget = new ReleaseBuildTarget( release );
			buildTarget.create( project , false );
			target.create( buildTarget , pos );
			addTarget( target );
		}
		else {
			for( String item : items ) {
				MetaSourceProjectItem projectItem = project.getItem( item );
				if( projectItem.distItem == null )
					continue;
				
				ReleaseTicketTarget target = new ReleaseTicketTarget( release , this );
				ReleaseDistTarget deliveryTarget = new ReleaseDistTarget( release );
				deliveryTarget.create( projectItem.distItem );
				target.create( deliveryTarget , pos );
				addTarget( target );
			}
		}
	}

	public void createTarget( ActionBase action , MetaDistrDelivery delivery , DBEnumDistTargetType type , String[] items ) throws Exception {
		int pos = targets.size() + 1;
		if( items == null ) {
			ReleaseTicketTarget target = new ReleaseTicketTarget( release , this );
			ReleaseDistTarget deliveryTarget = new ReleaseDistTarget( release );
			if( type == DBEnumDistTargetType.BINARYITEM )
				deliveryTarget.create( delivery , DBEnumDistTargetType.DELIVERYBINARIES );
			else
			if( type == DBEnumDistTargetType.CONFITEM )
				deliveryTarget.create( delivery , DBEnumDistTargetType.DELIVERYCONFS );
			else
			if( type == DBEnumDistTargetType.SCHEMA )
				deliveryTarget.create( delivery , DBEnumDistTargetType.DELIVERYDATABASE );
			else
			if( type == DBEnumDistTargetType.DOC )
				deliveryTarget.create( delivery , DBEnumDistTargetType.DELIVERYDOC );
			else
				Common.exitUnexpected();
			target.create( deliveryTarget , pos );
			addTarget( target );
		}
		else {
			for( String item : items ) {
				ReleaseTicketTarget target = new ReleaseTicketTarget( release , this );
				ReleaseDistTarget deliveryTarget = new ReleaseDistTarget( release );
				if( type == DBEnumDistTargetType.BINARYITEM ) {
					MetaDistrBinaryItem binaryItem = delivery.getBinaryItem( item );
					deliveryTarget.create( binaryItem );
				}
				else
				if( type == DBEnumDistTargetType.CONFITEM ) {
					MetaDistrConfItem confItem = delivery.getConfItem( item );
					deliveryTarget.create( confItem );
				}
				else
				if( type == DBEnumDistTargetType.SCHEMA ) {
					MetaDatabaseSchema schemaItem = delivery.getSchema( item );
					deliveryTarget.create( delivery , schemaItem );
				}
				else
				if( type == DBEnumDistTargetType.DOC ) {
					MetaProductDoc docItem = delivery.getDoc( item );
					deliveryTarget.create( delivery , docItem );
				}
				target.create( deliveryTarget , pos );
				addTarget( target );
			}
		}
	}

	public boolean references( MetaSourceProjectSet set ) {
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.DESCOPED && target.ACCEPTED )
				continue;
			if( target.isEqualTo( set ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaSourceProject project ) {
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.DESCOPED && target.ACCEPTED )
				continue;
			if( target.isEqualTo( project ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaSourceProjectItem item ) {
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.DESCOPED && target.ACCEPTED )
				continue;
			if( target.isEqualTo( item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrBinaryItem item ) {
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.DESCOPED && target.ACCEPTED )
				continue;
			if( target.references( item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrConfItem item ) {
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.DESCOPED && target.ACCEPTED )
				continue;
			if( target.references( item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrDelivery delivery , MetaDatabaseSchema item ) {
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.DESCOPED && target.ACCEPTED )
				continue;
			if( target.references( delivery , item ) )
				return( true );
		}
		return( false );
	}
	
	public boolean references( MetaDistrDelivery delivery , MetaProductDoc doc ) {
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isActive() )
				continue;
			if( target.DESCOPED && target.ACCEPTED )
				continue;
			if( target.references( delivery , doc ) )
				return( true );
		}
		return( false );
	}
	
}
