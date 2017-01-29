package org.urm.action.database;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnvServer;

public class ActionManageRegistry extends ActionBase {

	String RELEASEVER;
	String CMD;
	MetaDistrDelivery delivery;
	String indexScope;
	
	public ActionManageRegistry( ActionBase action , String stream , String RELEASEVER , String CMD , MetaDistrDelivery delivery , String indexScope ) {
		super( action , stream , "Change database registry, release=" + RELEASEVER + ", CMD=" + CMD );
		this.RELEASEVER = RELEASEVER;
		this.CMD = CMD;
		this.delivery = delivery;
		this.indexScope = ( indexScope == null )? "" : indexScope;
	}

	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit1( _Error.ConnectFailed1 , "unable to connect to server=" + server.NAME , server.NAME );

		info( "RELEASE " + RELEASEVER + ": " + CMD + " database registry ..." );
		DatabaseRegistry registry = DatabaseRegistry.getRegistry( this , client );
		registry.setActiveRelease( this , RELEASEVER );
		
		if( registry.isReleaseUnknown( this ) )
			exit1( _Error.UnknownReleaseVersion1 , "unknown release version=" + RELEASEVER , RELEASEVER );
		
		if( CMD.equals( "status" ) )
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
			exit1( _Error.UnexpectedManageCommand1 , "unexpected manage command=" + CMD , CMD );
		
		return( SCOPESTATE.RunSuccess );
	}

	private void executePrintRegistry( DatabaseRegistry registry ) throws Exception {
		String state = ( registry.isReleaseFinished( this ) )? "APPLIED" : "INCOMPLETE";
		info( "VERSION=" + RELEASEVER + " STATE=" + state );
		
		registry.readIncompleteScripts( this );
		for( String delivery : registry.getStateDeliveries( this ) ) {
			info( "DELIVERY: " + delivery );
			Map<String,String> data = registry.getStateData( this , delivery );
			for( String key : Common.getSortedKeys( data ) ) {
				String status = data.get( key );
				String value = ( status.equals( DatabaseRegistry.SCRIPT_STATUS_APPLIED ) )? "OK" : "FAILED";
				info( "\tkey=" + key + ", status=" + value );
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
			info( "DELIVERY: " + delivery.NAME );
			
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
		if( registry.isReleaseFinished( this ) )
			ifexit( _Error.ReleaseFinished0 , "release is finished" , null );
		
		if( delivery == null )
			registry.dropRelease( this );
		else
		if( indexScope.equals( "all" ) )
			registry.dropReleaseDelivery( this , delivery );
		else {
			registry.readDeliveryState( this , delivery );
			Map<String,String> data = registry.getStateData( this , delivery.NAME );
			
			List<String> items = new LinkedList<String>(); 
			for( String item : data.keySet() ) {
				if( item.matches( indexScope ) )
					items.add( item );
			}
			
			if( items.isEmpty() ) {
				info( "nothing matched, ignored." );
				return;
			}
			
			registry.dropReleaseDeliveryItems( this , delivery , items.toArray( new String[0] ) );
		}
	}
	
}
