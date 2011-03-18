package com.quantisan.JFUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

/**
 * Data recording utility class
 * 
 * @author plam
 *
 */
public class Recorder {
	//private static Analyzer instance = new Analyzer();
	private Recorder() {}

	/**
	 * Fetch and save order history into a CSV file.
	 * 
	 * @param fileName CSV file name to save order history. Will overwrite file.
	 * @return true if orders were successfully saved to fileName
	 */
	public static boolean record(String fileName) {
//		Thread worker;
//		Runnable task = instance.new fetchAndSaveOrders();
//		worker = new Thread(task);
//		worker.start();		
		
		List<IOrder> orders;
		try {
			orders = Orderer.getOrders();
		} catch (JFException ex) {
			Printer.printErr("Unable to get list of orders", ex);
			return false;
		}
		
		File fileDir = JForexContext.getContext().getFilesDir();								
		// full path name for file, with some string cleanup
		String fullFileName = 	fileDir.toString() + File.separator + fileName + ".csv";
		// TODO add date suffix to file name
		BufferedWriter writer;			
		try {
			// BufferedWriter to increase I/O performance
			writer = new BufferedWriter(new FileWriter(fullFileName));
			final String comma = ",";
			writer.write("id" + comma);
			writer.write("label" + comma);
			writer.write("instrument" + comma);
			writer.write("is_long" + comma);			
			writer.write("order_command" + comma);
			writer.write("amount" + comma);
			writer.write("requested_amount" + comma);
			writer.write("open_price" + comma);
			writer.write("creation_time" + comma);
			writer.write("fill_time" + comma);
			writer.write("close_price" + comma);
			writer.write("close_time" + comma);	
			writer.write("stoploss_price" + comma);
			writer.write("takeprofit_price" + comma);
			writer.write("trailing_step" + comma);			
			writer.write("profitloss_pips" + comma);
			writer.write("profitloss_usd" + comma);
			writer.write("profitloss_accountcurrency" + comma);
			writer.write("close_time" + comma);			
			writer.write("comment" + "\n");
			writer.flush();
		}
		catch (IOException ex) {
			Printer.printErr("Cannot open file.", ex);
			return false;
		}
		
		
		try	{
			writeToCSV(writer, orders);
			writer.flush();
			writer.close();
		}
		catch (IOException ex) {
			Printer.printErr("Cannot flush to file", ex);
			return false;
		}
		
		return true;		// reached end without exception return, thus okay
	}
	
	private static void writeToCSV(Writer writer, List<IOrder> orders) throws IOException 
	{
		final String comma = ",";
		for (IOrder order : orders) {
			if (order.getState() != IOrder.State.CLOSED)	
				continue;		// only record closed positions
			
			writer.write(order.getId() + comma);
			writer.write(order.getLabel() + comma);
			writer.write(order.getInstrument().toString() + comma);
			writer.write(order.isLong() + comma);			
			writer.write(order.getOrderCommand().toString() + comma);
			writer.write(order.getAmount() + comma);
			writer.write(order.getRequestedAmount() + comma);
			writer.write(order.getOpenPrice() + comma);
			writer.write(order.getCreationTime() + comma);
			writer.write(order.getFillTime() + comma);
			writer.write(order.getClosePrice() + comma);
			writer.write(order.getCloseTime() + comma);	
			writer.write(order.getStopLossPrice() + comma);
			writer.write(order.getTakeProfitPrice() + comma);
			writer.write(order.getTrailingStep() + comma);			
			writer.write(order.getProfitLossInPips() + comma);
			writer.write(order.getProfitLossInUSD() + comma);
			writer.write(order.getProfitLossInAccountCurrency() + comma);					
			writer.write(order.getComment() + "\n");
		}
	}	
	
//	private class fetchAndSaveOrders implements Runnable {
//
//		@Override
//		public void run() {
//		}
//		
//	}
}
