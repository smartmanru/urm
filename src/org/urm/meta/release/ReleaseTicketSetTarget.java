package org.urm.meta.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
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

public class ReleaseTicketSetTarget {

	public static String PROPERTY_POS = "pos";
	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_ITEM = "item";
	public static String PROPERTY_ACCEPTED = "accepted";
	public static String PROPERTY_DESCOPED = "descoped";
	
	public Meta meta;
	public ReleaseTicketSet set;
	public int POS;

	public DBEnumReleaseTargetType type;
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
		String TYPE = ConfReader.getRequiredAttrValue( root , PROPERTY_TYPE );
		type = DBEnumReleaseTargetType.getValue( TYPE , true );
		ITEM = ConfReader.getRequiredAttrValue( root , PROPERTY_ITEM );
		accepted = ConfReader.getBooleanAttrValue( root , PROPERTY_ACCEPTED , false );
		descoped = ConfReader.getBooleanAttrValue( root , PROPERTY_DESCOPED , false );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , PROPERTY_TYPE , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , PROPERTY_ITEM , ITEM );
		Common.xmlSetElementAttr( doc , root , PROPERTY_ACCEPTED , Common.getBooleanValue( accepted ) );
		Common.xmlSetElementAttr( doc , root , PROPERTY_DESCOPED , Common.getBooleanValue( descoped ) );
	}

	public void setPos( ActionBase action , int POS ) throws Exception {
		this.POS = POS;
	}
	
	public void create( ActionBase action , MetaSourceProjectSet projectSet ) {
		type = DBEnumReleaseTargetType.PROJECTSET;
		ITEM = projectSet.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaSourceProject project , boolean all ) {
		type = ( all )? DBEnumReleaseTargetType.PROJECTALLITEMS : DBEnumReleaseTargetType.PROJECTNOITEMS;
		ITEM = project.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrBinaryItem item ) {
		type = DBEnumReleaseTargetType.DISTITEM;
		ITEM = item.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrConfItem item ) {
		type = DBEnumReleaseTargetType.CONFITEM;
		ITEM = item.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrDelivery delivery , DBEnumReleaseTargetType type ) {
		this.type = type;
		ITEM = delivery.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) {
		this.type = DBEnumReleaseTargetType.SCHEMA;
		ITEM = delivery.NAME + ":" + schema.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc doc ) {
		this.type = DBEnumReleaseTargetType.DOC;
		ITEM = delivery.NAME + ":" + doc.NAME;
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
		if( type == DBEnumReleaseTargetType.PROJECTSET )
			return( true );
		return( false );
	}
	
	public boolean isProject() {
		if( type == DBEnumReleaseTargetType.PROJECTALLITEMS || type == DBEnumReleaseTargetType.PROJECTNOITEMS )
			return( true );
		return( false );
	}
		
	public boolean isBinary() {
		if( type == DBEnumReleaseTargetType.DISTITEM )
			return( true );
		return( false );
	}

	public boolean isConfiguration() {
		if( type == DBEnumReleaseTargetType.CONFITEM )
			return( true );
		return( false );
	}
		
	public boolean isDatabase() {
		if( type == DBEnumReleaseTargetType.SCHEMA )
			return( true );
		return( false );
	}

	public boolean isDoc() {
		if( type == DBEnumReleaseTargetType.DOC )
			return( true );
		return( false );
	}

	public boolean isDelivery() {
		if( type == DBEnumReleaseTargetType.DELIVERYBINARIES || 
			type == DBEnumReleaseTargetType.DELIVERYCONFS || 
			type == DBEnumReleaseTargetType.DELIVERYDATABASE ||
			type == DBEnumReleaseTargetType.DELIVERYDOC )
			return( true );
		return( false );
	}

	public boolean isDeliveryBinaries() {
		if( type == DBEnumReleaseTargetType.DELIVERYBINARIES )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryConfs() {
		if( type == DBEnumReleaseTargetType.DELIVERYCONFS )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryDatabase() {
		if( type == DBEnumReleaseTargetType.DELIVERYDATABASE )
			return( true );
		return( false );
	}
	
	public boolean isDeliveryDoc() {
		if( type == DBEnumReleaseTargetType.DELIVERYDOC )
			return( true );
		return( false );
	}
	
	public String getDatabaseDelivery() {
		return( Common.getPartBeforeFirst( ITEM , ":" ) );
	}
	
	public String getDatabaseSchema() {
		return( Common.getPartAfterFirst( ITEM , ":" ) );
	}
	
	public String getDocDelivery() {
		return( Common.getPartBeforeFirst( ITEM , ":" ) );
	}
	
	public String getDoc() {
		return( Common.getPartAfterFirst( ITEM , ":" ) );
	}
	
	public boolean isProjectBuildOnly() {
		if( type == DBEnumReleaseTargetType.PROJECTNOITEMS )
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

	public boolean references( MetaDistrDelivery delivery , MetaProductDoc item ) {
		if( isDoc() ) {
			String deliveryName = getDocDelivery();
			String docName = getDoc();
			if( deliveryName.equals( delivery.NAME ) && docName.equals( item.NAME ) )
				return( true );
			return( false );
		}

		if( isDeliveryDoc() ) {
			String deliveryName = getDocDelivery();
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
