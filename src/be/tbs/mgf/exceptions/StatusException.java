package be.tbs.mgf.exceptions;

public class StatusException extends FrameworkException {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -6257517070091373583L;

	/**
	 * Construct a new Status Error with given message
	 * @param message - the reason a status operation failed
	 */
	public StatusException(String message) {
		super(message);
	}
	
	/**
	 * Construct a new Status Error with given message and cause
	 * @param message - the reason a status operation failed
	 * @param cause - the underlying cause the operation failed
	 */
	public StatusException(String message, Throwable cause) {
		super(message, cause);
	}

}
