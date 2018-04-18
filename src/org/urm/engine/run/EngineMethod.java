package org.urm.engine.run;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.engine.DataService;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.status.ScopeState;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class EngineMethod extends EngineExecutorTask {
	
	private ActionBase action;
	private CommandExecutor executor;
	private CommandMethod command;
	private ScopeState parentState;

	private DBConnection connection;
	private List<DBConnection> dbstatus;
	private Map<String,EngineMethodProduct> metastatus;
	private List<EngineMethodNotify> notify;
	
	public EngineMethod( ActionBase action , CommandExecutor executor , CommandMethod command , ScopeState parentState ) {
		super( executor.commandInfo.name + "::" + command.method.name );
		this.action = action;
		this.executor = executor;
		this.command = command;
		this.parentState = parentState;
		
		dbstatus = new LinkedList<DBConnection>();
		metastatus = new HashMap<String,EngineMethodProduct>();
		notify = new LinkedList<EngineMethodNotify>(); 
	}

	@Override
	public void execute() throws Exception {
		boolean res = false;
		try {
			action.setMethod( this );
			if( executor.runExecutorImpl( parentState , action , command ) )
				res = true;
		}
		catch( Throwable e ) {
			action.log( "execute method error" , e );
		}

		try {
			if( res ) {
				commit();
				super.finishSuccessful();
			}
			else {
				abort();
				super.finishFailed( null );
			}
			
			finishDatabase();
		}
		catch( Throwable e ) {
			action.log( "execute method finish error" , e );
		}
	}
	
	private synchronized void commit() throws Exception {
		try {
			if( connection != null ) {
				connection.close( true );
				connection = null;
			}
			
			for( EngineMethodProduct emm : metastatus.values() )
				emm.commit();
			
			notifyEvents();
		}
		catch( Throwable e ) {
			action.log( "execute method commit error" , e );
			abort();
		}
	}
	
	private synchronized void abort() throws Exception {
		if( connection != null ) {
			connection.close( false );
			connection = null;
		}
		
		for( EngineMethodProduct emm : metastatus.values() )
			emm.abort();
	}

	public ActionBase getAction() {
		return( action );
	}
	
	public synchronized DBConnection getMethodConnection( ActionBase action ) throws Exception {
		if( connection == null ) {
			Engine engine = action.engine;
			DataService data = engine.getData();
			EngineDB db = data.getDatabase();
			connection = db.getConnection( action );
		}
		
		return( connection );
	}
	
	public synchronized DBConnection getStatusConnection( ActionBase action ) throws Exception {
		Engine engine = action.engine;
		DataService data = engine.getData();
		EngineDB db = data.getDatabase();
		DBConnection c = db.getConnection( action );
		dbstatus.add( c );
		return( c );
	}

	private void finishDatabase() throws Exception {
		for( DBConnection c : dbstatus ) {
			if( c.isConnected() )
				c.close( true );
		}
		dbstatus.clear();
	}

	public ReleaseRepository changeReleaseRepository( Meta meta ) throws Exception {
		return( changeReleaseRepository( meta.getStorage() ) );
	}
	
	public ReleaseRepository changeReleaseRepository( ProductMeta storage ) throws Exception {
		if( !storage.isPrimary() )
			Common.exitUnexpected();
		EngineMethodProduct emm = getEmm( storage.getEngineProduct() );
		return( emm.changeReleaseRepository( storage ) );
	}
	
	public DistRepository changeDistRepository( AppProduct product ) throws Exception {
		EngineMethodProduct emm = getEmm( product.getEngineProduct() );
		return( emm.changeDistRepository() );
	}

	private EngineMethodProduct checkUpdateReleaseRepository( ReleaseRepository repo ) throws Exception {
		EngineMethodProduct emm = findEmm( repo.meta.getEngineProduct() );
		if( emm == null )
			Common.exitUnexpected();
		emm.checkUpdateReleaseRepository( repo );
		return( emm );
	}

	private EngineMethodProduct checkUpdateDistRepository( DistRepository repo ) throws Exception {
		EngineMethodProduct emm = findEmm( repo.releases.ep );
		if( emm == null )
			Common.exitUnexpected();
		emm.checkUpdateDistRepository( repo );
		return( emm );
	}

	public void checkUpdateDistItem( DistRepositoryItem item ) throws Exception {
		EngineMethodProduct emm = findEmm( item.repo.releases.ep );
		if( emm == null )
			Common.exitUnexpected();
		emm.checkUpdateDistItem( item );
	}
	
	public void checkUpdateRelease( Release release ) throws Exception {
		Meta meta = release.getMeta();
		EngineMethodProduct emm = findEmm( meta.ep );
		if( emm == null )
			Common.exitUnexpected();
		emm.checkUpdateRelease( release );
	}
	
	public Release createRelease( ReleaseRepository repo , Release release ) throws Exception {
		EngineMethodProduct emm = checkUpdateReleaseRepository( repo );
		emm.createRelease( release );
		return( release );
	}
	
	public Release changeRelease( ReleaseRepository repo , Release release ) throws Exception {
		EngineMethodProduct emm = checkUpdateReleaseRepository( repo );
		return( emm.updateRelease( release ) );
	}
	
	public Release deleteRelease( ReleaseRepository repo , Release release ) throws Exception {
		EngineMethodProduct emm = checkUpdateReleaseRepository( repo );
		emm.deleteRelease( release );
		return( release );
	}
	
	public DistRepositoryItem createDistItem( DistRepository repo , DistRepositoryItem item ) throws Exception {
		EngineMethodProduct emm = checkUpdateDistRepository( repo );
		emm.createDistItem( item );
		return( item );
	}
	
	public DistRepositoryItem changeDistItem( DistRepository repo , DistRepositoryItem item ) throws Exception {
		EngineMethodProduct emm = checkUpdateDistRepository( repo );
		return( emm.updateDistItem( item ) );
	}

	public DistRepositoryItem deleteDistItem( DistRepository repo , DistRepositoryItem item ) throws Exception {
		EngineMethodProduct emm = checkUpdateDistRepository( repo );
		emm.deleteDistItem( item );
		return( item );
	}
	
	private synchronized EngineMethodProduct getEmm( EngineProduct ep ) throws Exception {
		EngineMethodProduct emm = metastatus.get( ep.productName );
		if( emm == null ) {
			emm = new EngineMethodProduct( this , ep );
			metastatus.put( ep.productName , emm );
		}
		
		return( emm ); 
	}

	private synchronized EngineMethodProduct findEmm( EngineProduct ep ) throws Exception {
		return( metastatus.get( ep.productName ) );
	}

	public synchronized void addCommitEvent( EngineEventsSource source , int eventOwner , int eventType , Object data ) {
		EngineMethodNotify event = new EngineMethodNotify( source , eventOwner , eventType , data );
		notify.add( event );
	}

	private void notifyEvents() {
		for( EngineMethodNotify event : notify )
			event.source.notifyCustomEvent( event.eventOwner , event.eventType , event.data );
	}
	
}
