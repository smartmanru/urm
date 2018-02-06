package org.urm.db.env;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types.VarNAMETYPE;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvStartGroup {

	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		SERVERS = ConfReader.getAttrValue( node , "servers" );
		
		for( String name : Common.splitSpaced( SERVERS ) ) {
			MetaEnvServer server = startInfo.sg.getServer( action , name );
			addServer( action , server );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		SERVERS = "";
		for( MetaEnvServer server : servers )
			SERVERS = Common.addToList( SERVERS , server.NAME , " " );
		Common.xmlSetElementAttr( doc , root , "servers" , SERVERS );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "startgroup" );
		if( items == null )
			return;
		
		for( Node sgnode : items ) {
			MetaEnvStartGroup sg = new MetaEnvStartGroup( meta , this );
			sg.load( action , sgnode );
			addGroup( sg );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaEnvStartGroup group : groups ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "startgroup" );
			group.save( action , doc , itemElement );
		}
	}

}
