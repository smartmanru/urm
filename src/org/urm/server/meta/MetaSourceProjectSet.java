package org.urm.server.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarCATEGORY;
import org.urm.server.meta.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaSourceProjectSet {

	boolean loaded = false;
	
	protected Meta meta;
	MetaSource source;

	public VarCATEGORY CATEGORY;
	public String NAME;

	public List<MetaSourceProject> originalList;
	public Map<String,MetaSourceProject> map;
	
	public MetaSourceProjectSet( Meta meta , MetaSource source ) {
		this.meta = meta;
		this.source = source;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		CATEGORY = meta.readCategoryAttr( node );
		originalList = new LinkedList<MetaSourceProject>(); 
		map = new HashMap<String, MetaSourceProject>();
		
		if( !meta.isSourceCategory( CATEGORY ) )
			action.exit( "invalid source.xml: unknown project category=" + Common.getEnumLower( CATEGORY ) );
		
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		loadProjects( action , CATEGORY , node );
	}

	public List<MetaSourceProject> getOriginalList( ActionBase action ) throws Exception {
		return( originalList );
	}

	public Map<String,MetaSourceProject> getProjects( ActionBase action ) throws Exception {
		return( map );
	}

	void loadProjects( ActionBase action , VarCATEGORY CATEGORY , Node pset ) throws Exception {
		Node[] projects = ConfReader.xmlGetChildren( pset , "project" );
		if( projects == null )
			return;
		
		for( Node node : projects ) {
			MetaSourceProject project = new MetaSourceProject( meta , this );
			project.load( action , node );
			
			originalList.add( project );
			map.put( project.PROJECT , project );
		}
	}
	
}
