package com.quantisan.JFUtil;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class OrdererTester implements IStrategy {
	private boolean isTrade = true;
	
	@Override
	public void onStart(IContext context) throws JFException {
		JForexContext.setContext(context);
		JForexAccount.setAccount(context.getAccount());

	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		

	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar,
			IBar bidBar) throws JFException {
		

	}

	@Override
	public void onMessage(IMessage message) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() throws JFException {
		// TODO Auto-generated method stub

	}

}
