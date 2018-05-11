package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.release.ReleaseRepository.ReleaseOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DistRepositoryItemAction {

	DistRepositoryItem item;

	public String ACTION_NAME;
	public long actionStarted;
	public boolean actionSuccess;
	public ReleaseOperation actionOp;
	public String ACTION_INFO;
	
	public DistRepositoryItemAction( DistRepositoryItem item ) {
		actionStarted = 0;
		actionSuccess = false;
	}

	public DistRepositoryItemAction copy( ActionBase action , DistRepositoryItem item ) throws Exception {
		DistRepositoryItemAction rh = new DistRepositoryItemAction( item );
		rh.ACTION_NAME = ACTION_NAME;
		rh.actionStarted = actionStarted;
		rh.actionSuccess = actionSuccess;
		rh.actionOp = actionOp;
		rh.ACTION_INFO = ACTION_INFO;
		return( rh );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		ACTION_NAME = ConfReader.getAttrValue( root , "action" );
		actionStarted = ConfReader.getLongAttrValue( root , "started" , 0 );
		actionSuccess = ConfReader.getBooleanAttrValue( root , "success" , false );
		actionOp = ReleaseOperation.valueOf( ConfReader.getAttrValue( root , "op" ).toUpperCase() );
		ACTION_INFO = ConfReader.getAttrValue( root , "info" );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "action" , ACTION_NAME );
		Common.xmlSetElementAttr( doc , root , "started" , Long.toString( actionStarted ) );
		Common.xmlSetElementAttr( doc , root , "success" , Common.getBooleanValue( actionSuccess ) );
		Common.xmlSetElementAttr( doc , root , "op" , Common.getEnumLower( actionOp ) );
		Common.xmlSetElementAttr( doc , root , "info" , ACTION_INFO );
	}
	
	public void create( ActionBase action , boolean success , ReleaseOperation op , String msg ) throws Exception {
		ACTION_NAME = action.getClass().getSimpleName();
		actionStarted = action.blotterTreeItem.startTime;
		actionSuccess = success;
		actionOp = op;
		ACTION_INFO = msg;
	}
	
}
