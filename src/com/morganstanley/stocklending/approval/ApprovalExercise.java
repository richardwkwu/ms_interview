package com.morganstanley.stocklending.approval;

import java.util.logging.Logger;

/**
 * The ApprovalExercise is the main class to get started the approval processing
 * flow.
 * 
 * ApprovalExercise provides the static void main() method
 * 
 * @author Richard WU
 */
public class ApprovalExercise {
	/**
	 * Java logger for ApprovalExercise
	 */
	private static final Logger LOGGER = Logger.getLogger(ApprovalExercise.class.getName());

	/*
	 * Entry point of the approval processing program. It initializes the
	 * necessary objects, namely approval source, decision service and response
	 * service through the factory methods.
	 */
	public static void main(String[] args) {

		// 1 create and initialize a concrete ApprovalDecisionService instance
		// through the DecisionServiceFactory
		// 1.1 get the DecisionServiceFactory
		String decisionServiceInterfaceName = ApprovalDecisionService.class.getSimpleName();
		LOGGER.info(new StringBuffer("Initialing ").append(decisionServiceInterfaceName).append("...").toString());
		AbstractApprovalServiceFactory decisionServiceFactory = ApprovalServiceFactory
		        .getFactory(ApprovalServiceType.DECISION_SERVICE);

		// 1.2 get the concrete ApprovalDecisionService through
		// DecisionServiceFactory
		ApprovalDecisionService decisionService = decisionServiceFactory.getApprovalDecisionService("STOCK_LENDING");
		LOGGER.info(new StringBuffer("Done. ").append(decisionService.getClass().getSimpleName())
		        .append(" is instantiated to support ").append(decisionServiceInterfaceName).append(".").toString());

		// 2 create and initialize a concrete ApprovalResponseService instance
		// through the ResponseServiceFactory
		// 2.1 get the DecisionServiceFactory
		String responseServiceIntefaceName = ApprovalResponseService.class.getSimpleName();
		LOGGER.info(new StringBuffer("Initialing ").append(responseServiceIntefaceName).append("...").toString());
		AbstractApprovalServiceFactory responseServiceFactory = ApprovalServiceFactory
		        .getFactory(ApprovalServiceType.RESPONSE_SERVICE);

		// 2.2 get the concrete ApprovalResponseService through
		// ApprovalResponseService
		ApprovalResponseService responseService = responseServiceFactory.getApprovalResponseService("STOCK_LENDING");
		LOGGER.info(new StringBuffer("Done. ").append(responseService.getClass().getSimpleName())
		        .append(" is instantiated to support ").append(responseServiceIntefaceName).append(".").toString());

		// 3 create and initialize a concrete ApprovalSource instance
		StockLendingApprovalSource approvalSource = new StockLendingApprovalSource();
		LOGGER.info("Initialing " + approvalSource.getClass().getSimpleName() + "...Done");

		// 4 create and initialize a ApprovalRequestSourceSimulator instance
		ApprovalRequestSourceSimulator simulator = new ApprovalRequestSourceSimulator();
		LOGGER.info(simulator.getClass().getSimpleName() + " is created");

		// need to connect the approvalSource to the simulator in order to get
		// the streaming of approval requests
		approvalSource.connect(simulator);

		ApprovalProcessor processor = new ApprovalProcessor(approvalSource, decisionService, responseService);
		LOGGER.info(processor.getClass().getSimpleName() + " is created and start to run ");
		processor.run();

		// need to disconnect the approvalSource from the simulator
		approvalSource.disconnect(simulator);
		LOGGER.info("Gratefully shutdown");
	}
}
