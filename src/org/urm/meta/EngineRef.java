package org.urm.meta;

public class EngineRef<ItemType extends EngineObject> {
	public ItemType value;
	
	public EngineRef() {
	}
	
	public EngineRef( ItemType value ) {
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
