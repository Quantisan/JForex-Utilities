package com.quantisan.JFUtil;

import com.dukascopy.api.IConsole;
import com.dukascopy.api.IOrder;

/**
 * Logging operations
 * 
 */
public class Logging {
	private IConsole console;
	
	/**
	 * Constructor
	 * 
	@param  console	console stream for printing
	 *  
	 */
	public Logging(IConsole console) {
		this.console = console;
	}

	public IConsole getConsole() {
		return console;
	}

	/**
	 * Printing a string to the JForex PrintStream
	                          
	@param  string a message to be printed
	 *  

	 */
	public void print(String string) {
		getConsole().getOut().println(string);
	}
	
	/**
	 * Printing an error message and the stack trace
	                          
	@param  string an error message to be printed
	 *  
	@param  ex the caught exception
	 *

	 */
	public void printErr(String string, Exception ex) {		
		ex.printStackTrace(getConsole().getErr());
		getConsole().getErr().println(string);
	}
	
	/**
	 * Printing information of an order to console
	                          
	@param  order	the order
	 *  
	 */
	public void printOrderInfo(IOrder order) {
		print(order.getLabel() + " long: " + order.isLong() +
				" amt: " + order.getAmount() +
				" op: " + order.getOpenPrice() +
				" st: " + order.getStopLossPrice() + 
				" tr: " + order.getTrailingStep() +
				" tp: " + order.getTakeProfitPrice() +
				" P/L pips: " + order.getProfitLossInPips());
		// TODO add time held display
	}
	
	
	// TODO logging to external file
}
