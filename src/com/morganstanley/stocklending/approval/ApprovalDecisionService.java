package com.morganstanley.stocklending.approval;

/**
 * ApprovalDecisionService is an interface that takes the approval requests and
 * return approval response instruction
 * 
 * Assumption: Candidate does not allow to change this interface
 *
 */
public interface ApprovalDecisionService {
	ApprovalResponse processRequest(ApprovalRequest request);
}