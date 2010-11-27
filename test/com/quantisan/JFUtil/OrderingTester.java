package com.quantisan.JFUtil;

import java.util.List;
import java.util.concurrent.*;
import com.dukascopy.api.*;

@Library("JFQuantisan.jar")
public class OrderingTester implements IStrategy {
	Ordering orderer;
	Logging logger;
	
	@Configurable("Instrument") public Instrument selectedInst = Instrument.EURJPY;
	
	@Override
	public void onStart(IContext context) throws JFException {		
		orderer = new Ordering(context, "Tester", 3);
		logger = new Logging(context.getConsole());
		
		IOrder order = null;
		logger.print("Placing bid");

		Future<IOrder> future = orderer.placeAsk(selectedInst, 
												 0.1, 112.00, 50, 0d);
//		try {
//			order = future.get();
//		}
//		catch (Exception ex) {
//			logger.printErr("Bid order not ready yet.", ex);				
//			return;
//		}
//		order.waitForUpdate(1000);
		logger.print("done onStart");
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		

	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar,
			IBar bidBar) throws JFException {
		if (period != Period.ONE_HOUR || instrument != selectedInst)	return;
		
		List<IOrder> orders = orderer.getOrders(selectedInst);
		for (IOrder order : orders)
			logger.printOrderInfo(order);
	}

	@Override
	public void onMessage(IMessage message) throws JFException {


	}

	@Override
	public void onAccount(IAccount account) throws JFException {

	}

	@Override
	public void onStop() throws JFException {
		//for (IOrder order : engine.getOrders())
			//engine.closeOrders(order);

	}

}
