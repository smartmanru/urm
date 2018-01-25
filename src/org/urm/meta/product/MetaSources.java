package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.ProductMeta;

public class MetaSources {

	public Meta meta;
	
	private Map<String,MetaSourceProjectSet> setMap;
	private Map<Integer,MetaSourceProjectSet> setMapById;
	private Map<String,MetaSourceProject> projectMap;
	private Map<Integer,MetaSourceProject> projectMapById;
	private Map<Integer,MetaSourceProjectItem> itemMapById;
	
	public MetaSources( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setSources( this );
		
		setMap = new HashMap<String,MetaSourceProjectSet>();
		setMapById = new HashMap<Integer,MetaSourceProjectSet>();
		projectMap = new HashMap<String,MetaSourceProject>();
		projectMapById = new HashMap<Integer,MetaSourceProject>();
		itemMapById = new HashMap<Integer,MetaSourceProjectItem>();
	}
	
	public MetaSources copy( Meta rmeta ) throws Exception {
		MetaSources r = new MetaSources( rmeta.getStorage() , rmeta );
		for( MetaSourceProjectSet set : setMap.values() ) {
			MetaSourceProjectSet rset = set.copy( rmeta , r );
			r.addProjectSet( rset );
		}

		for( MetaSourceProject project : projectMap.values() ) {
			MetaSourceProjectSet rset = r.setMap.get( project.set.NAME );
			MetaSourceProject rp = rset.getProject( project.NAME );
			r.addProject( rset , rp );
		}
		
		for( MetaSourceProjectItem item : itemMapById.values() ) {
			MetaSourceProject rp = r.getProject( item.project.ID );
			MetaSourceProjectItem ritem = rp.getItem( item.NAME );
			r.addProjectItem( ritem );
		}
		
		return( r );
	}
	
	public void addProjectSet( MetaSourceProjectSet projectset ) {
		setMap.put( projectset.NAME , projectset );
		setMapById.put( projectset.ID , projectset );
	}
	
	public void addProject( MetaSourceProjectSet set , MetaSourceProject project ) throws Exception {
		set.addProject( project );
		projectMap.put( project.NAME , project );
		projectMapById.put( project.ID , project );
	}

	public void updateProject( MetaSourceProject project ) throws Exception {
		Common.changeMapKey( projectMap , project , project.NAME );
		project.set.updateProject( project );
	}
	
	public void updateProjectItem( MetaSourceProjectItem item ) throws Exception {
		item.project.updateItem( item );
	}
	
	public void addProjectItem( MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		project.addItem( item );
		addProjectItem( item );
	}
	
	public void addProjectItem( MetaSourceProjectItem item ) throws Exception {
		itemMapById.put( item.ID , item );
	}

	public void removeProject( MetaSourceProjectSet set , MetaSourceProject project ) throws Exception {
		project.set.removeProject( project );
		removeProject( project );
	}
	
	private void removeProject( MetaSourceProject project ) throws Exception {
		projectMap.remove( project.NAME );
		projectMapById.remove( project.ID );
		
		for( MetaSourceProjectItem item : project.getItems() )
			removeProjectItem( item );
	}

	public void removeProjectItem( MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		item.project.removeItem( item );
		removeProjectItem( item );
	}

	private void removeProjectItem( MetaSourceProjectItem item ) throws Exception {
		item.project.removeItem( item );
	}

	public MetaSourceProject[] getBuildProjects( DBEnumBuildModeType buildMode ) {
		List<MetaSourceProject> all = getAllProjectList( true );
		List<MetaSourceProject> list = new LinkedList<MetaSourceProject>(); 
		for( MetaSourceProject project : all ) {
			if( buildMode == DBEnumBuildModeType.BRANCH ) {
				if( project.CODEBASE_PROD )
					list.add( project );
			}
			else if( buildMode == DBEnumBuildModeType.MAJORBRANCH ) {
				if( project.CODEBASE_PROD )
					list.add( project );
			}
		}
		
		return( list.toArray( new MetaSourceProject[0] ) );
	}

