package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSourceProjectSet {

	public Meta meta;
	MetaSource sources;

	public VarCATEGORY CATEGORY;
	public String NAME;

	public List<MetaSourceProject> originalList;
	public Map<String,MetaSourceProject> map;
	
	public MetaSourceProjectSet( Meta meta , MetaSource sources ) {
		this.meta = meta;
		this.sources = sources;
		
		originalList = new LinkedList<MetaSourceProject>(); 
		map = new HashMap<String, MetaSourceProject>();
	}
	
	public MetaSourceProjectSet copy( ActionBase action , Meta meta , MetaSource sources ) throws Exception {
		MetaSourceProjectSet r = new MetaSourceProjectSet( meta , sources );
		for( MetaSourceProject project : originalList ) {
			MetaSourceProject rproject = project.copy( action , meta , r );
			r.originalList.add( rproject );
			r.map.put( rproject.NAME , rproject );
		}
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		CATEGORY = Meta.readCategoryAttr( node );
		
		if( !Meta.isSourceCategory( CATEGORY ) ) {
			String name = Common.getEnumLower( CATEGORY );
			action.exit1( _Error.UnknownProjectCategory1 , "invalid source.xml: unknown project category=" + name , name );
		}
		
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		loadProjects( action , CATEGORY , node );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "category" , Common.getEnumLower( CATEGORY ) );
		
		for( MetaSourceProject project : originalList ) {
			Element projectElement = Common.xmlCreateElement( doc , root , "project" );
			project.save( action , doc , projectElement );
		}
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
			addProject( project );
		}
	}

	private void addProject( MetaSourceProject project ) {
		originalList.add( project );
		map.put( project.NAME , project );
	}
	
	public void removeProject( ServerTransaction transaction , MetaSourceProject project ) {
		originalList.remove( project );
		map.remove( project.NAME );
	}
	
	public void addProject( ServerTransaction transaction , MetaSourceProject project ) {
		addProject( project );
	}
	
}
