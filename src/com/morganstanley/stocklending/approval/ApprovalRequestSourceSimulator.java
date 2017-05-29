package com.morganstanley.stocklending.approval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * ApprovalRequestSourceSimulator is a simulator that generates a series of
 * approval requests (which follows Poisson distribution) to the approval
 * source.
 * 
 * ApprovalRequestSourceSimulator is mainly used for simulation purpose in this
 * assignment
 * 
 * @author Richard Wu
 */
public class ApprovalRequestSourceSimulator extends Thread {
	/**
	 * Java logger for ApprovalRequestSourceSimulator
	 */
	private static final Logger LOGGER = Logger.getLogger(ApprovalRequestSourceSimulator.class.getName());

	/**
	 * default number of request per second to be generated in the source
	 * simulator
	 */
	public static final int DEFAULT_REQUEST_RATE = 100;

	/**
	 * list of client for simulation purpose
	 */
	private static final String CLIENT_LIST[] = { "JPM", "GS", "UBS", "CLSA" };

	/**
	 * list of security for simulation purpose
	 */
	private static final String SECURITY_LIST[] = { "HSBC00005", "HKEX00388", "TENCENT00700", "CCB00939" };

	/**
	 * list of request quantity for simulation purpose
	 */
	private static final List<BigDecimal> REQUEST_QUANTITY_LIST;

	/**
	 * number of request per second to be generated in the simulator
	 */
	private volatile double requestRate;

	/**
	 * boolean flag controls to exit the while-loop in run()
	 */
	private volatile boolean isExit = false;

	/**
	 * boolean flag indicates that run() has been invoked and running
	 */
	private volatile boolean isRunning = false;

	/**
	 * After the simulator generates the approval request, it put them into the
	 * blocking queue.
	 */
	private BlockingQueue<ApprovalRequest> queue = null;

	/**
	 * rand is used for generating a random number and simulates Poisson
	 * distribution of approval request arrival
	 */
	private final Random rand;

	static {
		// Initialize the REQUEST_QUANTITY_LIST which holds an array of
		// BigDecimal from 2500, 3000... to 7000
		REQUEST_QUANTITY_LIST = new ArrayList<BigDecimal>(10);
		IntStream.range(5, 15).forEach(i -> REQUEST_QUANTITY_LIST.add(new BigDecimal(i * 500)));
	}

	/**
	 * Constructs a ApprovalRequestSourceSimulator
	 */
	public ApprovalRequestSourceSimulator() {
		this(DEFAULT_REQUEST_RATE);
	}

	/**
	 * Constructs a ApprovalRequestSourceSimulator
	 * 
	 * @param requestRate
	 *            number of approval request per second to be generated in the
	 *            simulator
	 */
	public ApprovalRequestSourceSimulator(double requestRate) {
		super("Approval-Request-Simulator-Thread");
		this.setRequestRate(requestRate);
		this.rand = new Random(System.currentTimeMillis());
	}

	/**
	 * Implements the Thread run() method and executes the following tasks i)
	 * pick a random client, security and quantity ii) calculate the inter
	 * arrival time according to Poisson distribution iii) put the requests into
	 * the blocking queue
	 */
	@Override
	public void run() {
		isRunning = true;
		LOGGER.info(new StringBuilder(Thread.currentThread().getName())
		        .append(": Simulator starts generating Approval Requests at the rate of ").append(requestRate)
		        .append(" request/second").toString());

		while (!isExit) {
			// pick a random client, security and quantity by using the random
			// number generator
			String client = CLIENT_LIST[rand.nextInt(CLIENT_LIST.length)];
			String security = SECURITY_LIST[rand.nextInt(SECURITY_LIST.length)];
			BigDecimal requestQuantity = REQUEST_QUANTITY_LIST.get(rand.nextInt(REQUEST_QUANTITY_LIST.size()));

			try {
				// calculate the inter arrival time according to Poisson
				// distribution mean arrival rate (i.e. requestRate)
				double interarrivalTimeMS = Math.log(1.0 - rand.nextDouble()) / -requestRate * 1000;
				long interarrivalTimeMSLong = (long) interarrivalTimeMS;
				// sleep for inter-arrival time period now
				Thread.sleep(interarrivalTimeMSLong, (int) ((interarrivalTimeMS - interarrivalTimeMSLong) * 1000));
				ApprovalRequest request = new ApprovalRequest(client, security, requestQuantity);
				while (!queue.offer(request, 10, TimeUnit.MILLISECONDS) && !isExit) {
					LOGGER.warning("Simulator cannot insert request on the Blocking queue: reason - overflow");
				}
			} catch (InterruptedException e) {
				// InterruptedException would be thrown from Thread.sleep() or
				// BlockingQueue.put()
				Thread.currentThread().interrupt();
				LOGGER.log(Level.SEVERE, "Catch InterruptedException... exit", e);
				isExit = true;
			} catch (ClassCastException | NullPointerException | IllegalArgumentException e) {
				System.out.println(e.getMessage());
				LOGGER.log(Level.SEVERE, "Catch Runtime Exception... exit", e);
				isExit = true;
			}
		}
		isRunning = false;
		LOGGER.info(new StringBuilder(Thread.currentThread().getName())
		        .append(": Simulator stops generating Approval Requests").toString());

	}

	/**
	 * Shutdown the simulator
	 */
	public void shutdown() {
		isExit = true;
		try {
			this.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.log(Level.SEVERE, "Interrupted Exception", e);
		}
	}

	/**
	 * Get current approval request rate
	 * 
	 * @return approval request rate
	 */
	public double getRequestRate() {
		return requestRate;
	}

	/**
	 * Set current approval request rate
	 *
	 * @param requestRate
	 *            the approval request rate
	 */
	public void setRequestRate(double requestRate) {
		// requestRate has volatile modifier. It is thread-safe to be updated
		// from main thread.
		this.requestRate = requestRate;
	}

	/**
	 * Get the blocking queue
	 *
	 * @return approval request rate
	 */
	public BlockingQueue<ApprovalRequest> getQueue() {
		return queue;
	}

	/**
	 * Set the blocking queue
	 *
	 * @param requestRate
	 *            the approval request rate
	 *
	 * @throws IllegalStateException
	 *             if set the blocking queue when the simulator is running
	 */
	public void setQueue(BlockingQueue<ApprovalRequest> queue) {
		// Not allow change the queue when this thread run() has been invoked
		// and running
		if (isRunning)
			throw new IllegalStateException(
			        "Simulator is running, cannot alternate the interal queue to the simiulator");

		this.queue = queue;
	}

	/**
	 * Get whether the simulator is running or not
	 * 
	 * @return whether the simulator is running or not
	 */
	public boolean isRunning() {
		return isRunning;
	}
}