package org.urm.meta.product;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.loader.MatchItem;

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
			MetaSourceProjectSet rset = set.copy( rmeta , r , true );
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
	
	public void addProjectSet( MetaSourceProjectSet set ) {
		for( MetaSourceProjectSet p : setMap.values() ) {
			if( p.SET_POS >= set.SET_POS )
				p.changeOrder( p.SET_POS + 1 );
		}
			
		addProjectSetOnly( set );
	}
	
	private void addProjectSetOnly( MetaSourceProjectSet set ) {
		setMap.put( set.NAME , set );
		setMapById.put( set.ID , set );
	}
	
	public void addProject( MetaSourceProjectSet set , MetaSourceProject project ) throws Exception {
		set.addProject( project );
		projectMap.put( project.NAME , project );
		projectMapById.put( project.ID , project );
	}

	public void updateProjectSet( MetaSourceProjectSet set ) throws Exception {
		Common.changeMapKey( setMap , set , set.NAME );
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
	
	public MetaSourceProjectSet findProjectSet( int id ) {
		MetaSourceProjectSet set = setMapById.get( id );
		return( set );
	}
	
	public MetaSourceProjectSet findProjectSet( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( setMapById.get( item.FKID ) );
		return( setMap.get( item.FKNAME ) );
	}
	
	public String findProjectSetName( MatchItem item ) {
		MetaSourceProjectSet set = findProjectSet( item );
		if( item == null )
			return( null );
		return( set.NAME );
	}
	
	public MetaSourceProjectSet getProjectSet( String name ) throws Exception {
		MetaSourceProjectSet set = setMap.get( name );
		if( set == null )
			Common.exit1( _Error.UnknownSourceSet1 , "unknown source set=" + name , name );
		
		return( set );
	}

	public String getProjectSetName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaSourceProjectSet set = findProjectSet( item );
		if( set == null )
			Common.exitUnexpected();
		return( set.NAME );
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

	public MetaSourceProject findProject( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( projectMapById.get( item.FKID ) );
		return( projectMap.get( item.FKNAME ) );
	}
	
	public String findProjectName( MatchItem item ) {
		MetaSourceProject project = findProject( item );
		if( item == null )
			return( null );
		return( project.NAME );
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

	public String getProjectName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaSourceProject project = findProject( item );
		if( project == null )
			Common.exitUnexpected();
		return( project.NAME );
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

	private void removeProjectSetOnly( MetaSourceProjectSet set ) throws Exception {
		setMap.remove( set.NAME );
		setMapById.remove( set.ID );
	}
	
	public void removeProjectSet( MetaSourceProjectSet set ) throws Exception {
		for( MetaSourceProject project : set.getProjects() )
			removeProject( project );
		
		removeProjectSetOnly( set );
	}

	public boolean hasProjects() {
		if( projectMap.isEmpty() )
			return( false );
		return( true );
	}

	public MatchItem getProjectSetMatchItem( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		MetaSourceProjectSet set = ( id == null )? findProjectSet( name ) : getProjectSet( id );
		MatchItem match = ( set == null )? new MatchItem( name ) : new MatchItem( set.ID );
		return( match );
	}
	
	public MatchItem getProjectMatchItem( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		MetaSourceProject project = ( id == null )? findProject( name ) : getProject( id );
		MatchItem match = ( project == null )? new MatchItem( name ) : new MatchItem( project.ID );
		return( match );
	}

	public void changeSetOrder( MetaSourceProjectSet set , int POS ) throws Exception {
		removeProjectSetOnly( set );
		for( MetaSourceProjectSet p : setMap.values() ) {
			if( p.SET_POS >= POS )
				p.changeOrder( p.SET_POS + 1 );
		}
			
		set.changeOrder( POS );
		addProjectSetOnly( set );
		reorderSets();
	}

	public void reorderSets() {
		List<String> order = new LinkedList<String>();
		for( MetaSourceProjectSet set : setMap.values() ) {
			String key = Common.getZeroPadded( set.SET_POS , 10 ) + "#" + set.NAME;
			order.add( key );
		}
		
		Collections.sort( order );
		
		int POS = 1;
		for( String key : order ) {
			String setName = Common.getPartAfterFirst( key , "#" );
			MetaSourceProjectSet set = setMap.get( setName );
			set.changeOrder( POS++ );
		}
	}

	public MetaSourceProjectSet[] getOrderedSets() {
		List<String> order = new LinkedList<String>();
		for( MetaSourceProjectSet set : setMap.values() ) {
			String key = Common.getZeroPadded( set.SET_POS , 10 ) + "#" + set.NAME;
			order.add( key );
		}
		
		Collections.sort( order );
		
		List<MetaSourceProjectSet> list = new LinkedList<MetaSourceProjectSet>();
		for( String key : order ) {
			String setName = Common.getPartAfterFirst( key , "#" );
			MetaSourceProjectSet set = setMap.get( setName );
			list.add( set );
		}
		
		return( list.toArray( new MetaSourceProjectSet[0] ) );
	}

	public int getSetCount() {
		return( setMap.size() );
	}
	
}
