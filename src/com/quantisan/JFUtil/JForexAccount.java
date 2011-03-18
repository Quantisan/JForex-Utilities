package com.quantisan.JFUtil;

import java.util.Currency;
import java.util.List;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

/**
 * Provides singleton access to JForex IAccount object
 * 
 * @author plam
 *
 */
public enum JForexAccount {
	INSTANCE;
	
	private IAccount account;
	//private double riskPct;
	
	private double maxEquity = Double.NEGATIVE_INFINITY;
	//private HashMap<Currency, Instrument> pairs = new HashMap<Currency, Instrument>();

	/**
	 * 
	 */
	private JForexAccount() {
	}
	
//	/**
//	 * @return the riskPct
//	 */
//	public static double getRiskPct() {
//		return INSTANCE.riskPct;
//	}
//
//	/**
//	 * @param riskPct Fraction of account to put at risk per position (0.0, 1.0]
//	 */
//	public static void setRiskPct(double riskPct) {
//		INSTANCE.riskPct = riskPct;
//	}
	
	/**
	 * @return the account
	 */
	public static IAccount getAccount() {
		return INSTANCE.account;
	}

	/**
	 * @deprecated use {@link #getCurrency()}
	 * @return the account currency
	 */
	@Deprecated public static Currency getAccountCurrency() {
		return INSTANCE.account.getCurrency();
	}
	
	/**
	 * @return the account currency
	 */
	public static Currency getCurrency() {
		return INSTANCE.account.getCurrency();
	}
	
	/**
	 * Set or update IAccount object.  For initializing and update in onAccount()
	 * 
	 * @param account the account to set
	 */
	public static void setAccount(IAccount account) {
		INSTANCE.account = account;
		INSTANCE.updateMaxEquity();
	}
	
	/**
	 * Maximum drawdown is calculated from peak of realised + unrealised gains to current equity.
	 * 
	 * @param maxDrawdown maximum drawdown in percent decimal, [0.0, 1.0]. 
	 * For example, max drawdown of 5% should be entered as 0.05
	 * 
	 * @return true if max drawdown is reached
	 */
	public static boolean isMaxDrawdownBroken(double maxDrawdown) {
		if (maxDrawdown < 0d || maxDrawdown > 1d) {
			throw new IllegalArgumentException("maxDrawdown must be [0.0, 1.0]");
		}
		INSTANCE.updateMaxEquity();
		return (INSTANCE.getDrawdown() < -maxDrawdown);	
	}
	
	private void updateMaxEquity() {
		if(getEquity() > this.maxEquity)
			this.maxEquity = getEquity();
	}
	
	/**
	 * 
	 * @return current drawdown in negative percentage, positive means profitable
	 */
	private double getDrawdown() {		
		return 1 - getEquity()/INSTANCE.maxEquity;
	}
	
	/**
	 * @return account equity
	 * 
	 */
	public static double getEquity() {
		return INSTANCE.account.getEquity();
	}
	
	/**
	 * Iterate through the list of opened orders and positions of an instrument
	 * to calculate the cummulative monetary amount (in account currency) 
	 * that is at risk. Only includes orders/positions with a stop loss price set.
	 * 
	 * @param instrument
	 * @return amount in account currency at risk, only include orders with stop loss set
	 * @throws JFException
	 */
	public static double getAmountAtRisk(Instrument instrument) throws JFException {
		double totalValueExposed = 0;
		
		List<IOrder> orders = Orderer.getOrders(instrument);
		for (IOrder order : orders) {
			if (order.getState() == IOrder.State.FILLED || 
				order.getState() == IOrder.State.OPENED ||
				order.getState() == IOrder.State.CREATED) 
			{			
				double stop = order.getStopLossPrice();
				if (stop != 0d) {
					double pipsExposed = order.getOpenPrice() - order.getStopLossPrice();
					pipsExposed *= order.isLong() ? 1d : -1d;
					totalValueExposed += pipsExposed * order.getAmount() * 1e6d;
				}
			}
		}
		return Pairer.convertValueToAccountCurrency(instrument, totalValueExposed);		
	}
}
