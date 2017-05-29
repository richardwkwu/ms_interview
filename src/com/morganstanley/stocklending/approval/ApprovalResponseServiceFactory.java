package com.morganstanley.stocklending.approval;

/**
 * ApprovalResponseServiceFactory is the factory pattern to generate concrete class that
 * implements ApprovalResponseService. 
 * 
 * Assumption: In this assignment it will create an instance of StockLendingResponseService
 * 
 * @author Richard Wu
 */
public class ApprovalResponseServiceFactory extends AbstractApprovalServiceFactory{

	@Override
	public ApprovalDecisionService getApprovalDecisionService(String approvalDecisionServiceName) {
		return null;
	}

	@Override
	/**
	 * Get the class instance that implements ApprovalResponseService interface  
	 * 
	 * @param approvalResponseServiceName service name
	 */
	public ApprovalResponseService getApprovalResponseService(String approvalResponseServiceName) {
		if (approvalResponseServiceName == null)
			throw new IllegalArgumentException("Argument \"approvalResponseServiceName\" is null");

		switch(approvalResponseServiceName) {
		case StockLendingResponseService.SERVICE_NAME:
			return new StockLendingResponseService();
		/*
		 * please add other types of concrete ApprovalDescisionService here
		 */
		default:
			return null;
		}
	}
}
