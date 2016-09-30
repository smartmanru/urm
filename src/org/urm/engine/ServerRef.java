package org.urm.engine;

public class ServerRef<ItemType extends ServerObject> {
	public ItemType value;
	
	public ServerRef() {
	}
	
	public ServerRef( ItemType value ) {
		set( value );
	}
	
	public void set( ItemType value ) {
		if( this.value == value )
			return;
		
		if( this.value != null )
			this.value.derefObject( this );
		this.value = value;
		if( this.value != null )
			this.value.refObject( this );
	}
	
	public void reflexObjectDeleted() {
		this.value = null;
	}
	
}
