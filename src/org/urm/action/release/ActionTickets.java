package org.urm.action.release;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionProductScopeMaker;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.core.DBEnums.*;
import org.urm.db.release.DBReleaseChanges;
import org.urm.db.release.DBReleaseTicketTarget;
import org.urm.engine.AuthService;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseTicket;
import org.urm.meta.release.ReleaseTicketSet;
import org.urm.meta.release.ReleaseTicketTarget;

public class ActionTickets extends ActionBase {

	String RELEASELABEL;
	public Meta meta;
	public Release release;
	public Release releaseNew;
	public String cmd;
	String[] args;
	
	EngineMethod method;
	
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
	public static String TARGET_DELIVERYDOC = "doc";
	
	public ActionTickets( ActionBase action , String stream , Release release , String cmd , String[] args ) {
		super( action , stream , "change tickets release=" + release.RELEASEVER );
		this.release = release;
		this.cmd = cmd;
		this.args = args;
		this.meta = release.getMeta();
		
		method = super.getMethod();
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		SCOPESTATE res = SCOPESTATE.RunSuccess;
		try {
			executeCommand( state );
		}
		catch( Throwable e ) {
			super.handle( "tickets command" , e );
			res = SCOPESTATE.RunFail;
		}

		return( res );
	}

	private void executeCommand( ScopeState state ) throws Exception {
		if( cmd.equals( METHOD_CREATESET ) ) {
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
		if( cmd.equals( METHOD_MODIFYSET ) ) {
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
		if( cmd.equals( METHOD_DROPSET ) ) {
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
		if( cmd.equals( METHOD_ACCEPTSET ) ) {
			if( args.length < 3 || args.length > 3 ) {
				exitInvalidArgs();
				return;
			}
			
			String code = args[0];
			
			String tickets = args[1];
			String[] ticketList = null;
			boolean allTickets = false;
			if( tickets.equals( "all" ) ) {
				ticketList = null;
				allTickets = true;
			}
			else
			if( tickets.equals( "none" ) )
				ticketList = new String[0];
			else
				ticketList = Common.split( tickets , "," );
			
			String targets = args[2];
			String[] targetList = null;
			boolean allTargets = false;
			if( targets.equals( "all" ) ) {
				targetList = null;
				allTargets = true;
			}
			else
			if( targets.equals( "none" ) )
				targetList = new String[0];
			else
				targetList = Common.split( targets , "," );
			
			executeAcceptSet( state , code , allTickets , ticketList , allTargets , targetList );
		}
		else
		if( cmd.equals( METHOD_CREATETICKET ) ) {
			if( args.length < 6 || args.length > 8 ) {
				exitInvalidArgs();
				return;
			}
			
			String setName = args[0];
			DBEnumTicketType type = DBEnumTicketType.getValue( args[1] , true );
			String code = args[2];
			String name = args[3];
			String link = ( args.length > 4 )? args[4] : "";
			String comments = ( args.length > 5 )? args[5] : "";
			String owner = ( args.length > 6 )? args[6] : "";
			AuthService auth = engine.getAuth();
			Integer ownerId = auth.getUserId( owner );
			boolean devdone = ( args.length > 7 )? Common.getBooleanValue( args[7] ) : false;
			executeCreateTicket( setName , type , code , name , link , comments , ownerId , devdone );
		}
		else
		if( cmd.equals( METHOD_MODIFYTICKET ) ) {
			if( args.length < 7 || args.length > 9 ) {
				exitInvalidArgs();
				return;
			}
			
			String setName = args[0];
			int pos = Integer.parseInt( args[1] );
			DBEnumTicketType type = DBEnumTicketType.getValue( args[2] , true );
			String code = args[3];
			String name = args[4];
			String link = ( args.length > 5 )? args[5] : "";
			String comments = ( args.length > 6 )? args[6] : "";
			String owner = ( args.length > 7 )? args[7] : "";
			AuthService auth = engine.getAuth();
			Integer ownerId = auth.getUserId( owner );
			boolean devdone = ( args.length > 8 )? Common.getBooleanValue( args[8] ) : false;
			executeModifyTicket( setName , pos , type , code , name , link , comments , ownerId , devdone );
		}
		else
		if( cmd.equals( METHOD_MOVETICKET ) ) {
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
		if( cmd.equals( METHOD_COPYTICKET ) ) {
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
		if( cmd.equals( METHOD_DELETETICKET ) ) {
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
		if( cmd.equals( METHOD_SETTICKETDEVDONE ) ) {
			if( args.length < 2 || args.length > 2 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int ticketPos = Integer.parseInt( args[1] );
			executeSetTicketDone( codeSet , ticketPos );
		}
		else
		if( cmd.equals( METHOD_SETTICKETQADONE ) ) {
			if( args.length < 2 || args.length > 2 ) {
				exitInvalidArgs();
				return;
			}
			
			String codeSet = args[0];
			int ticketPos = Integer.parseInt( args[1] );
			executeSetTicketVerified( codeSet , ticketPos );
		}
		else
		if( cmd.equals( METHOD_DROPTARGET ) ) {
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
		if( cmd.equals( METHOD_CREATETARGET ) ) {
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
				executeCreateSetTarget( codeSet , projectSet );
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
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			DBReleaseChanges.createSet( method , this , releaseUpdated , changes , code , name , comments );
		}
	}
	
	private void executeModifySet( String code , String codeNew , String nameNew , String commentsNew ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( code );
			DBReleaseChanges.modifySet( method , this , releaseUpdated , changes , set , codeNew , nameNew , commentsNew );
		}
	}
	
	private void executeDropSet( String code , boolean descope ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( code );
			DBReleaseChanges.dropSet( method , this , releaseUpdated , changes , set , descope );
		}
	}

	private void executeCreateTicket( String setCode , DBEnumTicketType type , String code , String name , String link , String comments , Integer owner , boolean devdone ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			DBReleaseChanges.createTicket( method , this , releaseUpdated , changes , set , type , code , name , link , comments , owner , devdone );
		}
	}
	
	private void executeModifyTicket( String setCode , int ticketPos , DBEnumTicketType type , String code , String name , String link , String comments , Integer owner , boolean devdone ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			ReleaseTicket ticket = set.getTicketByPos( ticketPos );
			DBReleaseChanges.modifyTicket( method , this , releaseUpdated , changes , set , ticket , type , code , name , link , comments , owner , devdone );
		}
	}
	
	private void executeDropTicket( String setCode , int ticketPos , boolean descope ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			ReleaseTicket ticket = set.getTicketByPos( ticketPos );
			DBReleaseChanges.dropTicket( method , this , releaseUpdated , changes , set , ticket , descope );
		}
	}
	
	private void executeSetTicketDone( String setCode , int ticketPos ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			ReleaseTicket ticket = set.getTicketByPos( ticketPos );
			DBReleaseChanges.setDevDone( method , this , releaseUpdated , changes , set , ticket , super.getUserId() );
		}
	}
	
	private void executeSetTicketVerified( String setCode , int ticketPos ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			ReleaseTicket ticket = set.getTicketByPos( ticketPos );
			DBReleaseChanges.setVerified( method , this , releaseUpdated , changes , set , ticket , super.getUserId() );
		}
	}
	
	private void executeCopyTicket( String setCode , int ticketPos , String newRelease , String newSetCode ) throws Exception {
		EngineMethod method = super.method;
		
		if( newRelease.equals( release.RELEASEVER ) )
			Common.exitUnexpected();
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			releaseNew = repoUpdated.findReleaseByLabel( this , newRelease );
			Release releaseNewUpdated = method.changeRelease( repoUpdated , releaseNew );
			
			ReleaseChanges changes = release.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			ReleaseTicket ticket = set.getTicketByPos( ticketPos );
			ReleaseChanges changesNew = releaseNewUpdated.getChanges();
			ReleaseTicketSet setNew = changesNew.getSet( newSetCode );
			DBReleaseChanges.copyTicket( method , this , releaseNewUpdated , changesNew , setNew , ticket );
		}
	}
	
