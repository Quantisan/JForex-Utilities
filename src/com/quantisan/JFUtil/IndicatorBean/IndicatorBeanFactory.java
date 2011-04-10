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
	 * Moving Average Convergence-Divergence (MACD).
	 * 
	 * Example use to cast the output:
	 * 
	 * <pre>{@code 
		*double[][] macd = new double[3][];
		*macd[0] = (double[])objs[0];		// macd values
		*macd[1] = (double[])objs[1];		// signal values
		*macd[2] = (double[])objs[2];		// histogram values}</pre>
	 * 
	 * @author plam
	 * @return macd bean
	 */
	public static MovingAverageConvergenceDivergence getMovingAverageConvergenceDivergence() { return new MovingAverageConvergenceDivergence(); }
	
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
	 * Fast Stochasic Oscillator (STOCHF)
	 * @return fast stoch bean
	 */
	public static FastStochastic getFastStochastic() { return new FastStochastic(); }
	
	/**
	 * Stochastic Relative Strength Index (STOCHRSI)
	 * 
	 * @return stoch-RSI bean
	 */
	public static StochasticRelativeStrengthIndex getStochasticRSI() { return new StochasticRelativeStrengthIndex(); }
}
