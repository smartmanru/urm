package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.TransactionBase;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.product.Meta.VarBUILDMODE;
import org.urm.meta.product.Meta.VarCATEGORY;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSource extends PropertyController {

	List<MetaSourceProjectSet> originalList;
	Map<String,MetaSourceProjectSet> setMap;
	Map<String,MetaSourceProject> projectMap;
	
	protected Meta meta;
	
	public MetaSource( ServerProductMeta storage , Meta meta ) {
		super( storage , "source" );
		this.meta = meta;
		meta.setSources( this );
		
		originalList = new LinkedList<MetaSourceProjectSet>();
		setMap = new HashMap<String,MetaSourceProjectSet>();
		projectMap = new HashMap<String,MetaSourceProject>();
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
		for( MetaSourceProjectSet set : originalList ) {
			MetaSourceProjectSet rset = set.copy( action , meta , r );
			r.originalList.add( set );
			r.setMap.put( rset.NAME , rset );
		}

		for( MetaSourceProject project : projectMap.values() ) {
			MetaSourceProjectSet rset = r.setMap.get( project.set.NAME );
			MetaSourceProject rp = rset.getProject( action , project.PROJECT );
			r.projectMap.put( rp.PROJECT , rp );
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

			originalList.add( projectset );
			setMap.put( projectset.NAME , projectset );
			projectMap.putAll( projectset.map );
		}
		
		super.initFinished();
	}

	public List<MetaSourceProject> getProjectList( ActionBase action , VarCATEGORY CATEGORY , VarBUILDMODE buildMode ) {
		List<MetaSourceProject> all = getAllProjectList( action , CATEGORY );
		List<MetaSourceProject> list = new LinkedList<MetaSourceProject>(); 
		for( MetaSourceProject project : all ) {
			if( buildMode == VarBUILDMODE.BRANCH ) {
				if( project.VERSION.equals( "branch" ) )
					list.add( project );
			}
			else if( buildMode == VarBUILDMODE.MAJORBRANCH ) {
				if( project.VERSION.equals( "branch" ) || project.VERSION.equals( "majorbranch" ) )
					list.add( project );
			}
		}
		
		return( list );
	}

	public List<MetaSourceProjectSet> getSetList( ActionBase action ) throws Exception {
		return( originalList );
	}
	
	public List<MetaSourceProject> getAllProjectList( ActionBase action , VarCATEGORY CATEGORY ) {
		List<MetaSourceProject> plist = new LinkedList<MetaSourceProject>();
		for( MetaSourceProjectSet pset : setMap.values() ) {
			if( CATEGORY == null || pset.CATEGORY == CATEGORY )
				plist.addAll( pset.originalList );
		}
		return( plist );
	}

	public Map<String,MetaSourceProjectSet> getSets( ActionBase action ) throws Exception {
		return( setMap );
	}
	
	public MetaSourceProjectSet getProjectSet( ActionBase action , String name ) throws Exception {
		MetaSourceProjectSet set = setMap.get( name );
		if( set == null )
			action.exit1( _Error.UnknownSourceSet1 , "unknown source set=" + name , name );
		
		return( set );
	}
	
	public MetaSourceProject getProject( ActionBase action , String name ) throws Exception {
		MetaSourceProject project = projectMap.get( name );
		if( project == null )
			action.exit1( _Error.UnknownSourceProject1 , "unknown source project=" + name , name );
		
		return( project );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveAsElements( doc , root , false );
		for( MetaSourceProjectSet set : originalList ) {
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
	
}
