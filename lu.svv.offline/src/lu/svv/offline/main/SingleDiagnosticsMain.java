/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */


package lu.svv.offline.main;

import java.io.IOException;

import org.eclipse.ocl.ParserException;

public class SingleDiagnosticsMain {

	public static void main(String[] args) throws IOException, ParserException, Exception {
		String propertyFile =String.valueOf(args[0]);
		String traceFile =String.valueOf(args[1]);
		SBTemPsyReport tr = new SBTemPsyReport(propertyFile,traceFile);	 // instantiate SBTemPsyReport clss
		System.out.println(propertyFile+"\t"+traceFile);
		System.out.printf("Result is:"+tr.report());
		System.out.println("\n"+"*****");

	}
}
