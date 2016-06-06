package org.urm.meta;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Metadata.VarCONFITEMTYPE;
import org.urm.meta.Metadata.VarNAMETYPE;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDistrConfItem {

	Metadata meta;
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
	
	public MetaDistrConfItem( Metadata meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		KEY = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		TYPE = action.meta.getConfItemType( action , ConfReader.getRequiredAttrValue( action , node , "type" ) );
		FILES = ConfReader.getAttrValue( action , node , "files" );
		SECURED = ConfReader.getAttrValue( action , node , "secured" );
		EXCLUDE = ConfReader.getAttrValue( action , node , "exclude" );
		TEMPLATES = ConfReader.getAttrValue( action , node , "templates" );
		EXTCONF = ConfReader.getAttrValue( action , node , "extconf" );
		OBSOLETE = ConfReader.getBooleanAttrValue( action , node , "obsolete" , false );
		CREATEDIR = ConfReader.getBooleanAttrValue( action , node , "createdir" , false );
		
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