	private void executeMoveTicket( String setCode , int ticketPos , String newSetCode ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			ReleaseTicket ticket = set.getTicketByPos( ticketPos );
			ReleaseTicketSet setNew = changes.getSet( newSetCode );
			DBReleaseChanges.moveTicket( method , this , releaseUpdated , changes , set , ticket , setNew );
		}
	}
	
	private void executeCreateSetTarget( String setCode , String element ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );

			// create
			Meta meta = releaseUpdated.getMeta();
			MetaSources sources = meta.getSources();
			MetaSourceProjectSet projectSet = sources.getProjectSet( element );
			DBReleaseTicketTarget.createProjectSetTarget( method , this , releaseUpdated , changes , set , projectSet );
		}
	}
	
	private void executeCreateProjectTarget( String setCode , String element , String[] items ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );

			// create
			Meta meta = releaseUpdated.getMeta();
			MetaSources sources = meta.getSources();
			MetaSourceProject project = sources.getProject( element );
			if( items == null )
				DBReleaseTicketTarget.createProjectTarget( method , this , releaseUpdated , changes , set , project , true );
			else {
				DBReleaseTicketTarget.createProjectTarget( method , this , releaseUpdated , changes , set , project , false );
				for( String item : items ) {
					MetaSourceProjectItem projectItem = project.getItem( item );
					DBReleaseTicketTarget.createProjectItemTarget( method , this , releaseUpdated , changes , set , projectItem );
				}
			}
		}
	}
	
	private void executeCreateDeliveryTarget( String setCode , String deliveryName , String type , String[] items ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			MetaDistr distr = meta.getDistr();
			MetaDistrDelivery delivery = distr.getDelivery( deliveryName );
			
			DBEnumDistTargetType category = null;
			if( type.equals( TARGET_DELIVERYBINARY ) )
				category = DBEnumDistTargetType.DELIVERYBINARIES;
			else
			if( type.equals( TARGET_DELIVERYCONF ) )
				category = DBEnumDistTargetType.DELIVERYCONFS;
			else
			if( type.equals( TARGET_DELIVERYSCHEMA ) )
				category = DBEnumDistTargetType.DELIVERYDATABASE;
			else
			if( type.equals( TARGET_DELIVERYDOC ) )
				category = DBEnumDistTargetType.DELIVERYDOC;
				
			if( items == null )
				DBReleaseTicketTarget.createDeliveryTarget( method , this , releaseUpdated , changes , set , delivery , category );
			else {
				for( String item : items ) {
					if( category == DBEnumDistTargetType.DELIVERYBINARIES ) {
						MetaDistrBinaryItem binary = delivery.getBinaryItem( item );
						DBReleaseTicketTarget.createDeliveryTargetItem( method , this , releaseUpdated , changes , set , delivery , binary );
					}
					else
					if( category == DBEnumDistTargetType.DELIVERYCONFS ) {
						MetaDistrConfItem conf = delivery.getConfItem( item );
						DBReleaseTicketTarget.createDeliveryTargetItem( method , this , releaseUpdated , changes , set , delivery , conf );
					}
					else
					if( category == DBEnumDistTargetType.DELIVERYDATABASE ) {
						MetaDatabase db = meta.getDatabase();
						MetaDatabaseSchema schema = db.getSchema( item );
						DBReleaseTicketTarget.createDeliveryTargetItem( method , this , releaseUpdated , changes , set , delivery , schema );
					}
					else
					if( category == DBEnumDistTargetType.DELIVERYDOC ) {
						MetaDocs docs = meta.getDocs();
						MetaProductDoc doc = docs.getDoc( item );
						DBReleaseTicketTarget.createDeliveryTargetItem( method , this , releaseUpdated , changes , set , delivery , doc );
					}
				}
			}
		}
	}
	
	private void executeDropTarget( String setCode , int targetPos , boolean descope ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
			ReleaseTicketTarget target = set.getTargetByPos( targetPos );
				
			if( descope )
				DBReleaseTicketTarget.descopeTarget( method , this , releaseUpdated , changes , set , target );
			else
				DBReleaseTicketTarget.deleteTarget( method , this , releaseUpdated , changes , set , target );
		}
	}
	
	private void executeAcceptSet( ScopeState state , String setCode , boolean allTickets , String[] tickets , boolean allTargets , String[] targets ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseChanges changes = releaseUpdated.getChanges();
			ReleaseTicketSet set = changes.getSet( setCode );
		
			executeAcceptSet( state , method , releaseUpdated , changes , set , allTickets , tickets , allTargets , targets );
		}
	}
	
	private void executeAcceptSet( ScopeState state , EngineMethod method , Release release , ReleaseChanges changes , ReleaseTicketSet set , boolean allTickets , String[] tickets , boolean allTargets , String[] targets ) throws Exception {
		// change release scope
		List<ReleaseTicketTarget> targetList = new LinkedList<ReleaseTicketTarget>();
		if( allTargets ) {
			for( ReleaseTicketTarget target : set.getTargets() ) {
				if( !target.isAccepted() )
					targetList.add( target );
			}
		}
		else {
			for( String targetPos : targets ) {
				ReleaseTicketTarget target = set.getTargetByPos( Integer.parseInt( targetPos ) );
				if( !target.isAccepted() )
					targetList.add( target );
			}
		}

		Meta meta = release.getMeta();
		ActionProductScopeMaker scopeNew = new ActionProductScopeMaker( this , meta );
		ActionProductScopeMaker scopeDescope = new ActionProductScopeMaker( this , meta );
		
		// add to scope
		for( ReleaseTicketTarget target : targetList ) {
			if( target.isDescoped() )
				executeAcceptTargetScope( target , scopeDescope );
			else
				executeAcceptTargetScope( target , scopeNew );
		}

		// execute change scope
		ActionScope scopeAdd = new ActionScope( this , meta );
		scopeAdd.createMinus( this , scopeNew.getScope() , scopeDescope.getScope() );
		ActionScope scopeRemove = new ActionScope( this , meta );
		scopeRemove.createMinus( this , scopeDescope.getScope() , scopeNew.getScope() );
		
		if( !scopeAdd.isEmpty() ) {
			ActionBase runAction = new ActionAddScope( this , null , release );
			if( !runAction.runAll( state , scopeAdd , null , SecurityAction.ACTION_RELEASE , false ) )
				super.fail1( _Error.CannotExtendScope1 , "Cannot extend scope of release=" + release.RELEASEVER , release.RELEASEVER );
		}
		
		if( !scopeRemove.isEmpty() ) {
			ActionBase runAction = new ActionDescope( this , null , release );
			if( !runAction.runAll( state , scopeRemove , null , SecurityAction.ACTION_RELEASE , false ) )
				super.fail1( _Error.CannotReduceScope1 , "Cannot extend scope of release=" + release.RELEASEVER , release.RELEASEVER );
		}
		
		// accept targets
		for( ReleaseTicketTarget target : targetList )
			DBReleaseTicketTarget.acceptTarget( method , this , release , changes , set , target );

		// accept set and tickets
		DBReleaseChanges.activateTicketSet( method , this , release , changes , set );
		if( allTickets ) {
			for( ReleaseTicket ticket : set.getTickets() ) {
				if( !ticket.isAccepted() )
					DBReleaseChanges.acceptTicket( method , this , release , changes , set , ticket );
			}
		}
		else {
			for( String ticketPos : tickets ) {
				ReleaseTicket ticket = set.getTicketByPos( Integer.parseInt( ticketPos ) );
				if( !ticket.isAccepted() )
					ticket.accept();
			}
		}
	}
	
	private void executeAcceptTargetScope( ReleaseTicketTarget target , ActionProductScopeMaker maker ) throws Exception {
		if( target.isProjectSet() ) {
			MetaSourceProjectSet set = target.getProjectSet();
			maker.addScopeProductSet( set.NAME , new String[] { "all" } );
		}
		else
		if( target.isProject() ) {
			MetaSourceProject project = target.getProject();
			maker.addScopeProductSet( project.set.NAME , new String[] { project.NAME } );
		}
		else
		if( target.isBinary() ) {
			MetaDistrBinaryItem item = target.getBinaryItem();
			maker.addScopeProductDistItems( new String[] { item.NAME } );
		}
		else
		if( target.isConfiguration() ) {
			MetaDistrConfItem item = target.getConfItem();
			maker.addScopeProductConfItems( new String[] { item.NAME } );
		}
		else
		if( target.isDatabase() ) {
			MetaDistrDelivery delivery = target.getDelivery();
			MetaDatabaseSchema schema = target.getDatabaseSchema();
			maker.addScopeProductDeliveryDatabaseSchemes( delivery.NAME , new String[] { schema.NAME } );
		}
		else
		if( target.isDoc() ) {
			MetaDistrDelivery delivery = target.getDelivery();
			MetaProductDoc doc = target.getDoc();
			maker.addScopeProductDeliveryDocs( delivery.NAME , new String[] { doc.NAME } );
		}
		else
		if( target.isDelivery() ) {
			MetaDistrDelivery delivery = target.getDelivery();
			if( target.isDeliveryBinaries() ) {
				maker.addScopeProductDistItems( delivery.getBinaryItemNames() );
			}
			else
			if( target.isDeliveryConfs() ) {
				maker.addScopeProductConfItems( delivery.getConfItemNames() );
			}
			else
			if( target.isDeliveryDatabase() ) {
				maker.addScopeProductDeliveryDatabaseSchemes( delivery.NAME , delivery.getDatabaseSchemaNames() );
			}
			else
			if( target.isDeliveryDoc() ) {
				maker.addScopeProductDeliveryDocs( delivery.NAME , delivery.getDocNames() );
			}
		}
	}
	
}
