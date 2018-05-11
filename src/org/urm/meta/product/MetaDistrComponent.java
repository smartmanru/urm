package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrComponent {

	public Meta meta;
	public MetaDistr dist;

	public String NAME;
	public String DESC;
	public boolean OBSOLETE;

	private Map<String,MetaDistrComponentItem> mapBinaryItems;
	private Map<String,MetaDistrComponentItem> mapConfItems;
	private Map<String,MetaDistrComponentItem> mapSchemaItems;
	private Map<String,MetaDistrComponentWS> mapWS;
	
	public MetaDistrComponent( Meta meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
		
		mapBinaryItems = new HashMap<String,MetaDistrComponentItem>();
		mapConfItems = new HashMap<String,MetaDistrComponentItem>();
		mapSchemaItems = new HashMap<String,MetaDistrComponentItem>();
		mapWS = new HashMap<String,MetaDistrComponentWS>();
	}

	public void createComponent( ServerTransaction transaction , String name ) throws Exception {
		this.NAME = name;
	}
	
	public void setData( ServerTransaction transaction , String desc ) throws Exception {
		this.DESC = desc;
	}
	
	public MetaDistrComponent copy( ActionBase action , Meta meta , MetaDistr distr ) throws Exception {
		MetaDistrComponent r = new MetaDistrComponent( meta , distr );
		r.NAME = NAME;
		r.DESC = DESC;
		r.OBSOLETE = OBSOLETE;
		
		for( MetaDistrComponentItem item : mapBinaryItems.values() ) {
			MetaDistrComponentItem ritem = item.copy( action , meta , r );
			r.mapBinaryItems.put( ritem.binaryItem.KEY , ritem );
		}
		
		for( MetaDistrComponentItem item : mapConfItems.values() ) {
			MetaDistrComponentItem ritem = item.copy( action , meta , r );
			r.mapConfItems.put( ritem.confItem.KEY , ritem );
		}
		
		for( MetaDistrComponentItem item : mapSchemaItems.values() ) {
			MetaDistrComponentItem ritem = item.copy( action , meta , r );
			r.mapSchemaItems.put( ritem.schema.SCHEMA , ritem );
		}

		for( MetaDistrComponentWS item : mapWS.values() ) {
			MetaDistrComponentWS ritem = item.copy( action , meta , r );
			r.mapWS.put( ritem.NAME , ritem );
		}
		
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		DESC = ConfReader.getAttrValue( node , "desc" );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
		
		Node[] items = ConfReader.xmlGetChildren( node , "distitem" );
		if( items != null ) {
			for( Node itemNode : items ) {
				MetaDistrComponentItem item = new MetaDistrComponentItem( meta , this );
				item.loadBinary( action , itemNode );
				mapBinaryItems.put( item.binaryItem.KEY , item );
			}
		}
		
		items = ConfReader.xmlGetChildren( node , "confitem" );
		if( items != null ) {
			for( Node itemNode : items ) {
				MetaDistrComponentItem item = new MetaDistrComponentItem( meta , this );
				item.loadConf( action , itemNode );
				mapConfItems.put( item.confItem.KEY , item );
			}
		}
		
		items = ConfReader.xmlGetChildren( node , "database" );
		if( items != null ) {
			for( Node itemNode : items ) {
				MetaDistrComponentItem item = new MetaDistrComponentItem( meta , this );
				item.loadSchema( action , itemNode );
				mapSchemaItems.put( item.schema.SCHEMA , item );
			}
		}
		
		items = ConfReader.xmlGetChildren( node , "webservice" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			MetaDistrComponentWS item = new MetaDistrComponentWS( meta , this );
			item.load( action , itemNode );
			mapWS.put( item.NAME , item );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "obsolete" , Common.getBooleanValue( OBSOLETE ) );
		
		for( MetaDistrComponentItem item : mapBinaryItems.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "distitem" );
			item.save( action , doc , itemElement );
		}
		
		for( MetaDistrComponentItem item : mapConfItems.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "confitem" );
			item.save( action , doc , itemElement );
		}
		
		for( MetaDistrComponentItem item : mapSchemaItems.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "database" );
			item.save( action , doc , itemElement );
		}

		for( MetaDistrComponentWS item : mapWS.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "webservice" );
			item.save( action , doc , itemElement );
		}
	}	
	
	public boolean hasWebServices() throws Exception {
		if( mapWS.isEmpty() )
			return( false );
		return( true );
	}

	public MetaDistrComponentWS[] getWebServices() {
		return( mapWS.values().toArray( new MetaDistrComponentWS[0] ) );
	}

	public boolean hasBinaryItems() {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}

	public boolean hasConfItems() {
		if( mapConfItems.isEmpty() )
			return( false );
		return( true );
	}

	public String[] getBinaryItemNames() {
		return( Common.getSortedKeys( mapBinaryItems ) );
	}
	
	public MetaDistrComponentItem[] getBinaryItems() {
		return( mapBinaryItems.values().toArray( new MetaDistrComponentItem[0] ) );
	}
	
	public String[] getConfItemNames() {
		return( Common.getSortedKeys( mapConfItems ) );
	}
	
	public MetaDistrComponentItem[] getConfItems() {
		return( mapConfItems.values().toArray( new MetaDistrComponentItem[0] ) );
	}
	
	public String[] getSchemaItemNames() {
		return( Common.getSortedKeys( mapSchemaItems ) );
	}
	
	public MetaDistrComponentItem[] getSchemaItems() {
		return( mapSchemaItems.values().toArray( new MetaDistrComponentItem[0] ) );
	}

	public MetaDistrComponentItem findBinaryItem( String name ) {
		return( mapBinaryItems.get( name ) );
	}
	
	public MetaDistrComponentItem getBinaryItem( ActionBase action , String name ) throws Exception {
		MetaDistrComponentItem item = mapBinaryItems.get( name );
		if( item == null )
			action.exit1( _Error.UnknownCompBinaryItem1 , "Unknown component binary item=" + name , name );
		return( item );
	}
	
	public MetaDistrComponentItem findConfItem( String name ) {
		return( mapConfItems.get( name ) );
	}
	
	public MetaDistrComponentItem getConfItem( ActionBase action , String name ) throws Exception {
		MetaDistrComponentItem item = mapConfItems.get( name );
		if( item == null )
			action.exit1( _Error.UnknownCompConfItem1 , "Unknown component configuration item=" + name , name );
		return( item );
	}
	
	public MetaDistrComponentItem findSchemaItem( String name ) {
		return( mapSchemaItems.get( name ) );
	}
	
	public MetaDistrComponentItem getSchemaItem( ActionBase action , String name ) throws Exception {
		MetaDistrComponentItem item = mapSchemaItems.get( name );
		if( item == null )
			action.exit1( _Error.UnknownCompSchemaItem1 , "Unknown component databce schema item=" + name , name );
		return( item );
	}

	public MetaDistrComponentWS findWebService( String name ) {
		return( mapWS.get( name ) );
	}
	
	public MetaDistrComponentWS getWebService( ActionBase action , String name ) throws Exception {
		MetaDistrComponentWS service = mapWS.get( name );
		if( service == null )
			action.exit1( _Error.UnknownCompWebService1 , "Unknown component web service=" + name , name );
		return( service );
	}
	
	public void removeCompItem( ServerTransaction transaction , MetaDistrComponentItem item ) throws Exception {
		if( item.binaryItem != null )
			mapBinaryItems.remove( item.binaryItem.KEY );
		else
		if( item.confItem != null )
			mapConfItems.remove( item.confItem.KEY );
		else
		if( item.schema != null )
			mapSchemaItems.remove( item.schema.SCHEMA );
	}

	public void createItem( ServerTransaction transaction , MetaDistrComponentItem item ) throws Exception {
		if( item.type == VarCOMPITEMTYPE.BINARY )
			mapBinaryItems.put( item.NAME , item );
		else
		if( item.type == VarCOMPITEMTYPE.CONF )
			mapConfItems.put( item.NAME , item );
		else
		if( item.type == VarCOMPITEMTYPE.SCHEMA )
			mapSchemaItems.put( item.NAME , item );
	}

	private void removeCompItemByRef( MetaDistrComponentItem ref ) throws Exception {
		for( Entry<String,MetaDistrComponentItem> entry : mapBinaryItems.entrySet() ) {
			if( entry.getValue() == ref ) {
				mapBinaryItems.remove( entry.getKey() );
				return;
			}
		}
		for( Entry<String,MetaDistrComponentItem> entry : mapConfItems.entrySet() ) {
			if( entry.getValue() == ref ) {
				mapConfItems.remove( entry.getKey() );
				return;
			}
		}
		for( Entry<String,MetaDistrComponentItem> entry : mapSchemaItems.entrySet() ) {
			if( entry.getValue() == ref ) {
				mapSchemaItems.remove( entry.getKey() );
				return;
			}
		}
	}

	public void modifyItem( ServerTransaction transaction , MetaDistrComponentItem item ) throws Exception {
		removeCompItemByRef( item );
		createItem( transaction , item );
	}

	public void deleteItem( ServerTransaction transaction , MetaDistrComponentItem item ) throws Exception {
		removeCompItem( transaction , item );
	}

	public void createWebService( ServerTransaction transaction , MetaDistrComponentWS service ) throws Exception {
		mapWS.put( service.NAME , service );
	}
	
	public void modifyWebService( ServerTransaction transaction , MetaDistrComponentWS service ) throws Exception {
		for( Entry<String,MetaDistrComponentWS> entry : mapWS.entrySet() ) {
			if( entry.getValue() == service ) {
				mapWS.remove( entry.getKey() );
				break;
			}
		}
		mapWS.put( service.NAME , service );
	}

	public void deleteWebService( ServerTransaction transaction , MetaDistrComponentWS service ) throws Exception {
		mapWS.remove( service.NAME );
	}
	
}
