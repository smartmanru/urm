package org.urm.action.release;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionProductScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.ReleaseTicket;
import org.urm.engine.dist.ReleaseTicketSet;
import org.urm.engine.dist.ReleaseTicketSetTarget;
import org.urm.meta.Types;
import org.urm.meta.Types.VarTICKETSETTARGETTYPE;
import org.urm.meta.Types.VarTICKETTYPE;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;

public class ActionTickets extends ActionBase {

	String RELEASELABEL;
	public Dist dist;
	public Dist distNew;
	public String method;
	String[] args;
	
	public static String METHOD_CREATESET = "createset";
	public static String METHOD_MODIFYSET = "modifyset";
	public static String METHOD_DROPSET = "dropset";
	public static String METHOD_ACCEPTSET = "acceptset";
	public static String METHOD_CREATETICKET = "createticket";
	public static String METHOD_MODIFYTICKET = "modifyticket";
	public static String METHOD_MOVETICKET = "moveticket";
	public static String METHOD_COPYTICKET = "copyticket";
	public static String METHOD_DELETETICKET = "deleteticket";
	public static String METHOD_SETTICKETDEVDONE = "setdevdone";
	public static String METHOD_SETTICKETQADONE = "setqadone";
	public static String METHOD_CREATETARGET = "createtarget";
	public static String METHOD_DROPTARGET = "droptarget";

	public static String OPTION_DESCOPE = "descope";
	
	public static String TARGET_SET = "set";
	public static String TARGET_PROJECT = "project";
	public static String TARGET_PROJECTNOITEMS = "projectnoitems";
	public static String TARGET_PROJECTITEMS = "projectitems";
	public static String TARGET_DELIVERY = "delivery";
	public static String TARGET_DELIVERYITEMS = "deliveryitems";
	public static String TARGET_DELIVERYBINARY = "binary";
	public static String TARGET_DELIVERYCONF = "conf";
	public static String TARGET_DELIVERYSCHEMA = "schema";
	
