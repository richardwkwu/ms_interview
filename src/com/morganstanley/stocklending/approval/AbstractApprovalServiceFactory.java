package com.morganstanley.stocklending.approval;
/*a new comment*/
/*master change comment*/
/**
 * AbstractApprovalServiceFactory is an abstract class to get factories for
 * ApprovalDecisionService and ApprovalResponseService
 * 
 * ApprovalServiceFactory classes extending AbstractApprovalServiceFactory to
 * generate object of concrete class based on given service name.
 * 
 * @author Richard Wu
 */
public abstract class AbstractApprovalServiceFactory {
	/**
	 * Get ApprovalDecisionService
	 * 
	 * @param approvalDecisionServiceName
	 *            service name
	 * @return ApprovalDecisionService
	 */
	abstract public ApprovalDecisionService getApprovalDecisionService(String approvalDecisionServiceName);

	/**
	 * Get ApprovalResponseService
	 * 
	 * @param approvalDecisionServiceName
	 *            service name
	 * @return ApprovalResponseService
	 */
	abstract public ApprovalResponseService getApprovalResponseService(String approvalResponseServiceName);
}
