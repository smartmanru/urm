package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.common.ExitException;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.w3c.dom.Document;

public class MetaEngine {

	PropertySet props;
	
	public MetaEngine( FinalMetaLoader loader ) {
	}

	public void load( String path , RunContext execrc ) throws Exception {
		props = new PropertySet( "root" , null );
		Document doc = ConfReader.readXmlFile( execrc , path );
		if( doc == null )
			throw new ExitException( "unable to reader engine property file " + path );
		
		props.loadRawFromAttributes( doc.getDocumentElement() );
	}

}
