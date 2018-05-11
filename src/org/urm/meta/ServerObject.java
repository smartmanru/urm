package org.urm.meta;

import java.util.LinkedList;
import java.util.List;

public class ServerObject {

	static int objectIdSequence = 0;
	public int objectId;
	
	private ServerObject parent;
	private List<ServerRef<?>> refs;
	private List<ServerObject> childs;
	boolean deleted;
	
	public ServerObject( ServerObject parent ) {
		this.parent = parent;
		
		deleted = false;
		objectId = ++objectIdSequence;
		refs = new LinkedList<ServerRef<?>>();
		childs = new LinkedList<ServerObject>();
		if( parent != null )
			parent.childs.add( this );
	}

	public void refObject( ServerRef<?> ref ) {
		refs.add( ref );
	}

	public void derefObject( ServerRef<?> ref ) {
		refs.remove( ref );
	}
	
	public void deleteObject() {
		deleteObjectDown();
		if( parent != null )
			parent.childs.remove( this );
	}
		
	public void deleteObjectDown() {
		for( ServerRef<?> ref : refs )
			ref.reflexObjectDeleted();
		refs.clear();
		
		for( ServerObject child : childs )
			child.deleteObjectDown();
		
		deleted = true;
	}

	@Override
	protected void finalize() throws Throwable {
		if( !deleted )
			System.out.println( "finalize not deleted object id=" + objectId + ", class=" + getClass().getSimpleName() );
		super.finalize();
	}
}
