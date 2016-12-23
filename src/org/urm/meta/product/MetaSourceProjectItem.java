package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	public MetaSourceProject project;
	
	public MetaSourceProjectItem( Meta meta , MetaSourceProject project ) {
		this.meta = meta;
		this.project = project;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		ITEMNAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		
		ITEMSRCTYPE = Types.getItemSrcType( ConfReader.getRequiredAttrValue( node , "type" ) , false );
		ITEMBASENAME = ConfReader.getAttrValue( node , "basename" );
		if( ITEMBASENAME.isEmpty() )
			ITEMBASENAME = ITEMNAME;

		INTERNAL = ConfReader.getBooleanAttrValue( node , "internal" , false );
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

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , ITEMNAME );
		
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( ITEMSRCTYPE ) );
		Common.xmlSetElementAttr( doc , root , "basename" , ITEMBASENAME );

		Common.xmlSetElementAttr( doc , root , "internal" , Common.getBooleanValue( INTERNAL ) );
		Common.xmlSetElementAttr( doc , root , "extension" , ITEMEXTENSION );
		Common.xmlSetElementAttr( doc , root , "version" , ITEMVERSION );
		Common.xmlSetElementAttr( doc , root , "svn.path" , SVN_ITEMPATH );

		Common.xmlSetElementAttr( doc , root , "nexus.path" , NEXUS_ITEMPATH );

		Common.xmlSetElementAttr( doc , root , "nuget.path" , NUGET_ITEMPATH );
		Common.xmlSetElementAttr( doc , root , "nuget.platform" , NUGET_PLATFORM );
		Common.xmlSetElementAttr( doc , root , "nuget.libname" , NUGET_LIBNAME );

		Common.xmlSetElementAttr( doc , root , "staticextension" , ITEMSTATICEXTENSION );
	}
	
	public MetaSourceProjectItem copy( ActionBase action , Meta meta , MetaSourceProject project ) throws Exception {
		MetaSourceProjectItem r = new MetaSourceProjectItem( meta , project );
		r.ITEMNAME = ITEMNAME;
		
		r.ITEMSRCTYPE = ITEMSRCTYPE;
		r.ITEMBASENAME = ITEMBASENAME;

		r.INTERNAL = INTERNAL;
		r.ITEMEXTENSION = ITEMEXTENSION;
		r.ITEMVERSION = ITEMVERSION;
		r.SVN_ITEMPATH = SVN_ITEMPATH;

		r.NEXUS_ITEMPATH = NEXUS_ITEMPATH;

		r.NUGET_ITEMPATH = NUGET_ITEMPATH;
		r.NUGET_PLATFORM = NUGET_PLATFORM;
		r.NUGET_LIBNAME = NUGET_LIBNAME;

		r.ITEMSTATICEXTENSION = ITEMSTATICEXTENSION;
		r.NEXUS_ITEMPATH = NEXUS_ITEMPATH;
		return( r );
	}
	
	public boolean isStoredInSvn( ActionBase action ) throws Exception {
		if( ITEMSRCTYPE == VarITEMSRCTYPE.SVN )
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
