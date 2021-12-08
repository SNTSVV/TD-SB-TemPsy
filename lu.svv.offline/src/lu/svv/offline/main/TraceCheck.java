/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */


package lu.svv.offline.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import lu.svv.offline.check.CheckPackage;
import lu.svv.offline.check.Monitor;
import lu.svv.offline.check.impl.CheckFactoryImpl;
import lu.svv.offline.diagnostics.DiagnosticsPackage;
import lu.svv.offline.sBTemPsy.PropertiesBlock;
import lu.svv.offline.sBTemPsy.SBTemPsyPackage;
import lu.svv.offline.trace.Trace;
import lu.svv.offline.trace.TracePackage;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.helper.OCLHelper;


public class TraceCheck {
	private Monitor monitor;
	public static final String reportFunctionsFile = "lib/sb-tempsy-report.ocl";
	private OCL<EPackage, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, ?, ?> ocl;
	private OCLHelper<EClassifier, ?, ?, Constraint> oclHelper;
	private ResourceLoader tempsyLoader;
	private ResourceLoader traceLoader;
	public static final String ReportProperty = "let exp:sBTemPsy::AbstractProperty = self.properties.properties->at(1), subtrace:OrderedSet(trace::TraceElement) = getInputTraceElements(self.trace) in reportProperty(subtrace, exp)";
	
	// load TemPsy properties (XMI) and trace instances (CSV)
	public void loadMonitor(String tempsyFilePath, String traceFilePath) {
		tempsyLoader = XmiTemPsyLoader.init();
		PropertiesBlock properties = (PropertiesBlock) tempsyLoader.load(tempsyFilePath);

		traceLoader = CsvTraceLoader.init();
		Trace trace = (Trace) traceLoader.load(traceFilePath);
		
		if (properties != null && trace != null) {
			Monitor monitor = new CheckFactoryImpl().createMonitor();
			monitor.setProperties(properties);
			monitor.setTrace(trace);
			this.monitor = monitor;
		}
	}
	
	
	public void parseOCL() {
		// Copied from org.eclipse.ocl.ecore.tests.DocumentationExamples.java
		EPackage.Registry registry = new EPackageRegistryImpl();
		registry.put(CheckPackage.eNS_URI, CheckPackage.eINSTANCE);
		registry.put(SBTemPsyPackage.eNS_URI, SBTemPsyPackage.eINSTANCE);
		registry.put(TracePackage.eNS_URI, TracePackage.eINSTANCE);
		registry.put(DiagnosticsPackage.eNS_URI, DiagnosticsPackage.eINSTANCE);
		EcoreEnvironmentFactory environmentFactory = new EcoreEnvironmentFactory(registry);
		ocl = OCL.newInstance(environmentFactory);

		// get an OCL text file via some hypothetical API
		InputStream in = null;
		try {
			in = new FileInputStream(reportFunctionsFile);
			in.skip(267);
			ocl.parse(new OCLInput(in));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    try {
		    	in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		oclHelper = ocl.createOCLHelper();
		oclHelper.setContext(CheckPackage.Literals.MONITOR);
	}

	
//	function for Trace Diagnostics: SBTemPsy-Report procedure
	public String reportSingle() throws ParserException 
	{
		return ocl.evaluate(this.monitor, oclHelper.createQuery(ReportProperty)).toString();
	}
	
//	reset monitor for memory release
	public void resetMonitor() {
		this.monitor = null;
	}


}
