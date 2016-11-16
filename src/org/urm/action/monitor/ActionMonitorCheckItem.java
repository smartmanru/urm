package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.SimpleHttp;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringItem;

public class ActionMonitorCheckItem extends ActionBase {

	public MetaMonitoring mon;
	public MetaMonitoringItem item;
	public MetaEnvServer server;
	
	public ActionMonitorCheckItem( ActionBase action , String stream , MetaMonitoring mon , MetaMonitoringItem item , MetaEnvServer server ) {
		super( action , stream );
		this.mon = mon;
		this.item = item;
		this.server = server;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		if( item.monitorUrl )
			monitorUrl( item.URL );
		else
		if( item.monitorWS )
			monitorWS( item.URL , item.WSDATA , item.WSCHECK );
		else
			exitUnexpectedState();
		return( SCOPESTATE.RunSuccess );
	}

	private void monitorUrl( String URL ) throws Exception {
		info( "monitor check URL " + URL + " ..." );
		boolean res = SimpleHttp.check( this , URL );
		if( !res )
			super.fail1( _Error.MonitorEnvHttpFailed1 , "Checkenv access failed URL=" + URL , URL );
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
	
}
