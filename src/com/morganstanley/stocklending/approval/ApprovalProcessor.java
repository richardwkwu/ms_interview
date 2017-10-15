package com.morganstanley.stocklending.approval;

import java.util.Scanner;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Response Service Status
 * 
 * <li>{@link #SUCCEED}</li>
 * <li>{@link #FAILED}</li>
 * 
 * @author Richard Wu
 *
 * change line 1
 * change line 2
 */
enum ResponseServiceStatus {
	SUCCEED, FAILED
}

/**
 * Class ApprovalProcessor that takes instances of the interfaces
 * (ApprovalSource, ApprovalDecisionService, ApprovalResponseService).
 * 
 * ApprovalProcessor controls the flow of approval request taken from the
 * ApprovalSource, passing to ApprovalDecisionService which will return an
 * ApprovalResponse, and finally every ApprovalResponse must be passed on to the
 * ApprovalResponseService
 *
 * Change here
 */
public class ApprovalProcessor {
	/**
	 * Java logger for ApprovalProcessor
	 */
	private static final Logger LOGGER = Logger.getLogger(ApprovalProcessor.class.getName());

	/**
	 * Minimum executor thread pool size
	 */
	private static final int MIN_THREAD_POOL_SIZE = 4;

	/**
	 * Maximum number of request from approval source through getApprovalBatch()
	 */
	private static final int DEFAULT_SOURCE_MAX_SIZE = 5;

	/**
	 * Maximum number of approval responses can be in a batch to response
	 * service
	 */
	private static final int DEFAULT_RESPONSE_SIZE = 4;
	/*
	 * the approval source interface which provides approval requests
	 */
	private final ApprovalSource source;

	/*
	 * ApprovalRequest must be passed to the ApprovalDecisionService which will
	 * return an ApprovalResponse.
	 */
	private final ApprovalDecisionService decisionService;

	/*
	 * ApprovalResponseService is the service to process approval response
	 */
	private final ApprovalResponseService responseService;

	/**
	 * Constructs a approval processor string builder with
	 * 
	 * @param source
	 *            approval source
	 * @param decisionService
	 *            approval decision service
	 * @param responseService
	 *            approval response service
	 */
	public ApprovalProcessor(final ApprovalSource source,
							 final ApprovalDecisionService decisionService,
							 final ApprovalResponseService responseService) {
		this.source = source;
		this.decisionService = decisionService;
		this.responseService = responseService;
	}

	/**
	 * Start the Approval Processor
	 */
	public void run() {
		//
		// ******************************
		// ENTRY POINT FOR YOUR CODE HERE
		// ******************************

		int numberOfProcessor = Runtime.getRuntime().availableProcessors();
		int executorThreadPoolSize = Math.max(numberOfProcessor, MIN_THREAD_POOL_SIZE);
		LOGGER.info("Approval Processor started..number of processors(cores) " + numberOfProcessor);

		// Initialize the multi-thread decisionCompletionService
		// (ExecutorCompletionService)
		ExecutorService decisionServiceES = Executors.newFixedThreadPool(executorThreadPoolSize);
		CompletionService<ApprovalResponse> decisionCompletionService = new ExecutorCompletionService<ApprovalResponse>(
		        decisionServiceES);

		// Initialize the multi-thread responseCompletionService
		// (ExecutorCompletionService)
		ExecutorService responseServiceES = Executors.newFixedThreadPool(executorThreadPoolSize);
		CompletionService<ResponseServiceStatus> responseCompletionService = new ExecutorCompletionService<ResponseServiceStatus>(
		        responseServiceES);

		Thread processRequestThread = null;
		Thread processResponseThread = null;
		try {
			// Initialize the processRequestRunnable and processRequestThread
			// which is
			// i) takes approval request from source
			// ii) invoke decisionService.processRequest() for each request on
			// the decisionCompletionService
			ApprovalSourcePicker processRequestRunnable = new ApprovalSourcePicker(source, decisionCompletionService,
			        req -> decisionService.processRequest(req), DEFAULT_SOURCE_MAX_SIZE);

			processRequestThread = new Thread(processRequestRunnable, "Process-Request-Thread");

			// Initialize the processResponseRunnable and processResponseThread
			// which is
			// i) takes approval response from decisionCompletionService
			// ii) invoke responseService.sendResponse(s) for each response on
			// the responseCompletionService
			ApprovalTaskPicker<ApprovalResponse, ResponseServiceStatus> processResponseRunnable = new ApprovalTaskPicker<>(
			        decisionCompletionService, responseCompletionService,
			        // process single approval response
			        singleResp -> {
				        try {
					        responseService.sendResponse(singleResp);
					        return ResponseServiceStatus.SUCCEED;
				        } catch (Exception e) {
					        LOGGER.log(Level.SEVERE, "Exception occur", e);
					        return ResponseServiceStatus.FAILED;
				        }
			        },
			        // process batch of approval responses
			        batchResp -> {
				        try {
					        responseService.sendResponses(batchResp);
					        return ResponseServiceStatus.SUCCEED;
				        } catch (Exception e) {
					        LOGGER.log(Level.SEVERE, "Exception occur", e);
					        return ResponseServiceStatus.FAILED;
				        }
			        }, DEFAULT_RESPONSE_SIZE);

			processResponseThread = new Thread(processResponseRunnable, "Process-Response-Thread");

			// start the processRequestThread and processResponseThread
			processRequestThread.start();
			processResponseThread.start();

			// waiting for "exit" command and stop the approval processor
			handleConsoleCommand();

			// start preparing shutdown gracefully (see the finally block)
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
		} finally {
			// gracefully shutdown the processRequestThread and
			// processResponseThread
			// a) stop the processRequestThread;
			if (processRequestThread != null) {
				shutdownThread(processRequestThread);
				try {
					processRequestThread.join();
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, "Interrupted", e);
				}
				LOGGER.info(processRequestThread.getName() + "'s Runnable is stopped");
			}

			// b) shutdown the decision service executor
			decisionServiceES.shutdown();
			// waiting all decisionService tasks are completed
			try {
				decisionServiceES.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, "Interrupted", e);
			}
			LOGGER.info("Decision Service ExecutorService has been shutdown");

			// c) stop the processResponseThread;
			if (processResponseThread != null) {
				shutdownThread(processResponseThread);
				try {
					processResponseThread.join();
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, "Interrupted", e);
				}
				LOGGER.info(processRequestThread.getName() + "'s Runnable is stopped");
			}

			// d) shutdown the decision service executor
			responseServiceES.shutdown();
			// waiting all responseService tasks are completed
			try {
				responseServiceES.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, "Interrupted", e);
			}
			LOGGER.info("Response Service ExecutorService has been shutdown");
		}
	}

	/**
	 * Shutdown the processing thread gracefully by interrupting the thread
	 * through Thread.interrupt(). The processing thread must be able to
	 * handling InterruptedException in a proper way and return from its
	 * internal Runnable.run().
	 *
	 * @param thread
	 *            the processing thread to be interrupted
	 * @return void
	 */
	private void shutdownThread(final Thread thread) {
		LOGGER.info("Prepare to shutdown " + thread.getName() + " by sending interrupt signal");
		thread.interrupt();
	}

	/**
	 * Handle console command
	 * 
	 * This is a blocking call which is waiting for the exit command (i.e. "e",
	 * "x", or "exit") from the console
	 */
	private void handleConsoleCommand() {
		// Scanner supports AutoCloseable()
		try (Scanner inx = new Scanner(System.in)) {
			String inputString;
			boolean isExit = false;
			while (!isExit) {
				inputString = inx.nextLine();
				String[] tokens = inputString.split(" ");
				for (String token : tokens) {
					switch (token) {
					case "e":
					case "x":
					case "exit":
						isExit = true;
						break;
					default:
					}
					if (isExit)
						break;
				}
			}
			LOGGER.info("Caught 'exit' command from console.");
		}
	}
}
