package com.morganstanley.stocklending.approval;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The StockLendingApprovalSource class is the class to implement ApprovalSource
 * interface and provides implementation of interface methods getNextApproval()
 * and getApprovalBatch().
 * 
 * @author Richard WU
 */
public class StockLendingApprovalSource implements ApprovalSource {
	/**
	 * Java logger for StockLendingResponseService
	 */
	private static final Logger LOGGER = Logger.getLogger(StockLendingApprovalSource.class.getName());

	/**
	 * The blocking queue to take the approval request
	 */
	private BlockingQueue<ApprovalRequest> sourceQueue;

	/**
	 * Initial capacity of the blocking queue
	 */
	private static final int INIT_CAPACITY = 200;

	/**
	 * Constructs a StockLendingApprovalSource
	 */
	public StockLendingApprovalSource() {
		// using linked blocking queue
		sourceQueue = new LinkedBlockingQueue<ApprovalRequest>(INIT_CAPACITY);
	}

	/**
	 * Get the next approval request. This will block until an approval request
	 * arrives.
	 * 
	 * This method implements ApprovalSource getNextApproval()
	 * 
	 * Assumption: I assume candidate cannot change the getNextApproval()
	 * declaration to throw InterruptedException. Hence, I wrap the checked
	 * InterruptedException to unchecked ApprovalSourceException and let the
	 * caller to handle
	 * 
	 * @return approval request from the approval source
	 * @throws ApprovalSourceException
	 *             if exception occurs in approval source
	 */
	@Override
	public ApprovalRequest getNextApproval() {
		try {
			return sourceQueue.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			// re-throw a new ApprovalSourceException that takes the cause of
			// the underlying interrupted exception
			LOGGER.log(Level.SEVERE, "Approval Source Queue is interrupted", e);
			throw new ApprovalSourceException("Approval Source Queue is interrupted", e);
		}
	}

	/**
	 * Get any queued approval requests (up to <code>maxSize</code>). This will
	 * return immediately even if there are no waiting approval requests.
	 *
	 * @param maxSize
	 *            maximum number of approvals requests to receive.
	 * @throws IllegalArgumentException
	 *             if maxSize is not greater than zero
	 * @throws ApprovalSourceException
	 *             if exception occurs in approval source
	 */
	@Override
	public List<ApprovalRequest> getApprovalBatch(int maxSize) {
		if (maxSize <= 0)
			throw new IllegalArgumentException(
			        "The argument \"maxSize\" should be greater than zero. Current value: " + maxSize);

		// if maxSize maybe is a huge number, it doesn't make sense to allocate
		// a huge memory on array list. Hence just allocate the current queue
		// length, although it is just a good approximation (the actual queue
		// length may be longer because request can put in the queue
		int queueSize = sourceQueue.size();
		List<ApprovalRequest> retList = new ArrayList<ApprovalRequest>(queueSize > maxSize ? maxSize : queueSize);

		try {
			// directly drain approval request to list from the blocking queue
			sourceQueue.drainTo(retList, maxSize);
		} catch (UnsupportedOperationException | ClassCastException | NullPointerException
		        | IllegalArgumentException e) {
			LOGGER.log(Level.SEVERE, "Approval Source Queue: drainTo() failed", e);
			throw new ApprovalSourceException("Approval Source Queue: drainTo() failed", e);
		}

		return retList;
	}

	/**
	 * Connects the approval source to the simulator. Simulator generates the
	 * approval requests and puts in the blocking queue
	 *
	 * @param simulator
	 *            The simulator generates the request and puts in the blocking
	 *            queue
	 */
	public void connect(ApprovalRequestSourceSimulator simulator) {
		try {
			simulator.setQueue(sourceQueue);
		} catch (IllegalStateException e) {
			LOGGER.log(Level.SEVERE, "Cannot connect source queue to the simulator", e);
			throw new ApprovalSourceException("Cannot connect source queue to the simulator", e);
		}

		try {
			simulator.start();
		} catch (IllegalThreadStateException e) {
			LOGGER.log(Level.SEVERE, "Simulator starting failed", e);
			throw new ApprovalSourceException("Simulator starting failed", e);
		}
	}

	/**
	 * Disconnect the approval source from the simulator.
	 *
	 * @param simulator
	 *            The simulator generates the request and puts in the blocking
	 *            queue
	 */
	public void disconnect(ApprovalRequestSourceSimulator simulator) {
		simulator.shutdown();
	}
}
