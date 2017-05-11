package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrConfItem {

	public Meta meta;
	public MetaDistrDelivery delivery;

	public String KEY;
	public VarCONFITEMTYPE itemType;
	public String FILES;
	public String TEMPLATES;
	public String SECURED;
	public String EXCLUDE;
	public String EXTCONF;
	public boolean OBSOLETE;
	public boolean CREATEDIR;
	
	public MetaDistrConfItem( Meta meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery;
	}

	public void createConfItem( ServerTransaction transaction , String key ) throws Exception {
		this.KEY = key;
		this.itemType = VarCONFITEMTYPE.DIR;
		this.FILES = "";
		this.TEMPLATES = "";
		this.SECURED = "";
		this.EXCLUDE = "";
		this.EXTCONF = "";
		this.OBSOLETE = false;
		this.CREATEDIR = false;
	}

	public void setCommonData( ServerTransaction transaction , String itemSecured , String itemExclude , String itemExtList , boolean itemCreateDir ) throws Exception {
		this.SECURED = itemSecured;
		this.EXCLUDE = itemExclude;
		this.EXTCONF = itemExtList;
		this.CREATEDIR = itemCreateDir;
	}
	
	public void setDirData( ServerTransaction transaction ) throws Exception {
		this.itemType = VarCONFITEMTYPE.DIR;
		this.FILES = "";
		this.TEMPLATES = "";
		this.EXCLUDE = "";
	}

	public void setFilesData( ServerTransaction transaction , String itemFiles , String itemTemplates ) throws Exception {
		this.itemType = VarCONFITEMTYPE.FILES;
		this.FILES = itemFiles;
		this.TEMPLATES = itemTemplates;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		KEY = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		itemType = Types.getConfItemType( ConfReader.getRequiredAttrValue( node , "type" ) , false );
		FILES = ConfReader.getAttrValue( node , "files" );
		SECURED = ConfReader.getAttrValue( node , "secured" );
		EXCLUDE = ConfReader.getAttrValue( node , "exclude" );
		TEMPLATES = ConfReader.getAttrValue( node , "templates" );
		EXTCONF = ConfReader.getAttrValue( node , "extconf" );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
		CREATEDIR = ConfReader.getBooleanAttrValue( node , "createdir" , false );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , KEY );
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( itemType ) );
		Common.xmlSetElementAttr( doc , root , "files" , FILES );
		Common.xmlSetElementAttr( doc , root , "secured" , SECURED );
		Common.xmlSetElementAttr( doc , root , "exclude" , EXCLUDE );
		Common.xmlSetElementAttr( doc , root , "templates" , TEMPLATES );
		Common.xmlSetElementAttr( doc , root , "extconf" , EXTCONF );
		Common.xmlSetElementAttr( doc , root , "obsolete" , Common.getBooleanValue( OBSOLETE ) );
		Common.xmlSetElementAttr( doc , root , "createdir" , Common.getBooleanValue( CREATEDIR ) );
	}
	
	public MetaDistrConfItem copy( ActionBase action , Meta meta , MetaDistrDelivery delivery ) throws Exception {
		MetaDistrConfItem r = new MetaDistrConfItem( meta , delivery );
		r.KEY = KEY;
		r.itemType = itemType;
		r.FILES = FILES;
		r.SECURED = SECURED;
		r.EXCLUDE = EXCLUDE;
		r.TEMPLATES = TEMPLATES;
		r.EXTCONF = EXTCONF;
		r.OBSOLETE = OBSOLETE;
		r.CREATEDIR = CREATEDIR;
		return( r );
	}
	
	public String getLiveIncludeFiles( ActionBase action ) throws Exception {
		if( itemType == VarCONFITEMTYPE.DIR )
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

	public String getLiveExcludeFiles( ActionBase action ) throws Exception {
		return( EXCLUDE );
	}

	public String getTemplateIncludeFiles( ActionBase action ) throws Exception {
		if( !TEMPLATES.isEmpty() )
			return( TEMPLATES );
		
		if( FILES.isEmpty() )
			return( "*" );
		
		return( FILES );
	}

	public String getTemplateExcludeFiles( ActionBase action ) throws Exception {
		if( SECURED.isEmpty() )
			return( EXCLUDE );
		
		String F_EXCLUDE = Common.addItemToUniqueSpacedList( SECURED , EXCLUDE );
		if( F_EXCLUDE == null )
			return( "" );
		
		return( F_EXCLUDE );
	}
	
}
