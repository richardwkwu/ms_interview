package com.morganstanley.stocklending.approval.test;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.morganstanley.stocklending.approval.ApprovalRequest;

public class ApprovalRequestTest {
	ApprovalRequest request;

	@Test
	public void test() {
		try {
			request = new ApprovalRequest(null, "HSBC00005", BigDecimal.TEN);
			fail("constructor must throw exception");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			request = new ApprovalRequest("UBS", null, BigDecimal.TEN);
			fail("constructor must throw exception");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			request = new ApprovalRequest("UBS", "HSBC00005", BigDecimal.ZERO);
			fail("constructor must throw exception");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		System.out.println("ApprovalRequestTest done..");
	}
}
