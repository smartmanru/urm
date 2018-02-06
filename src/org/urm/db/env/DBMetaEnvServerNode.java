package org.urm.db.env;

import org.urm.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvServerNode {

	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
	}
	
}
