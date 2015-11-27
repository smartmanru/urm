package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.meta.Metadata.VarDEPLOYTYPE;
import ru.egov.urm.run.ActionBase;

public class MetaEnvServerLocation {

	MetaEnvServer server;
	
	public VarDEPLOYTYPE DEPLOYTYPE;
	public String DEPLOYPATH;
	
	public Map<String,MetaDistrBinaryItem> binaryItems;
	public Map<String,String> deployNameMap;
	public Map<String,MetaDistrConfItem> confItems;
	
	public MetaEnvServerLocation( MetaEnvServer server , VarDEPLOYTYPE DEPLOYTYPE , String DEPLOYPATH ) {
		this.server = server;
		this.DEPLOYTYPE = DEPLOYTYPE;
		this.DEPLOYPATH = DEPLOYPATH;
		
		binaryItems = new HashMap<String,MetaDistrBinaryItem>();
		deployNameMap = new HashMap<String,String>();
		confItems = new HashMap<String,MetaDistrConfItem>();
	}
	
	public void addBinaryItem( ActionBase action , MetaDistrBinaryItem binaryItem , String deployName ) throws Exception {
		if( deployName.isEmpty() )
			deployName = binaryItem.DEPLOYBASENAME;

		MetaDistrBinaryItem item = binaryItems.get( binaryItem.KEY );
		if( deployNameMap.containsKey( binaryItem.KEY ) )
			action.exit( "unexpected duplcate item=" + binaryItem.KEY + " in location=" + DEPLOYPATH );
		
		if( item == null )
			binaryItems.put( binaryItem.KEY , binaryItem );
		
		deployNameMap.put( binaryItem.KEY , deployName );
	}

	public void addConfItem( ActionBase action , MetaDistrConfItem confItem ) throws Exception {
		if( confItems.get( confItem.KEY ) == null )
			confItems.put( confItem.KEY , confItem );
	}

	public boolean hasBinaryItems( ActionBase action ) throws Exception {
		if( binaryItems.isEmpty() )
			return( false );
		return( true );
	}

	public String getDeployName( ActionBase action , String key ) throws Exception {
		String itemName = deployNameMap.get( key );
		if( itemName == null || itemName.isEmpty() )
			action.exitUnexpectedState();
		return( itemName );
	}
	
}
