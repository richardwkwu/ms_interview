package com.morganstanley.stocklending.approval;

/**
 * Approval Service Type is type of abstract factory
 * 
 * Assumption: for this assignment, I defined 2 services namely decision service
 * and response service
 * 
 * <li>{@link #DECISION_SERVICE}</li>
 * <li>{@link #RESPONSE_SERVICE}</li>
 * 
 * @author Richard Wu
 */
public enum ApprovalServiceType {
	DECISION_SERVICE, RESPONSE_SERVICE
}