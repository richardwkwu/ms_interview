package com.morganstanley.stocklending.approval;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ApprovalSourcePicker implements the runnable interface which i) takes
 * approval request from ApprovalSource interface ii) creates a ApprovalCallable
 * which takes a Function<ApprovalRequest, ApprovalResponse> parameter which
 * applies approval processing function on an approval request and returns an
 * approval response iii) submits the ApprovalCallable to the 'sink' completion
 * service and then executor executes the callable.
 * 
 * @author Richard WU
 */
public class ApprovalSourcePicker implements Runnable {
	/**
	 * Java logger for ApprovalSourcePicker
	 */
	private static final Logger LOGGER = Logger.getLogger(ApprovalSourcePicker.class.getName());

	/**
	 * Default batch size is the default maximum number of approval requests
	 * that ApprovalSourcePicker can get from from approval source in a batch
	 */
	public static final int BATCH_SIZE = 4;

	/**
	 * Approval request source
	 */
	private final ApprovalSource source;

	/**
	 * Sink Completion Service
	 */
	private final CompletionService<ApprovalResponse> sink;
	/**
	 * Approval processing function that accepts one approval request and and
	 * produces an approval response.
	 */
	private final Function<ApprovalRequest, ApprovalResponse> approvalProcessingFunc;

	/**
	 * Batch size is the maximum number of approval requests that
	 * ApprovalSourcePicker can get from approval source in a batch through
	 * getApprovalBatch()
	 */
	private final int batchSize;

	/**
	 * Constructs an approval source picker
	 * 
	 * @param source
	 *            source completion service
	 * @param sink
	 *            sink completion service
	 * @param approvalProcessingFunc
	 *            a function that accepts one approval request and and produces
	 *            an approval response.
	 */
	public ApprovalSourcePicker(final ApprovalSource source, 
								final CompletionService<ApprovalResponse> sink,
								final Function<ApprovalRequest, ApprovalResponse> approvalProcessingFunc) {
		this(source, sink, approvalProcessingFunc, BATCH_SIZE);
	}

	/**
	 * Constructs an approval task picker
	 * 
	 * @param source
	 *            source completion service
	 * @param sink
	 *            sink completion service
	 * @param approvalProcessingFunc
	 *            a function that accepts one approval request and and produces
	 *            an approval response.
	 * @param batchSize
	 *            the maximum number of approval requests that
	 *            ApprovalSourcePicker can get from approval source in a batch
	 *            through getApprovalBatch()
	 */
	public ApprovalSourcePicker(final ApprovalSource source, 
								final CompletionService<ApprovalResponse> sink,
								final Function<ApprovalRequest, ApprovalResponse> approvalProcessingFunc, 
								final int batchSize) {
		this.source = source;
		this.sink = sink;
		this.approvalProcessingFunc = approvalProcessingFunc;
		this.batchSize = batchSize;
	}

	/**
	 * Implements the Runnable run() method and executes the following tasks i)
	 * takes approval request from ApprovalSource interface ii) create a
	 * ApprovalCallable which takes a Function<ApprovalRequest,
	 * ApprovalResponse> parameter which applies approval processing function on
	 * an approval request and returns an approval response iii) submit the
	 * ApprovalCallable to the 'sink' completion service and then executor
	 * executes the callable.
	 */
	@Override
	public void run() {
		LOGGER.info("Start running on " + Thread.currentThread().getName());
		while (!Thread.currentThread().isInterrupted()) {
			try {
				List<ApprovalRequest> requestlist = source.getApprovalBatch(batchSize);
				assert requestlist.size() < batchSize;

				if (requestlist.size() == 0) {
					// if the queue is empty, then wait on a blocking call.
					ApprovalRequest request = source.getNextApproval();
					sink.submit(
					        new ApprovalCallable<ApprovalRequest, ApprovalResponse>(approvalProcessingFunc, request));
				} else {
					for (ApprovalRequest request : requestlist) {
						sink.submit(new ApprovalCallable<ApprovalRequest, ApprovalResponse>(approvalProcessingFunc,
						        request));
					}
				}
			} catch (RejectedExecutionException e) {
				// ExecutorCompletionService rejected the callable task.
				LOGGER.log(Level.SEVERE, "ApprovalCallable is rejected when submitting to CompletionService", e);
			} catch (ApprovalSourceException e) {
				LOGGER.log(Level.SEVERE, "Exception on the approval source", e);
			}
		}
		LOGGER.info("End running on " + Thread.currentThread().getName());
	}
}
