package org.urm.server.action;

import org.urm.server.ServerTransaction;
import org.urm.server.SessionContext;
import org.urm.server.meta.FinalLoader;
import org.urm.server.meta.FinalRegistry;
import org.urm.server.storage.Artefactory;

public class ActionInit extends ActionBase {

	public CommandAction commandAction;
	public String actionName;

	private ServerTransaction transaction;
	
	public ActionInit( SessionContext session , Artefactory artefactory , CommandExecutor executor , CommandContext context , CommandOutput output , CommandAction commandAction , String actionName ) {
		super( session , artefactory , executor , context , output );
		this.commandAction = commandAction;
		this.actionName = actionName;
	}

	@Override
	protected void runBefore() throws Exception {
		exit( "unexpected operation" );
	}
	
	@Override
	protected void runBefore( ActionScope scope ) throws Exception {
		exit( "unexpected operation" );
	}
	
	public FinalLoader getMetaLoader() {
		return( engine.metaLoader );
	}
	
	public FinalRegistry changeRegistry( FinalRegistry sourceRegistry ) {
		synchronized( engine ) {
			try {
				if( !startTransaction() )
					return( null );
					
				if( sourceRegistry == engine.metaLoader.getRegistry( this ) )
					return( sourceRegistry.copy( this ) );
			}
			catch( Throwable e ) {
				log( e );
			}
			
			abortTransaction();
			return( null );
		}
	}

	public boolean saveRegisty( FinalRegistry registry ) {
		synchronized( engine ) {
			if( !continueTransaction() )
				return( false );
			
			FinalRegistry registryOld = engine.metaLoader.getRegistry( this );
			try {
				engine.metaLoader.setRegistry( registry );
				if( commitTransaction() )
					return( true );
			}
			catch( Throwable e ) {
				log( "unable to save registry" , e );
			}

			try {
				engine.metaLoader.setRegistry( registryOld );
			}
			catch( Throwable e ) {
				log( "unable to restore registry" , e );
			}
			
			abortTransaction();
		}
		
		return( false );
	}
	
	public void cancelChangeRegistry() throws Exception {
		synchronized( engine ) {
			abortTransaction();
		}
	}

	public ServerTransaction getTransaction() {
		return( transaction );
	}
	
	private boolean startTransaction() {
		if( transaction != null )
			return( false );
		
		transaction = engine.startTransaction( this );
		return( true );
	}

	private void abortTransaction() {
		engine.abortTransaction( transaction );
		transaction = null;
	}
	
	private boolean commitTransaction() {
		if( !engine.commitTransaction( transaction ) )
			return( false );
		
		transaction = null;
		return( true );
	}
	
	private boolean continueTransaction() {
		if( transaction == null )
			return( false );
		
		if( transaction != engine.getTransaction( this ) )
			return( false );
		
		return( true );
	}

}
