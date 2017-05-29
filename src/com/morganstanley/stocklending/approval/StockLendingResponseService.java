package com.morganstanley.stocklending.approval;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The StockLendingResponseService class is the class to implement
 * ApprovalResponseService interface and provides implementation of
 * interface methods sendResponse() and sendResponses().
 * 
 * Assumption: When sending responses in batch, the average processing
 * time is less than sending individually. In my example, sending 
 * one approval response takes a minimum of 50 ms but sending multiple
 * responses (at least 2) takes a minimum of 70 ms.
 * 
 * @author  Richard WU
 */
public class StockLendingResponseService implements ApprovalResponseService {
	/**
	 * Java logger for StockLendingResponseService
	 */
	private static final Logger LOGGER = Logger.getLogger(StockLendingResponseService.class.getName());
	/**
	 * Service Name of StockLendingResponseService 
	 */
	public static final String SERVICE_NAME = "STOCK_LENDING";
	
	/**
	 * Simulate sending approval response to the back end by waiting a random
	 * period of time and performing logging for demonstration purpose.
	 * 
	 * @param response approval response
	 */
	@Override
	public void sendResponse(ApprovalResponse response) {
		LOGGER.info("Single Approval Response Processing:\n" + response);
		try {
			Thread.sleep(15+(long)(Math.random() * 10));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.log(Level.SEVERE, "Exception occur", e);
		}
	}

	/**
	 * Simulate sending batch of approval response to the back end 
	 * by waiting a random period of time and performing logging 
	 * for demonstration purpose.
	 * 
	 * @param responses list of approval response
	 */
	@Override
	public void sendResponses(List<ApprovalResponse> responses) {
		LOGGER.info("Batch Approval Response Processing:\n" + responses.stream().map(x->x.toString()).collect(Collectors.joining("\n")));
		try {
			Thread.sleep(12*responses.size()+(long)(Math.random() * 20));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.log(Level.SEVERE, "Exception occur", e);
		}
	}
}
