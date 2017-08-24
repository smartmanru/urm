package org.urm.action.release;

import java.util.Arrays;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTicket;
import org.urm.engine.dist.ReleaseTicketSet;
import org.urm.meta.Types;
import org.urm.meta.Types.VarTICKETTYPE;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;

public class ActionTickets extends ActionBase {

	String RELEASELABEL;
	public Dist dist;
	public String method;
	String[] args;
	
	public static String METHOD_CREATESET = "createset";
	public static String METHOD_MODIFYSET = "modifyset";
	public static String METHOD_DROPSET = "dropset";
	public static String METHOD_ACCEPTSET = "acceptset";
	public static String METHOD_CREATETICKET = "createticket";
	public static String METHOD_MODIFYTICKET = "modifyticket";
	public static String METHOD_MOVETICKET = "moveticket";
	public static String METHOD_DELETETICKET = "deleteticket";
	public static String METHOD_SETTICKETDEVDONE = "setdevdone";
	public static String METHOD_SETTICKETQADONE = "setqadone";
	public static String METHOD_CREATETARGET = "createtarget";

	public static String OPTION_DESCOPE = "descope";
	
	public static String TARGET_SET = "set";
	public static String TARGET_PROJECT = "project";
	public static String TARGET_PROJECTNOITEMS = "projectnoitems";
	public static String TARGET_PROJECTITEMS = "projectitems";
	
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
		else
		if( method.equals( METHOD_ACCEPTSET ) ) {
			if( args.length < 1 || args.length > 1 ) {
				exitInvalidArgs();
				return;
			}
			
			String code = args[0];
			executeAcceptSet( code );
		}
		else
		if( method.equals( METHOD_CREATETICKET ) ) {
			if( args.length < 6 || args.length > 8 ) {
				exitInvalidArgs();
				return;
			}
			
			String setName = args[0];
			VarTICKETTYPE type = Types.getTicketType( args[1] , true );
			String code = args[2];
			String name = args[3];
			String link = ( args.length > 4 )? args[4] : "";
			String comments = ( args.length > 5 )? args[5] : "";
			String owner = ( args.length > 6 )? args[6] : "";
			boolean devdone = ( args.length > 7 )? Common.getBooleanValue( args[7] ) : false;
			executeCreateTicket( setName , type , code , name , link , comments , owner , devdone );
		}
		else
		if( method.equals( METHOD_MODIFYTICKET ) ) {
			if( args.length < 7 || args.length > 9 ) {
				exitInvalidArgs();
				return;
			}
			
			String setName = args[0];
			int pos = Integer.parseInt( args[1] );
			VarTICKETTYPE type = Types.getTicketType( args[2] , true );
			String code = args[3];
			String name = args[4];
			String link = ( args.length > 5 )? args[5] : "";
			String comments = ( args.length > 6 )? args[6] : "";
			String owner = ( args.length > 7 )? args[7] : "";
			boolean devdone = ( args.length > 8 )? Common.getBooleanValue( args[8] ) : false;
			executeModifyTicket( setName , pos , type , code , name , link , comments , owner , devdone );
		}
		else
		if( method.equals( METHOD_MOVETICKET ) ) {
			if( args.length < 3 || args.length > 3 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int ticketPos = Integer.parseInt( args[1] );
			String newSet = args[2];
			executeMoveTicket( codeSet , ticketPos , newSet );
		}
		else
		if( method.equals( METHOD_DELETETICKET ) ) {
			if( args.length < 2 || args.length > 3 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int ticketPos = Integer.parseInt( args[1] );
			String option = ( args.length > 2 )? args[2] : "";
			boolean descope = ( option.equals( OPTION_DESCOPE ) )? true : false;
			executeDropTicket( codeSet , ticketPos , descope );
		}
		else
		if( method.equals( METHOD_SETTICKETDEVDONE ) ) {
			if( args.length < 2 || args.length > 2 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int ticketPos = Integer.parseInt( args[1] );
			executeSetTicketDone( codeSet , ticketPos );
		}
		else
		if( method.equals( METHOD_SETTICKETQADONE ) ) {
			if( args.length < 2 || args.length > 2 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int ticketPos = Integer.parseInt( args[1] );
			executeSetTicketVerified( codeSet , ticketPos );
		}
		else
		if( method.equals( METHOD_CREATETARGET ) ) {
			if( args.length < 2 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			String cmd = args[1];
			if( cmd.equals( TARGET_SET ) ) {
				if( args.length != 3 ) {
					exitInvalidArgs();
					return;
				}
				
				String projectSet = args[2];
				executeCreateSetTarget( codeSet , projectSet , null );
			}
			else
			if( cmd.equals( TARGET_PROJECT ) ) {
				if( args.length != 3 ) {
					exitInvalidArgs();
					return;
				}
				
				String project = args[2];
				executeCreateProjectTarget( codeSet , project , null );
			}
			else
			if( cmd.equals( TARGET_PROJECTNOITEMS ) ) {
				if( args.length != 3 ) {
					exitInvalidArgs();
					return;
				}
				
				String project = args[2];
				executeCreateProjectTarget( codeSet , project , new String[0] );
			}
			else
			if( cmd.equals( TARGET_PROJECTITEMS ) ) {
				if( args.length <= 3 ) {
					exitInvalidArgs();
					return;
				}
				
				String project = args[2];
				String[] items = Arrays.copyOfRange( args , 3 , args.length );
				executeCreateProjectTarget( codeSet , project , items );
			}
		}
	}

	private void exitInvalidArgs() throws Exception {
		super.fail0( _Error.InvalidSyntax0 , "Invalid command syntax, see help" );
		
	}

	private void executeCreateSet( String code , String name , String comments ) throws Exception {
		dist.release.changes.createSet( this , code , name , comments );
	}
	
	private void executeModifySet( String code , String codeNew , String nameNew , String commentsNew ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , code );
		dist.release.changes.modifySet( this , set , codeNew , nameNew , commentsNew );
	}
	
	private void executeDropSet( String code , boolean descope ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , code );
		dist.release.changes.dropSet( this , set , descope );
	}
	
	private void executeAcceptSet( String code ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , code );
		dist.release.changes.acceptSet( this , set );
	}
	
	private void executeCreateTicket( String setCode , VarTICKETTYPE type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		dist.release.changes.createTicket( this , set , type , code , name , link , comments , owner , devdone );
	}
	
	private void executeModifyTicket( String setCode , int POS , VarTICKETTYPE type , String code , String name , String link , String comments , String owner , boolean devdone ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		ReleaseTicket ticket = set.getTicket( this , POS );
		set.modifyTicket( this , ticket , type , code , name , link , comments , owner , devdone );
	}
	
	private void executeDropTicket( String setCode , int ticketPos , boolean descope ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		set.dropTicket( this , ticketPos , descope );
	}
	
	private void executeSetTicketDone( String setCode , int ticketPos ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		ReleaseTicket ticket = set.getTicket( this , ticketPos );
		set.setDevDone( this , ticket );
	}
	
	private void executeSetTicketVerified( String setCode , int ticketPos ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		ReleaseTicket ticket = set.getTicket( this , ticketPos );
		set.setTicketVerified( this , ticket );
	}
	
	private void executeMoveTicket( String setCode , int ticketPos , String newSetCode ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		ReleaseTicketSet setNew = dist.release.changes.getSet( this , newSetCode );
		if( set == setNew )
			return;
		ReleaseTicket ticket = set.getTicket( this , ticketPos );
		set.moveTicket( this , ticket , setNew );
	}
	
	private void executeCreateSetTarget( String setCode , String element , String[] items ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		MetaSource sources = dist.meta.getSources( this );
		MetaSourceProjectSet projectSet = sources.getProjectSet( this , element );
		set.createTarget( this , projectSet );
	}
	
	private void executeCreateProjectTarget( String setCode , String element , String[] items ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		MetaSource sources = dist.meta.getSources( this );
		MetaSourceProject project = sources.getProject( this , element );
		set.createTarget( this , project , items );
	}
	
}
