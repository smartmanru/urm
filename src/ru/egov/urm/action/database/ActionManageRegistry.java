package ru.egov.urm.action.database;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaEnvServer;

public class ActionManageRegistry extends ActionBase {

	String RELEASEVER;
	String CMD;
	MetaDistrDelivery delivery;
	String indexScope;
	
	public ActionManageRegistry( ActionBase action , String stream , String RELEASEVER , String CMD , MetaDistrDelivery delivery , String indexScope ) {
		super( action , stream );
		this.RELEASEVER = RELEASEVER;
		this.CMD = CMD;
		this.delivery = delivery;
		this.indexScope = ( indexScope == null )? "" : indexScope;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit( "unable to connect to server=" + server.NAME );

		comment( "RELEASE " + RELEASEVER + ": " + CMD + " database registry ..." );
		DatabaseRegistry registry = DatabaseRegistry.getRegistry( this , client );
		registry.setActiveRelease( this , RELEASEVER );
		
		if( registry.isReleaseUnknown( this ) )
			exit( "unknown release version=" + RELEASEVER );
		
		if( CMD.equals( "print" ) )
			executePrintRegistry( registry );
		else
		if( CMD.equals( "correct" ) )
			executeCorrectRegistry( registry );
		else
		if( CMD.equals( "rollback" ) )
			executeRollbackRegistry( registry );
		else
		if( CMD.equals( "drop" ) )
			executeDropRegistry( registry );
		else
			exit( "unexpected manage command=" + CMD );
		
		return( true );
	}

	private void executePrintRegistry( DatabaseRegistry registry ) throws Exception {
		String state = ( registry.isReleaseFinished( this ) )? "APPLIED" : "INCOMPLETE";
		comment( "VERSION=" + RELEASEVER + " STATE=" + state );
		
		registry.readIncompleteScripts( this );
		for( String delivery : registry.getStateDeliveries( this ) ) {
			comment( "DELIVERY: " + delivery );
			Map<String,String> data = registry.getStateData( this , delivery );
			for( String key : Common.getSortedKeys( data ) ) {
				String status = data.get( key );
				String value = ( status.equals( DatabaseRegistry.SCRIPT_STATUS_APPLIED ) )? "OK" : "FAILED";
				comment( "\tkey=" + key + ", status=" + value );
			}
		}
	}
	
	private void executeCorrectRegistry( DatabaseRegistry registry ) throws Exception {
		registry.readIncompleteScripts( this );

		boolean present = false;
		for( String deliveryName : registry.getStateDeliveries( this ) ) {
			if( delivery != null && !delivery.NAME.equals( deliveryName ) ) {
				debug( "skip errors in delivery=" + deliveryName );
				continue;
			}
			
			Map<String,String> data = registry.getStateData( this , deliveryName );
			info( "DELIVERY: " + delivery );
			
			for( String key : Common.getSortedKeys( data ) ) {
				if( indexScope.isEmpty() == false && key.matches( indexScope ) )
					continue;

				info( "correct script " + key + " ..." );
				registry.correctScript( this , deliveryName , key );
				present = true;
			}
		}
		
		if( !present ) {
			info( "nothing to correct" );
			return;
		}
		
		registry.finishReleaseState( this );
	}
	
	private void executeRollbackRegistry( DatabaseRegistry registry ) throws Exception {
	}
	
	private void executeDropRegistry( DatabaseRegistry registry ) throws Exception {
	}
	
}
