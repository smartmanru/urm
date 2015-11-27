package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.custom.ICustomDeploy;
import ru.egov.urm.meta.Metadata.VarDISTITEMTYPE;
import ru.egov.urm.meta.Metadata.VarITEMVERSION;
import ru.egov.urm.run.ActionBase;

public class MetaDistrBinaryItem {

	Metadata meta;
	public MetaDistrDelivery delivery;
	public MetaSourceProjectItem sourceItem;

	public String KEY;
	public String EXT;
	public VarDISTITEMTYPE DISTTYPE;
	public String DISTBASENAME;
	public String DEPLOYBASENAME;
	public VarITEMVERSION DEPLOYVERSION;
	public String WAR_MRID;
	public String WAR_CONTEXT;
	public String WAR_STATICEXT;
	public String BUILDINFO;
	public boolean MANUAL;
	public String FILES;
	public String EXCLUDE;
	
	public boolean CUSTOMDEPLOY;
	Node node;
	ICustomDeploy deploy;
	
	public MetaDistrBinaryItem( Metadata meta , MetaDistrDelivery delivery ) {
		this.meta = meta;
		this.delivery = delivery; 
	}

	public void load( ActionBase action , Node node ) throws Exception {
		this.node = node;
		KEY = ConfReader.getNameAttr( action , node );
		MANUAL = ConfReader.getBooleanAttrValue( action , node , "manual" , false );
	
		// read attrs
		DISTTYPE = meta.getItemDistType( action , ConfReader.getRequiredAttrValue( action , node , "type" ) );
		DISTBASENAME = ConfReader.getAttrValue( action , node , "distname" , KEY );
		DEPLOYBASENAME = ConfReader.getAttrValue( action , node , "deployname" , DISTBASENAME );
		DEPLOYVERSION = meta.readItemVersionAttr( action , node , "deployversion" );
		BUILDINFO = ConfReader.getAttrValue( action , node , "buildinfo" );
	
		// binary item
		if( DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			EXT = ConfReader.getRequiredAttrValue( action , node , "extension" );
		}
		else
		// war item and static
		if( DISTTYPE == VarDISTITEMTYPE.WAR ) {
			EXT = ".war";
	
			WAR_MRID = ConfReader.getAttrValue( action , node , "mrid" );
			WAR_CONTEXT = ConfReader.getAttrValue( action , node , "context" , DEPLOYBASENAME );
			WAR_STATICEXT = ConfReader.getAttrValue( action , node , "extension" , "-webstatic.tar.gz" );
		}
		else
		// archive item
		if( DISTTYPE == VarDISTITEMTYPE.ARCHIVE_CHILD || DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT || DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR ) {
			EXT = ConfReader.getAttrValue( action , node , "extension" , ".tar.gz" );
			FILES = ConfReader.getAttrValue( action , node , "files" , "*" );
			EXCLUDE = ConfReader.getAttrValue( action , node , "exclude" );
		}
		else
			action.exit( "distribution item " + KEY + " has unknown type=" + Common.getEnumLower( DISTTYPE ) );
		
		CUSTOMDEPLOY = ConfReader.getBooleanAttrValue( action , node , "customdeploy" , false );
		if( CUSTOMDEPLOY ) {
			String className = ConfReader.getRequiredAttrValue( action , node , "class" );
			deploy = Common.getDeployClass( action , className );
			deploy.parseDistItem( action , this , node );
		}
	}

	public void setSource( ActionBase action , MetaSourceProjectItem sourceItem ) throws Exception {
		this.sourceItem = sourceItem;
	}

	public String getGrepMask( ActionBase action ) throws Exception {
		return( "./" + DISTBASENAME + EXT + 
				"|./.*[0-9]-" + DISTBASENAME + EXT + 
				"|./" + DISTBASENAME + "-[0-9].*" + EXT +
				"|./" + DISTBASENAME + "##[0-9].*" + EXT );
	}
	
}
