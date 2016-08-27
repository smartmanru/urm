package org.urm.server.meta;

import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaWebResource {

	public Meta meta;
	public MetaResources resources;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String TYPE;
	public String BASEURL;
	public String AUTHFILE;
	
	public MetaWebResource( Meta meta , MetaResources resources ) {
		this.meta = meta;
		this.resources = resources;
		loaded = false;
		loadFailed = false;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "resource" , null );
		properties.loadRawFromNodeAttributes( node );
		
		scatterSystemProperties( action );
		properties.finishRawProperties();
	}
	
	private void scatterSystemProperties( ActionBase action ) throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		TYPE = properties.getSystemRequiredStringProperty( "type" );
		BASEURL = properties.getSystemRequiredStringProperty( "baseurl" );
		AUTHFILE = properties.getSystemStringProperty( "authfile" , "" );
	}

	public boolean isSvn() {
		if( TYPE.equals( "svn" ) )
			return( true );
		return( false );
	}
	
	public boolean isGit() {
		if( TYPE.equals( "git" ) )
			return( true );
		return( false );
	}
	
	public boolean isNexus() {
		if( TYPE.equals( "nexus" ) )
			return( true );
		return( false );
	}
	
}
