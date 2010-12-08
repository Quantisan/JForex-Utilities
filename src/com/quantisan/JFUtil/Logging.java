package com.quantisan.JFUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.dukascopy.api.*;
import com.dukascopy.api.IOrder.State;

/**
 * Logging operations
 * 
 */
public class Logging {
	private IConsole console;
	private IHistory history;
	private File fileDir;		// JForex default write-accessible path
	private String currSymbol;
	
	/**
	 * Constructor
	 * 
	@param  console	console stream for printing
	 *  
	@deprecated use {@link #Logging(IContext)}
	 */
	public Logging(IConsole console) {
		this.console = console;		
	}
	
	/**
	 * Constructor
	 * 
	@param  context strategy context object
	 *  
	 */
	public Logging(IContext context) {
		this.fileDir = context.getFilesDir();
		this.console = context.getConsole();
		this.history = context.getHistory();
		this.currSymbol = context.getAccount().getCurrency().getSymbol();
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
	 * Printing an error message and the stack trace
	 *
	@param	console console object
	 *
	@param  string an error message to be printed
	 *  
	@param  ex the caught exception
	 *
	 */
	public static void printErr(IConsole console, String string, Exception ex) {		
		ex.printStackTrace(console.getErr());
		console.getErr().println(string);
	}
	
	/**
	 * Printing information of an order to console
	                          
	@param  order	the order
	 *  
	 */
	public void printOrderInfo(IOrder order) {
		long now;
		try {
			now = history.getLastTick(order.getInstrument()).getTime();
		} catch (JFException ex) {
			printErr("Cannot get time of last tick.", ex);
			return;
		}
		
		print(order.getLabel() + " long: " + order.isLong() +
				" amt: " + order.getAmount() +
				" op: " + order.getOpenPrice() +
				" st: " + order.getStopLossPrice() + 
				" tr: " + order.getTrailingStep() +
				" tp: " + order.getTakeProfitPrice() +
				" p/l pips: " + order.getProfitLossInPips() +
				" held for: " + timeConvert(now - order.getFillTime())); 
	}
	
	/**
	 * 
	 * @param time	time in milliseconds
	 * @return reformatted time in kk:hh:mm:ss, where kk is number of days,
	 * hh is hours, mm is minutes, ss is seconds
	 */
	private String timeConvert(long time) { 
		   return time/1000/24/60 + ":" + time/1000/60%24 + ":" + time/1000%60 +
		   			":" + time%1000;
	}

	/**
	 * Log closed orders to a CSV file
	 * 
	 * @param instrument
	 * @param filename must end in '.csv' without quotes
	 * @param from starting time to look to log orders data
	 * @throws JFException
	 */
	public void logClosedOrdersToCSV(Instrument instrument, 
									String filename,
									long from) throws JFException
	{	
		long now = history.getLastTick(instrument).getTime();
		
		List<IOrder> orders = history.getOrdersHistory(instrument, from, now);
		String fullFileName = this.fileDir.toString() + File.separator + filename;
		print("Writing " + orders.size() + " orders to " + fullFileName);
		
		Writer writer;			
		// Write header
		try {
			// BufferedWriter to increase I/O performance
			writer = new BufferedWriter(new FileWriter(fullFileName, true));
			writer.append("ID,");
			writer.append("Label,");
			writer.append("Fill Time,");
			writer.append("Close Time,");
			writer.append("Instrument,");
			writer.append("Is Long,");
			writer.append("Amount,");		// write column headers
			writer.append("Open Price,");
			writer.append("Clos Price,");
			writer.append("P&L [pips],");
			writer.append("P&L [" + this.currSymbol + "]\n");
			writer.flush();
		}
		catch (IOException ex) {
			printErr("Cannot write header.", ex);
			return;
		}
		

		for (IOrder order : orders) {				// Loops through all orders
			if (order.getState() == State.CLOSED) {	// only log closed ones
				try {	// Write row entry				
					writer.append(order.getId() + ",");
					writer.append(order.getLabel() + ",");
					writer.append(order.getFillTime() + ",");
					writer.append(order.getCloseTime() + ",");
					writer.append(order.getInstrument() + ",");
					writer.append(order.isLong() + ",");
					writer.append(order.getAmount() + ",");
					writer.append(order.getOpenPrice() + ",");
					writer.append(order.getClosePrice() + ",");
					// TODO include commission and swap
					writer.append(order.getProfitLossInPips() + ",");
					writer.append(order.getProfitLossInAccountCurrency() + "\n");
				} catch (IOException ex) {
					printErr("I/O error writing order: " + order.getId(), ex);
					return;
				}
			}
		}
		
		// File closing
		try	{
			writer.flush();
			writer.close();
		}
		catch (IOException ex) {
			printErr("Cannot flush to file", ex);
			return;
		}	
		
		print("Finished writing to " + fullFileName);
	}
}
