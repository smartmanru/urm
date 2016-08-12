package org.urm.server.meta;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarCONFITEMTYPE;
import org.urm.server.meta.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDistrConfItem {

	protected Meta meta;
	public MetaDistrDelivery delivery;

	public String KEY;
	public VarCONFITEMTYPE TYPE;
	private String FILES;
	private String TEMPLATES;
	private String SECURED;
	private String EXCLUDE;
	public String EXTCONF;
	public boolean OBSOLETE;
	public boolean CREATEDIR;
	
	public MetaDistrConfItem( Meta meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		KEY = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		TYPE = meta.getConfItemType( ConfReader.getRequiredAttrValue( node , "type" ) );
		FILES = ConfReader.getAttrValue( node , "files" );
		SECURED = ConfReader.getAttrValue( node , "secured" );
		EXCLUDE = ConfReader.getAttrValue( node , "exclude" );
		TEMPLATES = ConfReader.getAttrValue( node , "templates" );
		EXTCONF = ConfReader.getAttrValue( node , "extconf" );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
		CREATEDIR = ConfReader.getBooleanAttrValue( node , "createdir" , false );
		
		if( TYPE == VarCONFITEMTYPE.DIR ) {
			if( FILES.isEmpty() == false || TEMPLATES.isEmpty() == false )
				action.exit( "unexpected set files or templates attribute in confitem=" + KEY );
		}
			
	}

	public String getLiveIncludeFiles( ActionBase action ) throws Exception {
		if( TYPE == VarCONFITEMTYPE.DIR )
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
