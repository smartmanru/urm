package org.urm.db.env;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.env.MetaEnvDeployment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvSegment {

	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( env.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
		
		loadServers( action , node );
		loadStartOrder( action , node );
		loadDeployment( action , node );
		
		super.initFinished();
	}

	public void loadDeployment( ActionBase action , Node node ) throws Exception {
		deploy = new MetaEnvDeployment( meta , this );
		
		Node deployment = ConfReader.xmlGetFirstChild( node , ELEMENT_DEPLOYMENT );
		if( deployment == null )
			return;
		
		deploy.load( action , deployment );
	}
	
	public void loadServers( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_SERVER );
		if( items == null )
			return;
		
		for( Node srvnode : items ) {
			MetaEnvServer server = new MetaEnvServer( meta , this );
			server.load( action , srvnode );
			addServer( server );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element deployElement = Common.xmlCreateElement( doc , root , ELEMENT_DEPLOYMENT );
		deploy.save( action , doc , deployElement );
		Element startElement = Common.xmlCreateElement( doc , root , ELEMENT_STARTORDER );
		startInfo.save( action , doc , startElement );
		
		super.saveSplit( doc , root );
		for( MetaEnvServer server : originalList ) {
			Element serverElement = Common.xmlCreateElement( doc , root , ELEMENT_SERVER );
			server.save( action , doc , serverElement );
		}
	}
	
	public void loadStartOrder( ActionBase action , Node node ) throws Exception {
		startInfo = new MetaEnvStartInfo( meta , this );
		
		Node startorder = ConfReader.xmlGetFirstChild( node , ELEMENT_STARTORDER );
		if( startorder == null )
			return;
		
		startInfo.load( action , startorder );
	}

}
