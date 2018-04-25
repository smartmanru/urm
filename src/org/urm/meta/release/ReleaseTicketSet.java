package org.urm.meta.release;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumDistTargetType;
import org.urm.db.core.DBEnums.DBEnumTicketSetStatusType;
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
	public DBEnumTicketSetStatusType STATUS;
	public int RV;
	
	private List<ReleaseTicket> tickets;
	private Map<String,ReleaseTicket> map;
	private List<ReleaseTicketTarget> targets;
	
	public ReleaseTicketSet( Release release , ReleaseChanges changes ) {
		this.release = release; 
		this.changes = changes;
		
		tickets = new LinkedList<ReleaseTicket>();
		map = new HashMap<String,ReleaseTicket>();
		targets = new LinkedList<ReleaseTicketTarget>();
	}

	public ReleaseTicketSet copy( Release rrelease , ReleaseChanges rchanges ) throws Exception {
		ReleaseTicketSet r = new ReleaseTicketSet( rrelease , rchanges );

		r.ID = ID;
		r.CODE = CODE;
		r.NAME = NAME;
		r.DESC = DESC;
		r.STATUS = STATUS;
		r.RV = RV;
		
		for( ReleaseTicket ticket : tickets ) {
			ReleaseTicket rticket = ticket.copy( rrelease , r );
			r.addTicket( rticket );
		}
		
		for( ReleaseTicketTarget target : targets ) {
			ReleaseTicketTarget rtarget = target.copy( rrelease , r );
			r.addTarget( rtarget );
		}
		
		return( r );
	}

	public void addTicket( ReleaseTicket ticket ) {
		tickets.add( ticket );
		map.put( ticket.CODE , ticket );
	}
	
	public void updateTicket( ReleaseTicket ticket ) throws Exception {
		Common.changeMapKey( map , ticket , ticket.CODE );
	}
	
	public void removeTicket( ReleaseTicket ticket ) {
		map.remove( ticket.CODE );
		tickets.remove( ticket );
	}

	public void removeTarget( ReleaseTicketTarget target ) {
		targets.remove( target );
	}

	public void reorderTickets() throws Exception {
		int pos = 1;
		for( ReleaseTicket ticketUpdate : tickets ) {
			ticketUpdate.setPos( pos );
			pos++;
		}
	}
	
	public void reorderTargets() throws Exception {
		int pos = 1;
		for( ReleaseTicketTarget targetUpdate : targets ) {
			targetUpdate.setPos( pos );
			pos++;
		}
	}
	
	public void addTarget( ReleaseTicketTarget target ) {
		targets.add( target );
	}

	public void create( String code , String name , String comments , DBEnumTicketSetStatusType status ) throws Exception {
		this.CODE = code;
		this.NAME = name;
		this.DESC = comments;
		this.STATUS = status;
	}
	
	public void create( String code , String name , String comments ) throws Exception {
		create( code , name , comments , DBEnumTicketSetStatusType.NEW );
	}
	
	public void modify( String code , String name , String comments ) throws Exception {
		this.CODE = code;
		this.NAME = name;
		this.DESC = comments;
	}

	public void descope() throws Exception {
		STATUS = DBEnumTicketSetStatusType.DESCOPED;
	}

	public ReleaseTicket[] getTickets() {
		if( tickets.isEmpty() )
			return( new ReleaseTicket[0] );
		return( tickets.toArray( new ReleaseTicket[0] ) );
	}

	public ReleaseTicketTarget[] getTargets() {
		if( targets.isEmpty() )
			return( new ReleaseTicketTarget[0] );
		return( targets.toArray( new ReleaseTicketTarget[0] ) );
	}

	public ReleaseTicket findTicket( String code ) {
		return( map.get( code ) );
	}

	public ReleaseTicket getTicketByPos( int POS ) throws Exception {
		for( ReleaseTicket ticket : tickets ) {
			if( ticket.POS == POS )
				return( ticket );
		}
		Common.exitUnexpected();
		return( null );
	}

	public ReleaseTicketTarget getTargetByPos( int POS ) throws Exception {
		for( ReleaseTicketTarget target : targets ) {
			if( target.POS == POS )
				return( target );
		}
		Common.exitUnexpected();
		return( null );
	}

	public ReleaseTicketTarget findTarget( MetaSourceProjectSet set ) {
		for( ReleaseTicketTarget target : targets ) {
			if( target.isProjectSet() ) {
				MetaSourceProjectSet targetSet = target.findProjectSet();
				if( targetSet.ID == set.ID )
					return( target );
			}
		}
		return( null );
	}

	public ReleaseTicketTarget findTarget( int id ) {
		for( ReleaseTicketTarget target : targets ) {
			if( target.ID == id )
				return( target );
		}
		return( null );
	}
	
	public ReleaseTicketTarget findTarget( MetaSourceProject project ) {
		for( ReleaseTicketTarget target : targets ) {
			if( target.isProject() ) {
				MetaSourceProject targetProject = target.findProject();
				if( targetProject.ID == project.ID )
					return( target );
			}
		}
		return( null );
	}
	
	public void moveTicket( ReleaseTicket ticket , ReleaseTicketSet newSet ) throws Exception {
		removeTicket( ticket );
		reorderTickets();
		
		ticket.setSet( newSet );
		newSet.addTicket( ticket );
		newSet.reorderTickets();
	}

	public ReleaseTicket copyTicket( ReleaseTicket ticket ) throws Exception {
		if( findTicket( ticket.CODE ) != null ) {
			String releasever = changes.release.RELEASEVER;
			Common.exit3( _Error.DuplicateReleaseTicket3 , "Duplicate ticket release=" + releasever + ", set=" + CODE + ", ticket=" + ticket.CODE , releasever , CODE , ticket.CODE );
		}
		
		ReleaseTicket ticketNew = ticket.copyNew( release , this );
		int lastPos = getLastTicketPos();
		ticketNew.setNew();
		ticketNew.setPos( lastPos + 1 );
		return( ticketNew );
	}

	public boolean isNew() {
		if( STATUS == DBEnumTicketSetStatusType.NEW )
			return( true );
		return( false );
	}
	
	public boolean isActive() {
		if( STATUS == DBEnumTicketSetStatusType.ACTIVE )
			return( true );
		return( false );
	}
	
	public boolean isDescoped() {
		if( STATUS == DBEnumTicketSetStatusType.DESCOPED )
			return( true );
		return( false );
	}

	public boolean isRunning() {
		if( STATUS != DBEnumTicketSetStatusType.NEW )
			return( true );
		
		for( ReleaseTicket ticket : tickets ) {
			if( ticket.isRunning() )
				return( true );
		}
		return( false );
	}
	
	public boolean isCompleted() {
		for( ReleaseTicket ticket : tickets ) {
			if( !ticket.isCompleted() )
				return( false );
		}
		return( true );
	}
	
	public boolean isAccepted() {
		for( ReleaseTicket ticket : tickets ) {
			if( !ticket.isAccepted() )
				return( false );
		}
		for( ReleaseTicketTarget target : targets ) {
			if( !target.isAccepted() )
				return( false );
		}
		return( true );
	}

	public void activate() throws Exception {
		if( STATUS == DBEnumTicketSetStatusType.NEW )
			STATUS = DBEnumTicketSetStatusType.ACTIVE;
	}

	public void createTarget( MetaDistrDelivery delivery , DBEnumDistTargetType type , String[] items ) throws Exception {
		int pos = targets.size() + 1;
		if( items == null ) {
			ReleaseTicketTarget target = new ReleaseTicketTarget( release , this );
			ReleaseDistTarget deliveryTarget = new ReleaseDistTarget( changes );
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
				ReleaseDistTarget deliveryTarget = new ReleaseDistTarget( changes );
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

	public int getLastTicketPos() {
		int pos = 0;
		for( ReleaseTicket ticket : tickets ) {
			if( ticket.POS > pos )
				pos = ticket.POS;
		}
		return( pos );
	}

	public int getLastTargetPos() {
		int pos = 0;
		for( ReleaseTicketTarget target : targets ) {
			if( target.POS > pos )
				pos = target.POS;
		}
		return( pos );
	}

	public void sortTickets() {
		Map<String,ReleaseTicket> map = new HashMap<String,ReleaseTicket>();
		for( ReleaseTicket ticket : tickets ) {
			String key = Common.appendZeros( ticket.POS , 10 );
			map.put( key , ticket );
		}
		
		tickets.clear();
		for( String key : Common.getSortedKeys( map ) ) {
			ReleaseTicket ticket = map.get( key );
			tickets.add( ticket );
		}
	}
	
	public void sortTargets() {
		Map<String,ReleaseTicketTarget> map = new HashMap<String,ReleaseTicketTarget>();
		for( ReleaseTicketTarget target : targets ) {
			String key = Common.appendZeros( target.POS , 10 );
			map.put( key , target );
		}
		
		targets.clear();
		for( String key : Common.getSortedKeys( map ) ) {
			ReleaseTicketTarget target = map.get( key );
			targets.add( target );
		}
	}
	
}
