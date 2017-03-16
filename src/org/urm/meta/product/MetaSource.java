package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.ServerTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSource extends PropertyController {

	public Meta meta;
	
	private Map<String,MetaSourceProjectSet> setMap;
	private Map<String,MetaSourceProject> projectMap;
	
	public MetaSource( ServerProductMeta storage , Meta meta ) {
		super( storage , "source" );
		this.meta = meta;
		meta.setSources( this );
		
		setMap = new HashMap<String,MetaSourceProjectSet>();
		projectMap = new HashMap<String,MetaSourceProject>();
	}
	
	@Override
	public String getName() {
		return( "meta-source" );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
	}
	
	public MetaSource copy( ActionBase action , Meta meta ) throws Exception {
		MetaSource r = new MetaSource( meta.getStorage( action ) , meta );
		MetaProductSettings product = meta.getProductSettings( action );
		r.initCopyStarted( this , product.getProperties() );
		for( MetaSourceProjectSet set : setMap.values() ) {
			MetaSourceProjectSet rset = set.copy( action , meta , r );
			r.addProjectSet( rset );
		}

		for( MetaSourceProject project : projectMap.values() ) {
			MetaSourceProjectSet rset = r.setMap.get( project.set.NAME );
			MetaSourceProject rp = rset.getProject( action , project.NAME );
			r.projectMap.put( rp.NAME , rp );
		}
		r.initFinished();
		return( r );
	}
	
	public void createSources( TransactionBase transaction ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( transaction.action );
		if( !initCreateStarted( product.getProperties() ) )
			return;

		initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;

		Node[] sets = ConfReader.xmlGetChildren( root , "projectset" );
		if( sets == null )
			return;
		
		for( Node node : sets ) {
			MetaSourceProjectSet projectset = new MetaSourceProjectSet( meta , this );
			projectset.load( action , node );
			addProjectSet( projectset );
			for( MetaSourceProject project : projectset.getProjects() )
				projectMap.put( project.NAME , project );
		}
		
		super.initFinished();
	}

	private void addProjectSet( MetaSourceProjectSet projectset ) {
		setMap.put( projectset.NAME , projectset );
	}
	
	public MetaSourceProject[] getBuildProjects( ActionBase action , VarBUILDMODE buildMode ) {
		List<MetaSourceProject> all = getAllProjectList( action , true );
		List<MetaSourceProject> list = new LinkedList<MetaSourceProject>(); 
		for( MetaSourceProject project : all ) {
			if( buildMode == VarBUILDMODE.BRANCH ) {
				if( project.codebaseProd )
					list.add( project );
			}
			else if( buildMode == VarBUILDMODE.MAJORBRANCH ) {
				if( project.codebaseProd )
					list.add( project );
			}
		}
		
		return( list.toArray( new MetaSourceProject[0] ) );
	}

	public MetaSourceProjectSet[] getSetList() {
		return( setMap.values().toArray( new MetaSourceProjectSet[0] ) );
	}
	
	public List<MetaSourceProject> getAllProjectList( ActionBase action , boolean buildable ) {
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
	
	public MetaSourceProjectSet getProjectSet( ActionBase action , String name ) throws Exception {
		MetaSourceProjectSet set = setMap.get( name );
		if( set == null )
			action.exit1( _Error.UnknownSourceSet1 , "unknown source set=" + name , name );
		
		return( set );
	}

	public MetaSourceProject findProject( String name ) {
		MetaSourceProject project = projectMap.get( name );
		return( project );
	}
	
	public MetaSourceProject getProject( ActionBase action , String name ) throws Exception {
		MetaSourceProject project = projectMap.get( name );
		if( project == null )
			action.exit1( _Error.UnknownSourceProject1 , "unknown source project=" + name , name );
		
		return( project );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveAsElements( doc , root , false );
		for( MetaSourceProjectSet set : setMap.values() ) {
			Element setElement = Common.xmlCreateElement( doc , root , "projectset" );
			set.save( action , doc , setElement );
		}
	}

	public String[] getProjectNames() {
		return( Common.getSortedKeys( projectMap ) );
	}

	public MetaSourceProjectItem getProjectItem( ActionBase action , String name ) throws Exception {
		for( MetaSourceProject project : projectMap.values() ) {
			MetaSourceProjectItem item = project.findItem( name );
			if( item != null )
				return( item );
		}
		action.exit1( _Error.UnknownSourceProjectItem1 , "unknown source project item=" + name , name );
		return( null );
	}

	public MetaSourceProjectSet createProjectSet( ServerTransaction transaction , String name ) throws Exception {
		MetaSourceProjectSet set = new MetaSourceProjectSet( meta , this );
		set.create( transaction , name );
		addProjectSet( set );
		return( set );
	}

	public MetaSourceProject createProject( ServerTransaction transaction , MetaSourceProjectSet set , String name , int POS ) throws Exception {
		MetaSourceProject project = new MetaSourceProject( set.meta , set );
		project.createProject( transaction , name , POS );
		set.addProject( transaction , project );
		projectMap.put( project.NAME , project );
		return( project );
	}

	public void removeProjectSet( ServerTransaction transaction , MetaSourceProjectSet set ) throws Exception {
		for( MetaSourceProject project : set.getProjects() )
			projectMap.remove( project.NAME );
		setMap.remove( set.NAME );
	}

	public void removeProject( ServerTransaction transaction , MetaSourceProject project ) throws Exception {
		project.set.removeProject( transaction , project );
		projectMap.remove( project.NAME );
	}
	
}
