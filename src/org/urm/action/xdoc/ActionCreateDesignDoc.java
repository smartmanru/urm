package org.urm.action.xdoc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvs;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDesignDiagram;
import org.urm.meta.product.MetaDesignElement;
import org.urm.meta.product.MetaDesignLink;
import org.urm.meta.product.MetaDocs;

public class ActionCreateDesignDoc extends ActionBase {

	Meta meta;
	String CMD;
	String OUTDIR;
	Map<String,List<MetaEnvServer>> prodServers;
	
	public ActionCreateDesignDoc( ActionBase action , String stream , Meta meta , String CMD , String OUTDIR ) {
		super( action , stream , "Create diagram, cmd=" + CMD );
		this.meta = meta;
		this.CMD = CMD;
		this.OUTDIR = OUTDIR;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		getProdServers();
		
		MetadataStorage ms = artefactory.getMetadataStorage( this , meta );
		MetaDocs docs = meta.getDocs();
		for( String designFile : ms.getDesignFiles( this ) ) {
			MetaDesignDiagram design = docs.findDiagram( designFile );
			
			String designBase = Common.getPath( OUTDIR , Common.getPartBeforeLast( designFile , ".xml" ) );
			createDesignDocs( design , designBase );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
		
	private void createDesignDocs( MetaDesignDiagram design , String designBase ) throws Exception {
		verifyConfiguration( design );
		
		String dotFile = designBase + ".dot";
		if( CMD.equals( "png" ) ) {
			createDot( design , dotFile );
			createPng( dotFile , designBase + ".png" );
		}
		else if( CMD.equals( "dot" ) ) {
			createDot( design , dotFile );
		}
		else
			exit1( _Error.UnknownDesignCommand1 , "unknown command=" + CMD , CMD );
	}

	private void getProdServers() throws Exception {
		prodServers = new HashMap<String,List<MetaEnvServer>>();
		
		MetaEnvs envs = meta.getEnviroments();
		for( String envName : envs.getEnvNames() ) {
			MetaEnv env = envs.findEnv( envName );
			if( !env.isProd() )
				continue;
			
			for( MetaEnvSegment sg : env.getSegments() ) {
				for( MetaEnvServer server : sg.getServers() ) {
					List<MetaEnvServer> mapped = prodServers.get( server.XDOC );
					if( mapped == null ) {
						mapped = new LinkedList<MetaEnvServer>(); 
						prodServers.put( server.XDOC , mapped );
					}
					
					mapped.add( server );
				}
			}
		}
	}
	
	private void verifyConfiguration( MetaDesignDiagram design ) throws Exception {
		Map<String,List<MetaEnvServer>> designServers = new HashMap<String,List<MetaEnvServer>>();
		
		// verify all design servers are mentioned in prod environment
		for( MetaDesignElement element : design.elements.values() ) {
			if( !element.isServerType() )
				continue;
			
			List<MetaEnvServer> servers = prodServers.get( element.NAME );
			if( servers == null )
				ifexit( _Error.UnknownProdDesignServer1 , "design server=" + element.NAME + " is not found in PROD (production environments)" , new String[] { element.NAME } );
			
			designServers.put( element.NAME , servers );
		}
		
		// verify all PROD servers are mentioned in design
		if( design.fullProd ) {
			for( String server : prodServers.keySet() ) {
				if( !designServers.containsKey( server ) )
					ifexit( _Error.ProdServerNotDesign1 , "production server=" + server + " is not part of design" , new String[] { server } );
			}
		}
	}

	private void createDot( MetaDesignDiagram design , String fileName ) throws Exception {
		List<String> lines = new LinkedList<String>();
		
		createDotHeading( lines );
		
		// add top-level elements
		for( String elementName : Common.getSortedKeys( design.childs ) ) {
			MetaDesignElement element = design.getElement( elementName );
			createDotElement( lines , element , false );
		}
		lines.add( "" );
		
		// add subgraphs
		for( String elementName : Common.getSortedKeys( design.groups ) ) {
			MetaDesignElement element = design.getElement( elementName );
			createDotSubgraph( lines , element );
		}
		lines.add( "" );
		
		for( String elementName : Common.getSortedKeys( design.elements ) ) {
			MetaDesignElement element = design.getElement( elementName );
			for( String linkName : Common.getSortedKeys( element.links ) ) {
				MetaDesignLink link = element.getLink( this , linkName );
				createDotLink( lines , element , link );
			}
		}

		createDotFooter( lines );
		Common.createFileFromStringList( execrc , fileName ,  lines );
	}

	private void createDotHeading( List<String> lines ) throws Exception {
		lines.add( "digraph " + Common.getQuoted( meta.name ) + " {" );
		lines.add( "\tcharset=" + Common.getQuoted( "utf8" ) + ";" );
		lines.add( "\tsplines=false;" );
		lines.add( "\tnode [shape=box, style=" + Common.getQuoted( "filled" ) + ", fontsize=10];" );
		lines.add( "\tedge [fontsize=8];" );
		lines.add( "" );
	}

	private void createDotElement( List<String> lines , MetaDesignElement element , boolean group ) throws Exception {
		String dotdef = "";
		if( element.isAppServerType() )
			dotdef = "fillcolor=lawngreen";
		else if( element.isLibraryType() )
			dotdef = "fillcolor=darkolivegreen1";
		else if( element.isExternalType() )
			dotdef = "style=" + Common.getQuoted( "rounded,filled" ) + ", fillcolor=cornflowerblue";
		else if( element.isDatabaseServerType() )
			dotdef = "style=" + Common.getQuoted( "rounded,filled" ) + ", fillcolor=lightblue";
		else if( element.isGenericType() )
			dotdef = "style=" + Common.getQuoted( "rounded,filled" ) + ", fillcolor=grey52";
		else
			this.exitUnexpectedState();

		String prefix = ( group )? "\t\t" : "\t";
		String nodeline = prefix + element.getName( this );
		String label = "<b>" + element.NAME + "</b>";
		if( !element.FUNCTION.isEmpty() )
			label += "<br/>" + Common.replace( element.FUNCTION , "\\n" , "<br/>" );
		String s = dotdef + ", label=<" + label + ">";
		nodeline += " [" + s + "]";
		nodeline += ";";
		
		lines.add( nodeline );
	}

	private void createDotSubgraph( List<String> lines , MetaDesignElement element ) throws Exception {
		String label;
		if( element.FUNCTION.isEmpty() )
			label = element.NAME;
		else
			label = element.FUNCTION + " (" + element.NAME + ")";
		
		lines.add( "\tsubgraph " + element.getName( this ) + " {" );
		lines.add( "\t\tlabel=" + Common.getQuoted( label ) + ";" );
		if( !element.GROUPCOLOR.isEmpty() )
			lines.add( "\t\tcolor=" + Common.getQuoted( element.GROUPCOLOR ) + ";" );
		if( !element.GROUPFILLCOLOR.isEmpty() ) {
			lines.add( "\t\tstyle=filled;" );
			lines.add( "\t\tfillcolor=" + Common.getQuoted( element.GROUPFILLCOLOR ) + ";" );
		}
		lines.add( "" );

		// subgraph items
		for( String name : Common.getSortedKeys( element.childs ) ) {
			MetaDesignElement child = element.childs.get( name );
			createDotElement( lines , child , true );
		}
		lines.add( "\t}" );
	}

	private void createDotLink( List<String> lines , MetaDesignElement element , MetaDesignLink link ) throws Exception {
		String linkline = "\t" + element.getLinkName( this ) + " -> " + link.target.getLinkName( this );
		String dotdef = "";
		if( link.isGenericType() )
			dotdef = "color=blue";
		else if( link.isMsgType() )
			dotdef = "style=dotted";
		else
			this.exitUnexpectedState();
		
		if( !link.TEXT.isEmpty() )
			dotdef += ", label=" + Common.getQuoted( link.TEXT );
		
		if( element.isGroup() )
			dotdef += ", ltail=" + element.getName( this );
		if( link.target.isGroup() )
			dotdef += ", lhead=" + link.target.getName( this );
		
		linkline += " [" + dotdef + "];";
		lines.add( linkline );
	}

	private void createDotFooter( List<String> lines ) throws Exception {
		lines.add( "}" );
	}

	private void createPng( String fileDot , String filePng ) throws Exception {
		String cmd = "dot -Tpng " + fileDot + " -o " + filePng;
		shell.customCheckStatus( this , cmd );
	}
	
}
