package com.morganstanley.stocklending.approval.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.morganstanley.stocklending.approval.ApprovalRequest;
import com.morganstanley.stocklending.approval.ApprovalRequestSourceSimulator;
import com.morganstanley.stocklending.approval.StockLendingApprovalSource;

public class StockLendingApprovalSourceTest {
	StockLendingApprovalSource approvalSource;
	
	@Test
	public void test() {
		approvalSource = new StockLendingApprovalSource();
		ApprovalRequestSourceSimulator simulator = new ApprovalRequestSourceSimulator(10);
		
		approvalSource.connect(simulator);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		approvalSource.disconnect(simulator);

		//get single approval request
		ApprovalRequest request = approvalSource.getNextApproval();
		assertNotNull(request);
		System.out.println("getNextApproval():\n" + request);

		//get patch approval request
		List<ApprovalRequest> requestlist = approvalSource.getApprovalBatch(5);
		assertNotNull(requestlist);
		assert(requestlist.size()<= 5);
		System.out.println("getApprovalBatch(): ");
		requestlist.forEach(System.out::println);
		
		// try to get negative size
		try {
			//maxSize should be positive
			approvalSource.getApprovalBatch(0);
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		System.out.println("ApprovalServiceFactoryTest done..");
	}
}
