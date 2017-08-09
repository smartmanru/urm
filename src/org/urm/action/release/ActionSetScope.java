package org.urm.action.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseDistSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.meta.Types;
import org.urm.meta.Types.VarCATEGORY;
import org.urm.meta.Types.VarDEPLOYITEMTYPE;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;

public class ActionSetScope extends ActionBase {

	public Dist dist;
	boolean sourcePath;
	String[] pathItems;
	
	public ActionSetScope( ActionBase action , String stream , Dist dist , boolean sourcePath , String[] pathItems ) {
		super( action , stream , "Set scope, release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.sourcePath = sourcePath;
		this.pathItems = pathItems;
	}

	@Override 
	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		if( sourcePath ) {
			if( !executeBySource() )
				return( SCOPESTATE.RunFail );
		}
		else {
			if( !executeByDelivery() ) 
				return( SCOPESTATE.RunFail );
		}
		return( SCOPESTATE.RunSuccess );
	}

	private boolean executeBySource() throws Exception {
		MetaSource source = dist.meta.getSources( this );
		
		// add new
		dist.reloadCheckOpenedForDataChange( this );
		if( pathItems.length == 1 ) {
			if( pathItems[0].equals( "all" ) ) {
				for( MetaSourceProjectSet set : source.getSets() ) {
					if( !dist.addAllSource( this , set ) )
						return( false );
				}
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				dist.descopeAllProjects( this );
				return( true );
			}
		}
		
		Map<String,String> check = new HashMap<String,String>();
		for( String path : pathItems ) {
			check.put( path , "all" );
			String[] els = Common.split( path , "/" );
			if( els.length == 1 ) {
				MetaSourceProjectSet set = source.getProjectSet( this , els[0] );
				if( !dist.addAllSource( this , set ) )
					return( false );
				continue;
			}
			if( els.length == 2 ) {
				MetaSourceProjectSet set = source.getProjectSet( this , els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( this , els[1] );
				if( !dist.addProjectAllItems( this , project ) )
					return( false );
				continue;
			}
			if( els.length == 3 ) {
				MetaSourceProjectSet set = source.getProjectSet( this , els[0] );
				check.put( els[0] , "set" );
				MetaSourceProject project = set.getProject( this , els[1] );
				check.put( Common.concat( els[0] , els[1] , "/" ) , "project" );
				MetaSourceProjectItem item = project.getItem( this , els[2] );
				if( !dist.addProjectItem( this , project , item ) )
					return( false );
				continue;
			}
		}
		
		// descope missing
		for( ReleaseDistSet set : dist.release.getSourceSets() ) {
			String checkSet = check.get( set.set.NAME );
			if( checkSet == null ) {
				dist.descopeSet( this , set );
				continue;
			}

			if( checkSet.equals( "all" ) )
				continue;
			
			for( ReleaseTarget target : set.getTargets() ) {
				String checkProject = check.get( Common.concat( set.set.NAME , target.sourceProject.NAME , "/" ) );
				if( checkProject == null ) {
					dist.descopeTarget( this , target );
					continue;
				}
				
				if( checkProject.equals( "all" ) )
					continue;
				
				for( ReleaseTargetItem item : target.getItems() ) {
					String checkItem = check.get( Common.concat( Common.concat( set.set.NAME , target.sourceProject.NAME , "/" ) , item.sourceItem.ITEMNAME , "/" ) );
					if( checkItem == null )
						dist.descopeTargetItems( this , new ReleaseTargetItem[] { item } );
				}
			}
		}
		return( true );
	}

	private boolean executeByDelivery() throws Exception {
		MetaDistr distr = dist.meta.getDistr( this );
		MetaSource source = dist.meta.getSources( this );
		
		// add new 
		dist.reloadCheckOpenedForDataChange( this );
		if( pathItems.length == 1 ) {
			if( pathItems[0].equals( "all" ) ) {
				for( VarCATEGORY category : new VarCATEGORY[] { VarCATEGORY.MANUAL , VarCATEGORY.DERIVED , VarCATEGORY.CONFIG , VarCATEGORY.DB } ) {
					if( !dist.addAllCategory( this , category ) )
						return( false );
				}
				
				for( MetaSourceProjectSet set : source.getSets() ) {
					if( !dist.addAllSource( this , set ) )
						return( false );
				}
				return( true );
			}
			if( pathItems[0].equals( "none" ) ) {
				dist.descopeAll( this );
				return( true );
			}
		}
			
		Map<String,String> check = new HashMap<String,String>();
		for( String path : pathItems ) {
			check.put( path , "all" );
			String[] els = Common.split( path , "/" );
			if( els.length == 1 ) {
				MetaDistrDelivery delivery = distr.getDelivery( this , els[0] );
				if( !addAllDelivery( check , delivery ) )
					return( false );
				continue;
			}
			if( els.length == 2 ) {
				MetaDistrDelivery delivery = distr.getDelivery( this , els[0] );
				check.put( els[0] , "delivery" );
				
				VarDEPLOYITEMTYPE type = Types.getDeployItemType( els[1] , true );
				if( type == VarDEPLOYITEMTYPE.SCHEMA ) {
					if( !addDeliveryAllSchemes( check , delivery ) )
						return( false );
				}
			}
			if( els.length == 3 ) {
				MetaDistrDelivery delivery = distr.getDelivery( this , els[0] );
				check.put( els[0] , "delivery" );
				VarDEPLOYITEMTYPE type = Types.getDeployItemType( els[1] , true );
				
				if( type == VarDEPLOYITEMTYPE.BINARY ) {
					MetaDistrBinaryItem item = delivery.getBinaryItem( this , els[2] );
					if( !dist.addBinaryItem( this , item ) )
						return( false );
				}
				else
				if( type == VarDEPLOYITEMTYPE.CONF ) {
					MetaDistrConfItem item = delivery.getConfItem( this , els[2] );
					if( !dist.addConfItem( this , item ) )
						return( false );
				}
				else
				if( type == VarDEPLOYITEMTYPE.SCHEMA ) {
					MetaDatabaseSchema schema = delivery.getSchema( this , els[2] );
					if( !dist.addDatabaseDeliverySchema( this , delivery , schema ) )
						return( false );
				}
			}
		}

		// descope missing
		for( ReleaseDelivery delivery : dist.release.getDeliveries() ) {
			for( ReleaseTargetItem item : delivery.getProjectItems() ) {
				String checkItem = check.get( Common.getList( new String[] { delivery.distDelivery.NAME , VarDEPLOYITEMTYPE.BINARY.toString() , item.distItem.KEY } , "/" ) );
				if( checkItem == null ) {
					dist.descopeTargetItems( this , new ReleaseTargetItem[] { item } );
					continue;
				}
			}
			for( ReleaseTarget item : delivery.getManualItems() ) {
				String checkItem = check.get( Common.getList( new String[] { delivery.distDelivery.NAME , VarDEPLOYITEMTYPE.BINARY.toString() , item.distManualItem.KEY } , "/" ) );
				if( checkItem == null ) {
					dist.descopeTarget( this , item );
					continue;
				}
			}
			for( ReleaseTarget item : delivery.getDerivedItems() ) {
				String checkItem = check.get( Common.getList( new String[] { delivery.distDelivery.NAME , VarDEPLOYITEMTYPE.BINARY.toString() , item.distDerivedItem.KEY } , "/" ) );
				if( checkItem == null ) {
					dist.descopeTarget( this , item );
					continue;
				}
			}
			for( ReleaseTarget item : delivery.getConfItems() ) {
				String checkItem = check.get( Common.getList( new String[] { delivery.distDelivery.NAME , VarDEPLOYITEMTYPE.CONF.toString() , item.distConfItem.KEY } , "/" ) );
				if( checkItem == null ) {
					dist.descopeTarget( this , item );
					continue;
				}
			}
			
			for( ReleaseTargetItem item : delivery.getDatabaseItems() ) {
				String checkItem = check.get( Common.getList( new String[] { delivery.distDelivery.NAME , VarDEPLOYITEMTYPE.SCHEMA.toString() , item.schema.SCHEMA } , "/" ) );
				if( checkItem == null ) {
					dist.descopeTargetItems( this , new ReleaseTargetItem[] { item } );
					continue;
				}
			}
		}
		return( true );
	}

	private boolean addAllDelivery( Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDistrBinaryItem binaryItem : delivery.getBinaryItems() ) {
			if( !dist.addBinaryItem( this , binaryItem ) )
				return( false );
			check.put( Common.getList( new String[] { delivery.NAME , VarDEPLOYITEMTYPE.BINARY.toString() , binaryItem.KEY } , "/" ) , "binary" );
		}
		for( MetaDistrConfItem confItem : delivery.getConfItems() ) {
			if( !dist.addConfItem( this , confItem ) )
				return( false );
			check.put( Common.getList( new String[] { delivery.NAME , VarDEPLOYITEMTYPE.CONF.toString() , confItem.KEY } , "/" ) , "conf" );
		}
		if( delivery.hasDatabaseItems() ) {
			if( !addDeliveryAllSchemes( check , delivery ) )
				return( false );
		}
		return( true );
	}
	
	private boolean addDeliveryAllSchemes( Map<String,String> check , MetaDistrDelivery delivery ) throws Exception {
		if( !dist.addDatabaseDeliveryAllSchemes( this , delivery ) )
			return( false );
		
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() )
			check.put( Common.getList( new String[] { delivery.NAME , VarDEPLOYITEMTYPE.SCHEMA.toString() , schema.SCHEMA } , "/" ) , "database" );
		return( true );
	}
	
}
