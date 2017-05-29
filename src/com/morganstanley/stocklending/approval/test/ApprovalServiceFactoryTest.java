package com.morganstanley.stocklending.approval.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.morganstanley.stocklending.approval.AbstractApprovalServiceFactory;
import com.morganstanley.stocklending.approval.ApprovalDecisionService;
import com.morganstanley.stocklending.approval.ApprovalResponseService;
import com.morganstanley.stocklending.approval.ApprovalServiceFactory;
import com.morganstanley.stocklending.approval.ApprovalServiceType;
import com.morganstanley.stocklending.approval.StockLendingDecisionService;
import com.morganstanley.stocklending.approval.StockLendingResponseService;

public class ApprovalServiceFactoryTest {
	AbstractApprovalServiceFactory decisionServiceFactory;
	ApprovalDecisionService decisionService;
	ApprovalDecisionService badDecisionService;
	AbstractApprovalServiceFactory responseServiceFactory;
	ApprovalResponseService responseService;
	ApprovalResponseService badResponseService;
	
	@Test
	public void test() {
		decisionServiceFactory = ApprovalServiceFactory.getFactory(ApprovalServiceType.DECISION_SERVICE);

		// get the concrete ApprovalDecisionService through DecisionServiceFactory
		decisionService = decisionServiceFactory.getApprovalDecisionService("STOCK_LENDING");
		assertNotNull(decisionService);
		assertTrue(decisionService instanceof StockLendingDecisionService);

		badDecisionService = decisionServiceFactory.getApprovalDecisionService("NOT_EXIST");
		assertNull(badDecisionService);

		badResponseService = decisionServiceFactory.getApprovalResponseService("ANY");
		assertNull(badResponseService);
		
		responseServiceFactory = ApprovalServiceFactory.getFactory(ApprovalServiceType.RESPONSE_SERVICE);

		responseService = responseServiceFactory.getApprovalResponseService("STOCK_LENDING");
		assertTrue(responseService instanceof StockLendingResponseService);
		
		badResponseService = responseServiceFactory.getApprovalResponseService("NOT_EXIST");
		assertNull(badResponseService);
		
		badDecisionService = responseServiceFactory.getApprovalDecisionService("ANY");
		assertNull(badResponseService);
		
		System.out.println("ApprovalServiceFactoryTest done..");
	}

}
