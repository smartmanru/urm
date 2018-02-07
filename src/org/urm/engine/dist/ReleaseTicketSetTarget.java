package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTicketSetTarget {

	public Meta meta;
	public ReleaseTicketSet set;
	public int POS;

	public EnumTicketSetTargetType type;
	public String ITEM;
	public boolean accepted;
	public boolean descoped;
	
	public ReleaseTicketSetTarget( Meta meta , ReleaseTicketSet set , int POS ) {
		this.meta = meta; 
		this.set = set;
		this.POS = POS;
	}

	public ReleaseTicketSetTarget copy( ActionBase action , Meta meta , ReleaseTicketSet set ) throws Exception {
		ReleaseTicketSetTarget r = new ReleaseTicketSetTarget( meta , set , POS );
		r.POS = POS;
		r.type = type;
		r.ITEM = ITEM;
		r.accepted = accepted;
		r.descoped = descoped;
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		String TYPE = ConfReader.getRequiredAttrValue( root , Release.PROPERTY_TICKETTARGETTYPE );
		type = Types.getTicketSetTargetType( TYPE , true );
		ITEM = ConfReader.getRequiredAttrValue( root , Release.PROPERTY_TICKETTARGETITEM );
		accepted = ConfReader.getBooleanAttrValue( root , Release.PROPERTY_TICKETTARGETACCEPTED , false );
		descoped = ConfReader.getBooleanAttrValue( root , Release.PROPERTY_TICKETTARGETDESCOPED , false );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETTYPE , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETITEM , ITEM );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETACCEPTED , Common.getBooleanValue( accepted ) );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETDESCOPED , Common.getBooleanValue( descoped ) );
	}

	public void setPos( ActionBase action , int POS ) throws Exception {
		this.POS = POS;
	}
	
	public void create( ActionBase action , MetaSourceProjectSet projectSet ) {
		type = EnumTicketSetTargetType.PROJECTSET;
		ITEM = projectSet.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaSourceProject project , boolean all ) {
		type = ( all )? EnumTicketSetTargetType.PROJECTALLITEMS : EnumTicketSetTargetType.PROJECTNOITEMS;
		ITEM = project.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrBinaryItem item ) {
		type = EnumTicketSetTargetType.DISTITEM;
		ITEM = item.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrConfItem item ) {
		type = EnumTicketSetTargetType.CONFITEM;
		ITEM = item.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrDelivery delivery , EnumTicketSetTargetType type ) {
		this.type = type;
		ITEM = delivery.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) {
		this.type = EnumTicketSetTargetType.SCHEMA;
		ITEM = delivery.NAME + ":" + schema.NAME;
		accepted = false;
		descoped = false;
	}
	
	public boolean isAccepted() {
		return( accepted );
	}

	public boolean isDescoped() {
		return( descoped );
	}

	public boolean isProjectSet() {
		if( type == EnumTicketSetTargetType.PROJECTSET )
			return( true );
		return( false );
	}
	
	public boolean isProject() {
		if( type == EnumTicketSetTargetType.PROJECTALLITEMS || type == EnumTicketSetTargetType.PROJECTNOITEMS )
			return( true );
		return( false );
	}
		
	public boolean isBinary() {
		if( type == EnumTicketSetTargetType.DISTITEM )
			return( true );
		return( false );
	}

	public boolean isConfiguration() {
		if( type == EnumTicketSetTargetType.CONFITEM )
			return( true );
		return( false );
	}
		
	public boolean isDatabase() {
		if( type == EnumTicketSetTargetType.SCHEMA )
			return( true );
		return( false );
	}

	public boolean isDelivery() {
		if( type == EnumTicketSetTargetType.DELIVERYBINARIES || type == EnumTicketSetTargetType.DELIVERYCONFS || type == EnumTicketSetTargetType.DELIVERYDATABASE )
			return( true );
		return( false );
	}

	public boolean isDeliveryBinaries() {
		if( type == EnumTicketSetTargetType.DELIVERYBINARIES )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryConfs() {
		if( type == EnumTicketSetTargetType.DELIVERYCONFS )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryDatabase() {
		if( type == EnumTicketSetTargetType.DELIVERYDATABASE )
			return( true );
		return( false );
	}
	
	public String getDatabaseDelivery() {
		return( Common.getPartBeforeFirst( ITEM , ":" ) );
	}
	
	public String getDatabaseSchema() {
		return( Common.getPartAfterFirst( ITEM , ":" ) );
	}
	
	public boolean isProjectBuildOnly() {
		if( type == EnumTicketSetTargetType.PROJECTNOITEMS )
			return( true );
		return( false );
	}

	public void accept( ActionBase action ) throws Exception {
		if( accepted )
			return;
		
		accepted = true;
	}

	public void descope( ActionBase action ) throws Exception {
		if( !descoped ) {
			if( set.isActive() )
				accepted = false;
			
			descoped = true;
		}
	}

	public boolean isEqualTo( MetaSourceProjectSet set ) {
		if( isProjectSet() && ITEM.equals( set.NAME ) )
			return( true );
		return( false );
	}
	
	public boolean isEqualTo( MetaSourceProject project ) {
		if( isProject() && ITEM.equals( project.NAME ) )
			return( true );
		return( false );
	}
	
	public boolean isEqualTo( MetaSourceProjectItem item ) {
		if( isBinary() && item.distItem != null && ITEM.equals( item.distItem.NAME ) )
			return( true );
		return( false );
	}
	
	public boolean references( MetaDistrBinaryItem item ) {
		if( isProjectSet() ) {
			if( item.sourceProjectItem != null && ITEM.equals( item.sourceProjectItem.project.set.NAME ) )
				return( true );
			return( false );
		}
		
		if( isProject() ) {
			if( isProjectBuildOnly() )
				return( false );
			if( item.sourceProjectItem != null && ITEM.equals( item.sourceProjectItem.project.NAME ) )
				return( true );
			return( false );
		}
		
		if( isBinary() ) {
			if( ITEM.equals( item.NAME ) )
				return( true );
			return( false );
		}

		if( isDeliveryBinaries() ) {
			if( ITEM.equals( item.delivery.NAME ) )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean references( MetaDistrConfItem item ) {
		if( isConfiguration() ) {
			if( ITEM.equals( item.NAME ) )
				return( true );
			return( false );
		}

		if( isDeliveryConfs() ) {
			if( ITEM.equals( item.delivery.NAME ) )
				return( true );
			return( false );
		}
		
		return( false );
	}
	
	public boolean references( MetaDistrDelivery delivery , MetaDatabaseSchema item ) {
		if( isDatabase() ) {
			String deliveryName = getDatabaseDelivery();
			String schemaName = getDatabaseSchema();
			if( deliveryName.equals( delivery.NAME ) && schemaName.equals( item.NAME ) )
				return( true );
			return( false );
		}

		if( isDeliveryDatabase() ) {
			String deliveryName = getDatabaseDelivery();
			if( deliveryName.equals( delivery.NAME ) )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isActive() {
		if( descoped == false && accepted )
			return( true );
		if( descoped && accepted == false )
			return( true );
		return( false );
	}
	
}
