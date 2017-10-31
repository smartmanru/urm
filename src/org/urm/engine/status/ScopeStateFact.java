package org.urm.engine.status;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.status.ScopeState.FACTVALUE;
import org.urm.meta.Types.VarPROCESSMODE;

public class ScopeStateFact {

	public ScopeState state;
	public Enum<?> factType;
	public FactValue[] values;
	
	public ScopeStateFact( ScopeState state , Enum<?> factType , FactValue[] values ) {
		this.state = state;
		this.factType = factType;
		this.values = values;
	}

	public String getValue( FACTVALUE type ) {
		for( FactValue value : values ) {
			if( value.type == type )
				return( value.data );
		}
		return( "" );
	}

	public String[] getValues( FACTVALUE type ) {
		List<String> list = new LinkedList<String>();
		for( FactValue value : values ) {
			if( value.type == type )
				list.add( value.data );
		}
		return( list.toArray( new String[0] ) );
	}

	public boolean match( FACTVALUE dataType , String data ) {
		for( FactValue value : values ) {
			if( value.type == dataType && data.equals( value.data ) )
				return( true );
		}
		return( false );
	}

	public VarPROCESSMODE getProcessMode() {
		String value = getValue( FACTVALUE.PROCESSMODE );
		try {
			return( VarPROCESSMODE.valueOf( value ) );
		}
		catch( Throwable e ) {
		}
		return( VarPROCESSMODE.UNKNOWN );
	}
	
}
