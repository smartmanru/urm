package org.urm.meta.release;

import org.urm.action.ActionBase;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;

public class ReleaseTicketTarget {

	public static String PROPERTY_POS = "pos";
	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_ITEM = "item";
	public static String PROPERTY_ACCEPTED = "accepted";
	public static String PROPERTY_DESCOPED = "descoped";
	
	public Release release;
	public ReleaseTicketSet set;
	
	public int ID;
	public int POS;
	public Integer BUILDTARGET_ID;
	public Integer DELIVERYTARGET_ID;
	public boolean ACCEPTED;
	public boolean DESCOPED;
	public int RV;

	private ReleaseBuildTarget buildTarget;
	private ReleaseDistTarget deliveryTarget;
	
	public ReleaseTicketTarget( Release release , ReleaseTicketSet set ) {
		this.release = release; 
		this.set = set;
	}

	public ReleaseTicketTarget copy( Release rrelease , ReleaseTicketSet rset ) throws Exception {
		ReleaseTicketTarget r = new ReleaseTicketTarget( rrelease , rset );
		
		r.ID = ID;
		r.POS = POS;
		r.BUILDTARGET_ID = BUILDTARGET_ID;
		r.DELIVERYTARGET_ID = DELIVERYTARGET_ID;
		r.ACCEPTED = ACCEPTED;
		r.DESCOPED = DESCOPED;
		r.RV = RV;
		
		return( r );
	}

	public void setPos( int POS ) {
		this.POS = POS;
	}
	
	public boolean isAccepted() {
		return( ACCEPTED );
	}

	public boolean isDescoped() {
		return( DESCOPED );
	}

	public boolean isProjectSet() {
		if( buildTarget != null && buildTarget.TYPE == DBEnumBuildTargetType.PROJECTSET )
			return( true );
		return( false );
	}
	
	public boolean isProject() {
		if( buildTarget != null && ( buildTarget.TYPE == DBEnumBuildTargetType.PROJECTALLITEMS || buildTarget.TYPE == DBEnumBuildTargetType.PROJECTNOITEMS ) )
			return( true );
		return( false );
	}
		
	public boolean isBinary() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.BINARYITEM )
			return( true );
		return( false );
	}

	public boolean isConfiguration() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.CONFITEM )
			return( true );
		return( false );
	}
		
	public boolean isDatabase() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.SCHEMA )
			return( true );
		return( false );
	}

	public boolean isDoc() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.DOC )
			return( true );
		return( false );
	}

	public boolean isDelivery() {
		if( isDeliveryBinaries() || isDeliveryConfs() || isDeliveryDatabase() || isDeliveryDoc() )
			return( true );
		return( false );
	}

	public boolean isDeliveryBinaries() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.DELIVERYBINARIES )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryConfs() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.DELIVERYCONFS )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryDatabase() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.DELIVERYDATABASE )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryDoc() {
		if( deliveryTarget != null && deliveryTarget.TYPE == DBEnumDistTargetType.DELIVERYDOC )
			return( true );
		return( false );
	}
	
	public boolean isProjectBuildOnly() {
		if( buildTarget != null && buildTarget.TYPE == DBEnumBuildTargetType.PROJECTNOITEMS )
			return( true );
		return( false );
	}

	public void accept( ActionBase action ) throws Exception {
		if( ACCEPTED )
			return;
		
		ACCEPTED = true;
	}

	public void descope( ActionBase action ) throws Exception {
		if( !DESCOPED ) {
			if( set.isActive() )
				ACCEPTED = false;
			
			DESCOPED = true;
		}
	}

	public boolean isEqualTo( MetaSourceProjectSet set ) {
		if( isProjectSet() && buildTarget.SRCSET.equals( set.ID ) )
			return( true );
		return( false );
	}
	
	public boolean isEqualTo( MetaSourceProject project ) {
		if( isProject() && buildTarget.PROJECT.equals( project.ID ) )
			return( true );
		return( false );
	}
	
	public boolean isEqualTo( MetaSourceProjectItem item ) {
		if( isBinary() && item.distItem != null && deliveryTarget.BINARY.equals( item.distItem.ID ) )
			return( true );
		return( false );
	}
	
	public boolean references( MetaDistrBinaryItem item ) {
		if( isProjectSet() ) {
			if( item.sourceProjectItem != null && buildTarget.SRCSET.equals( item.sourceProjectItem.project.set.ID ) )
				return( true );
			return( false );
		}
		
		if( isProject() ) {
			if( isProjectBuildOnly() )
				return( false );
			if( item.sourceProjectItem != null && buildTarget.PROJECT.equals( item.sourceProjectItem.project.ID ) )
				return( true );
			return( false );
		}
		
		if( isBinary() ) {
			if( deliveryTarget.BINARY.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryBinaries() ) {
			if( deliveryTarget.DELIVERY.equals( item.delivery.ID ) )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean references( MetaDistrConfItem item ) {
		if( isConfiguration() ) {
			if( deliveryTarget.CONF.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryConfs() ) {
			if( deliveryTarget.DELIVERY.equals( item.delivery.ID ) )
				return( true );
			return( false );
		}
		
		return( false );
	}
	
	public boolean references( MetaDistrDelivery delivery , MetaDatabaseSchema item ) {
		if( isDatabase() ) {
			if( deliveryTarget.DELIVERY.equals( delivery.ID ) && deliveryTarget.SCHEMA.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryDatabase() ) {
			if( deliveryTarget.DELIVERY.equals( delivery.ID ) )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean references( MetaDistrDelivery delivery , MetaProductDoc item ) {
		if( isDoc() ) {
			if( deliveryTarget.DELIVERY.equals( delivery.ID ) && deliveryTarget.DOC.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryDoc() ) {
			if( deliveryTarget.DELIVERY.equals( delivery.ID ) )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isActive() {
		if( DESCOPED == false && ACCEPTED )
			return( true );
		if( DESCOPED && ACCEPTED == false )
			return( true );
		return( false );
	}

	public void create( ReleaseBuildTarget buildTarget , int pos ) {
		this.POS = pos;
		this.BUILDTARGET_ID = buildTarget.ID;
		this.buildTarget = buildTarget;
	}
	
	public void create( ReleaseDistTarget deliveryTarget , int pos ) {
		this.POS = pos;
		this.DELIVERYTARGET_ID = deliveryTarget.ID;
		this.deliveryTarget = deliveryTarget;
	}
	
}
