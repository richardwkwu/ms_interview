package com.morganstanley.stocklending.approval;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ApprovalTaskPicker implements the runnable interface which i) takes
 * source object (generic type T) from 'source' completion service ii) creates a
 * ApprovalCallable takes a Function<> which applies singleProcessingfunc() or
 * batchProcessingFunc() on source object (generic type T or List<T>) and
 * returns a result object (generic type R) iii) submits the ApprovalCallable to
 * the 'sink' completion service and then executor executes the callable.
 * 
 * Assumption: ApprovalTaskPicker applies an optimization technique such that it
 * will try to group multiple requests into a single callable (if multiple
 * source objects are already available (non-blocking)) and then invoke the
 * 'batchProcessingFunc' functional interface (if batchProcessingFunc is not
 * null).
 * 
 * Here I assume 'batchProcessingFunc' having a property that the average
 * processing time is less than sending individually through
 * 'singleProcessingfunc'. If it is not the case, caller can input
 * 'batchProcessingFunc' as null and force the ApprovalTaskPicker to process the
 * source object one by one (i.e. 'singleProcessingfunc')
 * 
 *                    (source object)
 *              .-----------------------. 
 *              |  ApprovalTaskPicker   |
 *              |                       v 
 *    .------------.                .-------------.
 *    |	  source   |                | destination |
 *    | completion |                | completion  | singleProcessingfunc T->R  
 *    |  service   |                |  service    | batchProcessingFunc List<T>->R
 *    |            |                |             |
 *    '------------'                '-------------'
 * If necessary, in fact user can chain multiple ApprovalTaskPicker instances 
 * together and form a multiple steps of approval workflow.
 * 
 *              .------------.      .------------.      .-----...--------------.   
 *              |  Approval  |      |  Approval  |      |            Approval  |
 *              |  Task      |      |  Task      |      |            Task      |      
 *              |  Picker1   |      |  Picker2   |      |            Picker n  |
 *              |            v      |            v      |                      v
 *    .------------.      .------------.      .------------.        .------------.        
 *    |	           |      |            |      |            |        |            |
 *    | completion |      | completion |      | completion |        | completion |  
 *    |  service 1 |      |  service 2 |      |  service 3 |  ...   |  service n |    
 *    |            |      |            |      |            |        |            |
 *    '------------'      '------------'      '------------'        '------------' 
 * 
 * @author Richard WU
 */
public class ApprovalTaskPicker<T, R> implements Runnable {
	/**
	 * Java logger for ApprovalTaskPicker
	 */
	private static final Logger LOGGER = Logger.getLogger(ApprovalTaskPicker.class.getName());

	/**
	 * Default batch size is default number of source objects that
	 * ApprovalTaskPicker can hold before sending to sink completion service
	 */
	public static final int DEFAULT_BATCH_SIZE = 4;

	/**
	 * Source Completion Service
	 */
	private final CompletionService<T> source;

	/**
	 * Sink Completion Service
	 */
	private final CompletionService<R> sink;

	/**
	 * Single processing function that accepts one object and produces a result.
	 */
	private final Function<T, R> singleProcessingfunc;

	/**
	 * Batch processing function that accepts a list of object and produces a
	 * result.
	 */
	private final Function<List<T>, R> batchProcessingFunc;

	/**
	 * Batch size is number of source objects that ApprovalTaskPicker can hold
	 * before sending to sink completion service
	 */
	private final int batchSize;

	/**
	 * Constructs an approval task picker
	 * 
	 * @param source
	 *            source completion service
	 * @param sink
	 *            sink completion service
	 * @param singleProcessingfunc
	 *            a function that accepts one object and produces a result
	 * @param batchProcessingFunc
	 *            a function that accepts a list of object and produces a result
	 */
	public ApprovalTaskPicker(final CompletionService<T> source, 
							  final CompletionService<R> sink,
							  final Function<T, R> singleProcessingfunc,
							  final Function<List<T>, R> batchProcessingFunc) {
		this(source, sink, singleProcessingfunc, batchProcessingFunc, DEFAULT_BATCH_SIZE);
	}

