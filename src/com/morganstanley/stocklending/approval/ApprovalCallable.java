package com.morganstanley.stocklending.approval;
/*callable*/
/*callable2*/
/*callable3*/
/*callable4*/

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * ApprovalCallable is a class implements the Callable interface.
 * 
 * ApprovalCallable override the Callable's call() method i) takes an object
 * (type T) as an input ii) invokes the Function<T, R> iii) and returns the
 * result object (type R)
 * 
 * @author Richard Wu
 */
public class ApprovalCallable<T, R> implements Callable<R> {
	private final Function<T, R> func;
	private T input;

	/**
	 * Constructs an ApprovalCallable
	 * 
	 * @param func
	 *            functional interface of Function<T,R>
	 */
	public ApprovalCallable(Function<T, R> func) {
		this(func, null);
	}

	/**
	 * Constructs an ApprovalCallable
	 * 
	 * @param func
	 *            functional interface of Function<T,R>
	 * @param input
	 *            input object
	 */
	public ApprovalCallable(Function<T, R> func, T input) {
		this.func = func;
		this.input = input;
	}

	/**
	 * Get the Function<T,R>
	 * 
	 * @return the Function<T,R>
	 */
	public Function<T, R> getFunc() {
		return func;
	}

	/**
	 * Get the input object
	 * 
	 * @return the input object
	 */
	public T getInput() {
		return input;
	}

	/**
	 * Set the input object
	 * 
	 * @param the
	 *            input object
	 */
	public void setInput(T input) {
		this.input = input;
	}

	/**
	 * Override the Callable's call() method i) takes the member variable object
	 * (type T) as an input ii) invokes the Function<T, R> iii) and returns the
	 * result object (type R)
	 */
	@Override
	public R call() throws Exception {
		return func.apply(input);
	}
}
