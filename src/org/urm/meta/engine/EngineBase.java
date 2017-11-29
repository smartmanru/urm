package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.EngineCore;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;

public class EngineBase extends EngineObject {

	public EngineCore core;
	
	private Map<String,BaseCategory> mapCategory;
	private Map<String,BaseGroup> mapGroup;
	private Map<Integer,BaseGroup> mapGroupById;
	private Map<String,BaseItem> mapItem;
	private Map<Integer,BaseItem> mapItemById;
	
	public EngineBase( EngineCore core ) {
		super( null );
		this.core = core;
		mapCategory = new HashMap<String,BaseCategory>();
		mapGroup = new HashMap<String,BaseGroup>();
		mapGroupById = new HashMap<Integer,BaseGroup>();
		mapItem = new HashMap<String,BaseItem>();
		mapItemById = new HashMap<Integer,BaseItem>();
		
		addCategory( new BaseCategory( this , DBEnumBaseCategoryType.HOST , "Host-Bound" ) );
		addCategory( new BaseCategory( this , DBEnumBaseCategoryType.ACCOUNT , "Account-Bound" ) );
		addCategory( new BaseCategory( this , DBEnumBaseCategoryType.APP , "Application-Bound" ) );
	}
	
	@Override
	public String getName() {
		return( "server-base" );
	}
	
	private void addCategory( BaseCategory category ) {
		mapCategory.put( category.LABEL , category );
	}

	public void addGroup( BaseGroup group ) {
		mapGroup.put( group.NAME , group );
		mapGroupById.put( group.ID , group );
		group.category.addGroup( group );
	}
	
	public void addItem( BaseItem item ) {
		mapItem.put( item.NAME , item );
		mapItemById.put( item.ID , item );
		item.group.addItem( item );
	}

	public BaseCategory findCategory( DBEnumBaseCategoryType type ) {
		return( findCategory( Common.getEnumLower( type ) ) );
	}

	public BaseCategory getCategory( DBEnumBaseCategoryType type ) throws Exception {
		BaseCategory category = findCategory( type );
		if( category == null )
			Common.exitUnexpected();
		return( category );
	}
	
	public BaseCategory findCategory( String name ) {
		return( mapCategory.get( name ) );
	}

	public BaseGroup findGroup( String groupName ) {
		return( mapGroup.get( groupName ) );
	}
	
	public BaseGroup getGroup( String groupName ) throws Exception {
		BaseGroup group = mapGroup.get( groupName );
		if( group == null )
			Common.exit1( _Error.UnknownBaseGroup1 , "unknown base group=" + groupName , groupName );
		return( group );
	}
	
	public BaseGroup getGroup( int groupId ) throws Exception {
		BaseGroup group = mapGroupById.get( groupId );
		if( group == null )
			Common.exit1( _Error.UnknownBaseGroup1 , "unknown base group=" + groupId , "" + groupId );
		return( group );
	}
	
	public BaseItem findItem( String name ) {
		return( mapItem.get( name ) );
	}

	public BaseItem getItem( String name ) throws Exception {
		BaseItem item = mapItem.get( name );
		if( item == null )
			Common.exit1( _Error.UnknownBaseItem1 , "unknown base item=" + name , name );
		return( item );
	}

	public BaseItem getItem( int itemId ) throws Exception {
		BaseItem item = mapItemById.get( itemId );
		if( item == null )
			Common.exit1( _Error.UnknownBaseItem1 , "unknown base item=" + itemId , "" + itemId );
		return( item );
	}

	public String[] getCategories() {
		return( Common.getSortedKeys( mapCategory ) );
	}

}
