package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.ServerObject;
import org.urm.meta.Types;
import org.urm.meta.Types.VarLCSTAGE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerReleaseLifecyclePhase extends ServerObject {

	ServerReleaseLifecycle lc;
	
	public String ID;
	public String DESC;
	public VarLCSTAGE stage;
	public int pos;
	public int days;
	
	public ServerReleaseLifecyclePhase( ServerReleaseLifecycle lc ) {
		super( lc );
		this.lc = lc;
	}
	
	public void load( Node root ) throws Exception {
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
		stage = Types.getLCStage( ConfReader.getAttrValue( root , "stage" ) , true );
		pos = ConfReader.getIntegerAttrValue( root , "pos" , 0 );
		days = ConfReader.getIntegerAttrValue( root , "days" , 0 );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "stage" , Common.getEnumLower( stage ) );
		Common.xmlSetElementAttr( doc , root , "pos" , "" + pos );
		Common.xmlSetElementAttr( doc , root , "days" , "" + days );
	}
	
	public ServerReleaseLifecyclePhase copy( ServerReleaseLifecycle rlc ) throws Exception {
		ServerReleaseLifecyclePhase r = new ServerReleaseLifecyclePhase( rlc );
		r.ID = ID;
		r.DESC = DESC;
		r.stage = stage;
		r.pos = pos;
		r.days = days;
		return( r );
	}

	public boolean isRelease() {
		if( stage == VarLCSTAGE.RELEASE )
			return( true );
		return( false );
	}

	public boolean isDeploy() {
		if( stage == VarLCSTAGE.DEPLOYMENT )
			return( true );
		return( false );
	}
	
}
