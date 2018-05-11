package org.urm.meta.product;

import java.util.Collections;
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
	public MetaSource sources;

	public String NAME;

	private List<MetaSourceProject> orderedList;
	private Map<String,MetaSourceProject> map;
	
	public MetaSourceProjectSet( Meta meta , MetaSource sources ) {
		this.meta = meta;
		this.sources = sources;
		
		orderedList = new LinkedList<MetaSourceProject>(); 
		map = new HashMap<String, MetaSourceProject>();
	}
	
	public MetaSourceProjectSet copy( ActionBase action , Meta meta , MetaSource sources ) throws Exception {
		MetaSourceProjectSet r = new MetaSourceProjectSet( meta , sources );
		r.NAME = NAME;
		for( MetaSourceProject project : orderedList ) {
			MetaSourceProject rproject = project.copy( action , meta , r );
			r.addProject( rproject );
		}
		return( r );
	}
	
	public void create( ServerTransaction transaction , String name ) throws Exception {
		NAME = name;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		loadProjects( action , node );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		
		for( MetaSourceProject project : orderedList ) {
			Element projectElement = Common.xmlCreateElement( doc , root , "project" );
			project.save( action , doc , projectElement );
		}
	}

	public MetaSourceProject findProject( String name ) {
		MetaSourceProject project = map.get( name );
		return( project );
	}
	
	public MetaSourceProject getProject( ActionBase action , String name ) throws Exception {
		MetaSourceProject project = map.get( name );
		if( project == null )
			action.exit1( _Error.UnknownSourceProject1 , "unknown source project=" + name , name );
		return( project );
	}

	public MetaSourceProject[] getProjects() {
		return( getOrderedList() );
	}
	
	public MetaSourceProject[] getOrderedList() {
		return( orderedList.toArray( new MetaSourceProject[0] ) );
	}

	public String[] getProjectNames() {
		return( Common.getSortedKeys( map ) );
	}

	public String[] getOrderedProjectNames() {
		List<String> list = new LinkedList<String>();
		for( MetaSourceProject project : orderedList )
			list.add( project.NAME );
		return( list.toArray( new String[0] ) );
	}

	private void loadProjects( ActionBase action , Node pset ) throws Exception {
		Node[] projects = ConfReader.xmlGetChildren( pset , "project" );
		if( projects == null )
			return;
		
		for( Node node : projects ) {
			MetaSourceProject project = new MetaSourceProject( meta , this );
			project.load( action , node );
			map.put( project.NAME , project );
		}
		
		reorderProjects();
	}

	private void addProject( MetaSourceProject project ) {
		orderedList.add( project );
		map.put( project.NAME , project );
	}
	
	public void removeProject( ServerTransaction transaction , MetaSourceProject project ) throws Exception {
		map.remove( project.NAME );
		reorderProjects();
	}
	
	public void addProject( ServerTransaction transaction , MetaSourceProject project ) throws Exception {
		map.put( project.NAME , project );
		reorderProjects();
	}

	public void changeProjectOrder( ServerTransaction transaction , MetaSourceProject project , int POS ) throws Exception {
		project.setOrder( transaction , POS );
		reorderProjects();
	}

	public void reorderProjects( ServerTransaction transaction ) throws Exception {
		reorderProjects();
	}
	
	private void reorderProjects() {
		List<String> order = new LinkedList<String>();
		for( MetaSourceProject project : map.values() ) {
			String key = project.POS + "#" + project.NAME;
			order.add( key );
		}
		
		Collections.sort( order );
		
		int POS = 1;
		orderedList.clear();
		for( String key : order ) {
			String projectName = Common.getPartAfterFirst( key , "#" );
			MetaSourceProject project = map.get( projectName );
			project.POS = POS++;
			orderedList.add( project );
		}
	}

	public boolean isEmpty() {
		if( map.size() == 0 )
			return( true );
		return( false );
	}
	
}
