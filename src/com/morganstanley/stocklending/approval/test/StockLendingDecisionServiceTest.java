package com.morganstanley.stocklending.approval.test;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.morganstanley.stocklending.approval.ApprovalCode;
import com.morganstanley.stocklending.approval.ApprovalDecisionService;
import com.morganstanley.stocklending.approval.ApprovalRequest;
import com.morganstanley.stocklending.approval.ApprovalResponse;
import com.morganstanley.stocklending.approval.StockLendingDecisionService;

public class StockLendingDecisionServiceTest {
	ApprovalDecisionService decisionService;
	
	@Test
	public void test() {
		decisionService = new StockLendingDecisionService();
		
		ApprovalRequest request = new ApprovalRequest("JPM", "HSBC00005", BigDecimal.TEN);
		ApprovalResponse response = decisionService.processRequest(request);
		assertNotNull(response);
		assertEquals(response.getStatusCode(), ApprovalCode.APPROVED);
		
		try {
		    response = decisionService.processRequest(null);
		    
		}
		catch(IllegalArgumentException e)
		{
		    assertNotNull(e);
		}
	}

}
