package org.urm.db.engine;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.DBEnumBaseCategoryType;
import org.urm.meta.engine.BaseCategory;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.EngineBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineBase {

	public static void importxml( EngineBase base , Node root , DBConnection c ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , "category" );
		if( list != null ) {
			for( Node node : list ) {
				BaseCategory category = new BaseCategory( base );
				category.load( node );
				base.addCategory( category );
				
				for( BaseGroup group : category.getGroups() ) {
					for( BaseItem item : group.getItems() )
						base.addItem( item );
				}
			}
		}
		
		if( base.findCategory( DBEnumBaseCategoryType.HOST ) == null )
			base.addCategory( new BaseCategory( base , DBEnumBaseCategoryType.HOST , "Host-Bound" ) );
		if( base.findCategory( DBEnumBaseCategoryType.ACCOUNT ) == null )
			base.addCategory( new BaseCategory( base , DBEnumBaseCategoryType.ACCOUNT , "Account-Bound" ) );
		if( base.findCategory( DBEnumBaseCategoryType.APP ) == null )
			base.addCategory( new BaseCategory( base , DBEnumBaseCategoryType.APP , "Application-Bound" ) );
	}
	
	public static void exportxml( ActionCore action , EngineBase base , Document doc , Element root ) throws Exception {
		for( String id : base.getCategories() ) {
			BaseCategory category = base.findCategory( id );
			Element node = Common.xmlCreateElement( doc , root , "category" );
			category.save( doc , node );
		}
	}

}
