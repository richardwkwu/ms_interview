package com.morganstanley.stocklending.approval.test;

import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;

import com.morganstanley.stocklending.approval.ApprovalRequest;
import com.morganstanley.stocklending.approval.ApprovalRequestSourceSimulator;

public class ApprovalRequestSourceSimulatorTest {
	ApprovalRequestSourceSimulator simulator ; 
	
	@Before
	public void setUp() {
		simulator = new ApprovalRequestSourceSimulator(3);
		
	}
	@Test
	public void test() {
		BlockingQueue<ApprovalRequest> queue = new LinkedBlockingQueue<ApprovalRequest>(10);
		simulator.setQueue(queue);
		simulator.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		//when the simulator is running, user cannot set queue
		try {
			assertTrue(simulator.isRunning());
			simulator.setQueue(new LinkedBlockingQueue<ApprovalRequest>(2));
			fail("setQueue must throw exception when simulator is running");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		}
		
		simulator.shutdown();
		//queue should have some requests being generated
		assertTrue(queue.size() > 0);
		System.out.println("Simulator has generated " + queue.size() + " request(s)" );
		queue.forEach(System.out::println);
		
		System.out.println("ApprovalRequestSourceSimulatorTest done..");
	}
}
