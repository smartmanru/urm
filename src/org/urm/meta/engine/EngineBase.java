package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineCore;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;

public class EngineBase extends EngineObject {

	public EngineCore core;
	
	private Map<String,BaseCategory> mapCategory;
	private Map<String,BaseItem> mapItem;
	
	public EngineBase( EngineCore core ) {
		super( null );
		this.core = core;
		mapCategory = new HashMap<String,BaseCategory>(); 
		mapItem = new HashMap<String,BaseItem>(); 
	}
	
	@Override
	public String getName() {
		return( "server-base" );
	}
	
	public void addCategory( BaseCategory category ) {
		mapCategory.put( category.ID , category );
	}

	public void addItem( BaseItem item ) {
		mapItem.put( item.ID , item );
	}

	public void createItem( EngineTransaction transaction , BaseItem item ) throws Exception {
		if( mapItem.get( item.ID ) != null )
			transaction.exit1( _Error.DuplicateBaseItem1 , "duplicate base item=" + item.ID , item.ID );
		
		addItem( item );
	}
	
	public BaseCategory findCategory( DBEnumBaseCategoryType type ) {
		return( findCategory( Common.getEnumLower( type ) ) );
	}

	public BaseCategory findCategory( String id ) {
		return( mapCategory.get( id ) );
	}

	public BaseGroup findGroup( DBEnumBaseCategoryType type , String groupName ) {
		BaseCategory ct = findCategory( type );
		if( ct != null )
			return( ct.findGroup( groupName ) );
		return( null );
	}
	
	public BaseItem findBase( String id ) {
		return( mapItem.get( id ) );
	}

	public String[] getCategories() {
		return( Common.getSortedKeys( mapCategory ) );
	}

}
