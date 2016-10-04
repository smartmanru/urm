package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarCATEGORY;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaSourceProjectSet {

	boolean loaded = false;
	
	protected Meta meta;
	MetaSource sources;

	public VarCATEGORY CATEGORY;
	public String NAME;

	public List<MetaSourceProject> originalList;
	public Map<String,MetaSourceProject> map;
	
	public MetaSourceProjectSet( Meta meta , MetaSource sources ) {
		this.meta = meta;
		this.sources = sources;
	}
	
	public MetaSourceProjectSet copy( ActionBase action , Meta meta , MetaSource sources ) throws Exception {
		MetaSourceProjectSet r = new MetaSourceProjectSet( meta , sources );
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		CATEGORY = Meta.readCategoryAttr( node );
		originalList = new LinkedList<MetaSourceProject>(); 
		map = new HashMap<String, MetaSourceProject>();
		
		if( !Meta.isSourceCategory( CATEGORY ) ) {
			String name = Common.getEnumLower( CATEGORY );
			action.exit1( _Error.UnknownProjectCategory1 , "invalid source.xml: unknown project category=" + name , name );
		}
		
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		loadProjects( action , CATEGORY , node );
	}

	public MetaSourceProject getProject( ActionBase action , String name ) throws Exception {
		MetaSourceProject project = map.get( name );
		if( project == null )
			action.exit1( _Error.UnknownSourceProject1 , "unknown source project=" + name , name );
		return( project );
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
