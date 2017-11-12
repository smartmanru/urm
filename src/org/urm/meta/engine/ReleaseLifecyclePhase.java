package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnumTypes.*;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseLifecyclePhase extends EngineObject {

	ReleaseLifecycle lc;
	
	public String ID;
	public String DESC;
	public DBEnumLifecycleStageType stage;
	public int pos;
	private int days;
	
	public ReleaseLifecyclePhase( ReleaseLifecycle lc ) {
		super( lc );
		this.lc = lc;
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public void load( Node root ) throws Exception {
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
		stage = DBEnumLifecycleStageType.getValue( ConfReader.getAttrValue( root , "stage" ) , true );
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
	
	public ReleaseLifecyclePhase copy( ReleaseLifecycle rlc ) throws Exception {
		ReleaseLifecyclePhase r = new ReleaseLifecyclePhase( rlc );
		r.ID = ID;
		r.DESC = DESC;
		r.stage = stage;
		r.pos = pos;
		r.days = days;
		return( r );
	}

	public void create( ActionBase action , String name , String desc , DBEnumLifecycleStageType stage , int pos , int days ) throws Exception {
		this.ID = name;
		this.DESC = desc;
		this.stage = stage;
		this.pos = pos;
		this.days = days;
	}
	
	public boolean isRelease() {
		if( stage == DBEnumLifecycleStageType.RELEASE )
			return( true );
		return( false );
	}

	public boolean isDeploy() {
		if( stage == DBEnumLifecycleStageType.DEPLOYMENT )
			return( true );
		return( false );
	}

	public int getDuration() {
		if( days < 0 )
			return( 0 );
		return( days );
	}

	public boolean isUnlimited() {
		if( days < 0 )
			return( true );
		return( false );
	}
	
}
