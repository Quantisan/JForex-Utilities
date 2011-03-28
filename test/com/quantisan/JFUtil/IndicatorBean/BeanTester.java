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
				
		StochasticRelativeStrengthIndex srBean = IndicatorBeanFactory.getStochasticRSI();
		Object[] objs = Indicating.calculateMultiDimension(instrument, Period.ONE_MIN, srBean, 1);

		double[][] sto = new double[2][];
		sto[0] = (double[])objs[0];		// %K values
		sto[1] = (double[])objs[1];		// %D values
		
		Printer.println(instrument.toString() + " sto K/D = " + sto[0][0]
		                                      + " / " + sto[1][0]);	
	}

	@Override
	public void onStart(IContext context) throws JFException {
		JForexContext.setContext(context);
		JForexAccount.setAccount(context.getAccount());
		
		Indicating.setFilter(Filter.WEEKENDS);
		Stochastic stochBean = IndicatorBeanFactory.getStochastic();

		Object[] objs = Indicating.calculateMultiDimension(Instrument.EURUSD, Period.ONE_MIN, stochBean, 1);

		double[][] sto = new double[2][];
		sto[0] = (double[])objs[0];
		sto[1] = (double[])objs[1];
		
		Printer.println(Instrument.EURUSD.toString() + " sto = " + sto[0][0]
		                                      + " / " + sto[1][0]);	
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
