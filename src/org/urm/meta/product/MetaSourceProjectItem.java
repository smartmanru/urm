package org.urm.meta.product;

import org.urm.db.core.DBEnums.*;

public class MetaSourceProjectItem {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_SRCTYPE = "type";
	public static String PROPERTY_BASENAME = "basename";
	public static String PROPERTY_EXT = "extension";
	public static String PROPERTY_STATICEXT = "staticextension";
	public static String PROPERTY_PATH = "itempath";
	public static String PROPERTY_VERSION = "version";
	public static String PROPERTY_NODIST = "internal";
	
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumSourceItemType SOURCEITEM_TYPE;
	public String BASENAME;
	public String EXT;
	public String STATICEXT;
	public String PATH;
	public String FIXED_VERSION;
	public boolean INTERNAL;
	public int PV;
	public DBEnumChangeType CHANGETYPE;
	
	public MetaDistrBinaryItem distItem;

	public Meta meta;
	public MetaSourceProject project;
	
	public MetaSourceProjectItem( Meta meta , MetaSourceProject project ) {
		this.meta = meta;
		this.project = project;
	}
	
	public MetaSourceProjectItem copy( Meta rmeta , MetaSourceProject rproject ) throws Exception {
		MetaSourceProjectItem r = new MetaSourceProjectItem( rmeta , rproject );
		
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.SOURCEITEM_TYPE = SOURCEITEM_TYPE;
		r.BASENAME = BASENAME;
		r.EXT = EXT;
		r.STATICEXT = STATICEXT;
		r.PATH = PATH;
		r.FIXED_VERSION = FIXED_VERSION;
		r.INTERNAL = INTERNAL;
		r.PV = PV;
		r.CHANGETYPE = CHANGETYPE;
		
		return( r );
	}
	
	public void createItem( String name , String desc ) throws Exception {
		modifyItem( name , desc );
	}
	
	public void modifyItem( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public void setSourceData( DBEnumSourceItemType srcType , String basename , String ext , String staticext , String path , String version , boolean internal ) throws Exception {
		this.SOURCEITEM_TYPE = srcType;
		this.BASENAME = basename;
		this.EXT = ext;
		this.STATICEXT = staticext;
		this.PATH = path;
		this.FIXED_VERSION = version;
		this.INTERNAL = internal;
	}
	
	public void setDistItem( MetaDistrBinaryItem distItem ) throws Exception {
		this.distItem = distItem;
		this.INTERNAL = ( distItem == null )? true : false;
	}
	
	public boolean isInternal() {
		if( INTERNAL )
			return( true );
		return( false );
	}

	public boolean isTargetLocal() {
		if( isSourceDirectory() )
			return( true );
		return( false );
	}
	
	public boolean isSourceDirectory() {
		if( SOURCEITEM_TYPE == DBEnumSourceItemType.DIRECTORY )
			return( true );
		return( false );
	}
	
	public boolean isSourceBasic() {
		if( SOURCEITEM_TYPE == DBEnumSourceItemType.BASIC )
			return( true );
		return( false );
	}
	
	public boolean isSourcePackage() {
		if( SOURCEITEM_TYPE == DBEnumSourceItemType.PACKAGE )
			return( true );
		return( false );
	}
	
	public boolean isSourceStaticWar() {
		if( SOURCEITEM_TYPE == DBEnumSourceItemType.STATICWAR )
			return( true );
		return( false );
	}
	
	public String getArtefactSampleFile() {
		String value = BASENAME;
		if( SOURCEITEM_TYPE == DBEnumSourceItemType.BASIC || SOURCEITEM_TYPE == DBEnumSourceItemType.PACKAGE || SOURCEITEM_TYPE == DBEnumSourceItemType.CUSTOM ) {
			if( !FIXED_VERSION.isEmpty() )
				value += "-" + FIXED_VERSION;
			value += EXT;
		}
		else
		if( SOURCEITEM_TYPE == DBEnumSourceItemType.DIRECTORY )
			value = BASENAME;
		else
		if( SOURCEITEM_TYPE == DBEnumSourceItemType.STATICWAR )
			value = BASENAME + EXT + "/" + BASENAME + STATICEXT;
		return( value );
	}
	
}
