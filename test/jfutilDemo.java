import java.util.*;
import com.dukascopy.api.*;
import com.quantisan.JFUtil.*;
import com.quantisan.JFUtil.IndicatorBean.*;

/*
 * Changelog:
 * March 17: 
 * 
 */

@Library("JFQuantisan.jar")		// place this file in your ../JForex/Strategy/files folder
public class jfutilDemo implements IStrategy {	
	private int counter = new Random().nextInt(100);
	
	@Override
	public void onStart(IContext context) throws JFException {
		// ** Essential steps **
		// must initialize objects once and for all
		JForexContext.setContext(context);
		JForexAccount.setAccount(context.getAccount());
		
		
		Set<Instrument> set = new HashSet<Instrument>(context.getSubscribedInstruments());       
		set = context.getSubscribedInstruments();	// get list of subscribed instruments
		// subscribe to transitional instruments for currency conversion calculations
		Pairer.subscribeTransitionalInstruments(set);
		// ** End of essential steps **
		
		Printer.println("-- Quantisan.com JFUtil v2.0 alpha: Usage demo --");
		Printer.println("");
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar,
			IBar bidBar) throws JFException {	
		if (period != Period.TEN_SECS || instrument != Instrument.EURUSD) 
			return;		// skipping most periods and instruments
		
		// *** 1. access IContext and IAccount from anywhere ***
		//Printer.println("Account equity = " + JForexAccount.getEquity());		

		// *** 2. simpler indicator use with intuitive method calls ***
		// get an EMA indicator value by building an indicator bean
		MovingAverage maBean = IndicatorBeanFactory.getMovingAverage();
		// then sets its parameters with obvious method names
		maBean.setAppliedPrice(IIndicators.AppliedPrice.MEDIAN_PRICE)
				.setMAType(IIndicators.MaType.EMA)
				.setWidth(14);		// all of these are optional parameters	
		// feed the bean into a generic calculation method to get the result
		double ema = Indicating.calculate(instrument, Period.ONE_MIN, maBean);		
		
		// printing the EMA value
		Printer.println(instrument.toString() + " EMA = " + ema);	
				
		// *** 3. Profit/loss calculation to account currency before placing your order ***
		// Demonstrating currency conversion
		double risk = 100 * Pairer.convertPipToAccountCurrency(instrument);
		String symbol = JForexAccount.getCurrency().getSymbol();
		Printer.println(symbol + risk + 
				" risked in for 1,000 units and 100 pips move in " + 
				instrument.toString());
		
		
		// ** 4. Simplify order parameters with order ticket builder ***
		// Demonstrating trade ordering		
		String label = instrument.toString().substring(0,2) + ++counter;
		OrderTicket mktBuyTicket = new OrderTicket			// order ticket
										.Builder(label, 	// setting required ticket info
												instrument, 
												IEngine.OrderCommand.BUY, 
												0.1)
										.build();	// build ticket
		Orderer.placeOrder(mktBuyTicket);	// placing order
				
		// market buy order with a 100 pips stop and 100 pips target
		double stopPrice = JForexContext.getPrice(instrument) - (100 * instrument.getPipValue());
		double targetPrice = JForexContext.getPrice(instrument) + (100 * instrument.getPipValue());
		label = instrument.toString().substring(0,2) + ++counter;
		OrderTicket buySpTicket = new OrderTicket
										.Builder(label, 
												instrument, 
												IEngine.OrderCommand.BUY, 
												0.1)
										.setStopLossPrice(stopPrice)	// set stop price to ticket
										.setTakeProfitPrice(targetPrice) // set target
										.build();
		// ** 5. Single method to placing orders for all order types and parameters ***
		Orderer.placeOrder(buySpTicket);	
	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		JForexAccount.setAccount(account);		// update IAccount to latest
	
	}

	@Override
	public void onStop() throws JFException {
		for (IOrder order : Orderer.getOrders())	// close all orders
			Orderer.close(order);
	}
	
	
	// methods below this line are not used
	
	
	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
	}



}
