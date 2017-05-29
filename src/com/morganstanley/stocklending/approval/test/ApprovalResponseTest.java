package com.morganstanley.stocklending.approval.test;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.morganstanley.stocklending.approval.ApprovalCode;
import com.morganstanley.stocklending.approval.ApprovalRequest;
import com.morganstanley.stocklending.approval.ApprovalResponse;

public class ApprovalResponseTest {
	ApprovalRequest request;
	ApprovalResponse response;
	
	@Before
	public void setUp() {
		request = new ApprovalRequest("UBS", "HSBC00005", BigDecimal.TEN);
	}
	
	@Test
	public void test() {
		try {
			//approved quantity > than request quantity
			response = new ApprovalResponse(request, new BigDecimal(20), ApprovalCode.APPROVED);
			fail("constructor must throw exception");
		}
		catch(IllegalArgumentException e) {
			assertNotNull(e);
		}
		System.out.println("ApprovalRequestTest done..");
	}
}
