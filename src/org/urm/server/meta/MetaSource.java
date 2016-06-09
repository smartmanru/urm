package org.urm.server.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.meta.Metadata.VarCATEGORY;
import org.urm.server.storage.MetadataStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MetaSource {

	List<MetaSourceProjectSet> originalList;
	Map<String,MetaSourceProjectSet> setMap;
	Map<String,MetaSourceProject> projectMap;
	boolean loaded = false;
	
	Metadata meta;
	
	public MetaSource( Metadata meta ) {
		this.meta = meta;
	}
	
	public void load( ActionBase action , MetadataStorage storage ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		originalList = new LinkedList<MetaSourceProjectSet>();
		setMap = new HashMap<String,MetaSourceProjectSet>();
		projectMap = new HashMap<String,MetaSourceProject>();
		
		// read xml
		String file = storage.getSourceConfFile( action );
		
		action.debug( "read source definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node[] sets = ConfReader.xmlGetChildren( doc.getDocumentElement() , "projectset" );
		if( sets == null )
			return;
		
		for( Node node : sets ) {
			MetaSourceProjectSet projectset = new MetaSourceProjectSet( meta , this );
			projectset.load( action , node );

			originalList.add( projectset );
			setMap.put( projectset.NAME , projectset );
			projectMap.putAll( projectset.map );
		}
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
			if( pset.CATEGORY == CATEGORY )
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
			action.exit( "unknown set=" + name );
		
		return( set );
	}
	
	public MetaSourceProject getProject( ActionBase action , String name ) throws Exception {
		MetaSourceProject project = projectMap.get( name );
		if( project == null )
			action.exit( "unknown project=" + name );
		
		return( project );
	}

}
