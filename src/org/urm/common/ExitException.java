package org.urm.common;

public class ExitException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6785595595488058740L;

	public ExitException( int errorCode , String message , String[] params ) {
        super( message );
    }
}