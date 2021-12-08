/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */

 
package lu.svv.offline.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class TracesPreprocessorAndInterpolator {
	/**
	 * instantiate the TracesMerger class to merge traces from RawTracesFolde 
	*/
	static TracesMerger merger= new TracesMerger();
	/**
	 * instantiate the TraceStatistics for computing interpolation/preprocessing statistics
	*/
	static TraceStatistics stats= new TraceStatistics();
	/**
	/**
	 * instantiate the CommonFunctions class to call commonly used functions by more than one class
	*/
	static CommonFunctions communFunct = new CommonFunctions();

	
	/**
	 * remove useless attributes from all traces under mergedTraces
	 * @throws ParseException 
	*/
	protected  void removeUselessAttributesAndConvertTimestamps(File mergedTracesFolder) throws ParseException  {
		File preProcFolder = new File("./PreProcRawData");
		if (!preProcFolder.exists()){
			preProcFolder.mkdirs();
		}
	   File[] directoryListing = mergedTracesFolder.listFiles();
	   if (directoryListing != null) {
	    for (File child : directoryListing) {
	    	if (!child.toString().contains("DS_Store") && child.toString().endsWith(".tsv")) {
		    	String traceBaseName=communFunct.getFileBaseName(child.toString().replace(".tsv", ""));
		    	try
		    	{
			    	BufferedReader br = new BufferedReader(new FileReader(child));
			    	BufferedWriter writer = new BufferedWriter(new FileWriter(preProcFolder+"/"+"trace"+traceBaseName+".csv", true));    	   	
			    	String line = "";
			    	while ((line = br.readLine()) != null) {
				    	String[] row = line.split("\t");
				    	writer.append(String.format("%.0f", merger.getTimestampInMicroSec(row[1]))+"," + row[0]+","+row[3]+"\n");
			    	}
			    	br.close();	
			    	writer.close();
		    	}    	
		    	catch(IOException ex) {
		    		ex.printStackTrace();
		    	}
	    	}	
	    }
	  }

	}
	
	/**
	 * get unique signals from a log 
	*/	
	protected  Set<String>  getUniqSignalsInOneFile(File file) {
		Set<String> uniqSignals=new HashSet<String>();
		try
    	{
	    	BufferedReader brsig = new BufferedReader(new FileReader(file));
	    	String line = "";
	    	while ((line = brsig.readLine()) != null) {
	    		String[] row = line.split(",");
	    		uniqSignals.add(row[1]);			    	
	    	}
	    	brsig.close();
    	}	
    	catch(IOException ex) {
    		ex.printStackTrace();
    	}
		return uniqSignals;
	}
	
	/**
	 * match all complex signals from a java expression: case of complex signals; 
	 */
	protected Set<String> getComplexSignalsFromJavaExpression(String javaExpression){
		Set<String> listOfComplexSignals=new HashSet<>();
		String complexSignals=javaExpression.replaceAll(
	          "[^a-zA-Z0-9]", ",");
		String [] allComplexSigs=complexSignals.replace(", ,", ",").split(",");
		for (int index=0; index < allComplexSigs.length; index++)
	    {
			if(!allComplexSigs[index].equals("Math")&& !allComplexSigs[index].equals("abs") &&!allComplexSigs[index].equals("sqrt") &&!allComplexSigs[index].equals("acos") && !allComplexSigs[index].isEmpty()) {
				listOfComplexSignals.add(allComplexSigs[index]);
			}
		}
		return listOfComplexSignals;
	}
	
	/**
	 * Get unique signals from an xmi property
	 * @throws IOException 
	 */
	protected  ArrayList<String> getUniqSignalsFromProperty(File property) throws IOException{
		/**
 		 * 	Loop over each property lines to retrieve all matched signal names
 		 */
 		BufferedReader br = new BufferedReader(new FileReader(property));
 		Set<String> propertySignals = new HashSet<String>();
 		String line = "";
 		String signalID="";
 		String javaExpression="";
    	while ((line = br.readLine()) != null ) {
			if (line.contains("<signal")) {
				String [] signalAttributes=line.split("\t");
				signalID=signalAttributes[1];
				javaExpression=signalAttributes[2];
				/**
	    		 * 	case of simple signals				
	    		 */
				if(javaExpression.split("=")[1].length()==2) {
					Pattern simpleSignalPattern = Pattern.compile("\"([^\"]*)\"", Pattern.DOTALL);  
	 		    	Matcher simpleSignalMatcher = simpleSignalPattern.matcher(signalID);
	 		    	while (simpleSignalMatcher.find()) {
	 		    		propertySignals.add(simpleSignalMatcher.group(1));
	 		    	}
				}
				else
				/**
	    		 * 	case of complex signals				
	    		 */
		    	if(javaExpression.split("=")[1].length()>2) {
		    		propertySignals.addAll(getComplexSignalsFromJavaExpression(javaExpression.split("=")[1]));	
		    	}
			}
	    }
	    br.close();
        return new ArrayList<>(propertySignals);
	}
	

	/**
	 * generated preprocessed traces stored under CsvLogs folder and generate the match.txt file stored under Match folder
	 * @throws IOException 
	*/
	protected void getTracesToInterpolate(File properties, File traces) throws IOException {	
		/**
		 * created CsvLogs and Match folders, if they are not created yet 
		*/
		File csvLogs = new File("./CsvLogs");
		if (!csvLogs.exists()){
			csvLogs.mkdirs();
		}		
		File matchFolder = new File("./Match");
		if (!matchFolder.exists()){
			matchFolder.mkdirs();
		}	
		
		File[] xmidirectoryListing = properties.listFiles();
		File[] csvdirectoryListing = traces.listFiles();
		if (properties != null) {
 		    for (File property : xmidirectoryListing) {
 		    	if (!property.toString().contains("DS_Store")){	    		
 		           ArrayList<String> uniqSignals = new ArrayList<>();
 		           uniqSignals.addAll(getUniqSignalsFromProperty(property));
 		           /**
 		    		 * 	Loop over traces 	
 		    		 */
 		   		if (traces != null) {
 		   		for (int i = 0; i < uniqSignals.size(); i++) {
        		    for (File trace : csvdirectoryListing) {
        		    	if(!trace.toString().contains("DS_Store")) {
        		    		/**
        		    		 * 	Loop over each trace lines to retrieve all records that match each signal name
        		    		 */
        		    		if (getUniqSignalsInOneFile(trace).containsAll(uniqSignals))
   		     		    	{		
   			     		    	String traceBaseName=communFunct.getFileBaseName(trace.toString());
   			     		    	String propertyBaseName=communFunct.getFileBaseName(property.toString());
   			     		    	try
   			     		    	{
   				     		    	BufferedReader brNew = new BufferedReader(new FileReader(trace));
   				     		    	String lineTrace = "";
   				     		    	while ((lineTrace = brNew.readLine()) != null) {
	   				     		    	String[] row = lineTrace.split(",");
	   		     		    			if (row[1].equals(uniqSignals.get(i)))
	   		     		    			{
	   					     		    	BufferedWriter writer = new BufferedWriter(new FileWriter(csvLogs+"/"+propertyBaseName.replace(".xmi", "")+traceBaseName.replace(".csv", "")+".csv", true)); 
	   					     		    	writer.append(row[0]+','+row[1]+','+row[2]+"\n");
	   					     		    	writer.close();
	   		     		    			} 	     		    			
   				     		    	}brNew.close();
   			     		    	}	    	
   			     		    	catch(IOException ex) {
   			     		    		ex.printStackTrace();
   			     		    	}
   		     		    	}		
        		     }
        		    }
 		       	}	
 		   	}	
 		          
 		    }
		}
 		     /**
 			 * generate match file to write all possible properties/ preprocessed traces combinations
 			 */		
 		    	System.out.println("generate Match/match.txt file...");
 		    	File[] preprocTraces = csvLogs.listFiles();
 		    	BufferedWriter writerMatch = new BufferedWriter(new FileWriter(matchFolder+"/"+"match.txt", true)); 
 		    	for (File pptrace : preprocTraces) {
 		    		String csv=communFunct.getFileBaseName(pptrace.toString()); 
 		    		String xmiProp=csv.split("trace")[0]; 
 		    		writerMatch.append(properties+"/"+xmiProp.replaceAll(".xmi", "")+".xmi"+"\t"+csvLogs+"/"+csv.replaceAll(".csv", "").replace(".xmi", "")+".csv"+"\n");		    	
		    	}
 		    	writerMatch.close();		    		    		
		}
		}

	 
	/**
	 * sort traces before applying any preprocessing
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */	
	protected void sort(File csvLogs) throws NumberFormatException, IOException  {
		File[] directoryListing = csvLogs.listFiles();
	    if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	File tempFile = new File(child+ ".tmp");
				FileReader fileReader = new FileReader(child);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String inputLine;
				ArrayList<String> lineList = new ArrayList<String>();
				while ((inputLine = bufferedReader.readLine()) != null) {
					lineList.add(String.format ("%.0f", Double.parseDouble(inputLine.split(",")[0]))+","+inputLine.split(",")[1]+","+inputLine.split(",")[2]);
				}
				fileReader.close();
				Collections.sort(lineList);
				FileWriter fileWriter = new FileWriter(tempFile);
				PrintWriter out = new PrintWriter(fileWriter);
				for (String outputLine : lineList) {
					out.println(String.format("%.0f", Double.parseDouble(outputLine.split(",")[0]))+","+outputLine.split(",")[1]+","+outputLine.split(",")[2]);
				}
				out.flush();
				out.close();
				fileWriter.close();
				child.delete();
				tempFile.renameTo(child);
				communFunct.removeDuplicatesFromCsvLogs(child);

			}
	    }
	}

	/**
	 * apply traces interpolation. Interpolation strategy depends on the required interpolation per property signal
	 * @throws IOException 
	 * @throws ParseException 
	 */	
	protected void applyInterpolation(File matchFile) throws IOException, ParseException{
		 BufferedReader br = new BufferedReader(new FileReader(matchFile));
		 String line="";
		 String xmiProperty="";
		 String csvLog="";
		 String signalID="";
		 String signalJExpression="";
		 String requiredInterpolation="";
		  while ((line = br.readLine()) != null) {
			  String artefacts[] = line.split("	");
			  xmiProperty=artefacts[0];
			  csvLog=artefacts[1];
			  File csvLogFile = new File(csvLog);
			  BufferedReader brxmi = new BufferedReader(new FileReader(xmiProperty));
			  /**interpolatedSignals is set of unique signals that were already interpolated. We constraint over
			   * the set elements to make sure each signal is interpolated only once.
			   *   
			   */
			  HashSet<String> interpolatedSignals = new HashSet<>();
			  Set<String> complexSignals = new HashSet<String>();
			  complexSignals.addAll(getUniqSignalsFromProperty(new File(xmiProperty)));
			  String xmiLine="";
			  while ((xmiLine = brxmi.readLine()) != null) {
				  /**
	 			 * retrieve all signals from the xmi property definition
	 			 */
				if (xmiLine.contains("<signal")) {
					String [] signalAttributes=xmiLine.split("\t");
					signalID=signalAttributes[1];
					signalJExpression=signalAttributes[2];
					requiredInterpolation=signalAttributes[3];
					 /**
		 			 * case1: simple signals; if no javaEpression (javaExpression="")
		 			 */	
					if (signalJExpression.split("=")[1].length()==2) {
						Pattern simpleSignalPattern = Pattern.compile("\"([^\"]*)\"");
		 		    	Matcher simpleSignalMatcher = simpleSignalPattern.matcher(signalID);
		 		    	if (simpleSignalMatcher.find() ){
		 		    		Pattern interpolationSimpleSignalPattern = Pattern.compile("\"([^\"]*)\"");
			 		    	Matcher interpolationMatcher = interpolationSimpleSignalPattern.matcher(requiredInterpolation);
			 		    	if (interpolationMatcher.find() && !interpolatedSignals.contains(simpleSignalMatcher.group(1))) {
			 		    		if (interpolationMatcher.group(1).equals("constant")) {
			 		    			 /**
			 			 			 * call pieceWiseCOnstantInterpolation function
			 			 			 */	
			 		    			System.out.println("interpolate "+xmiProperty+" with trace"+csvLog);
			 		    			pieceWiseConstantInterpolation(csvLogFile,simpleSignalMatcher.group(1));
			 		    		}
			 		    		else 
			 		    			if (interpolationMatcher.group(1).equals("linear")) {
			 		    				 /**
				 			 			 * call linearInterpolation function
				 			 			 */	
			 		    				System.out.println("interpolate "+xmiProperty+" with trace"+csvLog);
			 		    				linearInterpolation(csvLogFile,simpleSignalMatcher.group(1));
			 		    			}	 		    			
			 		    		}
			 		    	}
		 		    	interpolatedSignals.add(simpleSignalMatcher.group(1));
		 		    	complexSignals.remove(simpleSignalMatcher.group(1));
		 		    }
					
					else
						 /**
 			 			 * case2: complex signals, based on a java expression
 			 			 */	
					{ 
						for(String complexSig: complexSignals) {
							if(signalJExpression.contains(complexSig) && !interpolatedSignals.contains(complexSig))
							   { 
			 		    			Pattern interpolationComplexSignalPattern = Pattern.compile("\"([^\"]*)\"");
			 		    			Matcher interpolationMatcher = interpolationComplexSignalPattern.matcher(requiredInterpolation);
					 		    	if (interpolationMatcher.find()) {
						 		    	if (interpolationMatcher.group(1).equals("constant")) {
						 		    		System.out.println("interpolate "+xmiProperty+" with trace"+csvLog);
				 		    				pieceWiseConstantInterpolation(csvLogFile,complexSig);
				 		    				interpolatedSignals.add(complexSig);
					 		    			}
						 		    	else
						 		    		if (interpolationMatcher.group(1).equals("linear")) {
						 		    			System.out.println("interpolate "+xmiProperty+" with trace"+csvLog);
			 		 		    				linearInterpolation(csvLogFile,complexSig);
					 		    				interpolatedSignals.add(complexSig);
						 		 		    	}
					 		    	}
							   }		    	
						}
					}	 		    	
			}
 		}
			  brxmi.close();	 
			  interpolatedSignals.clear();
		  } br.close();		 
  }

	 /**
	 * retrieve the initial signals values; case of piece-wise constant interpolation
	 */
	protected void getTheInitialSignalsValuesForConstantInterpolation(File csvLog, Map<String,String> signalLastValueMap,  Map<String,Double> firstTimestamps) throws IOException {
	   BufferedReader br = new BufferedReader(new FileReader(csvLog));
	   String signalName="";
	   String signalValue= "";
	   Double firstTimestamp= 0.0;
	   /**
		*  I scan the file once to get the initial values for all the signals and the corresponding timestamps + I retrieve all records once
		*/
	   String line="";
	   while ((line = br.readLine()) != null) { 
			firstTimestamp=Double.parseDouble(line.split(",")[0]); 
			signalName=line.split(",")[1]; 
			signalValue=line.split(",")[2]; 
			if(!signalLastValueMap.keySet().contains(signalName)){ 
			signalLastValueMap.put(signalName, signalValue); 
			firstTimestamps.put(signalName, firstTimestamp); 
		}		
	}
		   br.close();	
	}
	
	/**
	 * piece-wise constant interpolation		
	 */
	protected void pieceWiseConstantInterpolation(File csvLog,String signalName) throws ParseException, IOException {
		   BufferedReader br = new BufferedReader(new FileReader(csvLog));
		   BufferedWriter writer = new BufferedWriter(new FileWriter(csvLog, true));
		   Map<String,String> signalLastValueMap=new HashMap<String,String>(); 
		   Map<String,Double> firstTimestamps=new HashMap<String,Double>(); 
		   ArrayList<String> allRecords=new ArrayList<String>(); 
		   /**
			* get all initial values of the signals and store existing records in the  input file once 
			*/
		   getTheInitialSignalsValuesForConstantInterpolation(csvLog,signalLastValueMap,firstTimestamps);		   
		   String signalValue= "";
		   Double firstTimestamp= 0.0;
		   /**
			* I scan the file once to get the initial values for all the signals and the corresponding timestamps + I retrieve all records once
			*/
		   String line="";
		   String sig="";
		   while ((line = br.readLine()) != null) {
			    allRecords.add(String.format ("%.0f", Double.parseDouble(line.split(",")[0]))+','+line.split(",")[1] +','+line.split(",")[2]); 
				firstTimestamp=Double.parseDouble(line.split(",")[0]); 
				sig=line.split(",")[1]; 
				signalValue=line.split(",")[2]; 
				if(!signalLastValueMap.keySet().contains(sig)){ 
				signalLastValueMap.put(sig, signalValue); 
				firstTimestamps.put(sig, firstTimestamp); 
			}				
		} 
		   br.close();
		   Collections.sort(allRecords);
		   /**
			* I add missing records
			*/
		  String lastRec="";
		  Set<Double> availableTimestamps = new HashSet<Double>();
		  Double firsttimestampPerSignal=0.0;
		  firsttimestampPerSignal=firstTimestamps.get(signalName);
		  availableTimestamps.add(firsttimestampPerSignal); 
		  lastRec=signalLastValueMap.get(signalName).toString();
		  Double currentTimeStamp=0.0;	
		  for (String element :allRecords) {
			   currentTimeStamp=Double.parseDouble(element.split(",")[0]); 
			   if (element.split(",")[1].equals(signalName)) {				   
				   lastRec=element.split(",")[2];
				   availableTimestamps.add(currentTimeStamp);
			   }	   
			   else if (!element.split(",")[1].equals(signalName) && !availableTimestamps.contains(currentTimeStamp) &&!allRecords.toString().contains(String.format ("%.0f", currentTimeStamp)+','+signalName) ) {				   
				   availableTimestamps.add(currentTimeStamp);
				   writer.write(String.format ("%.0f", currentTimeStamp)+','+signalName+','+lastRec+"\n");
			   }
		   } 
			   availableTimestamps.clear();  					   
		   writer.close();
		}	

	 /**
	* constraint on existence of more records after the current timestamp: function needed by the linear interpolation
	* @throws NumberFormatException
	* @throws IOException
	*/	
	protected  Integer noMoreRecords(File file,Double timestamp, String signal) throws NumberFormatException, IOException   {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String fileLine = "";
		Integer numberOfRecords = 0;
		while ((fileLine = br.readLine()) != null) {
			String[] row = fileLine.split(",");					 			
			if  (row[1].equals(signal) && Double.parseDouble(row[0]) > timestamp ){
				numberOfRecords++;
			} 					
		}	br.close();
		return  numberOfRecords;	
	}
	
	 /**
	* constraint on  non existence of a record at the current timestamp to avoid duplicating it to the preprcessed trace: function needed by the linear interpolation
	* @throws NumberFormatException
	* @throws IOException
	*/	
	protected Integer notAvailableRecordYet(File file,Double timestamp, String signal) throws NumberFormatException, IOException   {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String fileLine = "";
		Integer numberOfRecords = 0;
		while ((fileLine = br.readLine()) != null) {
			String[] row = fileLine.split(",");					  
			if  (row[1].equals(signal) && Double.parseDouble(row[0]) == timestamp ){ 
				numberOfRecords++;
			} 					
		}	br.close();
		return  numberOfRecords;
	}	

	protected void getTheInitialSignalsValuesForLinearInterpolation(File csvLog,Map<String,String> signalLastValueMap, Map<String,Double> firstTimestamps, Set<Double> allTimestamps) throws IOException {
		   BufferedReader br = new BufferedReader(new FileReader(csvLog));
		   String signalName="";
		   String signalValue= "";
		   Double firstTimestamp= 0.0;
		   /**
			* I scan the file once to get the initial values for all the signals and the corresponding timestamps + I retrieve all records once
			*/
		   String line="";
		   while ((line = br.readLine()) != null) { 
			    allTimestamps.add(Double.parseDouble(line.split(",")[0]));
				firstTimestamp=Double.parseDouble(line.split(",")[0]); 
				signalName=line.split(",")[1]; 
				signalValue=line.split(",")[2]; 
				if(!signalLastValueMap.keySet().contains(signalName)){ 
				signalLastValueMap.put(signalName, signalValue); 
				firstTimestamps.put(signalName, firstTimestamp);
			}		
		}   
		   br.close();	
	}
	
	 /**
	*  linear interpolation		
	*/
	protected void linearInterpolation(File csvLog, String signalName) throws ParseException, IOException {
		   Set<Double> allTimestamps = new HashSet<Double>();
		   Map<String,Double> firstTimestamps=new HashMap<String,Double>(); 
		   Map<String,String> signalLastValueMap=new HashMap<String,String>(); 
		   getTheInitialSignalsValuesForLinearInterpolation(csvLog,signalLastValueMap,firstTimestamps,allTimestamps);		
		   Set<Double> allSeenTimestamps = new HashSet<Double>();
		   Set<String> allRecords=new HashSet<String>(); 
		   /**
			*  /I add missing records
			*/
		  String lastRec="";
		  Set<Double> availableTimestamps = new HashSet<Double>();
		  Double firsttimestampPerSignal=0.0;
		  ArrayList<Double> missingTimestamps = new ArrayList<Double>(); 
		  firsttimestampPerSignal=firstTimestamps.get(signalName);
		  availableTimestamps.add(Double.parseDouble(String.format ("%.0f",firsttimestampPerSignal)));
		  allSeenTimestamps.add(Double.parseDouble(String.format ("%.0f",firsttimestampPerSignal)));
		  lastRec=signalLastValueMap.get(signalName).toString();
		  Double currentTimestamp=0.0;
		  String currentSignalValue="";			
		  String currentSignalName="";			
		  Double lastTimestamp=0.0;					   
		  BufferedReader brnew = new BufferedReader(new FileReader(csvLog));
		  String linenew="";
		  BufferedWriter writer = new BufferedWriter(new FileWriter(csvLog, true));
		  while ((linenew = brnew.readLine()) != null) {
			   writer.close();
			   writer = new BufferedWriter(new FileWriter(csvLog, true));
			   allRecords.add(String.format("%.0f", Double.parseDouble(linenew.split(",")[0]))+","+linenew.split(",")[1]);
			   currentTimestamp=Double.parseDouble(linenew.split(",")[0]); 
			   currentSignalName=linenew.split(",")[1]; 
			   currentSignalValue=linenew.split(",")[2]; 
			   allSeenTimestamps.add(Double.parseDouble(String.format ("%.0f",currentTimestamp)));
			   /**
				*  case of the target signal
				*/
			   if (currentSignalName.equals(signalName) ) {
				   /**
					*  corner case 1: no record at the first timestamp, so we add it.
					*/
				   if (!availableTimestamps.contains(Collections.min(allTimestamps)) && !allRecords.contains(String.format("%.0f",Collections.min(allTimestamps))+","+signalName)) {
					      allSeenTimestamps.add(Double.parseDouble(String.format ("%.0f",(Collections.min(allTimestamps)))));
					   	  availableTimestamps.add(Collections.min(allTimestamps));
						  writer.write(String.format ("%.0f", Collections.min(allTimestamps))+','+signalName+','+currentSignalValue+"\n");
						  lastTimestamp=Collections.min(allTimestamps);
						  lastRec=currentSignalValue;
						  allRecords.add(String.format("%.0f",Collections.min(allTimestamps))+","+signalName);
				   		}
			      else 								   
					   if (availableTimestamps.containsAll(allSeenTimestamps) ) {
						   allSeenTimestamps.add(currentTimestamp); 
						   lastTimestamp=currentTimestamp;
						   lastRec=currentSignalValue;
					   }								   
				   else 
					   /**
						*  case of missing records: I add them based on the interpolated value
						*/
				   {
					   allSeenTimestamps.add(Double.parseDouble(String.format ("%.0f",currentTimestamp)));
					   String usedValue=lastRec;
					   availableTimestamps.add(Double.parseDouble(String.format ("%.0f",currentTimestamp)));
					   missingTimestamps = (ArrayList<Double>) allSeenTimestamps.stream()
						            	.filter(elem -> !availableTimestamps.contains(elem))
						            	.collect(Collectors.toList());
					   TreeSet<Double> missingAll = new TreeSet<Double>(missingTimestamps); 
					   Double valueToInterpolate=0.0;
					   DecimalFormat valueToInterpolateTwoDecimalsOnly = new DecimalFormat("#.##");
					   for(Double missingTimestamp : missingAll) { 
						   if (currentTimestamp > lastTimestamp) { 
							   if (!allRecords.contains(String.format("%.0f",missingTimestamp)+","+signalName)) {	
								 valueToInterpolate=Double.parseDouble(usedValue)+(missingTimestamp-lastTimestamp)*((Double.parseDouble(currentSignalValue)-Double.parseDouble(usedValue))/(currentTimestamp-lastTimestamp));
								 availableTimestamps.add(Double.parseDouble(String.format ("%.0f",missingTimestamp)));
								 writer.write(String.format("%.0f",missingTimestamp)+","+signalName+','+valueToInterpolateTwoDecimalsOnly.format(valueToInterpolate).toString()+"\n");
								 allRecords.add(String.format ("%.0f",missingTimestamp)+","+signalName);
								 usedValue=valueToInterpolate.toString();
								 lastTimestamp=missingTimestamp;										
								}
					   		}
					   }
							 lastRec=currentSignalValue;
			   		}		 
			   }
			   
			   /**
				*  case of a different signal
				*/				   				   
			   else
				   if (!currentSignalName.equals(signalName)  ) 
				   {	
					   allSeenTimestamps.add(Double.parseDouble(String.format ("%.0f",currentTimestamp)));
				  	   missingTimestamps.add(Double.parseDouble(String.format ("%.0f",currentTimestamp)));	
				  	   /**
				  	    * if the current timestamp is not the last one 
				  	    */	
				  	   if  (currentTimestamp!=Collections.max(allTimestamps))
						{ 	
							 if (noMoreRecords(csvLog,currentTimestamp,signalName)==0 && notAvailableRecordYet(csvLog,currentTimestamp,signalName)==0 ) {
								 writer.write(String.format("%.0f",currentTimestamp)+","+signalName+','+lastRec+"\n");
								 allRecords.add(String.format ("%.0f",currentTimestamp)+","+signalName);
							 }
						}			  	 
				      else 
				    	  /**
					  	    * corner case 2: no record at the last timestamp, so we add it.  
					  	    */	
						  {	 if  (currentTimestamp!=Collections.max(allTimestamps))
							  	  writer.write(String.format("%.0f",currentTimestamp)+","+signalName+','+lastRec+"\n");
							      allRecords.add(String.format ("%.0f",Collections.max(allTimestamps))+","+signalName);
						  }	    
				   }
		  }
		  brnew.close();	
		  writer.close();
		  /**
	 	    * clear lists for the next signal
	 	    */
		   availableTimestamps.clear();
		   allSeenTimestamps.clear();
		   missingTimestamps.clear();
   }	
	
	 /**
	  * get  final traces stored under csvLogs; keep only the needed records from the traces
	  */	
	protected void getFinalLogs(File files) throws IOException {					
		/**
	  	 *  remove all records with smaller columns
	  	 */	
		File[] directoryListing = files.listFiles();
		for (File child : directoryListing) {
			 if(getUniqSignalsInOneFile(child).size()>1) { 
				 BufferedReader br = new BufferedReader(new FileReader(child));
				 File tempFile = new File(child+ ".tmp");
				 String line = "";
				  PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
				  ArrayList<String> listOfRecords = new ArrayList<String>();
				  while ((line = br.readLine()) != null) {
					  String[] row = line.split(",");	
			    		if (row.length==getUniqSignalsInOneFile(child).size() * 2+1) {
			    			listOfRecords.add(line.substring(0,line.indexOf(","))+line.substring(line.indexOf(","),line.length()));						        
						        }	 
				  }			
				  Collections.sort(listOfRecords);
				  for (String record: listOfRecords) {
					  pw.write(record.substring(0,record.indexOf(","))+record.substring(record.indexOf(","),record.length()-1)+"\n");
				  }		  
				  pw.flush();
				  pw.close();
			      br.close();
			      child.delete();
			      tempFile.renameTo(child);       
		 }
		 }
	}	
	
	/**
	* get unique timestamps from each log 
	*/	
	protected Set <String> getUniqTimestampsPerLog(File file) {
		 Set<String> timestamps = new HashSet<String>();
		 try
	   		{
			   	BufferedReader br = new BufferedReader(new FileReader(file));
			   	String line = "";
			   	while ((line = br.readLine()) != null) {
			   		String[] row = line.split(",");
			   		timestamps.add(row[0]);			    	
			   	}
			   	br.close();
	   		}			
	   	catch(IOException ex) {
	   		ex.printStackTrace();
	   	}
		return timestamps;
	}
	
	
	/**
	* merge the preprocressed traces, based on timestamps
	*/
	protected void mergeRecordsByTimestamp(File csvLogs) throws ParseException, IOException {
	   File[] directoryListing = csvLogs.listFiles();
	   if (directoryListing != null) {
		   for (File child : directoryListing) {
			   if (!child.toString().contains("DS_Store") && child.toString().endsWith(".csv"))
			    {	BufferedWriter writer = new BufferedWriter(new FileWriter(child, true));     			
	    			if(getUniqSignalsInOneFile(child).size()>1) {
		    			for (String timestamp: getUniqTimestampsPerLog(child)) {	
	    					writer.append(timestamp.toString()+','); 
	    				 	BufferedReader br = new BufferedReader(new FileReader(child));
	    			    	String line = "";
					    	while ((line = br.readLine()) != null) {   			
					    		String[] row = line.split(",");					  
								if (row[0].equals(timestamp)) 
									{
										writer.append(row[1]+','+row[2]+',');						    		
									}
					    	}	
					    	br.close();	    	
					    	writer.append('\n');
		    			}
	    			}	
	    			 writer.close();
		   		}	 
	    	}
	    }
	}
	
	 /**
	  * evaluate mathematical expressions from complex signals definitions
	  */
		protected void evaluateComplexSignals(File xmiProperties, File csvLogs) throws ScriptException, IOException{
			File[] xmiDirectoryListing = xmiProperties.listFiles();
			File[] csvDirectoryListing = csvLogs.listFiles();
		    if (xmiDirectoryListing != null) {		 
			   for (File xmiProperty : xmiDirectoryListing) { 
				   if (!xmiProperty.toString().contains("DS_Store") && xmiProperty.toString().endsWith(".xmi")) {
						 /**
						  * loop over traces
						  */
						 if (csvDirectoryListing != null) {
						    for (File csvLog : csvDirectoryListing) {
						    	/**
								  * loop only  in case the property and the trace match
								  */
						    	if (!csvLog.toString().contains("DS_Store") && csvLog.toString().endsWith(".csv")) {
						    		 if (communFunct.getFileBaseName(csvLog.toString()).contains(communFunct.getFileBaseName(xmiProperty.toString().replace(".xmi", "")+"trace"))) {
						    			 String xmiLine="";
										 String signalJExpression="";
						    			 String javaExpression="";
						    			 BufferedReader brxmi = new BufferedReader(new FileReader(xmiProperty));
						    			 /**
						    			 * read the xmi property once and retrieve all complex signals defined in a java expression
						    			//*/
						    			 Set<String> listOfUniqExpressions=new HashSet<>();
						    			 while ((xmiLine = brxmi.readLine()) != null) {
						    				if (xmiLine.contains("<signal")) {
						    					String [] signalAttributes=xmiLine.split("\t");
						    					signalJExpression=signalAttributes[2];
						    					System.out.println("JEEE "+signalJExpression+ " "+ xmiLine );
						    					if (signalJExpression.split("=")[1].length()>2) {
						    						javaExpression=signalJExpression.split("=")[1];
						    						listOfUniqExpressions.add(javaExpression);
						    					}
						    				 }
						    				}
						    				 brxmi.close(); 
						    				/**
						    				//* if the property contains complex signals
						    				//*/
						    					/**
						    					 * match all traces that have records based on the complex property signals
						    					//*/
				    							String csvLine="";											  
				    							BufferedReader brcsv = new BufferedReader(new FileReader(csvLog));
				    							BufferedWriter writer = new BufferedWriter(new FileWriter(csvLog, true)); 
				    							
				    							ScriptEngineManager mgr = new ScriptEngineManager();
				    							ScriptEngine engine = mgr.getEngineByName("JavaScript");
				    							for(String javaExp: listOfUniqExpressions){
				    								while ((csvLine = brcsv.readLine()) != null && !csvLine.contains("EVAL")) {
				    									String csignal="";
						    							String csignalValue="";
				    									javaExp=signalJExpression.split("=")[1];
				    									 Set<String> complexSignals = new HashSet<String>();
				    									 complexSignals.addAll(getComplexSignalsFromJavaExpression(javaExp));
					    								String row[]= csvLine.split(",");
					    								for (Integer index=1; index< row.length-1;index++) {
					    									for (String complexSignal: complexSignals) {
					    										if (row[index].equals(complexSignal)){
					    											csignal=complexSignal;
					    											csignalValue= row[index+1];
					    											javaExp.replaceAll(csignal, csignalValue);			
					    										}
					    									}
					    									javaExp=javaExp.replaceAll(csignal, csignalValue);	
					    								}
					    								String signalToAdd=javaExp;
					    								System.out.println(signalToAdd);
					    								for(String timestamp: getUniqTimestampsPerLog(csvLog)) {
					    									//DecimalFormat df = new DecimalFormat("#.##");
					    									//df.setMaximumFractionDigits(20);
					    									String eval=engine.eval(signalToAdd.replace("\"", "")).toString();	
					    									if(signalToAdd.toString().length()>2 && row[0].equals(timestamp)) {
					    										System.out.println(eval); 
					    										writer.write(csvLine+","+"EVAL"+"," +eval+", \n"); 						
					    									}
					    								}						  
				    								}
					    							brcsv.close();
						    					}	
				    							writer.close();			 
						    			}				    		 
						    		 }
						    }    								     
	
						  }
						}
				   }
			   }
		}
		
		/**
		* produce the final traces: case of properties with complex signals
		*/
		protected void getFinalTracesWithComplexSignals(File csvLogs) throws IOException {
			 File[] directoryListing = csvLogs.listFiles();
			 for (File child : directoryListing) {
				 BufferedReader br = new BufferedReader(new FileReader(child));
				 File tempFile = new File(child+ ".tmp");
				 String line = "";
				  PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
				  ArrayList<String> listOfRecords = new ArrayList<String>();
				  Set<Integer> recordEntries = new HashSet<Integer>();
				  /**
				   * get the maximum number of entries per log
				   */
				  while ((line = br.readLine()) != null) {
					  String[] row = line.split(",");
					  recordEntries.add(row.length);
				  }  
				  br.close();
				  /**
				   * scan each file and remove all records with entries less than the maximum number of entries per log
				   */
				  if (recordEntries.size() > 1) {
				  String linenew="";
				  BufferedReader brnew = new BufferedReader(new FileReader(child));
				  while ((linenew = brnew.readLine()) != null) {
					  String[] currentRow = linenew.split(",");
					  if(currentRow.length==Collections.max(recordEntries) ) {
						  listOfRecords.add(linenew.substring(0,linenew.indexOf(","))+linenew.substring(linenew.indexOf(","),linenew.length()-1));						  
					  }
				  } brnew.close();
				  Collections.sort(listOfRecords);
				  for (String record: listOfRecords) {
					  System.out.println(record);
					  System.out.println(record.substring(0,record.indexOf(","))+record.substring(record.indexOf(","),record.length()-1)+"\n");
					  pw.write(record.substring(0,record.indexOf(","))+record.substring(record.indexOf(","),record.length()-1)+"\n");
				  	}		  
				  pw.flush();
				  pw.close();
			      br.close();
			      child.delete();
			      tempFile.renameTo(child);          
				  }
			 }
			 /**
			   * remove all temporarily files
			   */
			 File fList[] = csvLogs.listFiles(); 
			 for (File f : fList) {
                   if (f.getName().endsWith(".tmp")) {
                       f.delete(); 
                   }}
			 }

	protected void tracesPreprocessingAndInterpolation(File rawTracesFolder, File properties) throws ParseException, IOException, ScriptException {
		/**
		 * delete all generated folders before any preprocessing
		 */	
		File mergedTraces = new File("./MergedTraces");
		File preprocFolder = new File("./PreProcRawData");
		File matchFolder = new File("./Match");
	    File csvLogsFolder = new File("./CsvLogs");
//	    communFunct.deleteDirectory(mergedTraces);
//	    communFunct.deleteDirectory(preprocFolder);	    
	    communFunct.deleteDirectory(matchFolder);
	    communFunct.deleteDirectory(csvLogsFolder);
		
		/**
		 *  step 0:  call the merger to merge traces from RawTraces and call the statistics to produce merge statistics
		 /*/	
//	    System.out.println("Merge traces: Generate MergedTraces folder...");
//		merger.mergeAllLogsBasedOnSimulationDuration(rawTracesFolder,mergedTraces);	
//		merger.writeToStatisticsFile(mergedTraces);	
		/**
		 *  step 1: Create PreProcRawData folder and store preprocessed traces into it
		 */	
//		System.out.println("keep relevant columns only from merged traces: Generate PreProcRawData folder...");
//	    removeUselessAttributesAndConvertTimestamps(mergedTraces);	
	 	/**
	    * step 2: Create Preprocessed traces stored under CsvLogs folder and generate MatchFiles folder
   	    */
	    System.out.println("generate traces under CsvFolder...");
		getTracesToInterpolate(properties,preprocFolder);	
		if(stats.getFolderTraces(csvLogsFolder)>0) {
			/**
			 *  step 3: sort logs under csvLogs and remove duplicated records from each of these logs
			 */	
			System.out.println("sort traces under CsvLogs folder...");
			sort(csvLogsFolder);		
			/**
			 *  step 4: apply interpolation
			 */	
			File matchFile = new File(matchFolder+"/match.txt");	
			System.out.println("get unique timestamps...");
			for (File csv: csvLogsFolder.listFiles()) {
				if (csv.toString().endsWith(".csv")){
					System.out.println(csv+"==="+ getUniqTimestampsPerLog(csv).size()); 
				}
			}
			System.out.println("apply interpolation...");
			System.out.println(matchFile);
			applyInterpolation(matchFile);	
			/**
			*  step 5: merge all records from the preprocessed traces under CsvLogs folder, based on similar timestamps and produce final logs
			*/	
			System.out.println("merge all CsvLogs records by timestamp...");
			mergeRecordsByTimestamp(csvLogsFolder);
			System.out.println("get final traces under CsvLogs folder: case of simple signals...");
			getFinalLogs(csvLogsFolder);
			/**
			*  step 6: case of complex signals: add a new signal in each row with a value that comes from the evaluation of the java expression
			*/	
			System.out.println("evaluate math expressions for complex signals...");
			evaluateComplexSignals(properties,csvLogsFolder);
			System.out.println("get final traces under CsvLogs folder: case of complex signals...");
			getFinalTracesWithComplexSignals(csvLogsFolder);
			System.out.println("DONE");
		  }
		  else System.out.println("No matched properties/traces...!");
	}
	
	
	public static void main(String[] args) throws IOException, ParseException, ScriptException {
		/**
		 *initialize the preprocessor
		 */	
	
		/**
		 * apply traces preprocessing and interpolation
		 */	
		TracesPreprocessorAndInterpolator preprocessor = new TracesPreprocessorAndInterpolator();
		/**
		 * preprocessor inputs
		 */	
	    File rawTracesFolder = new File("./RawTraces");	
		File properties = new File("./XmiProperties");
		/**
		 * call the preprocessor/ interpolator 
		 */			
		preprocessor.tracesPreprocessingAndInterpolation(rawTracesFolder, properties);
		/**
		 * write preprocessing/interpolation statistics
		 */	
		File mergedTraces = new File("./MergedTraces");
		File csvLogs = new File("./CsvLogs");
		//stats.createPreProcInterpolStatisticsFile(mergedTraces,csvLogs);
	}
}
