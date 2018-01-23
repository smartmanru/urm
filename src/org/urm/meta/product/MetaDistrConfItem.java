package org.urm.meta.product;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumConfItemType;

public class MetaDistrConfItem {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_FILES = "files";
	public static String PROPERTY_TEMPLATES = "templates";
	public static String PROPERTY_SECURED = "secured";
	public static String PROPERTY_EXCLUDE = "exclude";
	public static String PROPERTY_EXTCONF = "extconf";
	
	public Meta meta;
	public MetaDistrDelivery delivery;

	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumConfItemType CONFITEM_TYPE;
	public String FILES;
	public String TEMPLATES;
	public String SECURED;
	public String EXCLUDE;
	public String EXTCONF;
	public int PV;
	
	public MetaDistrConfItem( Meta meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery;
	}

	public MetaDistrConfItem copy( Meta rmeta , MetaDistrDelivery rdelivery ) throws Exception {
		MetaDistrConfItem r = new MetaDistrConfItem( rmeta , rdelivery );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.CONFITEM_TYPE = CONFITEM_TYPE;
		r.FILES = FILES;
		r.TEMPLATES = TEMPLATES;
		r.SECURED = SECURED;
		r.EXCLUDE = EXCLUDE;
		r.EXTCONF = EXTCONF;
		r.PV = PV;
		return( r );
	}
	
	public void createConfItem( String name , String desc , DBEnumConfItemType type , String files , String templates , String secured , String exclude , String extconf ) throws Exception {
		modifyConfItem( name , desc , type , files , templates , secured , exclude , extconf );
	}
	
	public void modifyConfItem( String name , String desc , DBEnumConfItemType type , String files , String templates , String secured , String exclude , String extconf ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.CONFITEM_TYPE = type;
		this.FILES = files;
		this.TEMPLATES = templates;
		this.SECURED = secured;
		this.EXCLUDE = exclude;
		this.EXTCONF = extconf;
	}

	public void setCommonData( String itemSecured , String itemExclude , String itemExtList ) throws Exception {
		this.SECURED = itemSecured;
		this.EXCLUDE = itemExclude;
		this.EXTCONF = itemExtList;
	}
	
	public void setDirData() throws Exception {
		this.CONFITEM_TYPE = DBEnumConfItemType.DIR;
		this.FILES = "";
		this.TEMPLATES = "";
	}

	public void setFilesData( String itemFiles , String itemTemplates ) throws Exception {
		this.CONFITEM_TYPE = DBEnumConfItemType.FILES;
		this.FILES = itemFiles;
		this.TEMPLATES = itemTemplates;
	}
	
	public String getLiveIncludeFiles() {
		if( CONFITEM_TYPE == DBEnumConfItemType.DIR )
			return( "*" );
			
		if( !FILES.isEmpty() )
			return( FILES );

		if( TEMPLATES.isEmpty() && SECURED.isEmpty() )
			return( "*" );
		
		String F_INCLUDE = Common.addItemToUniqueSpacedList( TEMPLATES , SECURED );
		if( F_INCLUDE == null )
			return( "" );
		
		return( F_INCLUDE );
	}

	public String getLiveExcludeFiles() {
		return( EXCLUDE );
	}

	public String getTemplateIncludeFiles() {
		if( !TEMPLATES.isEmpty() )
			return( TEMPLATES );
		
		if( FILES.isEmpty() )
			return( "*" );
		
		return( FILES );
	}

	public String getTemplateExcludeFiles() {
		if( SECURED.isEmpty() )
			return( EXCLUDE );
		
		String F_EXCLUDE = Common.addItemToUniqueSpacedList( SECURED , EXCLUDE );
		if( F_EXCLUDE == null )
			return( "" );
		
		return( F_EXCLUDE );
	}
	
}
