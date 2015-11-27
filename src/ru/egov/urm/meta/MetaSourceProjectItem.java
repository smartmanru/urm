package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.Metadata.VarITEMSRCTYPE;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;
import ru.egov.urm.run.ActionBase;

public class MetaSourceProjectItem {

	public String ITEMNAME;
	public String ITEMBASENAME;
	public VarITEMSRCTYPE ITEMSRCTYPE;
	public String ITEMEXTENSION;
	public String ITEMPATH;
	public String ITEMVERSION;
	public String ITEMSTATICEXTENSION;
	public boolean INTERNAL;

	Metadata meta;
	public MetaDistrBinaryItem distItem;
	public MetaSourceProject project;
	
	public MetaSourceProjectItem( Metadata meta , MetaSourceProject project ) {
		this.meta = meta;
		this.project = project;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		ITEMNAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		distItem = null;
		
		ITEMSRCTYPE = meta.getItemSrcType( action , ConfReader.getRequiredAttrValue( action , node , "type" ) );
		ITEMBASENAME = ConfReader.getAttrValue( action , node , "basename" );
		if( ITEMBASENAME.isEmpty() )
			ITEMBASENAME = ITEMNAME;

		INTERNAL = ConfReader.getBooleanAttrValue( action , node , "internal" , false );
		distItem = meta.distr.getBinaryItem( action , ITEMNAME );
		distItem.setSource( action , this );

		ITEMEXTENSION = ConfReader.getAttrValue( action , node , "extension" );

		if( ITEMSRCTYPE != VarITEMSRCTYPE.GENERATED ) {
			ITEMPATH = ConfReader.getAttrValue( action , node , "path" );
			ITEMVERSION = ConfReader.getAttrValue( action , node , "version" );
		}

		if( ITEMSRCTYPE == VarITEMSRCTYPE.STATICWAR ) {
			ITEMSTATICEXTENSION = ConfReader.getAttrValue( action , node , "staticextension" );

			if( ITEMSTATICEXTENSION.isEmpty() )
				ITEMSTATICEXTENSION="-webstatic.tar.gz";
		}
	}

	public boolean isStoredInSvn( ActionBase action ) throws Exception {
		if( ITEMSRCTYPE == VarITEMSRCTYPE.SVN || ITEMSRCTYPE == VarITEMSRCTYPE.SVNOLD || ITEMSRCTYPE == VarITEMSRCTYPE.SVNNEW )
			return( true );
		return( false );
	}
	
	public boolean isStoredInSvnOld( ActionBase action ) throws Exception {
		if( ITEMSRCTYPE == VarITEMSRCTYPE.SVN || ITEMSRCTYPE == VarITEMSRCTYPE.SVNOLD )
			return( true );
		return( false );
	}

	public boolean isStoredInSvnNew( ActionBase action ) throws Exception {
		if( ITEMSRCTYPE == VarITEMSRCTYPE.SVNNEW )
			return( true );
		return( false );
	}

	public boolean isStoredInNexus( ActionBase action ) throws Exception {
		if( ITEMSRCTYPE == VarITEMSRCTYPE.NEXUS )
			return( true );
		return( false );
	}

}
