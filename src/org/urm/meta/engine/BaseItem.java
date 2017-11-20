package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BaseItem extends EngineObject {

	public BaseGroup group;
	public String ID;
	public String DESC;
	
	public BaseItem( BaseGroup group ) {
		super( group );
		this.group = group;
	}

	@Override
	public String getName() {
		return( ID );
	}
	
	public void createBaseItem( EngineTransaction transaction , String ID , String DESC ) {
		this.ID = ID;
		this.DESC = DESC;
	}
	
	public void modifyBaseItem( EngineTransaction transaction , String ID , String DESC ) {
		this.ID = ID;
		this.DESC = DESC;
	}
	
	public BaseItem copy( BaseGroup rgroup ) {
		return( null );
	}

	public void load( Node root ) throws Exception {
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
	}
	
	public boolean isHostBound() {
		if( group.category.type == DBEnumBaseCategoryType.HOST )
			return( true );
		return( false );
	}
	
	public boolean isAccountBound() {
		if( group.category.type == DBEnumBaseCategoryType.ACCOUNT )
			return( true );
		return( false );
	}
	
	public boolean isAppBound() {
		if( group.category.type == DBEnumBaseCategoryType.APP )
			return( true );
		return( false );
	}
	
}
