package org.urm.engine.status;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.system.RoleItemFailed;
import org.urm.meta.system.WholeUrlFailed;

public class ServerStatus extends Status {

	public MetaEnvServer server;
	
	public boolean itemsStatus;
	
	public boolean nodeFailed;
	public boolean roleFailed;
	public boolean wholeUrlFailed;
	public boolean databaseFailed;

	List<RoleItemFailed> roles;
	List<WholeUrlFailed> wholeUrls;
	
	public ServerStatus( MetaEnvServer server ) {
		super( STATETYPE.TypeServer , null , server );
		this.server = server;
	
		itemsStatus = false;
		nodeFailed = false;
		roleFailed = false;
		wholeUrlFailed = false;
		databaseFailed = false;
		roles = new LinkedList<RoleItemFailed>();
		wholeUrls = new LinkedList<WholeUrlFailed>();
	}

	public void setItemsStatus( boolean itemsStatus ) {
		this.itemsStatus = itemsStatus;
	}
	
	public void addNodeStatus( NodeStatus status ) {
		if( status.isFailed() ) 
			nodeFailed = true;
		itemState = StatusData.addState( itemState , status.itemState );
	}

	public void addRoleStatus( String role , MetaEnvServerNode node , boolean failed ) {
		if( failed ) {
			roleFailed = true;
			roles.add( new RoleItemFailed( role , node ) );
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_ERRORS_ALERTS );
		}
		else
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_HEALTHY );
	}

	public void addWholeUrlStatus( String URL , String role , boolean ok ) throws Exception {
		if( !ok ) {
			wholeUrlFailed = true;
			wholeUrls.add( new WholeUrlFailed( URL , role ) );
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_ERRORS_ALERTS );
		}
		else
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_HEALTHY );
	}

	public void addDatabaseStatus( boolean ok ) {
		if( ok )
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_HEALTHY );
		else {
			databaseFailed = true;
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_ERRORS_ALERTS );
		}
	}
	
}
