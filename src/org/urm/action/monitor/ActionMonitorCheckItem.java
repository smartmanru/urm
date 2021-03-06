package org.urm.action.monitor;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.SimpleHttp;
import org.urm.db.core.DBEnums.DBEnumMonItemType;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ServerStatus;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.system.AppProductMonitoringItem;
import org.urm.meta.system.AppProductMonitoringTarget;

public class ActionMonitorCheckItem extends ActionBase {

	AppProductMonitoringTarget target;
	public AppProductMonitoringItem item;
	public MetaEnvServer server;
	
	public ServerStatus serverStatus;
	List<NodeStatus> nodeData;
	
	public ActionMonitorCheckItem( ActionBase action , String stream , AppProductMonitoringTarget target , AppProductMonitoringItem item , MetaEnvServer server ) {
		super( action , stream , "Monitoring, check item" );
		this.target = target;
		this.item = item;
		this.server = server;
		nodeData = new LinkedList<NodeStatus>(); 
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		if( server == null ) {
			if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKURL )
				monitorUrl( item.URL );
			else
			if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS )
				monitorWS( item.URL , item.WSDATA , item.WSCHECK );
			else
				exitUnexpectedState();
		}
		else
			monitorServerItems();
		return( SCOPESTATE.RunSuccess );
	}

	private boolean monitorUrl( String URL ) throws Exception {
		info( "monitor check URL " + URL + " ..." );
		boolean res = SimpleHttp.check( this , URL );
		if( !res )
			super.fail1( _Error.MonitorEnvHttpFailed1 , "Checkenv access failed URL=" + URL , URL );
		return( res );
	}
	
	private void monitorWS( String URL , String WSDATA , String WSCHECK ) throws Exception {
		info( "monitor check WS " + URL + " ..." );
		SimpleHttp query = SimpleHttp.post( this , URL , WSDATA );
		boolean res = true;
		if( !query.valid( this ) ) {
			super.fail1( _Error.MonitorEnvHttpPostFailed1 , "Checkenv post failed URL=" + URL , URL );
			res = false;
		}
		else {
			res = false;
			if( query.response.indexOf( WSCHECK ) >= 0 || query.response.matches( WSCHECK ) )
				res = true;
			
			if( !res )
				super.fail1( _Error.MonitorEnvWebServiceFailed1 , "Checkenv post result mismatched URL=" + URL , URL );
		}
	}

	private void monitorServerItems() throws Exception {
		int serverIndex = super.logStartCapture();
		serverStatus = new ServerStatus( server );
		info( "Run fast server checks, server=" + server.NAME + " ..." );
		boolean res = monitorServerItems( null );
		serverStatus.setItemsStatus( res );
		
		if( res )
			info( "Fast server checks successfully finished" );
		else
			error( "Fast server checks failed" );
		String[] log = super.logFinishCapture( serverIndex );
		serverStatus.setLog( log );
		
		for( MetaEnvServerNode node : server.getNodes() ) {
			NodeStatus nodeStatus = new NodeStatus( node );
			nodeData.add( nodeStatus );
			
			int nodeIndex = super.logStartCapture();
			info( "Run fast server checks, node=" + node.POS + " ..." );
			if( monitorServerItems( nodeStatus ) )
				info( "Fast server checks successfully finished" );
			else
				error( "Fast server checks failed" );
			log = super.logFinishCapture( nodeIndex );
			nodeStatus.setLog( log );
		}
	}
	
	private boolean monitorServerItems( NodeStatus nodeStatus ) throws Exception {
		boolean res = true;
		if( server.isRunWebUser() && !server.WEBMAINURL.isEmpty() ) {
			if( !monitorServerItemsWebUser( nodeStatus ) )
				res = false;
		}
		else
		if( server.isRunWebApp() && !server.WEBSERVICEURL.isEmpty() ) {
			for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
				if( deployment.isComponent() ) {
					MetaDistrComponent comp = deployment.getComponent();
					for( MetaDistrComponentItem ws : comp.getWebServices() ) {
						if( !monitorServerItemsWebApp( nodeStatus , ws ) )
							res = false;
					}
				}
			}
		}
		return( res );
	}

	private boolean monitorServerItemsWebUser( NodeStatus nodeStatus ) throws Exception {
		boolean res = true; 
		if( nodeStatus == null ) {
			if( !server.WEBMAINURL.isEmpty() ) {
				if( !monitorServerItemsUrl( server.WEBMAINURL ) )
					res = false;
			}
			return( res );
		}
		
		String ACCESSPOINT = nodeStatus.node.getAccessPoint( this );
		if( !monitorNodeItemsUrl( nodeStatus , ACCESSPOINT ) )
			res = false;
		return( res );
	}		

	private boolean monitorServerItemsWebApp( NodeStatus nodeStatus , MetaDistrComponentItem ws ) throws Exception {
		boolean res = true; 
		if( nodeStatus == null ) { 
			if( !server.WEBSERVICEURL.isEmpty() ) {
				String URL = ws.getURL( server.WEBSERVICEURL );
				if( !monitorServerItemsUrl( URL ) )
					res = false;
			}
			return( res );
		}
		
		String ACCESSPOINT = nodeStatus.node.getAccessPoint( this );
		String URL = ws.getURL( ACCESSPOINT );
		if( !monitorNodeItemsUrl( nodeStatus , URL ) )
			res = false;
		return( res );
	}

	private boolean monitorServerItemsUrl( String URL ) throws Exception {
		boolean res = monitorUrl( URL );
		serverStatus.addWholeUrlStatus( URL , URL , res );
		return( res );
	}		

	private boolean monitorNodeItemsUrl( NodeStatus nodeStatus , String URL ) throws Exception {
		boolean res = monitorUrl( URL );
		nodeStatus.addWholeUrlStatus( URL , URL , res );
		return( res );
	}		

	public NodeStatus[] getNodes() {
		return( nodeData.toArray( new NodeStatus[0] ) );
	}
	
}
