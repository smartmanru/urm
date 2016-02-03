package ru.egov.urm.run.xdoc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDesign;
import ru.egov.urm.meta.MetaDesign.VarELEMENTTYPE;
import ru.egov.urm.meta.MetaDesign.VarLINKTYPE;
import ru.egov.urm.meta.MetaDesignElement;
import ru.egov.urm.meta.MetaDesignLink;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.MetadataStorage;

public class ActionCreateDesignDoc extends ActionBase {

	String CMD;
	String OUTDIR;
	Map<String,List<MetaEnvServer>> prodServers;
	
	public ActionCreateDesignDoc( ActionBase action , String stream , String CMD , String OUTDIR ) {
		super( action , stream );
		this.CMD = CMD;
		this.OUTDIR = OUTDIR;
	}

	@Override protected boolean executeSimple() throws Exception {
		getProdServers();
		
		MetadataStorage ms = artefactory.getMetadataStorage( this );
		for( String designFile : ms.getDesignFiles( this ) ) {
			MetaDesign design = meta.loadDesignData( this , designFile );
			
			String designBase = Common.getPath( OUTDIR , Common.getPartBeforeLast( designFile , ".xml" ) );
			createDesignDocs( design , designBase );
		}
		
		return( true );
	}
		
	private void createDesignDocs( MetaDesign design , String designBase ) throws Exception {
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
			exit( "unknown command=" + CMD );
	}

	private void getProdServers() throws Exception {
		prodServers = new HashMap<String,List<MetaEnvServer>>();
		
		MetadataStorage ms = artefactory.getMetadataStorage( this );
		String[] files = ms.getEnvFiles( this );
		for( String envFile : files ) {
			MetaEnv env = meta.loadEnvData( this , envFile , false );
			if( !env.PROD )
				continue;
			
			for( MetaEnvDC dc : env.getDCMap( this ).values() ) {
				for( MetaEnvServer server : dc.getServerMap( this ).values() ) {
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
	
	private void verifyConfiguration( MetaDesign design ) throws Exception {
		Map<String,List<MetaEnvServer>> designServers = new HashMap<String,List<MetaEnvServer>>();
		
		// verify all design servers are mentioned in prod environment
		for( MetaDesignElement element : design.elements.values() ) {
			if( element.elementType == VarELEMENTTYPE.GROUP || 
				element.elementType == VarELEMENTTYPE.EXTERNAL || 
				element.elementType == VarELEMENTTYPE.GENERIC )
				continue;
			
			if( element.elementType == VarELEMENTTYPE.SERVER || element.elementType == VarELEMENTTYPE.DATABASE ) {
				List<MetaEnvServer> servers = prodServers.get( element.NAME );
				if( servers == null )
					exit( "design server=" + element.NAME + " is not found in PROD (production environments)" );
				
				designServers.put( element.NAME , servers );
			}
			else
				exitUnexpectedState();
		}
		
		// verify all PROD servers are mentioned in design
		if( design.fullProd ) {
			for( String server : prodServers.keySet() ) {
				if( !designServers.containsKey( server ) )
					exit( "design server=" + server + " is not part of any PROD environment" );
			}
		}
	}

	private void createDot( MetaDesign design , String fileName ) throws Exception {
		List<String> lines = new LinkedList<String>();
		
		createDotHeading( lines );
		
		// add top-level elements
		for( String elementName : Common.getSortedKeys( design.childs ) ) {
			MetaDesignElement element = design.getElement( this , elementName );
			createDotElement( lines , element , false );
		}
		lines.add( "" );
		
		// add subgraphs
		for( String elementName : Common.getSortedKeys( design.groups ) ) {
			MetaDesignElement element = design.getElement( this , elementName );
			createDotSubgraph( lines , element );
		}
		lines.add( "" );
		
		for( String elementName : Common.getSortedKeys( design.elements ) ) {
			MetaDesignElement element = design.getElement( this , elementName );
			for( String linkName : Common.getSortedKeys( element.links ) ) {
				MetaDesignLink link = element.getLink( this , linkName );
				createDotLink( lines , element , link );
			}
		}

		createDotFooter( lines );
		Common.createFileFromStringList( fileName ,  lines );
	}

	private void createDotHeading( List<String> lines ) throws Exception {
		lines.add( "digraph " + Common.getQuoted( meta.product.CONFIG_PRODUCT ) + " {" );
		lines.add( "\tcharset=" + Common.getQuoted( "utf8" ) + ";" );
		lines.add( "\tcompound=true;" );
		lines.add( "\tnode [shape=box, style=" + Common.getQuoted( "filled, rounded" ) + ", fontsize=10];" );
		lines.add( "" );
	}

	private void createDotElement( List<String> lines , MetaDesignElement element , boolean group ) throws Exception {
		String dotdef = "";
		if( element.elementType == VarELEMENTTYPE.SERVER )
			dotdef = "fillcolor=green";
		else if( element.elementType == VarELEMENTTYPE.EXTERNAL )
			dotdef = "fillcolor=yellow";
		else if( element.elementType == VarELEMENTTYPE.DATABASE )
			dotdef = "fillcolor=lightblue";
		else if( element.elementType == VarELEMENTTYPE.GENERIC )
			dotdef = "fillcolor=lightgray";
		else
			this.exitUnexpectedState();

		String prefix = ( group )? "\t\t" : "\t";
		String nodeline = prefix + Common.getQuoted( element.NAME );
		String label = "<b>" + element.NAME + "</b>";
		if( !element.FUNCTION.isEmpty() )
			label += "<br/>" + element.FUNCTION;
		String s = dotdef + ", label=<" + label + ">";
		nodeline += " [" + s + "]";
		nodeline += ";";
		
		lines.add( nodeline );
	}

	private void createDotSubgraph( List<String> lines , MetaDesignElement element ) throws Exception {
		String label = element.NAME;
		if( !element.FUNCTION.isEmpty() )
			label += " (" + element.FUNCTION + ")";
		
		lines.add( "\tsubgraph " + Common.getQuoted( element.NAME ) + " {" );
		lines.add( "\t\tlabel=" + Common.getQuoted( label ) + ";" );
		lines.add( "" );

		// subgraph items
		for( String name : Common.getSortedKeys( element.childs ) ) {
			MetaDesignElement child = element.childs.get( name );
			createDotElement( lines , child , true );
		}
		lines.add( "\t}" );
	}

	private void createDotLink( List<String> lines , MetaDesignElement element , MetaDesignLink link ) throws Exception {
		String linkline = "\t" + Common.getQuoted( element.NAME ) + " -> " + Common.getQuoted( link.TARGET );
		String dotdef = "";
		if( link.linkType == VarLINKTYPE.GENERIC )
			dotdef = "color=blue";
		else if( link.linkType == VarLINKTYPE.MSG )
			dotdef = "style=dotted";
		else
			this.exitUnexpectedState();
		
		if( !link.TEXT.isEmpty() )
			dotdef += ", label=" + Common.getQuoted( link.TEXT );
		
		linkline += " [" + dotdef + "];";
		lines.add( linkline );
	}

	private void createDotFooter( List<String> lines ) throws Exception {
		lines.add( "}" );
	}

	private void createPng( String fileDot , String filePng ) throws Exception {
		String cmd = "dot -Tpng " + fileDot + " -o " + filePng;
		session.customCheckStatus( this , cmd );
	}
	
}
