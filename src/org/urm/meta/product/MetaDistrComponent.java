package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrComponent {

	protected Meta meta;
	MetaDistr dist;

	public String NAME;
	public String UNIT;
	public String DESC;
	public boolean OBSOLETE;

	private Map<String,MetaDistrComponentItem> mapBinaryItems;
	private Map<String,MetaDistrComponentItem> mapConfItems;
	private Map<String,MetaDistrComponentItem> mapSchemaItems;
	private List<MetaDistrComponentWS> listWS;
	
	public MetaDistrComponent( Meta meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
		
		mapBinaryItems = new HashMap<String,MetaDistrComponentItem>();
		mapConfItems = new HashMap<String,MetaDistrComponentItem>();
		mapSchemaItems = new HashMap<String,MetaDistrComponentItem>();
		listWS = new LinkedList<MetaDistrComponentWS>();
	}

	public MetaDistrComponent copy( ActionBase action , Meta meta , MetaDistr distr ) throws Exception {
		MetaDistrComponent r = new MetaDistrComponent( meta , distr );
		r.NAME = NAME;
		r.UNIT = UNIT;
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

		for( MetaDistrComponentWS item : listWS ) {
			MetaDistrComponentWS ritem = item.copy( action , meta , r );
			r.listWS.add( ritem );
		}
		
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		UNIT = ConfReader.getAttrValue( node , "unit" );
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
			listWS.add( item );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "unit" , UNIT );
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

		for( MetaDistrComponentWS item : listWS ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "webservice" );
			item.save( action , doc , itemElement );
		}
	}	
	
	public boolean hasWebServices( ActionBase action ) throws Exception {
		if( listWS.isEmpty() )
			return( false );
		return( true );
	}

	public List<MetaDistrComponentWS> getWebServices( ActionBase action ) throws Exception {
		return( listWS );
	}

	public boolean hasBinaryItems( ActionBase action ) throws Exception {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}

	public boolean hasConfItems( ActionBase action ) throws Exception {
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
		return( null );
	}
	
	public MetaDistrComponentItem findConfItem( String name ) {
		return( mapConfItems.get( name ) );
	}
	
	public MetaDistrComponentItem getConfItem( ActionBase action , String name ) throws Exception {
		MetaDistrComponentItem item = mapConfItems.get( name );
		if( item == null )
			action.exit1( _Error.UnknownCompConfItem1 , "Unknown component configuration item=" + name , name );
		return( null );
	}
	
	public MetaDistrComponentItem findSchemaItem( String name ) {
		return( mapSchemaItems.get( name ) );
	}
	
	public MetaDistrComponentItem getSchemaItem( ActionBase action , String name ) throws Exception {
		MetaDistrComponentItem item = mapSchemaItems.get( name );
		if( item == null )
			action.exit1( _Error.UnknownCompSchemaItem1 , "Unknown component databce schema item=" + name , name );
		return( null );
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
	
}
