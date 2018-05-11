package org.urm.meta.release;

import org.urm.common.Common;
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
	public Integer DISTTARGET_ID;
	public boolean ACCEPTED;
	public boolean DESCOPED;
	public int RV;

	private ReleaseBuildTarget buildTarget;
	private ReleaseDistTarget distTarget;
	
	public ReleaseTicketTarget( Release release , ReleaseTicketSet set ) {
		this.release = release; 
		this.set = set;
	}

	public ReleaseTicketTarget copy( Release rrelease , ReleaseTicketSet rset ) throws Exception {
		ReleaseTicketTarget r = new ReleaseTicketTarget( rrelease , rset );
		
		r.ID = ID;
		r.POS = POS;
		r.BUILDTARGET_ID = BUILDTARGET_ID;
		r.DISTTARGET_ID = DISTTARGET_ID;
		r.ACCEPTED = ACCEPTED;
		r.DESCOPED = DESCOPED;
		r.RV = RV;
		
		ReleaseChanges rchanges = rrelease.getChanges();
		if( BUILDTARGET_ID != null )
			r.buildTarget = rchanges.getBuildTarget( BUILDTARGET_ID );
		if( DISTTARGET_ID != null )
			r.distTarget = rchanges.getDistTarget( DISTTARGET_ID );
		
		return( r );
	}

	public void create( int POS , Integer BUILDTARGET_ID , Integer DISTTARGET_ID , boolean ACCEPTED , boolean DESCOPED ) throws Exception {
		ReleaseChanges changes = release.getChanges();
		if( BUILDTARGET_ID != null )
			buildTarget = changes.getBuildTarget( BUILDTARGET_ID );
		if( DISTTARGET_ID != null )
			distTarget = changes.getDistTarget( DISTTARGET_ID );

		this.POS = POS;
		this.ACCEPTED = ACCEPTED;
		this.DESCOPED = DESCOPED;
	}
	
	public void create( int POS , ReleaseBuildTarget buildTarget , ReleaseDistTarget distTarget , boolean ACCEPTED , boolean DESCOPED ) throws Exception {
		this.buildTarget = buildTarget;
		this.BUILDTARGET_ID = ( buildTarget == null )? null : buildTarget.ID;
		this.distTarget = distTarget;
		this.DISTTARGET_ID = ( distTarget == null )? null : distTarget.ID;
		
		this.POS = POS;
		this.ACCEPTED = ACCEPTED;
		this.DESCOPED = DESCOPED;
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

	public ReleaseBuildTarget getBuildTarget() {
		return( buildTarget );
	}
	
	public ReleaseDistTarget getDistTarget() {
		return( distTarget );
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
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.BINARYITEM )
			return( true );
		return( false );
	}

	public boolean isConfiguration() {
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.CONFITEM )
			return( true );
		return( false );
	}
		
	public boolean isDatabase() {
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.SCHEMA )
			return( true );
		return( false );
	}

	public boolean isDoc() {
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.DOC )
			return( true );
		return( false );
	}

	public boolean isBuildTarget() {
		if( buildTarget != null )
			return( true );
		return( false );
	}

	public boolean isDistTarget() {
		if( distTarget != null )
			return( true );
		return( false );
	}

	public boolean isDelivery() {
		if( isDeliveryBinaries() || isDeliveryConfs() || isDeliveryDatabase() || isDeliveryDoc() )
			return( true );
		return( false );
	}

	public boolean isDeliveryBinaries() {
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.DELIVERYBINARIES )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryConfs() {
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.DELIVERYCONFS )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryDatabase() {
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.DELIVERYDATABASE )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryDoc() {
		if( distTarget != null && distTarget.TYPE == DBEnumDistTargetType.DELIVERYDOC )
			return( true );
		return( false );
	}
	
	public boolean isProjectBuildOnly() {
		if( buildTarget != null && buildTarget.TYPE == DBEnumBuildTargetType.PROJECTNOITEMS )
			return( true );
		return( false );
	}

	public void accept() throws Exception {
		if( ACCEPTED )
			return;
		
		ACCEPTED = true;
	}

	public void descope() throws Exception {
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
		if( isBinary() && item.distItem != null && distTarget.BINARY.equals( item.distItem.ID ) )
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
			if( distTarget.BINARY.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryBinaries() ) {
			if( distTarget.DELIVERY.equals( item.delivery.ID ) )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean references( MetaDistrConfItem item ) {
		if( isConfiguration() ) {
			if( distTarget.CONF.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryConfs() ) {
			if( distTarget.DELIVERY.equals( item.delivery.ID ) )
				return( true );
			return( false );
		}
		
		return( false );
	}
	
	public boolean references( MetaDistrDelivery delivery , MetaDatabaseSchema item ) {
		if( isDatabase() ) {
			if( distTarget.DELIVERY.equals( delivery.ID ) && distTarget.SCHEMA.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryDatabase() ) {
			if( distTarget.DELIVERY.equals( delivery.ID ) )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean references( MetaDistrDelivery delivery , MetaProductDoc item ) {
		if( isDoc() ) {
			if( distTarget.DELIVERY.equals( delivery.ID ) && distTarget.DOC.equals( item.ID ) )
				return( true );
			return( false );
		}

		if( isDeliveryDoc() ) {
			if( distTarget.DELIVERY.equals( delivery.ID ) )
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
		this.DISTTARGET_ID = deliveryTarget.ID;
		this.distTarget = deliveryTarget;
	}

	public String getName() {
		if( isBuildTarget() ) {
			if( buildTarget.isBuildAll() )
				return( "(all)" );
			if( buildTarget.isBuildSet() ) {
				MetaSourceProjectSet set = buildTarget.getProjectSet();
				return( set.NAME );
			}
			if( buildTarget.isBuildProject() ) {
				MetaSourceProject project = buildTarget.getProject();
				return( project.NAME );
			}
		}
		else
		if( isDistTarget() ) {
			if( distTarget.isDistAll() )
				return( "(all)" );
			if( distTarget.isDelivery() ) {
				MetaDistrDelivery delivery = distTarget.getDelivery();
				return( delivery.NAME );
			}
			if( distTarget.isBinaryItem() ) {
				MetaDistrBinaryItem item = distTarget.getBinaryItem();
				return( item.NAME );
			}
			if( distTarget.isConfItem() ) {
				MetaDistrConfItem item = distTarget.getConfItem();
				return( item.NAME );
			}
			if( distTarget.isSchema() ) {
				MetaDatabaseSchema schema = distTarget.getSchema();
				return( schema.NAME );
			}
			if( distTarget.isDoc() ) {
				MetaProductDoc doc = distTarget.getDoc();
				return( doc.NAME );
			}
		}
		return( "?" );
	}
	
	public MetaSourceProject findProject() {
		return( buildTarget.getProject() );
	}
	
	public MetaSourceProjectSet findProjectSet() {
		return( buildTarget.getProjectSet() );
	}
	
	public MetaDistrBinaryItem getBinaryItem() {
		return( distTarget.getBinaryItem() );
	}

	public MetaDistrConfItem getConfItem() {
		return( distTarget.getConfItem() );
	}
	
	public MetaDistrDelivery getDelivery() {
		return( distTarget.getDelivery() );
	}
	
	public MetaDatabaseSchema getDatabaseSchema() {
		return( distTarget.getSchema() );
	}

	public MetaProductDoc getDoc() {
		return( distTarget.getDoc() );
	}

	public MetaSourceProjectSet getProjectSet() {
		return( buildTarget.getProjectSet() );
	}
	
	public MetaSourceProject getProject() {
		return( buildTarget.getProject() );
	}
	
	public String getType() {
		if( isBuildTarget() )
			return( Common.getEnumLower( buildTarget.TYPE ) );
		if( isDistTarget() )
			return( Common.getEnumLower( distTarget.TYPE ) );
		return( "" );
	}
	
}
