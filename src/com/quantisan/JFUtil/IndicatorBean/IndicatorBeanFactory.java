package com.quantisan.JFUtil.IndicatorBean;

public class IndicatorBeanFactory {
	protected IndicatorBeanFactory() {};
	
	/**
	 * Average True Range (ATR)
	 * 
	 * @return ATR indicator bean
	 */
	public static AverageTrueRange getAverageTrueRange() { return new AverageTrueRange(); }
	
	/**
	 * Moving Average (MA), includes different implementations
	 * 
	 * @return moving average bean
	 */
	public static MovingAverage getMovingAverage() { return new MovingAverage(); }
	
	/**
	 * Ultimate Oscillator (UltOsc)
	 * 
	 * @return Ult Osc bean
	 */
	public static UltimateOscillator getUltimateOscillator() { return new UltimateOscillator(); }
	
	/**
	 * Stochastic Oscillator (STOCH)
	 * 
	 * @return stoch bean
	 */
	public static Stochastic getStochastic() { return new Stochastic(); }
	
	/**
	 * Stochastic Relative Strength Index (STOCHRSI)
	 * 
	 * @return stoch-RSI bean
	 */
	public static StochasticRelativeStrengthIndex getStochasticRSI() { return new StochasticRelativeStrengthIndex(); }
}
