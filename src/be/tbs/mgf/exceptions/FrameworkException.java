package be.tbs.mgf.exceptions;

/**
 * Represents a default Framework Exception
 * @author philip
 *
 */
public class FrameworkException extends Exception {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 788398154405186362L;
	
	/**
	 * Construct a new Framework Exception with given message and cause
	 * @param message - the reason for throwing this error
	 * @param cause - the cause for this error
	 */
	public FrameworkException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Construct a new Framework Exception with given message
	 * @param message - the reason for throwing this error
	 */
	public FrameworkException(String message) {
		super(message);
	}
	
	
}
