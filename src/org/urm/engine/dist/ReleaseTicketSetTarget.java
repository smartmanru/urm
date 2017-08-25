package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTicketSetTarget {

	public Meta meta;
	public ReleaseTicketSet set;

	public VarTICKETSETTARGETTYPE type;
	public String ITEM;
	public boolean accepted;
	public boolean descoped;
	
	public ReleaseTicketSetTarget( Meta meta , ReleaseTicketSet set ) {
		this.meta = meta; 
		this.set = set;
	}

	public ReleaseTicketSetTarget copy( ActionBase action , Meta meta , ReleaseTicketSet set ) throws Exception {
		ReleaseTicketSetTarget r = new ReleaseTicketSetTarget( meta , set );
		r.type = type;
		r.ITEM = ITEM;
		r.accepted = accepted;
		r.descoped = descoped;
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		String TYPE = ConfReader.getRequiredAttrValue( root , Release.PROPERTY_TICKETTARGETTYPE );
		type = Types.getTicketSetTargetType( TYPE , true );
		ITEM = ConfReader.getRequiredAttrValue( root , Release.PROPERTY_TICKETTARGETITEM );
		accepted = ConfReader.getBooleanAttrValue( root , Release.PROPERTY_TICKETTARGETACCEPTED , false );
		descoped = ConfReader.getBooleanAttrValue( root , Release.PROPERTY_TICKETTARGETDESCOPED , false );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETTYPE , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETITEM , ITEM );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETACCEPTED , Common.getBooleanValue( accepted ) );
		Common.xmlSetElementAttr( doc , root , Release.PROPERTY_TICKETTARGETDESCOPED , Common.getBooleanValue( descoped ) );
	}

	public void create( ActionBase action , MetaSourceProjectSet projectSet ) {
		type = VarTICKETSETTARGETTYPE.PROJECTSET;
		ITEM = projectSet.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaSourceProject project , boolean all ) {
		type = ( all )? VarTICKETSETTARGETTYPE.PROJECTALLITEMS : VarTICKETSETTARGETTYPE.PROJECTNOITEMS;
		ITEM = project.NAME;
		accepted = false;
		descoped = false;
	}
	
	public void create( ActionBase action , MetaDistrBinaryItem item ) {
		type = VarTICKETSETTARGETTYPE.DISTITEM;
		ITEM = item.KEY;
		accepted = false;
		descoped = false;
	}
	
	public boolean isAccepted() {
		return( accepted );
	}

	public boolean isDescoped() {
		return( descoped );
	}

	public boolean isProjectSet() {
		if( type == VarTICKETSETTARGETTYPE.PROJECTSET )
			return( true );
		return( false );
	}
	
	public boolean isProject() {
		if( type == VarTICKETSETTARGETTYPE.PROJECTALLITEMS || type == VarTICKETSETTARGETTYPE.PROJECTNOITEMS )
			return( true );
		return( false );
	}
		
	public boolean isBinary() {
		if( type == VarTICKETSETTARGETTYPE.DISTITEM )
			return( true );
		return( false );
	}
		
	public boolean isConfiguration() {
		if( type == VarTICKETSETTARGETTYPE.CONFITEM )
			return( true );
		return( false );
	}
		
	public boolean isDatabase() {
		if( type == VarTICKETSETTARGETTYPE.SCHEMA )
			return( true );
		return( false );
	}

	public boolean isProjectBuildOnly() {
		if( type == VarTICKETSETTARGETTYPE.PROJECTNOITEMS )
			return( true );
		return( false );
	}

	public void accept( ActionBase action ) throws Exception {
		accepted = true;
	}

	public void descope( ActionBase action ) throws Exception {
		if( !descoped ) {
			if( set.isActive() )
				accepted = false;
			
			descoped = true;
		}
	}
	
}
