package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarITEMSRCTYPE;
import org.urm.meta.product.Meta.VarNAMETYPE;
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

	protected Meta meta;
	public MetaDistrBinaryItem distItem;
	public MetaSourceProject project;
	
	public MetaSourceProjectItem( Meta meta , MetaSourceProject project ) {
		this.meta = meta;
		this.project = project;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		ITEMNAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		distItem = null;
		
		ITEMSRCTYPE = Meta.getItemSrcType( ConfReader.getRequiredAttrValue( node , "type" ) , false );
		ITEMBASENAME = ConfReader.getAttrValue( node , "basename" );
		if( ITEMBASENAME.isEmpty() )
			ITEMBASENAME = ITEMNAME;

		INTERNAL = ConfReader.getBooleanAttrValue( node , "internal" , false );
		MetaDistr distr = meta.getDistr( action );
		distItem = distr.getBinaryItem( action , ITEMNAME );
		distItem.setSource( action , this );

		ITEMEXTENSION = ConfReader.getAttrValue( node , "extension" );
		ITEMVERSION = ConfReader.getAttrValue( node , "version" );

		if( isStoredInSvn( action ) ) {
			SVN_ITEMPATH = ConfReader.getAttrValue( node , "svn.path" );
		}

		if( isStoredInNexus( action ) ) {
			NEXUS_ITEMPATH = ConfReader.getAttrValue( node , "nexus.path" );
		}

		if( isStoredInNuget( action ) ) {
			NUGET_ITEMPATH = ConfReader.getAttrValue( node , "nuget.path" );
			NUGET_PLATFORM = ConfReader.getAttrValue( node , "nuget.platform" );
			NUGET_LIBNAME = ConfReader.getAttrValue( node , "nuget.libname" );
		}

		if( ITEMSRCTYPE == VarITEMSRCTYPE.STATICWAR ) {
			ITEMSTATICEXTENSION = ConfReader.getAttrValue( node , "staticextension" );
			NEXUS_ITEMPATH = ConfReader.getAttrValue( node , "nexus.path" );

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
