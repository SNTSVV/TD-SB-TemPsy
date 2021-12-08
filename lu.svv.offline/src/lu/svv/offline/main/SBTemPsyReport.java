/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */


package lu.svv.offline.main;


import org.eclipse.ocl.ParserException;


public class SBTemPsyReport {

	private final String propertyFile;
	private final String traceFile;
	
	private boolean result;
	
	private TraceCheck tc;
	
	/**
	 * Creates a new TemPsyCheck checker
	 * @param propertyFile the path of the property file to be checked
	 * @param traceFile the path of the property file to be checked
	 * @throws Exception 
	 * @throws IllegalArgumentException if one of the parameter is null
	 */
	public SBTemPsyReport(String propertyFile, String traceFile) throws Exception {
		if(propertyFile==null) {
			throw new IllegalArgumentException("The property file cannot be null");
		}
		if(traceFile==null) {
			throw new IllegalArgumentException("The trace file cannot be null");
		}

		this.propertyFile=propertyFile;
		this.traceFile=traceFile;
		 tc= new TraceCheck();
		// Load OCL functions once
		tc.parseOCL(); // fixed		
		// getting the parameters:
		tc.loadMonitor(propertyFile, traceFile); 
		System.out.println("Trace loaded!");
	}
	
	
	/**
	 * Runs the TemPsyCheck checker
	 * @return true if the property is satisfied, false otherwise
	 * @throws ParserException 
	 */

	public String report() throws ParserException {
		long startTime = System.currentTimeMillis();
		String vInfos=tc.reportSingle();// SB-TemPsy-Report procedure
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("%.2f ms\t", elapsedTime / 1.0);
		// Reset the monitor instance for memory release
		tc.resetMonitor(); 
		System.gc();
		System.out.println();
		return vInfos;
	
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	
}
