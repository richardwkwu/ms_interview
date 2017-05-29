package com.morganstanley.stocklending.approval;

import java.util.List;

/**
 * Approval Source provides the interfaces the get the approval requests
 * 
 * Assumption: Candidate does not allow to change this interface
 *
 */
public interface ApprovalSource {
	/**
	 * Get the next approval request. This will block until an approval request
	 * arrives.
	 */
	ApprovalRequest getNextApproval();

	/**
	 * Get any queued approval requests (up to <code>maxSize</code>). This will
	 * return immediately even if there are no waiting approval requests.
	 *
	 * @param maxSize
	 *            maximum number of approvals requests to receive.
	 */
	List<ApprovalRequest> getApprovalBatch(int maxSize);
}
