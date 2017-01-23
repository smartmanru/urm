package org.urm.action.monitor;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.SimpleHttp;
import org.urm.meta.product.MetaDistrComponentWS;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringItem;
import org.urm.meta.product.MetaMonitoringTarget;

public class ActionMonitorCheckItem extends ActionBase {

	public MetaMonitoring mon;
	MetaMonitoringTarget target;
	public MetaMonitoringItem item;
	public MetaEnvServer server;
	
	public ServerStatus serverStatus;
	List<NodeStatus> nodeData;
	
	public ActionMonitorCheckItem( ActionBase action , String stream , MetaMonitoring mon , MetaMonitoringTarget target , MetaMonitoringItem item , MetaEnvServer server ) {
		super( action , stream , "Monitoring, check item" );
		this.mon = mon;
		this.target = target;
		this.item = item;
		this.server = server;
		nodeData = new LinkedList<NodeStatus>(); 
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		if( server == null ) {
			if( item.monitorUrl )
				monitorUrl( item.URL );
			else
			if( item.monitorWS )
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
		serverStatus = new ServerStatus( this , server );
		info( "Run fast server checks, server=" + server.NAME + " ..." );
		boolean res = monitorServerItems( null );
		serverStatus.setActionStatus( res );
		
		if( res )
			info( "Fast server checks successfully finished" );
		else
			error( "Fast server checks failed" );
		String[] log = super.logFinishCapture( serverIndex );
		serverStatus.setLog( log );
		
		for( MetaEnvServerNode node : server.getNodes() ) {
			NodeStatus nodeStatus = new NodeStatus( this , node );
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
		if( server.isWebUser() && !server.WEBMAINURL.isEmpty() ) {
			if( !monitorServerItemsWebUser( nodeStatus ) )
				res = false;
		}
		else
		if( server.isWebApp() && !server.WEBSERVICEURL.isEmpty() ) {
			for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
				if( deployment.comp != null ) {
					for( MetaDistrComponentWS ws : deployment.comp.getWebServices() ) {
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

	private boolean monitorServerItemsWebApp( NodeStatus nodeStatus , MetaDistrComponentWS ws ) throws Exception {
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
		MetaMonitoringItem item = new MetaMonitoringItem( server.meta , target );
		item.setUrlItem( this , URL );
		boolean res = monitorUrl( item.URL );
		item.setMonitorStatus( res );
		serverStatus.addWholeUrlStatus( item.URL , item.NAME , res );
		return( res );
	}		

	private boolean monitorNodeItemsUrl( NodeStatus nodeStatus , String URL ) throws Exception {
		MetaMonitoringItem item = new MetaMonitoringItem( server.meta , target );
		item.setUrlItem( this , URL );
		boolean res = monitorUrl( item.URL );
		item.setMonitorStatus( res );
		
		nodeStatus.addWholeUrlStatus( item.URL , item.NAME , res );
		return( res );
	}		

	public NodeStatus[] getNodes() {
		return( nodeData.toArray( new NodeStatus[0] ) );
	}
	
}
