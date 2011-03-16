package com.quantisan.JFUtil;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.dukascopy.api.*;
import com.dukascopy.api.IOrder.State;

/**
 * Executing orders in separate threads
 * 
 */
public enum Orderer {	
	INSTANCE;
	
	/**
	 * Set a trail step for an order
	 * 
	@param	order the order to be changed
	 *
	@param	trailStep the trail step size in pips, greater than 10
	 *
	 */
	public static void setTrailStep(IOrder order, double trailStep) 
	{	// TODO use OrderTicket
		(new Thread(INSTANCE.new TrailStepTask(order, trailStep))).start();
	}
	
	/**
	 * Set a trail step for a {@link Future} order
	 * 
	@param	future the Future order to be changed
	 *
	@param	trailStep the trail step size in pips, greater than 10
	 *
	 */
	public static void setTrailStep(Future<IOrder> future, double trailStep) 
	{ 
		(new Thread(INSTANCE.new TrailStepTask(future, trailStep))).start();
	}
	
	/**
	 * Send an order in its own separate thread
	 * 
	 * @param ticket encapsulated order information
	 *
	 * @see IEngine#submitOrder(String, Instrument, com.dukascopy.api.IEngine.OrderCommand, double, double, double, double, double, long, String)
	**/
	public static Future<IOrder> placeOrder(OrderTicket ticket) 
	{		
		OrderTask task = INSTANCE.new OrderTask(ticket);
		return JForexContext.getContext().executeTask(task);	
	}	
	
	/**
	 * Close an order at market price
	 * 
	@param	order	the order to be closed
	 */
	public static void close(IOrder order) {
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
	public static void close(IOrder order, double percentage) {
		if (percentage <= 0d || percentage > 1d) {
			throw new IllegalArgumentException("percentage must in range (0, 1]");
		}
		else if (percentage == 1d) {		// close all
			// TODO move to Callable?
			try {
				order.close();
			}
			catch (JFException ex) {
				Printer.printErr("Cannot close order.", ex);
			}
		}
		else {
			try {
				order.close(Rounding.lot(order.getAmount() * percentage));
			}
			catch (JFException ex) {
				Printer.printErr("Cannot close order.", ex);
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
	public static Future<IOrder> setStopLoss(IOrder order, double newStop, 
			double trailStep) {		
		StopTask task = INSTANCE.new StopTask(order, newStop, trailStep);
		return JForexContext.getContext().executeTask(task);
	}
	
	/**
	 * Get a list of orders for the particular instrument
	 * 
	@param	instrument	the instrument of which orders are to be fetched
	 *
	@return	a list of orders for the instrument
	*/
	public static List<IOrder> getOrders(Instrument instrument) throws JFException {
		return JForexContext.getEngine().getOrders(instrument);
	}
	
	/**
	 * Get a list of orders for all instruments
	 * 
	@return	a list of orders
	*/
	public static List<IOrder> getOrders() throws JFException {
		return JForexContext.getEngine().getOrders();
	}

	private class TrailStepTask implements Runnable {
		private Future<IOrder> future;
		private IOrder order;
		private double trailStep;
	
		public TrailStepTask(Future<IOrder> future, double trailStep) 
		{
			this.future = future;
			this.trailStep = trailStep;
		}
		
		public TrailStepTask(IOrder order, double trailStep) 
		{
			this.order = order;
			this.trailStep = trailStep;
		}
		
		@Override
		public void run() {			
			if (future != null) {
				try {
					order = future.get();
				} catch (InterruptedException ex) {
					Printer.printErr("TrailStepTask interrupted.", ex);
	
				} catch (ExecutionException ex) {
					Printer.printErr("TrailStepTask cannot execute.", ex);
				}
			}
			
			// set trailing step only if trailStep is >= 10d and 
			// there is no trailing step in order already
			if (trailStep < 10d || order.getTrailingStep() != 0d)
				return;
					
			for (int i = 0; i < 10; i++) {
				if (order.getState() != State.FILLED && order.getState() != State.OPENED) {
					order.waitForUpdate(1000);
				}
				else	break;
			}
			
			// TODO how to overcome "change to same stop loss price warning"
			setStopLoss(order, order.getStopLossPrice(), this.trailStep);			
		}
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
			OfferSide side = order.isLong() ? OfferSide.BID : OfferSide.ASK;
			
			this.newStop = Rounding.pip(order.getInstrument(), newStop);
			this.trailStep = Math.round(this.trailStep);
			
			try {
				// TODO check if order is in Filled state?
				order.setStopLossPrice(newStop, side, trailStep);
				order.waitForUpdate(1000);
			}
			catch (JFException ex) {				
				Printer.printErr(order.getLabel() + "-- couldn't set newStop: " + 
								newStop + ", trailStep: " + trailStep, ex);
				return null;
			}
			return order;
		}
	}
	
	/**
	 * Inner class for sending buy order in a Callable thread
	 * 
	 */
	private class OrderTask implements Callable<IOrder>{
    	private OrderTicket ticket;
        
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
    	public OrderTask(OrderTicket ticket)
    	{
    		this.ticket = ticket;
    	}
 
    	public IOrder call() {
    		IOrder order;
    		try {
    			order = JForexContext.getEngine().submitOrder(
    						ticket.getLabel(), 
    						ticket.getInstrument(), 
    						ticket.getOrderCmd(), 
	    					ticket.getLot(), 
	    					ticket.getPrice(), 
	    					ticket.getSlippage(), 
	        				ticket.getStopLossPrice(),
	        				ticket.getTakeProfitPrice(),
	        				ticket.getGoodTillTime(),
	        				ticket.getComment());
    		}
    		catch (JFException ex) {
				Printer.printErr(ticket.getLabel() + " -- cannot place order.", ex);
				return null;
    		}
    		return order;
    	}
    }
}
