package com.quantisan.JFUtil;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.dukascopy.api.*;

/**
 * Executing orders in separate threads
 * 
 */
public class Ordering {
	private IContext context;
	private IEngine engine;
	private String strategyTag;
	private int counter;
	private int slippage;
	
	/**
	 * Constructor
	 * 
	@param context context of the strategy
	 *
	@param strategyTag a tag to identify the strategy for labelling orders
	 *
	@param slippage slippage to use
	 *
	 */
	public Ordering(IContext context, String strategyTag, int slippage) {
		this.context = context;
		this.engine = context.getEngine();
		this.strategyTag = strategyTag;
		this.slippage = slippage;
		this.counter = 0;
	}
	
	/**
	 * Send a market short order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossSize size of the stop loss, in scale of instrument
	 *
	**/
	public Future<IOrder> placeAsk(Instrument instrument, double amount, 
			double stopLossSize) 
	{
		double price;
		try {
			price = this.context.getHistory().getLastTick(instrument).getBid();
		}
		catch(JFException ex) {
			Logging logger = new Logging(Ordering.this.context.getConsole());
			logger.printErr("Cannot get price.", ex);
			return null;
		}
		double stopLossPrice = price + stopLossSize;
		return placeAsk(instrument, amount, stopLossPrice, 0d);	
	}
	
	/**
	 * Send a market short order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossPrice stop loss price, 0 for no stop
	 *
	@param targetPrice target price, 0 for no target
	 *
	**/
	public Future<IOrder> placeAsk(Instrument instrument, double amount, 
			double stopLossPrice, double targetPrice) {
		SellTask task = new SellTask(instrument, amount, 
				stopLossPrice, targetPrice);
		return this.context.executeTask(task);	
	}
	
	
	/**
	 * Send a market buy order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossSize size of the stop loss, in scale of instrument
	 *
	**/
	public Future<IOrder> placeBid(Instrument instrument, double amount, 
			double stopLossSize) 
	{
		double price;
		try {
			price = this.context.getHistory().getLastTick(instrument).getAsk();
		}
		catch(JFException ex) {
			Logging logger = new Logging(Ordering.this.context.getConsole());
			logger.printErr("Cannot get price.", ex);
			return null;
		}
		double stopLossPrice = price - stopLossSize;
		return placeBid(instrument, amount, stopLossPrice, 0d);	
	}
	
	/**
	 * Send a market buy order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossPrice stop loss price, 0 for no stop
	 *
	@param targetPrice target price, 0 for no target
	 *
	**/
	public Future<IOrder> placeBid(Instrument instrument, double amount, 
			double stopLossPrice, double targetPrice) {
		BuyTask task = new BuyTask(instrument, amount, 
				stopLossPrice, targetPrice);
		return this.context.executeTask(task);	
	}

	/**
	 * Close an order at market price
	 * 
	@param	order	the order to be closed
	 */
	public void close(IOrder order) {
		close(order , 1d);
	}
	
	/**
	 * Partially close an order at market price
	 * 
	@param	order	the order to be closed
	 *
	@param	percentage	the percentage of the amount to be closed, 
	 *		value (0, 1]
	*/
	public void close(IOrder order, double percentage) {
		if (percentage <= 0d || percentage > 1d) {
			throw new IllegalArgumentException(
					"percentage must in range (0, 1]");
		}
		else if (percentage == 1d) {		// close all
			try {
				order.close();
			}
			catch (JFException ex) {
				Logging logger = new Logging(Ordering.this.context.getConsole());
				logger.printErr("Cannot close order.", ex);
			}
		}
		else {
			try {
				order.close(Rounding.lot(order.getAmount() * percentage));
			}
			catch (JFException ex) {
				Logging logger = new Logging(Ordering.this.context.getConsole());
				logger.printErr("Cannot close order.", ex);
			}
		}
	}
	
	
	/**
	 * Set the stop loss and trail step of an order in a new Thread
	 * 
	@param	order the order to be changed
	 *
	@param	newStop new stop loss price
	 *
	@param	trailStep trailing step in pips
	 *
	*/
	public Future<IOrder> setStopLoss(IOrder order, double newStop, 
			double trailStep) {		
		StopTask task = new StopTask(order, newStop, trailStep);
		return this.context.executeTask(task);
	}
	
