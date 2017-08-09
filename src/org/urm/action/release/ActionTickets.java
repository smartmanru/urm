package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTicketSet;

public class ActionTickets extends ActionBase {

	String RELEASELABEL;
	public Dist dist;
	public String method;
	String[] args;
	
	public static String METHOD_CREATESET = "createset";
	public static String METHOD_MODIFYSET = "modifyset";
	public static String METHOD_DROPSET = "dropset";

	public static String OPTION_DESCOPE = "descope";
	
	public ActionTickets( ActionBase action , String stream , Dist dist , String method , String[] args ) {
		super( action , stream , "change tickets release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.method = method;
		this.args = args;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.openForDataChange( this );
		
		try {
			executeCommand();
			dist.saveReleaseXml( this );
		}
		catch( Throwable e ) {
			dist.closeDataChange( this );
			return( SCOPESTATE.RunFail );
		}
		
		dist.closeDataChange( this );
		return( SCOPESTATE.RunSuccess );
	}

	private void executeCommand() throws Exception {
		if( method.equals( METHOD_CREATESET ) ) {
			if( args.length < 2 || args.length > 3 ) {
				exitInvalidArgs();
				return;
			}
			
			String code = args[0];
			String name = args[1];
			String comments = ( args.length > 2 )? args[2] : "";
			executeCreateSet( code , name , comments );
		}
		else
		if( method.equals( METHOD_MODIFYSET ) ) {
			if( args.length < 3 || args.length > 4 ) {
				exitInvalidArgs();
				return;
			}
			
			String code = args[0];
			String codeNew = args[1];
			String nameNew = args[2];
			String commentsNew = ( args.length > 3 )? args[3] : "";
			executeModifySet( code , codeNew , nameNew , commentsNew );
		}
		else
		if( method.equals( METHOD_DROPSET ) ) {
			if( args.length < 1 || args.length > 2 ) {
				exitInvalidArgs();
				return;
			}
			
			String code = args[0];
			String option = ( args.length > 1 )? args[1] : "";
			boolean descope = ( option.equals( OPTION_DESCOPE ) )? true : false;
			executeDropSet( code , descope );
		}
			
	}

	private void exitInvalidArgs() throws Exception {
		super.fail0( _Error.InvalidSyntax0 , "Invalid command syntax, see help" );
		
	}

	private void executeCreateSet( String code , String name , String comments ) throws Exception {
		dist.release.changes.createSet( this , code , name , comments );
	}
	
	private void executeModifySet( String code , String codeNew , String nameNew , String commentsNew ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.findSet( code );
		if( set == null ) {
			exitInvalidArgs();
			return;
		}
		dist.release.changes.modifySet( this , set , codeNew , nameNew , commentsNew );
	}
	
	private void executeDropSet( String code , boolean descope ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.findSet( code );
		if( set == null ) {
			exitInvalidArgs();
			return;
		}
		dist.release.changes.dropSet( this , set , descope );
	}
	
}