	public MetaSourceProjectSet[] getSetList() {
		return( setMap.values().toArray( new MetaSourceProjectSet[0] ) );
	}
	
	public List<MetaSourceProject> getAllProjectList( boolean buildable ) {
		List<MetaSourceProject> plist = new LinkedList<MetaSourceProject>();
		for( MetaSourceProjectSet pset : setMap.values() ) {
			for( MetaSourceProject project : pset.getProjects() ) {
				if( buildable && !project.isBuildable() )
					continue;
				plist.add( project );
			}
		}
		return( plist );
	}

	public MetaSourceProjectSet[] getSets() {
		return( setMap.values().toArray( new MetaSourceProjectSet[0] ) );
	}

	public String[] getSetNames() {
		return( Common.getSortedKeys( setMap ) ); 
	}
	
	public MetaSourceProjectSet findProjectSet( String name ) {
		MetaSourceProjectSet set = setMap.get( name );
		return( set );
	}
	
	public MetaSourceProjectSet getProjectSet( String name ) throws Exception {
		MetaSourceProjectSet set = setMap.get( name );
		if( set == null )
			Common.exit1( _Error.UnknownSourceSet1 , "unknown source set=" + name , name );
		
		return( set );
	}

	public MetaSourceProjectSet getProjectSet( int id ) throws Exception {
		MetaSourceProjectSet set = setMapById.get( id );
		if( set == null )
			Common.exit1( _Error.UnknownSourceSet1 , "unknown source set=" + id , "" + id );
		
		return( set );
	}

	public MetaSourceProject findProject( String name ) {
		MetaSourceProject project = projectMap.get( name );
		return( project );
	}
	
	public MetaSourceProject getProject( String name ) throws Exception {
		MetaSourceProject project = projectMap.get( name );
		if( project == null )
			Common.exit1( _Error.UnknownSourceProject1 , "unknown source project=" + name , name );
		
		return( project );
	}

	public MetaSourceProject getProject( int id ) throws Exception {
		MetaSourceProject project = projectMapById.get( id );
		if( project == null )
			Common.exit1( _Error.UnknownSourceProject1 , "unknown source project=" + id , "" + id );
		
		return( project );
	}

	public String[] getProjectNames() {
		return( Common.getSortedKeys( projectMap ) );
	}

	public MetaSourceProjectItem getProjectItem( String name ) throws Exception {
		for( MetaSourceProject project : projectMap.values() ) {
			MetaSourceProjectItem item = project.findItem( name );
			if( item != null )
				return( item );
		}
		Common.exit1( _Error.UnknownSourceProjectItem1 , "unknown source project item=" + name , name );
		return( null );
	}

	public Integer getProjectItemId( String name ) throws Exception {
		if( name.isEmpty() )
			return( null );
		MetaSourceProjectItem item = getProjectItem( name );
		return( item.ID );
	}

	public MetaSourceProjectItem getProjectItem( int id ) throws Exception {
		MetaSourceProjectItem item = itemMapById.get( id );
		if( item != null )
			return( item );
		
		Common.exit1( _Error.UnknownSourceProjectItem1 , "unknown source project item=" + id , "" + id );
		return( null );
	}

	public String getProjectItemName( Integer id ) throws Exception {
		if( id == null )
			return( "" );
		MetaSourceProjectItem item = getProjectItem( id );
		return( item.NAME );
	}

	public void removeProjectSet( MetaSourceProjectSet set ) throws Exception {
		for( MetaSourceProject project : set.getProjects() )
			removeProject( project );
		
		setMap.remove( set.NAME );
		setMapById.remove( set.ID );
	}

	public boolean hasProjects() {
		if( projectMap.isEmpty() )
			return( false );
		return( true );
	}
	
}
