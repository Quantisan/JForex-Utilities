package com.quantisan.JFUtil;

import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.OrderCommand;;

/**
 * Order ticket information encapsulation class using Builder pattern.
 * 
 * Example:
 * <pre>
 * {@code 
 * OrderTicket smallTicket = new OrderTicket.Builder(label, instrument, cmd, amt).build();
 * OrderTicket bigTicket = new OrderTicket.Builder(label, instrument, cmd, amt).setStopLossPrice(sl).setTakeProfitPrice(tp).build();
 * }
 * </pre>
 * @author plam
 *
 */
public class OrderTicket {
	/**
	 * Builder for OrderTicket
	 * 
	 * @author plam
	 *
	 */
	public static class Builder {
		// set default values here
		// required parameters
		private String label;
		private Instrument instrument;
		private IEngine.OrderCommand command; 
		private double amount;
		
		// optional parameters
		private double price = 0;
		private double slippage = 5;
		private double stopLossPrice = 0; 
		private double takeProfitPrice = 0; 
		private long goodTillTime = 0;
		private String comment = "";
		
		/**
		 * @param label unique user defined identifier for the order.
		 * @param instrument instrument to trade
		 * @param command type of submitted order
		 * @param amount amount in millions for the order, will be implicitly rounded with {@link Rounder#lot(double) Rounder.lot}
		 */
		public Builder(String label, Instrument instrument, 
						OrderCommand command, double amount) {
		      this.label = label;
		      this.instrument = instrument;
		      this.command = command;
		      this.amount = amount;
		  }
		
		/**
		 * Default is zero.  
		 * If zero, then last market price visible on the JForex will be used. 
		 * In case of market orders,incorrect price (worse than current market) 
		 * will be changed to current price and slippage.
		 * 
		 * 
		 * 
		 * @param price Preferred price for order, will be implicitly rounded with {@link Rounder#pip(Instrument, double) Rounder.pip} 
		 * @return
		 */
		public Builder setPrice(double price) {
			this.price = price;
			return this;
		}

		/**
		 * Default is 5 pips. If negative then default value of 5 pips is used. 
		 * Set in pips, you should pass 1, not 0.0001.
		 * 
		 * @param slippage slippage to to the order
		 * @return
		 */
		public Builder setSlippage(double slippage) {
			this.slippage = slippage;
			return this;
		}

		/**
		 * Default is 0, which means no stop loss set.
		 * 
		 * @param stopLossPrice Price of the stop loss, will be implicitly rounded with {@link Rounder#pip(Instrument, double) Rounder.pip}
		 * @return
		 */
		public Builder setStopLossPrice(double stopLossPrice) {
			this.stopLossPrice = stopLossPrice;
			return this;
		}

		/**
		 * Default is 0, which means no take profit set.
		 * 
		 * @param takeProfitPrice Price of the take profit, will be implicitly rounded with {@link Rounder#pip(Instrument, double) Rounder.pip}
		 * @return
		 */
		public Builder setTakeProfitPrice(double takeProfitPrice) {
			this.takeProfitPrice = takeProfitPrice;
			return this;
		}

		/**
		 * how long order should live if not executed. 
		 * Only if > 0, then orderCommand should be 
		 * {@link OrderCommand#PLACE_BID} or {@link OrderCommand#PLACE_OFFER}
		 * 
		 * @param goodTillTime
		 * @return
		 * 
		 */
		public Builder setGoodTillTime(long goodTillTime) {
			this.goodTillTime = goodTillTime;
			return this;
		}

		/**
		 * @param comment Comment that will be saved in order
		 * @return
		 */
		public Builder setComment(String comment) {
			this.comment = comment;
			return this;
		}

		
		/**
		 * Last method in chain to build OrderTicket with the set parameters.
		 * @return {@link OrderTicket} with the set parameters.
		 */
		public OrderTicket build() {
			return new OrderTicket(this);
		}
	}	// end of Builder inner class
	
	private OrderTicket(Builder builder) {
		label = builder.label;
		instrument = builder.instrument;
		command = builder.command;
		amount = Rounding.lot(builder.amount);
		price = Rounding.pip(instrument, builder.price);
		slippage = builder.slippage;
		stopLossPrice = Rounding.pip(instrument, builder.stopLossPrice);
		takeProfitPrice = Rounding.pip(instrument, builder.takeProfitPrice);
		goodTillTime = builder.goodTillTime;
		comment = builder.comment;
	}
	
	public String getLabel() {
		return label;
	}
	public Instrument getInstrument() {
		return instrument;
	}
	public IEngine.OrderCommand getOrderCmd() {
		return command;
	}
	/**
	 * @return amount is implicitly rounded with {@link Rounder#lot(double) Rounder.lot}
	 */
	public double getAmount() {
		return amount;
	}
	/**
	 * @return price is implicitly rounded with {@link Rounder#pip(Instrument, double) Rounder.pip}
	 */
	public double getPrice() {
		return price;
	}
	public double getSlippage() {
		return slippage;
	}
	/**
	 * @return stop loss price is implicitly rounded with {@link Rounder#pip(Instrument, double) Rounder.pip}
	 */
	public double getStopLossPrice() {
		return stopLossPrice;
	}
	/**
	 * @return take profit price is implicitly rounded with {@link Rounder#pip(Instrument, double) Rounder.pip}
	 */
	public double getTakeProfitPrice() {
		return takeProfitPrice;
	}
	public long getGoodTillTime() {
		return goodTillTime;
	}
	public String getComment() {
		return comment;
	}

	private final String label;
	private final Instrument instrument;
	private final IEngine.OrderCommand command;
	private final double amount;
	private final double price;
	private final double slippage;
	private final double stopLossPrice;
	private final double takeProfitPrice;
	private final long goodTillTime;
	private final String comment;
}
