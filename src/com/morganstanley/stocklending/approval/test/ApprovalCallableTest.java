package com.morganstanley.stocklending.approval.test;

import static org.junit.Assert.*;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.morganstanley.stocklending.approval.ApprovalCallable;
import com.morganstanley.stocklending.approval.ApprovalCode;
import com.morganstanley.stocklending.approval.ApprovalRequest;
import com.morganstanley.stocklending.approval.ApprovalResponse;

public class ApprovalCallableTest {
	ApprovalCallable<ApprovalRequest, ApprovalResponse> callable;

	@Before
	public void setUp() {
		ApprovalRequest request = new ApprovalRequest("UBS", "HSBC00005", BigDecimal.TEN);
		callable = new ApprovalCallable<>(r -> new ApprovalResponse(r, r.getQuantity(), ApprovalCode.APPROVED),
		        request);
	}

	@Test
	public void test() {
		try {
			ApprovalResponse result = callable.call();
			assertEquals(result.getApprovedQuantity(), BigDecimal.TEN);
			assertEquals(result.getStatusCode(), ApprovalCode.APPROVED);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		System.out.println("ApprovalCallableTest done..");
	}
}
