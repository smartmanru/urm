package org.urm.action.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	List<MetaMonitoringItem> serverItems;
	Map<MetaEnvServerNode,List<MetaMonitoringItem>> nodeItems;
	
	public ActionMonitorCheckItem( ActionBase action , String stream , MetaMonitoring mon , MetaMonitoringTarget target , MetaMonitoringItem item , MetaEnvServer server ) {
		super( action , stream );
		this.mon = mon;
		this.target = target;
		this.item = item;
		this.server = server;
		serverItems = new LinkedList<MetaMonitoringItem>();
		nodeItems = new HashMap<MetaEnvServerNode,List<MetaMonitoringItem>>(); 
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
		if( server.isWebUser() && !server.WEBMAINURL.isEmpty() )
			monitorServerItemsWebUser();
		else
		if( server.isWebApp() && !server.WEBSERVICEURL.isEmpty() ) {
			for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
				if( deployment.comp != null ) {
					for( MetaDistrComponentWS ws : deployment.comp.getWebServices() )
						monitorServerItemsWebApp( ws );
				}
			}
		}
	}

	private void monitorServerItemsWebUser() throws Exception {
		if( !server.WEBMAINURL.isEmpty() )
			monitorServerItemsUrl( server.WEBMAINURL );
		
		for( MetaEnvServerNode node : server.getNodes() ) {
			String ACCESSPOINT = node.getAccessPoint( this );
			monitorNodeItemsUrl( node , ACCESSPOINT );
		}
	}		

	private void monitorServerItemsWebApp( MetaDistrComponentWS ws ) throws Exception {
		if( !server.WEBSERVICEURL.isEmpty() ) {
			String URL = ws.getURL( server.WEBSERVICEURL );
			monitorServerItemsUrl( URL );
		}
		
		for( MetaEnvServerNode node : server.getNodes() ) {
			String ACCESSPOINT = node.getAccessPoint( this );
			String URL = ws.getURL( ACCESSPOINT );
			monitorNodeItemsUrl( node , URL );
		}
	}

	private void monitorServerItemsUrl( String URL ) throws Exception {
		MetaMonitoringItem item = new MetaMonitoringItem( server.meta , target );
		item.setUrlItem( this , URL );
		boolean res = monitorUrl( item.URL );
		item.setMonitorStatus( res );
		serverItems.add( item );
	}		

	private void monitorNodeItemsUrl( MetaEnvServerNode node , String URL ) throws Exception {
		MetaMonitoringItem item = new MetaMonitoringItem( server.meta , target );
		item.setUrlItem( this , URL );
		boolean res = monitorUrl( item.URL );
		item.setMonitorStatus( res );
		
		List<MetaMonitoringItem> items = nodeItems.get( node );
		if( items == null ) {
			items = new LinkedList<MetaMonitoringItem>();
			nodeItems.put( node , items );
		}
		items.add( item );
	}		

	public MetaMonitoringItem[] getServerItems() {
		return( serverItems.toArray( new MetaMonitoringItem[0] ) );
	}
	
	public MetaMonitoringItem[] getNodeItems( MetaEnvServerNode node ) {
		List<MetaMonitoringItem> items = nodeItems.get( node );
		if( items == null )
			return( new MetaMonitoringItem[0] );
		return( items.toArray( new MetaMonitoringItem[0] ) );
	}
	
}
