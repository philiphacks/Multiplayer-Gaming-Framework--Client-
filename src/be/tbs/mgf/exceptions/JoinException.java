package be.tbs.mgf.exceptions;

/**
 * Join Exception
 * @author philip
 *
 */
public class JoinException extends FrameworkException {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -186041008249820423L;

	/**
	 * Construct a new Join Error with given message
	 * @param message - the reason a join operation failed
	 */
	public JoinException(String message) {
		super(message);
	}

	/**
	 * Construct a new Join Error with given message and cause
	 * @param message - the reason a join operation failed
	 * @param cause - the underlying cause the operation failed
	 */
	public JoinException(String message, Throwable cause) {
		super(message, cause);
	}
}
