package com.morganstanley.stocklending.approval;

/**
 * ApprovalSourceException is the runtime exception (unchecked exception) and
 * indicates the exception occurred during process of getting approval request
 * from the approval source.
 *
 * @author Richard WU
 */
public class ApprovalSourceException extends RuntimeException {

	private static final long serialVersionUID = 2459907128869233284L;

	/**
	 * Constructs an ApprovalSourceException with no detail message.
	 */
	public ApprovalSourceException() {
	}

	/**
	 * Constructs an ApprovalSourceException with the specified detail message.
	 * 
	 * @param message
	 *            detail message
	 */
	public ApprovalSourceException(String message) {
		super(message);
	}

	/**
	 * Constructs an ApprovalSourceException with the cause
	 *
	 * @param cause
	 *            - the cause (which is saved for later retrieval by the
	 *            Throwable.getCause() method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 */
	public ApprovalSourceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            Throwable.getMessage() method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            Throwable.getCause() method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 */
	public ApprovalSourceException(String message, Throwable cause) {
		super(message, cause);
	}
}
