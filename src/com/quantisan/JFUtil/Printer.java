package com.quantisan.JFUtil;

/**
 * JForex printing functions
 * 
 * @author plam
 *
 */
public class Printer {
	private Printer() {}
	
	/**
	 * @param input to be printed to the JForex PrintStream
	 */
	public static void println(Object input) {
		JForexContext.getConsole().getOut().println(input);		
	}

	/**
	 * @param input to be printed to the JForex PrintStream
	 * @param ex the caught exception to be printed with stack trace
	 */
	public static void printErr(Object input, Exception ex) {
		ex.printStackTrace(JForexContext.getConsole().getErr());
		println(input);
	}

}
