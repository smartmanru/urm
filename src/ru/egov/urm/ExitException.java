package ru.egov.urm;

public class ExitException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6785595595488058740L;

	public ExitException( String message ) {
        super(message);
    }
}