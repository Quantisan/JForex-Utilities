package com.quantisan.JFUtil;

import java.util.concurrent.*;
import com.dukascopy.api.*;

@Library("JFQuantisan.jar")
public class OrderingTester implements IStrategy {
	Ordering orderer;
	Logging logger;
	
	@Override
	public void onStart(IContext context) throws JFException {		
		orderer = new Ordering(context, "Tester", 3);
		logger = new Logging(context.getConsole());
		
		IOrder order = null;
		logger.print("Placing bid");

		Future<IOrder> future = orderer.placeBid(Instrument.GBPUSD, 0.1, .10);
		try {
			order = future.get();
		}
		catch (Exception ex) {
			logger.printErr("Bid order not ready yet.", ex);				
			return;
		}
		order.waitForUpdate(1000);
		logger.print("Changing stop");
		orderer.setStopLoss(order, 1.6016d, 50d);
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		

	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar,
			IBar bidBar) throws JFException {
		if (period != Period.TEN_SECS || instrument != Instrument.GBPUSD)	return;
		

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
