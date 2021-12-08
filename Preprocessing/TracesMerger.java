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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TracesMerger {
	/**
	 * instantiate the TraceStatistics class to compute merge statistics right after merging traces
	*/
	TraceStatistics mergeStat= new TraceStatistics();
	/**
	 * instantiate the CommonFunctions class to call commonly used functions by more than one class
	*/
	static CommonFunctions communFunct = new CommonFunctions();
		
	/**
	 * Get the unique timestamps from a single raw trace
	 * @throws NumberFormatException 
	 * @throws IOException 
	 *  * @throws ParseException 
	*/
	protected Set <Double> getUniqTimestampsFromMergedLog(File rawTrace) throws NumberFormatException, IOException, ParseException {
		Set<Double> timestamps = new HashSet<Double>();
		BufferedReader br = new BufferedReader(new FileReader(rawTrace));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] row = line.split("\t");
			timestamps.add(getTimestampInMicroSec(row[1]));			    	
			}
		br.close();			
		return timestamps;
		}
		
	 /**
	 * create file to merge all .tsv files from each directory. The merge is done without considering the sort of merged records based on timestamps. 
	 * @throws ParseException 
	 * @throws IOException 
	*/	
	protected  void mergeTracesWithinSingleFolder(File mergedTraces,File directory) throws IOException, ParseException  {
		 BufferedWriter logWithMergedTraces = new BufferedWriter(new FileWriter(mergedTraces+"/"+directory.getName().toString()+".tsv", true)); 		 
		 File[] logsPerDir = directory.listFiles();
		 for (File log : logsPerDir) {
			 String tsvLine=""; 
			 if(!log.toString().contains("DS_Store")) {
				 BufferedReader brLog = new BufferedReader(new FileReader(log));
				 while ((tsvLine = brLog.readLine()) != null) {
					 logWithMergedTraces.write(tsvLine+"\n");
				 		}
			 	brLog.close();
				} 					    	
		 }	 
		 logWithMergedTraces.close();
		}	
	
	/**
	 * merge traces from one directory in a single .tsv file, that is then stored under MergedTraces folder.  
	 * @throws ParseException 
	 * @throws IOException 
	*/
	protected  void mergeAllLogsBasedOnSimulationDuration(File rawTracesFolder, File mergedTraces) throws ParseException, IOException  {
		/**
		 * create mergedTraces folder if it does not exist yet
		*/			
		if (!mergedTraces.exists()){
			mergedTraces.mkdirs();
		}
		
		File[] allDirectories = rawTracesFolder.listFiles(); 
		
		/**
		 * loop over the rawTraces sub-directories
		*/
		 for (File directory:allDirectories) {
		 	/**
		 	 * call the traces merger per directory
			*/
			 if ( !directory.toString().contains("DS_Store") && directory.listFiles().length>0 ) {
				 mergeTracesWithinSingleFolder(mergedTraces,directory);
			 	/**
				 * remove duplicates from the merged traces
				 */
		    	 File allMergedInOneDir = new File(mergedTraces+"/"+communFunct.getFileBaseName(directory.toString())+".tsv");
		    	 removeDuplicatesFromLog(allMergedInOneDir);
			 }
		 }
	}
			
	/**
	 * write to the statistics file: write information about the merged traces from a single sub-folder located under RawTraces folder
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 */
	protected void writeToStatisticsFile(File mergedTraces) throws IOException, NumberFormatException, ParseException {		
		/** 
    	 * 1-write to statistics file all information about the merged traces from a single sub-folder located under RawTraces folder
    	 */
		File csvLogFile = new File(mergedTraces+"/"+"mergeStats.txt");
		for (File mergedTrace:mergedTraces.listFiles()) {
			if(mergedTrace.toString().endsWith(".tsv")) {
				Double simulationDurationPerDirectory=(Collections.max(getUniqTimestampsFromMergedLog(mergedTrace))-Collections.min(getUniqTimestampsFromMergedLog(mergedTrace)))/Double.parseDouble("3600000000");
				mergeStat.createMergeStatisticsFile(mergedTraces,mergedTrace,simulationDurationPerDirectory);
			}
		}
		/** 
    	 * 2- write to statistics file all information about the merged traces from all the sub-folders located under MergedTraces folder
    	 */
		mergeStat.addInformationAfterTracesMerge(mergedTraces,csvLogFile);
	}
	
	/** 
	 * get the unique timestamps in mircroseconds given a specific date format
	 * @throws ParseException
	 */
	protected  Double getTimestampInMicroSec(String timestamp) throws ParseException {
		/**
		 * Specifying the pattern of input date and time
		 */
    	SimpleDateFormat sdf = new SimpleDateFormat("YYYY.DDD.HH.MM.ss.SSSSSS");
    	Calendar calendar = Calendar.getInstance();
    	java.util.Date date = sdf.parse(timestamp); 
 	   	calendar.setTime(date);
 	   	return (double) (System.currentTimeMillis()*1000-calendar.getTimeInMillis()*1000);  
	}
			
	/**
	 *remove duplicates from merged traces, and ignore empty ones
	 *@throws IOException			
	 */
	protected void removeDuplicatesFromLog(File mergedTrace) throws IOException {
		/**
		 *delete trace if it is empty
		 */		
		if(mergedTrace.length()==0) {
				mergedTrace.delete();
			}
		else { 
			communFunct.removeDuplicates(mergedTrace);		
			 }	
		 }	

	
	public static void main(String[] args) throws IOException, ParseException {
		/**
		 * instantiate the TracesMerger class:
		 */	
		TracesMerger merger=new TracesMerger();
		/**
		 * delete all generated folder before any new merge:
		 */	
		File mergedTraces = new File("./MergedTraces");
		communFunct.deleteDirectory(mergedTraces);		
		/**
		 * merge all traces from one folder in one trace 
		 */	
		File rawTracesFolder = new File("./RawTraces");
		merger.mergeAllLogsBasedOnSimulationDuration(rawTracesFolder,mergedTraces);	
		/**
		 * compute the merge statistics
		 */	
		merger.writeToStatisticsFile(mergedTraces);
	}
}	
