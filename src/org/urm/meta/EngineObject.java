package org.urm.meta;

import java.util.LinkedList;
import java.util.List;

abstract public class EngineObject {

	abstract public String getName();
	
	static int objectIdSequence = 0;
	public int objectId;
	
	private EngineObject parent;
	private List<EngineRef<?>> refs;
	private List<EngineObject> childs;
	boolean deleted;
	
	public EngineObject( EngineObject parent ) {
		this.parent = parent;
		
		deleted = false;
		objectId = ++objectIdSequence;
		refs = new LinkedList<EngineRef<?>>();
		childs = new LinkedList<EngineObject>();
		if( parent != null )
			parent.childs.add( this );
	}

	@Override
	protected void finalize() throws Throwable {
		if( !deleted )
			System.out.println( "finalize not deleted object id=" + objectId + ", class=" + getClass().getSimpleName() );
		super.finalize();
	}

	public void refObject( EngineRef<?> ref ) {
		refs.add( ref );
	}

	public void derefObject( EngineRef<?> ref ) {
		refs.remove( ref );
	}
	
	public void deleteObject() {
		deleteObjectDown();
		if( parent != null )
			parent.childs.remove( this );
	}
		
	public void deleteObjectDown() {
		for( EngineRef<?> ref : refs )
			ref.reflexObjectDeleted();
		refs.clear();
		
		for( EngineObject child : childs )
			child.deleteObjectDown();
		
		deleted = true;
	}

}
