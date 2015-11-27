package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;
import ru.egov.urm.run.ActionBase;

public class MetaSourceProjectSet {

	boolean loaded = false;
	
	Metadata meta;

	public VarCATEGORY CATEGORY;
	public String NAME;

	List<MetaSourceProject> originalList;
	Map<String,MetaSourceProject> map;
	
	public MetaSourceProjectSet( Metadata meta ) {
		this.meta = meta;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		CATEGORY = meta.readCategoryAttr( action , node );
		originalList = new LinkedList<MetaSourceProject>(); 
		map = new HashMap<String, MetaSourceProject>();
		
		if( !meta.isSourceCategory( action , CATEGORY ) )
			action.exit( "invalid source.xml: unknown project category=" + Common.getEnumLower( CATEGORY ) );
		
		NAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		loadProjects( action , CATEGORY , node );
	}

	public List<MetaSourceProject> getOriginalList( ActionBase action ) throws Exception {
		return( originalList );
	}

	public Map<String,MetaSourceProject> getProjects( ActionBase action ) throws Exception {
		return( map );
	}

	void loadProjects( ActionBase action , VarCATEGORY CATEGORY , Node pset ) throws Exception {
		Node[] projects = ConfReader.xmlGetChildren( action , pset , "project" );
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