	/**
	 * Get a list of orders for the particular instrument
	 * 
	@param	instrument	the instrument of which orders are to be fetched
	 *
	@return	a list of orders for the instrument
	*/
	public List<IOrder> getOrders(Instrument instrument) throws JFException {
		return this.engine.getOrders(instrument);
	}
	
	/**
	 * Returns a string for labelling an order, which is  
	 * = Strategy Tag + Day of Month + a counter.
	 * 
	@return a String label
	 */
	private String getLabel() {	
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		return (this.strategyTag + day + "d" + counter++);						
	}

	/**
	 * Inner class for sending stop order in a Callable thread
	 * 
	 */
	private class StopTask implements Callable<IOrder> {
		private IOrder order;
		private double newStop, trailStep;	
		
		/**
		 * Construct a StopTask
		 * 
		@param order the order to be updated
		 *
		@param newStop the price of the new stop loss
		 *
		@param trailStep step size of the trailing order
		 *
		 */
		public StopTask(IOrder order, double newStop, 
				double trailStep) {
			this.order = order;
			this.newStop = newStop;
			this.trailStep = trailStep;
		}
		
		public IOrder call() {
			Logging logger;
			OfferSide side = order.isLong() ? OfferSide.BID : OfferSide.ASK;
			
			this.newStop = Rounding.pip(order.getInstrument(), newStop);
			this.trailStep = Math.round(this.trailStep);
			
			try {
				// TODO check if order is in Filled state?
				order.setStopLossPrice(newStop, side, trailStep);
				order.waitForUpdate(1000);
			}
			catch (JFException ex) {
				logger = new Logging(Ordering.this.context.getConsole());				
				logger.printErr("Couldn't set newStop: " + newStop + 
						", trailStep: " + trailStep, ex);
				return null;
			}
			return order;
		}
	}
	
	/**
	 * Inner class for sending buy order in a Callable thread
	 * 
	 */
	private class BuyTask implements Callable<IOrder>{
    	private Instrument instrument;
        private double stopLossPrice, targetPrice, amount;  
		/**
		 * Construct a BuyTask
		 * 
		@param instrument the purchasing instrument
		 *
		@param amount the amount to buy, in millions, minimum 0.001
		 *
		@param stopLossPrice stop loss price, 0 for no stop
		 *
		@param targetPrice target price, 0 for no target
		 *
		 */
    	public BuyTask(Instrument instrument, double amount, 
    			double stopLossPrice, double targetPrice)
    	{
    		this.instrument = instrument;
    		this.amount = amount;
    		this.stopLossPrice = stopLossPrice;    		
    		this.targetPrice = targetPrice;
    	}
 
    	public IOrder call() {
    		IOrder order;
    		try {
    			order = engine.submitOrder(Ordering.this.getLabel(), 
    					this.instrument, IEngine.OrderCommand.BUY, this.amount, 
        				0, Ordering.this.slippage, 
        				Rounding.pip(this.instrument, this.stopLossPrice),
        				Rounding.pip(this.instrument, this.targetPrice));
    		}
    		catch (JFException ex) {
				Logging logger = new Logging(Ordering.this.context.getConsole());
				logger.printErr("Cannot place bid.", ex);
				return null;
    		}
    		return order;
    	}
    }
	
	/**
	 * Inner class for sending sell order in a Callable thread
	 * 
	 */
	private class SellTask implements Callable<IOrder>{
    	private Instrument instrument;
        private double stopLossPrice, targetPrice, amount;  
		/**
		 * Construct a BuyTask
		 * 
		@param instrument the purchasing instrument
		 *
		@param amount the amount to buy, in millions, minimum 0.001
		 *
		@param stopLossPrice stop loss price
		 *
		@param targetPrice target price
		 *
		 */
    	public SellTask(Instrument instrument, double amount, 
    			double stopLossPrice, double targetPrice)
    	{
    		this.instrument = instrument;
    		this.amount = amount;
    		this.stopLossPrice = stopLossPrice;    		
    		this.targetPrice = targetPrice;
    	}
 
    	public IOrder call() throws Exception {
    		return engine.submitOrder(Ordering.this.getLabel(), this.instrument, 
    				IEngine.OrderCommand.SELL, this.amount, 
    				0, Ordering.this.slippage, 
     				Rounding.pip(this.instrument, this.stopLossPrice),
    				Rounding.pip(this.instrument, this.targetPrice));
    	}
    }
}
