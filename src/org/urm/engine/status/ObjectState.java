package org.urm.engine.status;

import java.util.LinkedList;
import java.util.List;

public class ObjectState {

	public enum STATETYPE {
		TypeApp ,
		TypeSystem ,
		TypeProduct ,
		TypeScope ,
		TypeSet ,
		TypeTarget ,
		TypeItem ,
		TypeAccount ,
		TypeEnv ,
		TypeSegment ,
		TypeServer ,
		TypeServerNode
	};

	public STATETYPE type;	
	public ObjectState parent;
	public Object object;

	protected List<ObjectState> childs;
	
	public ObjectState( STATETYPE type , ObjectState parent , Object object ) {
		this.type = type;
		this.parent = parent;
		this.object = object;
		
		childs = new LinkedList<ObjectState>();
		if( parent != null )
			parent.childs.add( this );
	}

}
