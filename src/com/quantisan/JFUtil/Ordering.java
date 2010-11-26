package com.quantisan.JFUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
	//private String setupTag;	// TODO implement in label
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
		setSlippage(slippage);
		this.counter = 0;
	}
	
	/**
	 * Set a trail step for an order
	 * 
	@param	order the order to be changed
	 *
	@param	trailStep the trail step size in pips, greater than 10
	 *
	 */
	public void setTrailStep(IOrder order, double trailStep) 
	{
		// set trailing step only if trailStep is >= 10d and 
		// there is no trailing step in order already
		if (trailStep >= 10d && order.getTrailingStep() == 0d)
			setStopLoss(order, order.getStopLossPrice(), trailStep);
	}
	
	/**
	 * Set a trail step for a Future order
	 * 
	@param	future the Future order to be changed
	 *
	@param	trailStep the trail step size in pips, greater than 10
	 *
	 */
	public void setTrailStep(Future<IOrder> future, double trailStep) 
	{ 
		(new Thread(new TrailStepTask(future, trailStep))).start();
	}

	private class TrailStepTask implements Runnable {
		private Future<IOrder> future;
		private double trailStep;

		public TrailStepTask(Future<IOrder> future, double trailStep) 
		{
			this.future = future;
			this.trailStep = trailStep;
		}
		@Override
		public void run() {
			IOrder order = null;
			try {
				order = future.get();
			} catch (InterruptedException ex) {
				Logging logger = new Logging(getContext().getConsole());
				logger.printErr("TrailStepTask interrupted.", ex);

			} catch (ExecutionException ex) {
				Logging logger = new Logging(getContext().getConsole());
				logger.printErr("TrailStepTask cannot execute.", ex);
			}
			
			try {
				setTrailStep(order, this.trailStep);
			} catch (NullPointerException ex) {
				Logging logger = new Logging(getContext().getConsole());
				logger.printErr("order in TrailStepTask is null.", ex);
			}
		}
	}
	
	/**
	 * Check to see if there is any risk exposure
	 * 
	 * 	                          
	@param  orders a list of orders to scan
	 *  

	@return true if there is exposed risk, false is not
	 */
	public static boolean checkExposed(List<IOrder> orders) {
		return !(getExposed(orders).isEmpty());		// no exposed position
	}

	/**
	 * Get a list of opened positions that have a risk exposure,
	 * i.e. stop price below opening price for long, and vice versa
	 * 
	 * 	                          
	@param  orders a list of orders to scan
	 *  

	@return a list of risk-exposed filled orders
	 */
	public static List<IOrder> getExposed(List<IOrder> orders) {
		List<IOrder> outList = new ArrayList<IOrder>(); 
		boolean isLong;
		double diff, stop;
		for (IOrder order : orders) {
			if (order.getState() == IOrder.State.FILLED) {
				isLong = order.isLong();
				stop = order.getStopLossPrice();
				diff = stop - order.getOpenPrice(); 
				diff *= isLong ? 1d : -1d;
				if (diff < 0d || stop == 0d)
					outList.add(order);
			}
		} // end FOR loop		
		return outList;
	}
	
	// TODO combine placeAsk and placeBid into placeOrder with IEngine.OrderCommand
	
	/**
	 * Send a market ask order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossSize size of the stop loss, in scale of instrument
	 *
	@see #placeAsk(Instrument, double, double, double)
	**/
	public Future<IOrder> placeAsk(Instrument instrument, double amount, 
			double stopLossSize) 
	{
		double price;
		try {
			price = getContext().getHistory().getLastTick(instrument).getBid();
		}
		catch(JFException ex) {
			Logging logger = new Logging(getContext().getConsole());
			logger.printErr("Cannot get price.", ex);
			return null;
		}
		double stopLossPrice = price + stopLossSize;
		return placeAsk(instrument, amount, stopLossPrice, 0d);	
	}
	
	/**
	 * Send a market ask order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossPrice stop loss price, 0 for no stop
	 *
	@param targetPrice target price, 0 for no target
	 *
	@see IEngine#submitOrder(String, Instrument, com.dukascopy.api.IEngine.OrderCommand, double, double, double, double, double)
	**/
	public Future<IOrder> placeAsk(Instrument instrument, double amount, 
			double stopLossPrice, double targetPrice) {
		SellTask task = new SellTask(instrument, amount, 
				stopLossPrice, targetPrice);
		return getContext().executeTask(task);	
	}	
	
	/**
	 * Send a market ask order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossPrice stop loss price, 0 for no stop
	 *
	@param trailStep sets a trailing step to the order
	 *
	@param targetPrice target price, 0 for no target
	 *
	@see IEngine#submitOrder(String, Instrument, com.dukascopy.api.IEngine.OrderCommand, double, double, double, double, double)
	**/
	public Future<IOrder> placeAsk(Instrument instrument, double amount, 
			double stopLossPrice, double trailStep, double targetPrice) {
		Future<IOrder> future = placeAsk(instrument, amount, stopLossPrice, targetPrice);
		setTrailStep(future, trailStep);	// creates new thread and wait for order to set trailstep
		return future;
	}
	
	/**
	 * Send a market bid order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossSize size of the stop loss, in scale of instrument
	 *
	@see #placeBid(Instrument, double, double, double)
	**/
	public Future<IOrder> placeBid(Instrument instrument, double amount, 
			double stopLossSize) 
	{
		double price;
		try {
			price = getContext().getHistory().getLastTick(instrument).getAsk();
		}
		catch(JFException ex) {
			Logging logger = new Logging(getContext().getConsole());
			logger.printErr("Cannot get price.", ex);
			return null;
		}
		double stopLossPrice = price - stopLossSize;
		return placeBid(instrument, amount, stopLossPrice, 0d);	
	}
	
	/**
	 * Send a market bid order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossPrice stop loss price, 0 for no stop
	 *
	@param targetPrice target price, 0 for no target
	 *
	@see IEngine#submitOrder(String, Instrument, com.dukascopy.api.IEngine.OrderCommand, double, double, double, double, double)
	**/
	public Future<IOrder> placeBid(Instrument instrument, double amount, 
			double stopLossPrice, double targetPrice) {
		BuyTask task = new BuyTask(instrument, amount, 
				stopLossPrice, targetPrice);
		return getContext().executeTask(task);	
	}

	/**
	 * Send a market bid order in its own thread
	 * 
	@param instrument the purchasing instrument
	 *
	@param amount the amount to buy, in millions, minimum 0.001
	 *
	@param stopLossPrice stop loss price, 0 for no stop
	 *
	@param trailStep sets a trailing step to the order
	 *
	@param targetPrice target price, 0 for no target
	 *
	@see IEngine#submitOrder(String, Instrument, com.dukascopy.api.IEngine.OrderCommand, double, double, double, double, double)
	**/
	public Future<IOrder> placeBid(Instrument instrument, double amount, 
			double stopLossPrice, double trailStep, double targetPrice) {
		Future<IOrder> future = placeBid(instrument, amount, stopLossPrice, targetPrice);
		setTrailStep(future, trailStep);	// creates new thread and wait for order to set trailstep
		return future;
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
				Logging logger = new Logging(getContext().getConsole());
				logger.printErr("Cannot close order.", ex);
			}
		}
		else {
			try {
				order.close(Rounding.lot(order.getAmount() * percentage));
			}
			catch (JFException ex) {
				Logging logger = new Logging(getContext().getConsole());
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
	@param	trailStep trailing step in pips, greater than 10
	 *
	@see IOrder#setStopLossPrice(double, OfferSide, double)
	 *
	*/
	public Future<IOrder> setStopLoss(IOrder order, double newStop, 
			double trailStep) {		
		StopTask task = new StopTask(order, newStop, trailStep);
		return getContext().executeTask(task);
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
	 * = Strategy Tag + Day of Month + a counter.  Override this method
	 * for custom trade label.
	 * 
	@return a String label
	 *
	@see java.lang.Override
	 */
	private String getLabel() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		return (this.strategyTag + day + "d" + counter++);						
	}

	/**
	 * @return the context
	 * 
	 * @see IContext
	 */
	protected IContext getContext() {
		return context;
	}
	
	/**
	 * @return the strategyTag
	 */
	protected String getStrategyTag() {
		return strategyTag;
	}

	/**
	 * @param strategyTag the strategy tag to set
	 */
	protected void setStrategyTag(String strategyTag) {
		this.strategyTag = strategyTag;
	}

	/**
	 * @return the slippage
	 */
	protected int getSlippage() {
		return slippage;
	}

	/**
	 * @param slippage the slippage to set
	 */
	protected void setSlippage(int slippage) {
		this.slippage = slippage;
	}

	/**
	 * Reset the trade label counter.
	 * 
	 */
	public void resetCounter() {
		this.counter = 0;
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
		@see IOrder#setStopLossPrice(double, OfferSide, double)
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
				logger = new Logging(getContext().getConsole());				
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
    					this.instrument, IEngine.OrderCommand.BUY, 
    					Rounding.lot(this.amount), 0, Ordering.this.slippage, 
        				Rounding.pip(this.instrument, this.stopLossPrice),
        				Rounding.pip(this.instrument, this.targetPrice));
    		}
    		catch (JFException ex) {
				Logging logger = new Logging(getContext().getConsole());
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
