package org.urm.meta.engine;

import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;

public class BaseItem extends EngineObject {

	public static String PROPERTY_NAME = "id";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_OFFLINE = "offline";
	
	public BaseGroup group;
	public int ID;
	public String NAME;
	public String DESC;
	public boolean OFFLINE;
	public int CV;
	
	public ObjectProperties parameters;
	
	public BaseItem( BaseGroup group , ObjectProperties parameters ) {
		super( group );
		this.group = group;
		this.parameters = parameters;
		ID = -1;
		CV = 0;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void createBaseItem( String name , String desc ) throws Exception {
		OFFLINE = false;
		modifyBaseItem( name , desc );
	}
	
	public void modifyBaseItem( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		parameters.setStringProperty( PROPERTY_NAME , NAME );
		parameters.setStringProperty( PROPERTY_DESC , DESC );
		parameters.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
	}
	
	public BaseItem copy( BaseGroup rgroup , ObjectProperties rparameters ) {
		BaseItem r = new BaseItem( rgroup , rparameters );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.CV = CV;
		return( r );
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
