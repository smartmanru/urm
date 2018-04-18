package org.urm.engine.action;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.Engine;
import org.urm.engine.TaskService;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.products.EngineProductReleases;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.release.Release;

public abstract class CommandExecutor {

	public Engine engine;
	public CommandMeta commandInfo;
		
	public Map<String,CommandMethod> actionsMap = new HashMap<String,CommandMethod>();
	public List<CommandMethod> actionsList = new LinkedList<CommandMethod>();
	
	public abstract boolean runExecutorImpl( ScopeState parentState , ActionBase action , CommandMethod method );

	public CommandExecutor( Engine engine , CommandMeta commandInfo ) {
		this.engine = engine;
		this.commandInfo = commandInfo;
	}
	
	public void defineAction( CommandMethod action , String name ) throws Exception {
		actionsMap.put( name , action );
		actionsList.add( action );
		
		CommandMethodMeta method = commandInfo.getMethod( name );
		action.setMethod( method );
	}
	
	public CommandMethod getAction( String action ) throws Exception {
		CommandMethod commandAction = actionsMap.get( action ); 
		if( commandAction == null )
			Common.exit2( _Error.UnknownExecutorAction2 , "unknown action=" + action , commandInfo.name , action );
		return( commandAction );
	}

	public boolean runExecutor( ScopeState parentState , ActionBase action , CommandMethod command , boolean runTask ) {
		if( runTask ) {
			TaskService tasks = engine.getTaskService();
			EngineMethod method = new EngineMethod( action , this , command , parentState );
			tasks.executeOnceWait( method );
			if( method.runFailed )
				return( false );
			return( true );
		}

		return( runExecutorImpl( parentState , action , command ) );
	}
	
	public boolean runMethod( ScopeState parentState , ActionBase action , CommandMethod method ) {
		try {
			action.debug( "execute " + method.getClass().getSimpleName() + " ..." );
			action.debug( "context: " + action.context.getInfo() );
			method.run( parentState , action );
			if( action.isOK() )
				return( true );
		}
		catch( Throwable e ) {
			action.fail1( _Error.ActionException1 , "Exception in method=" + method.method.name + ": " + e.toString() , method.method.name );
			String trace = System.getenv( "TRACE" );
			if( trace != null && trace.equals( "yes" ) )
				e.printStackTrace();
			else {
				RunError ex = Common.getExitException( e );
				if( ex == null || action.context.CTX_SHOWALL )
					action.handle( e );
				else
					action.error( "exception: " + ex.getMessage() );
			}
		}
		
		return( false );
	}
	
	public void checkRequired( ActionBase action , String value , String name ) throws Exception {
		if( value == null || value.isEmpty() )
			action.exit1( _Error.NameUndefined1 , name + " is undefined. Exiting" , name );
	}
	
	public String[] getArgList( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArgList( pos ) );
	}
	
	public void checkNoArgs( ActionBase action , int pos ) throws Exception {
		String[] args = getArgList( action , pos );
		if( pos >= args.length )
			return;
		
		String xargs = Common.getList( args );
		action.exit1( _Error.UnexpectedExtraArguments1 , "unexpected extra arguments: " + Common.getQuoted( xargs ) + "; see help to find syntax" , xargs );
	}
	
	public DBEnumScopeCategoryType getRequiredCategoryArg( ActionBase action , int pos ) throws Exception {
		DBEnumScopeCategoryType CATEGORY = getCategoryArg( action , pos );
		if( CATEGORY == null )
			action.exit1( _Error.ArgumentRequired1 , "CATEGORY argument is required" , "CATEGORY" );
		return( CATEGORY );
	}
	
	public DBEnumBuildModeType getRequiredBuildModeArg( ActionBase action , int pos ) throws Exception {
		String value = getRequiredArg( action , pos , "BUILDMODE" );
		DBEnumBuildModeType BUILDMODE = null;
		for( DBEnumBuildModeType x : DBEnumBuildModeType.values() ) {
			if( value.equals( Common.getEnumLower( x ) ) ) {
				BUILDMODE = x;
				break;
			}
		}
				
		if( BUILDMODE == null )
			action.exit1( _Error.UnknownBuildMode1 , "unknown buildMode=" + value , value );
		return( BUILDMODE );
	}
	
	public DBEnumScopeCategoryType getCategoryArg( ActionBase action , int pos ) throws Exception {
		if( pos >= action.context.options.getArgCount() )
			return( null );
		
		return( DBEnumScopeCategoryType.getValue( getArg( action , pos ) , true ) );
	}
	
	public String getRequiredArg( ActionBase action , int pos , String argName ) throws Exception {
		String value = getArg( action , pos );
		if( value.isEmpty() )
			action.exit1( _Error.ArgumentRequired1 , argName + " is empty" , argName );
		
		return( value );
	}

	public Date getRequiredDateArg( ActionBase action , int pos , String argName ) throws Exception {
		String value = getArg( action , pos );
		if( value.isEmpty() )
			action.exit1( _Error.ArgumentRequired1 , argName + " is empty" , argName );
		
		return( Common.getDateValue( value ) );
	}

	public String getArg( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArg( pos ) );
	}

	public int getIntArg( ActionBase action , int pos , int defValue ) {
		return( action.context.options.getIntArg( pos , defValue ) );
	}
	
	public Date getDateArg( ActionBase action , int pos ) {
		return( action.context.options.getDateArg( pos ) );
	}

	public ReleaseLifecycle getLifecycleArg( ActionBase action , int pos ) throws Exception {
		String value = getArg( action , pos );
		if( value.isEmpty() )
			return( null );
		
		EngineLifecycles lifecycles = action.getEngineLifecycles();
		return( lifecycles.getLifecycle( value ) );
	}

	public Release getReleaseByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		AppProduct product = action.getContextProduct();
		EngineProductReleases releases = product.findReleases();
		return( releases.getReleaseByLabel( action , RELEASELABEL ) );
	}
	
}