	public ActionTickets( ActionBase action , String stream , Dist dist , String method , String[] args ) {
		super( action , stream , "change tickets release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.method = method;
		this.args = args;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.openForDataChange( this );
		
		SCOPESTATE res = SCOPESTATE.RunSuccess;
		try {
			executeCommand();
			dist.saveReleaseXml( this );
			if( distNew != null )
				distNew.saveReleaseXml( this );
		}
		catch( Throwable e ) {
			super.handle( "tickets command" , e );
			res = SCOPESTATE.RunFail;
		}

		dist.closeDataChange( this );
		if( distNew != null )
			distNew.closeDataChange( this );
		return( res );
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
			if( args.length < 3 || args.length > 3 ) {
				exitInvalidArgs();
				return;
			}
			
			String code = args[0];
			
			String tickets = args[1];
			String[] ticketList = null;
			if( tickets.equals( "all" ) )
				ticketList = null;
			else
			if( tickets.equals( "none" ) )
				ticketList = new String[0];
			else
				ticketList = Common.split( tickets , "," );
			
			String targets = args[2];
			String[] targetList = null;
			if( targets.equals( "all" ) )
				targetList = null;
			else
			if( targets.equals( "none" ) )
				targetList = new String[0];
			else
				targetList = Common.split( targets , "," );
			
			executeAcceptSet( code , ticketList , targetList );
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
		if( method.equals( METHOD_COPYTICKET ) ) {
			if( args.length < 4 || args.length > 4 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int ticketPos = Integer.parseInt( args[1] );
			String newRelease = args[2];
			String newSet = args[3];
			executeCopyTicket( codeSet , ticketPos , newRelease , newSet );
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
		if( method.equals( METHOD_DROPTARGET ) ) {
			if( args.length < 2 || args.length > 3 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int targetPos = Integer.parseInt( args[1] );
			String option = ( args.length > 2 )? args[2] : "";
			boolean descope = ( option.equals( OPTION_DESCOPE ) )? true : false;
			executeDropTarget( codeSet , targetPos , descope );
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
			else
			if( cmd.equals( TARGET_DELIVERY ) ) {
				if( args.length != 4 ) {
					exitInvalidArgs();
					return;
				}
				
				String delivery = args[2];
				String type = args[3];
				executeCreateDeliveryTarget( codeSet , delivery , type , null );
			}
			else
			if( cmd.equals( TARGET_DELIVERYITEMS ) ) {
				if( args.length <= 4 ) {
					exitInvalidArgs();
					return;
				}
				
				String delivery = args[2];
				String type = args[3];
				String[] items = Arrays.copyOfRange( args , 4 , args.length );
				executeCreateDeliveryTarget( codeSet , delivery , type , items );
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
	
	private void executeAcceptSet( String code , String[] tickets , String[] targets ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , code );
		
		// change release scope
		List<ReleaseTicketSetTarget> targetList = new LinkedList<ReleaseTicketSetTarget>();
		if( targets == null ) {
			for( ReleaseTicketSetTarget target : set.getTargets() ) {
				if( !target.isAccepted() )
					targetList.add( target );
			}
		}
		else {
			for( String targetPos : targets ) {
				ReleaseTicketSetTarget target = set.getTarget( this , Integer.parseInt( targetPos ) );
				if( !target.isAccepted() )
					targetList.add( target );
			}
		}

		ActionProductScopeMaker scopeNew = new ActionProductScopeMaker( this , dist.meta );
		ActionProductScopeMaker scopeDescope = new ActionProductScopeMaker( this , dist.meta );
		
		// add to scope
		for( ReleaseTicketSetTarget target : targetList ) {
			if( target.isDescoped() )
				executeAcceptTargetScope( target , scopeDescope );
			else
				executeAcceptTargetScope( target , scopeNew );
		}

		// execute change scope
		ActionScope scopeAdd = new ActionScope( this , dist.meta );
		scopeAdd.createMinus( this , scopeNew.getScope() , scopeDescope.getScope() );
		ActionScope scopeRemove = new ActionScope( this , dist.meta );
		scopeRemove.createMinus( this , scopeDescope.getScope() , scopeNew.getScope() );
		
		if( !scopeAdd.isEmpty() ) {
			ActionBase runAction = new ActionAddScope( this , null , dist );
			if( !runAction.runAll( scopeAdd , null , SecurityAction.ACTION_RELEASE , false ) )
				super.fail1( _Error.CannotExtendScope1 , "Cannot extend scope of release=" + dist.RELEASEDIR , dist.RELEASEDIR );
		}
		
		if( !scopeRemove.isEmpty() ) {
			ActionBase runAction = new ActionDescope( this , null , dist );
			if( !runAction.runAll( scopeRemove , null , SecurityAction.ACTION_RELEASE , false ) )
				super.fail1( _Error.CannotReduceScope1 , "Cannot extend scope of release=" + dist.RELEASEDIR , dist.RELEASEDIR );
		}
		
		// accept targets
		for( ReleaseTicketSetTarget target : targetList )
			target.accept( this );

		// accept set and tickets
		set.activate( this );
		if( tickets == null ) {
			for( ReleaseTicket ticket : set.getTickets() ) {
				if( !ticket.isAccepted() )
					ticket.accept( this );
			}
		}
		else {
			for( String ticketPos : tickets ) {
				ReleaseTicket ticket = set.getTicket( this , Integer.parseInt( ticketPos ) );
				if( !ticket.isAccepted() )
					ticket.accept( this );
			}
		}
	}
	
	private void executeAcceptTargetScope( ReleaseTicketSetTarget target , ActionProductScopeMaker maker ) throws Exception {
		if( target.isProjectSet() ) {
			maker.addScopeProductSet( target.ITEM , new String[] { "all" } );
		}
		else
		if( target.isProject() ) {
			MetaSource sources = dist.meta.getSources( this );
			MetaSourceProject project = sources.getProject( this , target.ITEM );
			maker.addScopeProductSet( project.set.NAME , new String[] { target.ITEM } );
		}
		else
		if( target.isBinary() ) {
			maker.addScopeProductDistItems( new String[] { target.ITEM } );
		}
		else
		if( target.isConfiguration() ) {
			maker.addScopeProductConfItems( new String[] { target.ITEM } );
		}
		else
		if( target.isDatabase() ) {
			String delivery = target.getDatabaseDelivery();
			String schema = target.getDatabaseSchema();
			maker.addScopeProductDatabaseDeliverySchemes( delivery , new String[] { schema } );
		}
		else
		if( target.isDelivery() ) {
			MetaDistr distr = dist.meta.getDistr( this );
			MetaDistrDelivery delivery = distr.findDelivery( target.ITEM );
			if( target.isDeliveryBinaries() ) {
				maker.addScopeProductDistItems( delivery.getBinaryItemNames() );
			}
			else
			if( target.isDeliveryConfs() ) {
				maker.addScopeProductConfItems( delivery.getConfItemNames() );
			}
			else
			if( target.isDeliveryDatabase() ) {
				maker.addScopeProductDatabaseDeliverySchemes( delivery.NAME , delivery.getDatabaseSchemaNames() );
			}
		}
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
	
	private void executeDropTarget( String setCode , int targetPos , boolean descope ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		set.dropTarget( this , targetPos , descope );
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
	
	private void executeCopyTicket( String setCode , int ticketPos , String newRelease , String newSetCode ) throws Exception {
		if( newRelease.equals( dist.RELEASEDIR ) )
			return;
		
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		ReleaseTicket ticket = set.getTicket( this , ticketPos );
		
		DistRepository repo = dist.meta.getDistRepository();
		distNew = repo.getDistByLabel( this , newRelease );
		distNew.openForDataChange( this );
		
		ReleaseTicketSet setNew = distNew.release.changes.getSet( this , newSetCode );
		set.copyTicket( this , ticket , setNew );
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
	
	private void executeCreateDeliveryTarget( String setCode , String deliveryName , String type , String[] items ) throws Exception {
		ReleaseTicketSet set = dist.release.changes.getSet( this , setCode );
		MetaDistr distr = dist.meta.getDistr( this );
		MetaDistrDelivery delivery = distr.getDelivery( this , deliveryName );
		if( type.equals( TARGET_DELIVERYBINARY ) )
			set.createTarget( this , delivery , VarTICKETSETTARGETTYPE.DISTITEM , items );
		else
		if( type.equals( TARGET_DELIVERYCONF ) )
			set.createTarget( this , delivery , VarTICKETSETTARGETTYPE.CONFITEM , items );
		else
		if( type.equals( TARGET_DELIVERYSCHEMA ) )
			set.createTarget( this , delivery , VarTICKETSETTARGETTYPE.SCHEMA , items );
	}
	
}
