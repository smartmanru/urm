package org.urm.meta.engine;

import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;

public class BaseItem extends EngineObject {

	public BaseGroup group;
	public String ID;
	public String DESC;
	
	public ObjectProperties parameters;
	
	public BaseItem( BaseGroup group , ObjectProperties parameters ) {
		super( group );
		this.group = group;
		this.parameters = parameters;
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
	
	public BaseItem copy( BaseGroup rgroup , ObjectProperties rparameters ) {
		BaseItem ritem = new BaseItem( rgroup , rparameters );
		ritem.ID = ID;
		ritem.DESC = DESC;
		return( ritem );
	}

	public boolean isHostBound() {
		if( group.category.TYPE == DBEnumBaseCategoryType.HOST )
			return( true );
		return( false );
	}
	
	public boolean isAccountBound() {
		if( group.category.TYPE == DBEnumBaseCategoryType.ACCOUNT )
			return( true );
		return( false );
	}
	
	public boolean isAppBound() {
		if( group.category.TYPE == DBEnumBaseCategoryType.APP )
			return( true );
		return( false );
	}
	
}
