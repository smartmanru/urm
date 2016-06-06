package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Metadata.VarITEMSRCTYPE;
import org.urm.server.meta.Metadata.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaSourceProjectItem {

	public String ITEMNAME;
	public String ITEMBASENAME;
	public VarITEMSRCTYPE ITEMSRCTYPE;
	public String ITEMEXTENSION;
	public String ITEMVERSION;
	public String ITEMSTATICEXTENSION;
	public boolean INTERNAL;
	
	public String SVN_ITEMPATH;
	
	public String NEXUS_ITEMPATH;
	
	public String NUGET_ITEMPATH;
	public String NUGET_PLATFORM;
	public String NUGET_LIBNAME;

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
		ITEMVERSION = ConfReader.getAttrValue( action , node , "version" );

		if( isStoredInSvn( action ) ) {
			SVN_ITEMPATH = ConfReader.getAttrValue( action , node , "svn.path" );
		}

		if( isStoredInNexus( action ) ) {
			NEXUS_ITEMPATH = ConfReader.getAttrValue( action , node , "nexus.path" );
		}

		if( isStoredInNuget( action ) ) {
			NUGET_ITEMPATH = ConfReader.getAttrValue( action , node , "nuget.path" );
			NUGET_PLATFORM = ConfReader.getAttrValue( action , node , "nuget.platform" );
			NUGET_LIBNAME = ConfReader.getAttrValue( action , node , "nuget.libname" );
		}

		if( ITEMSRCTYPE == VarITEMSRCTYPE.STATICWAR ) {
			ITEMSTATICEXTENSION = ConfReader.getAttrValue( action , node , "staticextension" );
			NEXUS_ITEMPATH = ConfReader.getAttrValue( action , node , "nexus.path" );

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
		if( ITEMSRCTYPE == VarITEMSRCTYPE.NEXUS || ITEMSRCTYPE == VarITEMSRCTYPE.STATICWAR )
			return( true );
		return( false );
	}

	public boolean isStoredInNuget( ActionBase action ) throws Exception {
		if( ITEMSRCTYPE == VarITEMSRCTYPE.NUGET || ITEMSRCTYPE == VarITEMSRCTYPE.NUGET_PLATFORM )
			return( true );
		return( false );
	}

}
