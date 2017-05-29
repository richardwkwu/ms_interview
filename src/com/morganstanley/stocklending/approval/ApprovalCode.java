package com.morganstanley.stocklending.approval;

/**
 * Approval code is the approval response status from Approval Decision Service
 * 
 * <li>{@link #APPROVED}</li>
 * <li>{@link #REJECTED}</li>
 * <li>{@link #REJECTED_CLIENT_NOT_FOUND}</li>
 * <li>{@link #REJECTED_SECURITY_NOT_FOUND}</li>
 * <li>{@link #REJECTED_SYSTEM_ERROR}</li>
 */
public enum ApprovalCode {
	/**
	 * Approval Request is approved.
	 */
	APPROVED,

	/**
	 * Approval Request is rejected according to the client/security/quantity.
	 */
	REJECTED,

	/**
	 * Approval Request is rejected because client information is not found in
	 * decision service.
	 */
	REJECTED_CLIENT_NOT_FOUND,

	/**
	 * Approval Request is rejected because security information is not found in
	 * decision service
	 */
	REJECTED_SECURITY_NOT_FOUND,

	/**
	 * Approval Request is rejected because decision service has system error
	 */
	REJECTED_SYSTEM_ERROR,
}
