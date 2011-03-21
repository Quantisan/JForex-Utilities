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
	 * @return UltOsc bean
	 */
	public static UltimateOscillator getUltimateOscillator() { return new UltimateOscillator(); }
}
