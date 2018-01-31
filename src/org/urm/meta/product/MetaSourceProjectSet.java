package org.urm.meta.product;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;

public class MetaSourceProjectSet {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	
	public Meta meta;
	public MetaSources sources;

	public int ID;
	public String NAME;
	public String DESC;
	public int PV;

	private List<MetaSourceProject> orderedList;
	private Map<String,MetaSourceProject> map;
	
	public MetaSourceProjectSet( Meta meta , MetaSources sources ) {
		this.meta = meta;
		this.sources = sources;
		
		orderedList = new LinkedList<MetaSourceProject>(); 
		map = new HashMap<String, MetaSourceProject>();
	}
	
	public MetaSourceProjectSet copy( Meta rmeta , MetaSources rsources ) throws Exception {
		MetaSourceProjectSet r = new MetaSourceProjectSet( rmeta , rsources );
		
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PV = PV;
		
		for( MetaSourceProject project : orderedList ) {
			MetaSourceProject rproject = project.copy( rmeta , r );
			r.addProject( rproject );
		}
		
		return( r );
	}
	
	public void createProjectSet( String name , String desc ) {
		modifyProjectSet( name , desc );
	}
	
	public void modifyProjectSet( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public MetaSourceProject findProject( String name ) {
		MetaSourceProject project = map.get( name );
		return( project );
	}
	
	public MetaSourceProject getProject( String name ) throws Exception {
		MetaSourceProject project = map.get( name );
		if( project == null )
			Common.exit1( _Error.UnknownSourceProject1 , "unknown source project=" + name , name );
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

	public void addProject( MetaSourceProject project ) throws Exception {
		for( MetaSourceProject p : orderedList ) {
			if( p.PROJECT_POS >= project.PROJECT_POS )
				p.changeOrder( p.PROJECT_POS + 1 );
		}
			
		map.put( project.NAME , project );
		reorderProjects();
	}
	
	public void updateProject( MetaSourceProject project ) throws Exception {
		Common.changeMapKey( map , project , project.NAME );
	}
	
	public void removeProject( MetaSourceProject project ) throws Exception {
		map.remove( project.NAME );
		reorderProjects();
	}
	
	public void changeProjectOrder( MetaSourceProject project , int POS ) throws Exception {
		for( MetaSourceProject p : orderedList ) {
			if( p.PROJECT_POS >= POS )
				p.changeOrder( p.PROJECT_POS + 1 );
		}
			
		project.changeOrder( POS );
		reorderProjects();
	}

	public void reorderProjects() {
		List<String> order = new LinkedList<String>();
		for( MetaSourceProject project : map.values() ) {
			String key = Common.getZeroPadded( project.PROJECT_POS , 10 ) + "#" + project.NAME;
			order.add( key );
		}
		
		Collections.sort( order );
		
		int POS = 1;
		orderedList.clear();
		for( String key : order ) {
			String projectName = Common.getPartAfterFirst( key , "#" );
			MetaSourceProject project = map.get( projectName );
			project.PROJECT_POS = POS++;
			orderedList.add( project );
		}
	}

	public boolean isEmpty() {
		if( map.size() == 0 )
			return( true );
		return( false );
	}
	
}
