package com.morganstanley.stocklending.approval;

/**
 * ApprovalServiceFactory implements the abstract factory pattern to create the
 * approval service factory.
 * 
 * Assumption: for this assignment, it will generate
 * ApprovalDecisionServiceFactory and ApprovalResponseServiceFactory
 * 
 */
public class ApprovalServiceFactory {
	/**
	 * Get the ApprovalServiceFactory according to the ApprovalServiceType
	 * approval service factory()
	 * 
	 * @param approvalServiceType
	 *            approval service type
	 */
	public static AbstractApprovalServiceFactory getFactory(ApprovalServiceType approvalServiceType) {
		switch (approvalServiceType) {
		case DECISION_SERVICE:
			return new ApprovalDecisionServiceFactory();
		case RESPONSE_SERVICE:
			return new ApprovalResponseServiceFactory();
		default:
			return null;
		}
	}
}
