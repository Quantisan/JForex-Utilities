package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.Period;
import com.quantisan.JFUtil.JForexAccount;
import com.quantisan.JFUtil.JForexContext;
import com.quantisan.JFUtil.Printer;

@Library("JFQuantisan.jar")
public class BeanTester implements IStrategy {

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar,
			IBar bidBar) throws JFException 
	{
		if (period != Period.TEN_SECS || instrument != Instrument.EURUSD)	return;
				
		MovingAverageConvergenceDivergence macdBean = IndicatorBeanFactory.getMovingAverageConvergenceDivergence();
		Object[] objs = Indicating.calculateMultiDimension(instrument, Period.ONE_MIN, macdBean, 1);

		double[][] macd = new double[3][];
		macd[0] = (double[])objs[0];		// macd values
		macd[1] = (double[])objs[1];		// signal values
		macd[2] = (double[])objs[2];		// histogram values
		
		Printer.println(instrument.toString() + " macd = " + macd[0][0]
		                                      + " / " + macd[1][0]
		                                      + " / " + macd[2][0]);	
	}

	@Override
	public void onStart(IContext context) throws JFException {
		JForexContext.setContext(context);
		JForexAccount.setAccount(context.getAccount());
		
		Indicating.setFilter(Filter.WEEKENDS);
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
	}

	@Override
	public void onAccount(IAccount account) throws JFException {
	}

	@Override
	public void onStop() throws JFException {
	}

}
