package org.urm.engine.data;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.BaseCategory;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine._Error;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.Engine;

public class EngineBase extends EngineObject {

	public Engine engine;
	
	private Map<String,BaseCategory> mapCategory;
	private Map<String,BaseGroup> mapGroup;
	private Map<Integer,BaseGroup> mapGroupById;
	private Map<String,BaseItem> mapItem;
	private Map<Integer,BaseItem> mapItemById;
	
	public EngineBase( Engine engine ) {
		super( null );
		this.engine = engine;
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
	
	public void updateGroup( BaseGroup group ) throws Exception {
		Common.changeMapKey( mapGroup , group , group.NAME );
	}
	
	public void removeGroup( BaseGroup group ) {
		group.category.removeGroup( group );
		mapGroup.remove( group.NAME );
		mapGroupById.remove( group.ID );
	}
	
	public void updateItem( BaseItem item ) throws Exception {
		Common.changeMapKey( mapItem , item , item.NAME );
		item.group.modifyItem( item );
	}
	
	public void addItem( BaseItem item ) {
		mapItem.put( item.NAME , item );
		mapItemById.put( item.ID , item );
		item.group.addItem( item );
	}

	public void removeItem( BaseItem item ) {
		mapItem.remove( item.NAME );
		mapItemById.remove( item.ID );
		item.group.removeItem( item );
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
	
	public BaseItem[] getItems() {
		return( mapItem.values().toArray( new BaseItem[0] ) );
	}
	
	public BaseItem findItem( String name ) {
		return( mapItem.get( name ) );
	}

	public BaseItem findItem( int id ) {
		return( mapItemById.get( id ) );
	}

	public BaseItem getItem( String name ) throws Exception {
		BaseItem item = mapItem.get( name );
		if( item == null )
			Common.exit1( _Error.UnknownBaseItem1 , "unknown base item=" + name , name );
		return( item );
	}

	public BaseItem getItem( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getItem( item.FKID ) );
		return( getItem( item.FKNAME ) );
	}
	
	public String getItemName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		BaseItem baseItem = getItem( item.FKID );
		return( baseItem.NAME );
	}
	
	public BaseItem getItem( int itemId ) throws Exception {
		BaseItem item = mapItemById.get( itemId );
		if( item == null )
			Common.exit1( _Error.UnknownBaseItem1 , "unknown base item=" + itemId , "" + itemId );
		return( item );
	}

	public String[] getCategoryNames() {
		return( Common.getSortedKeys( mapCategory ) );
	}

	public MatchItem matchBaseItem( String name ) throws Exception {
		if( name == null || name.isEmpty() )
			return( null );
		
		BaseItem item = findItem( name );
		if( item == null )
			return( new MatchItem( name ) );
		return( new MatchItem( item.ID ) );
	}
	
	public boolean matchBaseItem( MatchItem item ) throws Exception {
		if( item == null )
			return( true );
		
		BaseItem baseItem = null;
		if( item.MATCHED ) {
			baseItem = getItem( item.FKID );
			return( true );
		}
		
		baseItem = findItem( item.FKNAME );
		if( baseItem != null ) {
			item.match( baseItem.ID );
			return( true );
		}
		return( false );
	}
	
}
