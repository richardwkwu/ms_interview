package com.morganstanley.stocklending.approval;

/**
 * ApprovalDecisionServiceFactory is the factory to instantiate concrete class
 * that implements AbstractApprovalService.
 * 
 * Assumption: In this assignment it will create an instance of
 * StockLendingDecisionService
 * 
 * @author Richard Wu
 */
public class ApprovalDecisionServiceFactory extends AbstractApprovalServiceFactory {
	/**
	 * Get the class instance that implements ApprovalDecisionService interface
	 * 
	 * @param approvalResponseServiceName
	 *            service name
	 */
	@Override
	public ApprovalDecisionService getApprovalDecisionService(String approvalDecisionServiceName) {
		if (approvalDecisionServiceName == null)
			throw new IllegalArgumentException("Argument \"decisionServiceName\" is null");

		switch (approvalDecisionServiceName) {
		case StockLendingDecisionService.SERVICE_NAME:
			return new StockLendingDecisionService();
		/*
		 * please add other types of concrete ApprovalDescisionService here
		 */
		default:
			return null;
		}
	}

	@Override
	public ApprovalResponseService getApprovalResponseService(String approvalResponseServiceName) {
		return null;
	}
}
