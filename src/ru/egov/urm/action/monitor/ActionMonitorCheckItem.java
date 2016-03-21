package ru.egov.urm.run.monitor;

import ru.egov.urm.SimpleHttp;
import ru.egov.urm.meta.MetaMonitoring;
import ru.egov.urm.meta.MetaMonitoringItem;
import ru.egov.urm.run.ActionBase;

public class ActionMonitorCheckItem extends ActionBase {

	MetaMonitoring mon;
	MetaMonitoringItem item;
	
	public ActionMonitorCheckItem( ActionBase action , String stream , MetaMonitoring mon , MetaMonitoringItem item ) {
		super( action , stream );
		this.mon = mon;
		this.item = item;
	}

	@Override protected boolean executeSimple() throws Exception {
		if( item.monitorUrl )
			monitorUrl( item.URL );
		else
		if( item.monitorWS )
			monitorWS( item.URL , item.WSDATA , item.WSCHECK );
		else
			exitUnexpectedState();
		return( true );
	}

	private void monitorUrl( String URL ) throws Exception {
		log( "monitor check URL " + URL + " ..." );
		if( !SimpleHttp.check( this , URL ) )
			super.setFailed();
	}
	
	private void monitorWS( String URL , String WSDATA , String WSCHECK ) throws Exception {
		log( "monitor check WS " + URL + " ..." );
		SimpleHttp query = SimpleHttp.post( this , URL , WSDATA );
		if( !query.valid( this ) ) {
			super.setFailed();
			return;
		}
		
		if( query.response.indexOf( WSCHECK ) >= 0 || query.response.matches( WSCHECK ) )
			return;
		
		super.setFailed();
	}
	
}
