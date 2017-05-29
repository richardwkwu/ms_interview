package com.morganstanley.stocklending.approval;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The StockLendingDecisionService class is the class to implement
 * ApprovalDecisionService interface and provides implementation of
 * interface method processRequest().
 * 
 * Assumption: using a series of hashmaps to hold the clients,
 * securities and approval ratio/reject decision for assignment
 * purpose
 * 
 * @author  Richard WU
 */	
public class StockLendingDecisionService implements ApprovalDecisionService {
	/**
	 * Java logger for StockLendingResponseService
	 */
	private static final Logger LOGGER = Logger.getLogger(StockLendingDecisionService.class.getName());
	
	/**
	 * Rejected ratio is 0.0
	 */
	
	private static final BigDecimal REJECTRATIO = BigDecimal.ZERO;
	/**
	 *  The static hashMap "approvalDB" is to hold the approval ratio for client and security combinations. 
	 *
	 * It is solely for the demonstration purpose. For real-live situation, it should make approval decision 
	 * based on the result set from database query or from other services.
	 * 
	 * Assume the approvalDB can be change later. Hence use ConncurrentHashMap rather than original HashMap.
	 */
	private static final Map<String, Map<String, BigDecimal>> approvalDB = 
		new ConcurrentHashMap<String , Map<String, BigDecimal>>() {{
			put("HSBC00005", new ConcurrentHashMap<String, BigDecimal>() {{ put("GS", new BigDecimal(0.55)); 
																			put("JPM", BigDecimal.ONE);
																			put("UBS", BigDecimal.ONE);
																			put("CLSA", REJECTRATIO);	
	    																   }});	
			put("HKEX00388", new ConcurrentHashMap<String, BigDecimal>() {{ put("GS", new BigDecimal(0.45)); 
																			put("JPM", new BigDecimal(0.5));
																			put("UBS", REJECTRATIO);
																			put("CLSA", BigDecimal.ONE);	
																			}});
			put("TENCENT00700", new ConcurrentHashMap<String, BigDecimal>() {{ put("GS", new BigDecimal(0.6)); 
																			put("JPM", new BigDecimal(0.8));
																			put("UBS", BigDecimal.ONE);
																			put("CLSA", new BigDecimal(0.6));	
																			}});
			put("CCB00939", new ConcurrentHashMap<String, BigDecimal>() {{ 	put("GS", new BigDecimal(0.7)); 
																		   	put("JPM", BigDecimal.ONE);
																		   	put("UBS", BigDecimal.ONE);
																		   	put("CLSA", new BigDecimal(0.6));	
		}});
	}};
	
	/**
	 * Service Name of StockLendingDecisionService
	 */
	public static final String SERVICE_NAME = "STOCK_LENDING";
	
	/**
	 * Process the approval request and return the approval response.
	 * The approval decision is based on datastored in approvalDB.
	 * 
	 * @param  request the approval request
	 * @return approval response
	 * @throws IllegalArgumentException
	 */
	@Override
	public ApprovalResponse processRequest(ApprovalRequest request) {
		String clientName;
		String securityName;
		BigDecimal requestQuantity;
		BigDecimal approvedQuantity = ApprovalResponse.REJECTED_QUANTITY;
		ApprovalCode statusCode; 
		
		if( request == null )
            throw new IllegalArgumentException( "Arguement request is null" );
		    
		clientName = request.getClient();
		
		// Start Approval Request validation
		// i) validate client name
		if(clientName == null || clientName.length() == 0)
			throw new IllegalArgumentException( "The Approval Request's client attribute is null or empty" );

		// ii) validate security name
		securityName = request.getSecurity();
		if(securityName == null || securityName.length() == 0)
			throw new IllegalArgumentException( "The Approval Request's security attribute is null or empty" );

		// iii) validate request quantity (must be greater than 0)
		requestQuantity = request.getQuantity();
		if(requestQuantity == null)
		{	
			throw new IllegalArgumentException( "The Approval Request's quantity attribute is null" );
		}
		// iv) approval request quantity must be greater than 0
		else if(requestQuantity.signum() <= 0)
		{
			throw new IllegalArgumentException( "The Approval Request's quantity attribute must be greater than 0, current value: " + requestQuantity);
		}
			
		Map<String, BigDecimal> clientMap = approvalDB.get(securityName);
		if(clientMap == null)
		{
			// security is not found
			statusCode = ApprovalCode.REJECTED_SECURITY_NOT_FOUND;
		}
		else 
		{
			// look for the client (key) in clientMap and get the approval percentage 
			BigDecimal approvalPercentage = clientMap.get(clientName);
			if(approvalPercentage == null)
			{
				// client not found
				statusCode = ApprovalCode.REJECTED_CLIENT_NOT_FOUND;
			}
			else 
			{
				if(approvalPercentage.compareTo(ApprovalResponse.REJECTED_QUANTITY) <= 0) // assume less than or equal to 0 mean reject the request
				{
					statusCode = ApprovalCode.REJECTED;
					approvedQuantity = ApprovalResponse.REJECTED_QUANTITY;
				}
				else 
				{
					statusCode = ApprovalCode.APPROVED;
					approvedQuantity = requestQuantity.multiply(approvalPercentage);
					approvedQuantity = approvedQuantity.setScale(0, BigDecimal.ROUND_HALF_UP);
				}			
			}
		}
		
		// Simulate the processRequest() that takes a random amount of processing time (50ms+)
		try {
            Thread.sleep(10+(long)(Math.random()*10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Exception occur", e);
        }

		// return the Approval response with approved quantity and status code
		ApprovalResponse response = new ApprovalResponse(request, approvedQuantity, statusCode);
		LOGGER.info("Decision Service Done: " + response);
		return response;
	}
}
