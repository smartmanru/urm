package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.action.ActionBase;
import org.urm.engine.meta.Meta.VarCONTENTTYPE;
import org.urm.engine.meta.Meta.VarDEPLOYTYPE;

public class MetaEnvServerLocation {

	class BinaryDeploymentPair {
		public MetaEnvServerDeployment deployment;
		public MetaDistrBinaryItem item;
		
		public BinaryDeploymentPair( MetaEnvServerDeployment deployment , MetaDistrBinaryItem item ) {
			this.deployment = deployment;
			this.item = item;
		}
	}

	class ConfDeploymentPair {
		public MetaEnvServerDeployment deployment;
		public MetaDistrConfItem item;
		
		public ConfDeploymentPair( MetaEnvServerDeployment deployment , MetaDistrConfItem item ) {
			this.deployment = deployment;
			this.item = item;
		}
	}
	
	Meta meta;
	MetaEnvServer server;
	
	public VarDEPLOYTYPE DEPLOYTYPE;
	public String DEPLOYPATH;
	
	public Map<String,String> deployNameMap;
	private List<BinaryDeploymentPair> binaryItems;
	private List<ConfDeploymentPair> confItems;
	private List<MetaEnvServerDeployment> deployments;
	
	public MetaEnvServerLocation( Meta meta , MetaEnvServer server , VarDEPLOYTYPE DEPLOYTYPE , String DEPLOYPATH ) {
		this.meta = meta;
		this.server = server;
		this.DEPLOYTYPE = DEPLOYTYPE;
		this.DEPLOYPATH = DEPLOYPATH;
		
		binaryItems = new LinkedList<BinaryDeploymentPair>();
		deployNameMap = new HashMap<String,String>();
		confItems = new LinkedList<ConfDeploymentPair>();
		deployments = new LinkedList<MetaEnvServerDeployment>(); 
	}
	
	public void addBinaryItem( ActionBase action , MetaEnvServerDeployment deployment , MetaDistrBinaryItem binaryItem , String deployName ) throws Exception {
		if( deployName.isEmpty() )
			deployName = binaryItem.DEPLOYBASENAME;

		BinaryDeploymentPair pair = new BinaryDeploymentPair( deployment , binaryItem );
		binaryItems.add( pair );
		deployNameMap.put( binaryItem.KEY , deployName );
		
		if( !deployments.contains( deployment ) )
			deployments.add( deployment );
	}

	public void addConfItem( ActionBase action , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem ) throws Exception {
		ConfDeploymentPair pair = new ConfDeploymentPair( deployment , confItem );
		confItems.add( pair );
		
		if( !deployments.contains( deployment ) )
			deployments.add( deployment );
	}

	public boolean hasBinaryItems( ActionBase action ) throws Exception {
		if( binaryItems.isEmpty() )
			return( false );
		return( true );
	}

	public boolean hasConfItems( ActionBase action ) throws Exception {
		if( confItems.isEmpty() )
			return( false );
		return( true );
	}

	public String getDeployName( ActionBase action , String key ) throws Exception {
		String itemName = deployNameMap.get( key );
		if( itemName == null || itemName.isEmpty() )
			action.exitUnexpectedState();
		return( itemName );
	}

	public VarCONTENTTYPE getContentType( ActionBase action , boolean binary ) throws Exception {
		VarCONTENTTYPE contentType;
		if( DEPLOYTYPE == VarDEPLOYTYPE.HOT )
			contentType = ( binary )? VarCONTENTTYPE.BINARYHOTDEPLOY : VarCONTENTTYPE.CONFHOTDEPLOY;
		else
		if( DEPLOYTYPE == VarDEPLOYTYPE.COPYONLY )
			contentType = ( binary )? VarCONTENTTYPE.BINARYCOPYONLY : VarCONTENTTYPE.CONFCOPYONLY;
		else
			contentType = ( binary )? VarCONTENTTYPE.BINARYCOLDDEPLOY : VarCONTENTTYPE.CONFCOLDDEPLOY;
		return( contentType );
	}

	public String[] getNodeBinaryItems( ActionBase action , MetaEnvServerNode node ) throws Exception {
		Map<String, MetaDistrBinaryItem> items = new HashMap<String, MetaDistrBinaryItem>();
		for( BinaryDeploymentPair pair : binaryItems ) {
			if( node == null || checkNodeDeployment( action , node , pair.deployment ) ) {
				if( !items.containsKey( pair.item.KEY ) )
					items.put( pair.item.KEY , pair.item );
			}
		}
		return( Common.getSortedKeys( items ) );
	}

	public String[] getNodeConfItems( ActionBase action , MetaEnvServerNode node ) throws Exception {
		Map<String, MetaDistrConfItem> items = new HashMap<String, MetaDistrConfItem>();
		for( ConfDeploymentPair pair : confItems ) {
			if( node == null || checkNodeDeployment( action , node , pair.deployment ) ) {
				if( !items.containsKey( pair.item.KEY ) )
					items.put( pair.item.KEY , pair.item );
			}
		}
		return( Common.getSortedKeys( items ) );
	}

	public boolean checkNodeDeployment( ActionBase action , MetaEnvServerNode node , MetaEnvServerDeployment deployment ) throws Exception {
		if( node.isAdmin( action ) )
			return( deployment.isNodeAdminDeployment( action ) );
		if( node.isSlave( action ) )
			return( deployment.isNodeSlaveDeployment( action ) );
		if( node.isSelf( action ) )
			return( deployment.isNodeSelfDeployment( action ) );
		action.exitUnexpectedState();
		return( false );
	}
	
	public String[] getBinaryItems( ActionBase action ) throws Exception {
		return( getNodeBinaryItems( action , null ) );
	}
	
	public String[] getConfItems( ActionBase action ) throws Exception {
		return( getNodeConfItems( action , null ) );
	}
	
}
