package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;

public class ReleaseBuildScopeSet {

	public Release release;
	public MetaSourceProjectSet set;
	
	public ReleaseBuildTarget scopeTarget;
	public ReleaseBuildTarget scopeSetTarget;

	private Map<String,ReleaseBuildScopeProject> buildProjects;
	
	public ReleaseBuildScopeSet( Release release , MetaSourceProjectSet set ) {
		this.release = release;
		this.set = set;
		buildProjects = new HashMap<String,ReleaseBuildScopeProject>(); 
	}

	public boolean isEmpty() {
		if( buildProjects.isEmpty() )
			return( true );
		return( false );
	}

	public ReleaseBuildScopeProject findProject( MetaSourceProject project ) {
		return( buildProjects.get( project.NAME ) );
	}

	public ReleaseBuildScopeProject findProject( String projectName ) {
		return( buildProjects.get( projectName ) );
	}

	public void addProject( ReleaseBuildScopeProject scopeProject ) {
		buildProjects.put( scopeProject.project.NAME , scopeProject );
	}

	public ReleaseBuildScopeProject[] getProjects() {
		return( buildProjects.values().toArray( new ReleaseBuildScopeProject[0] ) );
	}
}
