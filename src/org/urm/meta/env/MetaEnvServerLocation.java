package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.loader.Types.*;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;

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
	
	public Meta meta;
	public MetaEnvServer server;
	
	public DBEnumDeployModeType DEPLOYTYPE;
	public String DEPLOYPATH;
	
	private Map<String,String> deployNameMap;
	private List<BinaryDeploymentPair> binaryItems;
	private List<ConfDeploymentPair> confItems;
	private List<MetaEnvServerDeployment> deployments;
	
	public MetaEnvServerLocation( Meta meta , MetaEnvServer server , DBEnumDeployModeType deployType , String deployPath ) {
		this.meta = meta;
		this.server = server;
		this.DEPLOYTYPE = deployType;
		this.DEPLOYPATH = deployPath;
		
		binaryItems = new LinkedList<BinaryDeploymentPair>();
		deployNameMap = new HashMap<String,String>();
		confItems = new LinkedList<ConfDeploymentPair>();
		deployments = new LinkedList<MetaEnvServerDeployment>(); 
	}
	
	public void addBinaryItem( MetaEnvServerDeployment deployment , MetaDistrBinaryItem binaryItem , String deployName ) {
		if( deployName.isEmpty() )
			deployName = binaryItem.BASENAME_DEPLOY;

		BinaryDeploymentPair pair = new BinaryDeploymentPair( deployment , binaryItem );
		binaryItems.add( pair );
		deployNameMap.put( binaryItem.NAME , deployName );
		
		if( !deployments.contains( deployment ) )
			deployments.add( deployment );
	}

	public void addConfItem( MetaEnvServerDeployment deployment , MetaDistrConfItem confItem ) {
		ConfDeploymentPair pair = new ConfDeploymentPair( deployment , confItem );
		confItems.add( pair );
		
		if( !deployments.contains( deployment ) )
			deployments.add( deployment );
	}

	public boolean hasBinaryItems() {
		if( binaryItems.isEmpty() )
			return( false );
		return( true );
	}

	public boolean hasConfItems() {
		if( confItems.isEmpty() )
			return( false );
		return( true );
	}

	public String getDeployName( String key ) throws Exception {
		String itemName = deployNameMap.get( key );
		if( itemName == null || itemName.isEmpty() )
			Common.exitUnexpected();
		return( itemName );
	}

	public EnumContentType getContentType( boolean binary ) {
		EnumContentType contentType;
		if( DEPLOYTYPE == DBEnumDeployModeType.HOT )
			contentType = ( binary )? EnumContentType.BINARYHOTDEPLOY : EnumContentType.CONFHOTDEPLOY;
		else
		if( DEPLOYTYPE == DBEnumDeployModeType.COPYONLY )
			contentType = ( binary )? EnumContentType.BINARYCOPYONLY : EnumContentType.CONFCOPYONLY;
		else
			contentType = ( binary )? EnumContentType.BINARYCOLDDEPLOY : EnumContentType.CONFCOLDDEPLOY;
		return( contentType );
	}

	public String[] getNodeBinaryItems( MetaEnvServerNode node ) throws Exception {
		Map<String, MetaDistrBinaryItem> items = new HashMap<String, MetaDistrBinaryItem>();
		for( BinaryDeploymentPair pair : binaryItems ) {
			if( node == null || checkNodeDeployment( node , pair.deployment ) ) {
				if( !items.containsKey( pair.item.NAME ) )
					items.put( pair.item.NAME , pair.item );
			}
		}
		return( Common.getSortedKeys( items ) );
	}

	public String[] getNodeConfItems( MetaEnvServerNode node ) throws Exception {
		Map<String, MetaDistrConfItem> items = new HashMap<String, MetaDistrConfItem>();
		for( ConfDeploymentPair pair : confItems ) {
			if( node == null || checkNodeDeployment( node , pair.deployment ) ) {
				if( !items.containsKey( pair.item.NAME ) )
					items.put( pair.item.NAME , pair.item );
			}
		}
		return( Common.getSortedKeys( items ) );
	}

	public boolean checkNodeDeployment( MetaEnvServerNode node , MetaEnvServerDeployment deployment ) throws Exception {
		if( node.isAdmin() )
			return( deployment.isNodeAdminDeployment() );
		if( node.isSlave() )
			return( deployment.isNodeSlaveDeployment() );
		if( node.isSelf() )
			return( deployment.isNodeSelfDeployment() );
		Common.exitUnexpected();
		return( false );
	}
	
	public String[] getBinaryItems() throws Exception {
		return( getNodeBinaryItems( null ) );
	}
	
	public String[] getConfItems( ActionBase action ) throws Exception {
		return( getNodeConfItems( null ) );
	}
	
}