	/**
	 * Constructs an approval task picker
	 * 
	 * @param source
	 *            source completion service
	 * @param sink
	 *            sink completion service
	 * @param singleProcessingfunc
	 *            a function that accepts one object and produces a result
	 * @param batchProcessingFunc
	 *            a function that accepts a list of object and produces a result
	 * @param batchSize
	 *            the number of object that ApprovalTaskPicker can hold before
	 *            sending to sink completion service
	 */
	public ApprovalTaskPicker(final CompletionService<T> source, 
							  final CompletionService<R> sink,
							  final Function<T, R> singleProcessingfunc,
							  final Function<List<T>, R> batchProcessingFunc,
							  final int batchSize) {
		if (singleProcessingfunc == null && batchProcessingFunc == null) {
			throw new IllegalArgumentException("Either singleProcessingfunc or batchProcessingFunc must be specified");
		}
		this.source = source;
		this.sink = sink;
		this.singleProcessingfunc = singleProcessingfunc;
		this.batchProcessingFunc = batchProcessingFunc;
		this.batchSize = batchSize;
	}

	/**
	 * Implements the Runnable run() method and executes the following tasks i)
	 * takes source object (generic type T) from 'source' completion service ii)
	 * create a callable takes a functional interface which applies
	 * singleProcessingfunc() or batchProcessingFunc() on source object (generic
	 * type T or List<T>) and returns a result object (generic type R) iii)
	 * submit the callable to the 'sink' completion service and then executor
	 * execute the callable.
	 */
	@Override
	public void run() {
		LOGGER.info("Start running on " + Thread.currentThread().getName());
		while (!Thread.currentThread().isInterrupted()) {
			List<T> taskList = new ArrayList<T>(batchSize);
			Future<T> future;
			try {
				// support singleProcessingfunc only
				if (batchProcessingFunc == null) {
					sink.submit(new ApprovalCallable<T, R>(singleProcessingfunc, source.take().get()));
				} else {
					// support batchProcessingfunc
					//
					// i) First poll the source. If there is a task from the
					// source immediately, queue it and then continue next poll
					// ii) If the poll returns null, it means there is no task
					// from the source completion service at this moment. If the
					// array list is not empty, then just send the task list
					// to the sink completion service. Otherwise if the array
					// list is still empty, it waits on the blocking call and
					// wait for next task to come to source completion service.
					while (taskList.size() < batchSize) {
						future = source.poll();
						if (future == null) {
							if (taskList.size() > 0) {
								break;
							} else {
								future = source.take();
								// NOTE: ExecutorCompletionService guarantee
								// that the future.get() must have completed
								// result if future is returned from take()
								taskList.add(future.get());
							}
						} else {
							// NOTE: ExecutorCompletionService guarantee that
							// the future.get() must have completed result if
							// future is returned from take()
							taskList.add(future.get());
						}
					}

					assert taskList.size() > 0;

					// NOTE: ExecutorCompletionService guarantee that the
					// future.get() must have completed result
					Callable<R> approvalCallable;
					if (taskList.size() == 1 && singleProcessingfunc != null) {
						approvalCallable = new ApprovalCallable<T, R>(singleProcessingfunc, taskList.get(0));
					} else {
						approvalCallable = new ApprovalCallable<List<T>, R>(batchProcessingFunc, taskList);
					}

					sink.submit(approvalCallable);
				}
			} catch (InterruptedException e) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
				LOGGER.log(Level.SEVERE, "Interrupted Exception", e);
			} catch (RejectedExecutionException e) {
				// ExecutorCompletionService rejected the callable task.
				LOGGER.log(Level.SEVERE, "ApprovalCallable is rejected when submitting to CompletionService", e);
			} catch (ExecutionException e) {
				// ExecutorCompletionService throw ExecutionException when
				// executing the callable task.
				LOGGER.log(Level.SEVERE, "Execution Exception on the CompletionService", e);
			}
		}
		LOGGER.info("End running on " + Thread.currentThread().getName());
	}
}
