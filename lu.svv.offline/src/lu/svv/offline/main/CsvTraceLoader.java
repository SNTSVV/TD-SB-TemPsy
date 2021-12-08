/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */


package lu.svv.offline.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lu.svv.offline.sBTemPsy.SBTemPsyFactory;
import lu.svv.offline.sBTemPsy.Signal;
import lu.svv.offline.sBTemPsy.Value;
import lu.svv.offline.trace.Record;
import lu.svv.offline.trace.Trace;
import lu.svv.offline.trace.TraceElement;
import lu.svv.offline.trace.TraceFactory;

public class CsvTraceLoader  implements ResourceLoader {
	private static CsvTraceLoader instance;
	@Override
	public Object load(String traceFilePath) {
		TraceFactory tf = TraceFactory.eINSTANCE;
		Trace trace = tf.createTrace();
		
		SBTemPsyFactory sb=SBTemPsyFactory.eINSTANCE;
		
		try {
			List<TraceElement> traceElements = new ArrayList<TraceElement>();
			
			try (BufferedReader br = new BufferedReader(new FileReader(traceFilePath))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	
			    	TraceElement next = tf.createTraceElement();
			    	
			    
			    	List<String> strings=Arrays.asList(line.split(","));
			    	next.setGenerationTime(Double.parseDouble(strings.get(0)));
			    	
			    	for(int index=1; index<strings.size(); index=index+2) {
			    		Signal s=sb.createSignal();
			    		s.setId(strings.get(index));
			    		
			    		Record a=tf.createRecord();
			    		a.setKey(s);
			    		
			    		Value v=sb.createValue();
			    		v.setVal(Double.parseDouble(strings.get(index+1)));
			    		a.setValue(v);
			    		next.getSignalValue().add(a);
			    	}
			    	traceElements.add(next);
			     }
			} 
			   
			trace.getTraceElements().addAll(traceElements);
			
			return trace;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return trace;
	}

	
	
	
	private CsvTraceLoader(){
		
		super();
	}
	
	public static CsvTraceLoader init() {
		if (instance == null) {
			instance = new CsvTraceLoader();
		}
		return instance;
	}

}


