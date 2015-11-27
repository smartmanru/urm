package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.Metadata.VarCONFITEMTYPE;
import ru.egov.urm.run.ActionBase;

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
		KEY = ConfReader.getNameAttr( action , node );
		TYPE = action.meta.getConfItemType( action , ConfReader.getRequiredAttrValue( action , node , "type" ) );
		FILES = ConfReader.getAttrValue( action , node , "files" );
		
		if( TYPE == VarCONFITEMTYPE.FILES ) {
			if( FILES.isEmpty() )
				action.exit( "unexpected files not set, component=" + KEY );
		}
		else {
			if( !FILES.isEmpty() )
				action.exit( "unexpected files set, component=" + KEY );
		}

		SECURED = ConfReader.getAttrValue( action , node , "secured" );
		EXCLUDE = ConfReader.getAttrValue( action , node , "exclude" );
		TEMPLATES = ConfReader.getAttrValue( action , node , "templates" );
		EXTCONF = ConfReader.getAttrValue( action , node , "extconf" );
		OBSOLETE = ConfReader.getBooleanAttrValue( action , node , "obsolete" , false );
		CREATEDIR = ConfReader.getBooleanAttrValue( action , node , "createdir" , false );
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
