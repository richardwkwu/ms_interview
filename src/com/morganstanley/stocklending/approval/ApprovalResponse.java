package com.morganstanley.stocklending.approval;

import java.math.BigDecimal;

/**
 * Approval Response is the response on the approval request after being
 * processed by ApprovalDecisionService. The response is comprised of original
 * request, approved quantity and approval response code.
 */
public class ApprovalResponse {
	/**
	 * If the approved quantity is REJECTED_QUANTITY, its value is zero.
	 */
	public static final BigDecimal REJECTED_QUANTITY = BigDecimal.ZERO;

	/**
	 * Original request for approval response
	 */
	private final ApprovalRequest request;

	/**
	 * Approval quantity for approval response
	 */
	private final BigDecimal approvedQuantity;

	/**
	 * Approval code for response
	 */
	private final ApprovalCode statusCode;

	/**
	 * Constructs a APproval Response
	 * 
	 * @param request
	 *            approval request
	 * @param approvedQuantity
	 *            approved quantity of security
	 * @param statusCode
	 *            response status code
	 */
	public ApprovalResponse(final ApprovalRequest request, final BigDecimal approvedQuantity,
	        final ApprovalCode statusCode) {
		// perform input validation checking
		if (request.getQuantity().compareTo(approvedQuantity) < 0)
			throw new IllegalArgumentException(String.format("Request quantity %s is less than approved quantity %s",
			        request.getQuantity(), approvedQuantity));
		this.request = request;
		this.approvedQuantity = approvedQuantity;
		this.statusCode = statusCode;
	}

	/**
	 * Get approval request
	 * 
	 * @return approval request
	 */
	public ApprovalRequest getRequest() {
		return request;
	}

	/**
	 * Get approved quantity
	 * 
	 * @return approved quantity
	 */
	public BigDecimal getApprovedQuantity() {
		return approvedQuantity;
	}
	
	/**
	 * Get approved quantity
	 * 
	 * @return approved quantity
	 */
	public ApprovalCode getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns a string representing the data in this sequence. 	
	 * 
	 * @return  a string representation of this sequence of characters.
	 */	
	@Override
	public String toString() {
		return new StringBuilder("Approval Response: [ ").append(request).append("\tApproved Quantity: ")
		        .append(approvedQuantity).append("\tStatus Code: ").append(statusCode).append(" ]").toString();

	}
}