package com.morganstanley.stocklending.approval;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Approval Request is a security lending request. It consists of client, 
 * security and security quantity information.
 */
public class ApprovalRequest
{
	/**
	 * static AtomicInteger to generate the monotonically increase 
	 * transaction number for all newly created approval request
	 * Assumption: the transaction number is started from 1 for demonstration
	 * purpose. In real use-case,
	 * the start of transaction number should be started from last used transaction
	 * number, either from transaction log or database.
	 */
    private static final AtomicInteger globalTxNum = new AtomicInteger(1); 
    
    /**
     * Transaction number - an monotonically increase number that will be stamp
     * on the approval request. It eases keeping track of the Approval Request
     * and Response through the Approval Source, DecisionService and Response
     * Service for logging/demonstration purpose.
     */
    private final int txNum;	
    
    /** 
     * Client name
     */
	private final String client;
	
	/**
	 * Security name/code
	 */
	private final String security;
	
	/**
	 * quantity of security in the approval request
	 */
	private final BigDecimal quantity;
	
	/**
	 * Constructs an ApprovalRequest
	 * 
	 * @param  client  client name
	 * @param  security security name/code
	 * @param  quantity quantity of security being requested
	 */
	public ApprovalRequest(final String client,
						   final String security,
						   final BigDecimal quantity)
	{
		if (client == null)
			throw new IllegalArgumentException("Client name is null");

		if (security == null)
			throw new IllegalArgumentException("Security is null");

		if (quantity.compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException("Quantity must be great than zero");

		this.client = client;
		this.security = security;
		this.quantity = quantity;
		
		// get the transaction number and increment atomically
	    this.txNum = globalTxNum.getAndIncrement();
	}

	/**
	 * Client getter
	 * 
	 * @return  client name
	 */
	public String getClient() { return client; }

	/**
	 * Security getter
	 * 
	 * @return  security name
	 */
	public String getSecurity() { return security; }

	/**
	 * Quantity getter
	 * 
	 * @return  quantity of security being requested
	 */	
	public BigDecimal getQuantity() { return quantity; }

	/**
	 * Returns a string representing the data in this sequence. 	
	 * 
	 * @return  a string representation of this sequence of characters.
	 */	
	@Override
	public String toString() {
		return new StringBuilder("Approval Request #").append(txNum).append(": {  Client: ").append(client)
				  						     		   .append("\tSecurity: ").append(security)
				  						     		   .append("\tQuantity: ").append(quantity).append(" }").toString();
	}
}