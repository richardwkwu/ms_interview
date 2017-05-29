package com.morganstanley.stocklending.approval;

import java.util.List;

/**
 * ApprovalResponseService is to process the approval request coming from ApprovalDecisionService
 * 
 * Assumption: Candidate does not allow to change this interface
 *
 */
public interface ApprovalResponseService {
	void sendResponse(ApprovalResponse response);

	void sendResponses(List<ApprovalResponse> responses);
}