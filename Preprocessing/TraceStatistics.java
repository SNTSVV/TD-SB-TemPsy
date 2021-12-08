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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ocl.ParserException;

public class TraceStatistics {
	
	/**
	 * instantiate the CommonFunctions class to call commonly used functions by more than one class
	*/
	CommonFunctions communFunct = new CommonFunctions();
		
	
		/**
		 * I. create mergeStats.txt statistics file to store information about the traces merge/ simulation duration
		*/
	protected void createMergeStatisticsFile(File traces, File directory,Double simulationDurationPerDirectory) throws IOException {
		BufferedWriter statisticsFile = new BufferedWriter(new FileWriter(traces+"/"+"mergeStats.txt", true));
		File statsReader= new File(traces+"/"+"mergeStats.txt");
		/**
		 * write the header of the statistics file only once
		*/
		if (statsReader.length()==0){
			 statisticsFile.write("*******Traces Merge*******"+"\n"); 
			 statisticsFile.write("#Entries,Simulation[h]"+"\n");
			}
		 DecimalFormat ignoreDecimals = new DecimalFormat("#.##");
		 statisticsFile.write(countLines(traces+"/"+communFunct.getFileBaseName(directory.toString()))+","+ignoreDecimals.format(simulationDurationPerDirectory)+"\n");	    
		 statisticsFile.close(); 
		}
	
	/**
	 * Create mergeStats.txt statistics file to store information about the traces merge/ simulation duration
	*/
protected void createFinalLogsStatisticsFile(File traces, File directory,Double simulationDurationPerLog) throws IOException {
	BufferedWriter statisticsFile = new BufferedWriter(new FileWriter(traces+"/"+"finalLogsStat.txt", true));
	File statsReader= new File(traces+"/"+"finalLogsStat.txt");
	/**
	 * write the header of the statistics file only once
	*/
	if (statsReader.length()==0){
		 statisticsFile.write("*******Final Logs Data*******"+"\n"); 
		 statisticsFile.write("#Entries,Simulation[h]"+"\n");
		}
	 DecimalFormat ignoreDecimals = new DecimalFormat("#.##");
	 statisticsFile.write(countLines(traces+"/"+communFunct.getFileBaseName(directory.toString()))+","+ignoreDecimals.format(simulationDurationPerLog)+"\n");	    
	 statisticsFile.close(); 
	}
	
	
	
	/**
	 * write to the statistics file: write information about the file traces stored under CsvLogs folder
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 */
	
	protected Set <Double> getUniqTimestampsFromFinalLogs(File rawTrace) throws NumberFormatException, IOException, ParseException {
		Set<Double> timestamps = new HashSet<Double>();
		BufferedReader br = new BufferedReader(new FileReader(rawTrace));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] row = line.split(",");
			timestamps.add(Double.parseDouble(row[0]));			    	
			}
		br.close();			
		return timestamps;
		}
	
	protected void writeStatisticsOfFinalLogs(File finalCsvLogs) throws IOException, NumberFormatException, ParseException {		
		/** 
    	 * 1-write to statistics file all information about the merged traces from a single sub-folder located under RawTraces folder
    	 */
		File csvLogFile = new File(finalCsvLogs+"/"+"finalLogsStat.txt");
		for (File csvLog:finalCsvLogs.listFiles()) {
			if(csvLog.toString().endsWith(".csv")) {
				Double simulationDurationPerLog=(Collections.max(getUniqTimestampsFromFinalLogs(csvLog))-Collections.min(getUniqTimestampsFromFinalLogs(csvLog)))/Double.parseDouble("3600000000");
				createFinalLogsStatisticsFile(finalCsvLogs,csvLog,simulationDurationPerLog);
			}
		}
		/** 
    	 * 2- write to statistics file all information about the merged traces from all the sub-folders located under MergedTraces folder
    	 */
		addInformationAfterTracesMerge(finalCsvLogs,csvLogFile);
	}
	
	
	
	
	protected void addInformationAfterTracesMerge(File mergedTraces,File csvLogFile) throws IOException {
		/**
		 * rewrite to mergeStats.txt: add information about the simulation duration per merged log
		 */ 
		
		/**
		 * 1- store all entries and the simulation durations from the mergedTraces in arrayLists to compute their statistics 
		 */ 
		 BufferedReader brStat = new BufferedReader(new FileReader(csvLogFile)); 
		 String statLine="";
		 ArrayList<Double> simulationDurations=new ArrayList<Double>();
		 ArrayList<Double> entries=new ArrayList<Double>();
			 while ((statLine = brStat.readLine()) != null ) {
				if (!statLine.contains("*") && !statLine.contains("#")) {
					String[] data=statLine.split(",");
					entries.add(Double.parseDouble(data[0]));
					simulationDurations.add(Double.parseDouble(data[1]));
				}
			}
			brStat.close();
		/**
		 * 2- add details about the simulation time of the merged traces
		 */ 
		BufferedWriter statisticsFile = new BufferedWriter(new FileWriter(csvLogFile+"/"+"", true));
		statisticsFile.write("******"+"\n");
		statisticsFile.write("min simulation duration[h]: "+Collections.min(simulationDurations)+"\n");
		statisticsFile.write("max simulation duration[h]: "+Collections.max(simulationDurations)+"\n");
		statisticsFile.write("avg simulation duration[h]: "+getAverage(simulationDurations)+"\n");
		statisticsFile.write("std dev simulation duration[h]: "+String.format("%.2f",getStandardDeviation(simulationDurations))+"\n");
		statisticsFile.write("******"+"\n");
		statisticsFile.write("min entries: "+String.format("%.2f",Collections.min(entries))+"\n");
		statisticsFile.write("max entries: "+String.format("%.2f",Collections.max(entries))+"\n");
		statisticsFile.write("avg entries: "+String.format("%.2f",getAverage(entries))+"\n");
		statisticsFile.write("std dev entries: "+String.format("%.2f",getStandardDeviation(entries))+"\n");
		statisticsFile.close();	
	}
		
	
	
	protected void addInformationAboutFinalLogs(File finalCsvLogs,File csvLogFile) throws IOException {
		/**
		 * rewrite to mergeStats.txt: add information about the simulation duration per merged log
		 */ 
		
		/**
		 * 1- store all entries and the simulation durations from the final csv files in arrayLists to compute their statistics 
		 */ 
		 BufferedReader brStat = new BufferedReader(new FileReader(csvLogFile)); 
		 String statLine="";
		 ArrayList<Double> simulationDurations=new ArrayList<Double>();
		 ArrayList<Double> entries=new ArrayList<Double>();
			 while ((statLine = brStat.readLine()) != null ) {
				if (!statLine.contains("*") && !statLine.contains("#")) {
					String[] data=statLine.split(",");
					entries.add(Double.parseDouble(data[0]));
					simulationDurations.add(Double.parseDouble(data[1]));
				}
			}
			brStat.close();
		/**
		 * 2- add details about the simulation time of the merged traces
		 */ 
		BufferedWriter statisticsFile = new BufferedWriter(new FileWriter(finalCsvLogs+"/"+"finalLogsStats.txt", true));
		statisticsFile.write("******"+"\n");
		statisticsFile.write("min simulation duration[h]: "+Collections.min(simulationDurations)+"\n");
		statisticsFile.write("max simulation duration[h]: "+Collections.max(simulationDurations)+"\n");
		statisticsFile.write("avg simulation duration[h]: "+getAverage(simulationDurations)+"\n");
		statisticsFile.write("std dev simulation duration[h]: "+String.format("%.2f",getStandardDeviation(simulationDurations))+"\n");
		statisticsFile.write("******"+"\n");
		statisticsFile.write("min entries: "+String.format("%.2f",Collections.min(entries))+"\n");
		statisticsFile.write("max entries: "+String.format("%.2f",Collections.max(entries))+"\n");
		statisticsFile.write("avg entries: "+String.format("%.2f",getAverage(entries))+"\n");
		statisticsFile.write("std dev entries: "+String.format("%.2f",getStandardDeviation(entries))+"\n");
		statisticsFile.close();	
	}
		
	
	
	
	
	
	
	
	
	
	
	
	 /**
		 * auxiliary functions for computing the average and the std deviation of the simulation duration/ entries from all the merged logs
		 */ 
		/**
		 * Count the number of lines per trace
		*/
	protected  int countLines(String filename) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    int lines = 0;
	    while (reader.readLine() != null) lines++;
	    reader.close();
	    return lines;
	}
	 /**
		 * compute average simulation time per merged log
		 */
	protected Double getAverage(ArrayList<Double> entries) {
		Double average=0.0;
		for(Double record:entries) {
			average+=record;
		 }
		return average/entries.size();
	}	
		 /**
		 * compute std deviation simulation time 
		 */
	protected Double getStandardDeviation(ArrayList<Double> entries) {
		 Double simulationAvg=getAverage(entries);
		 Integer numberOfRecords=entries.size();
		 Double numerator=0.0;
		 for(Double record:entries) {
			 numerator+=Math.pow(Math.abs(record-simulationAvg),2);
		 }
		 Double stdDev=Math.sqrt(numerator/numberOfRecords);		 
		 return stdDev;	 
	 }
	

	/**
	 *  II. create preProcInterpol.txt statistics about the initial/final statistics of the preprocessing/interpolation
	 * @throws IOException 
	 */
	protected void createPreProcInterpolStatisticsFile(File mergedTraces,File csvLogs) throws IOException {
		if(getFolderTraces(csvLogs)>0) {
			BufferedWriter statisticsFile = new BufferedWriter(new FileWriter(csvLogs+"/"+"preProcInterpol.txt", true));
			statisticsFile.write("*******Preprocessing/Interpolation*******"+"\n"); 
			statisticsFile.write("\n"+"Initial Stats"+"\n"+"*************"+"\n");
			statisticsFile.write("Number of traces: "+getFolderTraces(mergedTraces)+"\n");
			statisticsFile.write("Min entries: "+Collections.min(logsEntries(mergedTraces))+"\n");
			statisticsFile.write("Max entries: "+Collections.max(logsEntries(mergedTraces))+"\n");
			statisticsFile.write("Avg entries:: "+getAverage(logsEntries(mergedTraces))+"\n");
			statisticsFile.write("Std dev entries: "+getStandardDeviation(logsEntries(mergedTraces))+"\n");
			statisticsFile.write("\n"+"Final Stats"+"\n"+"*************"+"\n");
			statisticsFile.write("Number of traces: "+getFolderTraces(csvLogs)+"\n");
			statisticsFile.write("Min entries: "+Collections.min(logsEntries(csvLogs))+"\n");
			statisticsFile.write("Max entries: "+Collections.max(logsEntries(csvLogs))+"\n");
			statisticsFile.write("Avg entries:: "+getAverage(logsEntries(csvLogs))+"\n");
			statisticsFile.write("Std dev entries: "+getStandardDeviation(logsEntries(csvLogs))+"\n");
			statisticsFile.close();
		}
		else System.out.println("CsvLogs folder is empty...!");
	}
	
	
	
	
	/**
	 *  auxiliaury functions for the preprocessing/interpolation statistics
	 * @throws IOException 
	 */	
	protected ArrayList<Double> logsEntries(File logs) throws IOException {
		Double records=0.0;
		ArrayList<Double> allLogsEntries=new ArrayList<>();
		for (File file: logs.listFiles()) {
			if(file.getName().endsWith(".tsv") || file.getName().endsWith(".csv")) {
				records=Double.valueOf(countLines(file.toString()));
				allLogsEntries.add(records);
			}
		}
		return allLogsEntries;
	}
		
	protected Integer getFolderTraces(File folder) {
		Integer traces=0;
		for(File file: folder.listFiles()) {
			if (file.getName().contains(".tsv")|| file.getName().contains(".csv")) {
				traces++;
				}
		}
		return traces;
	}	
	
	public static void main(String[] args) throws IOException, ParserException, Exception {
		TraceStatistics tc = new TraceStatistics();		
		//File mergedTraces = new File("./MergedTraces");
		File csvLogs = new File("./CsvLogs");
		//tc.createPreProcInterpolStatisticsFile(mergedTraces,csvLogs);
		//File statsReader= new File("./CsvLogs/finalLogsStats.txt");
		//tc.addInformationAfterTracesMerge(csvLogs,statsReader);
		
		// add statistics for final preprocessed traces (entries, simulation times)
		tc.writeStatisticsOfFinalLogs(csvLogs);
	
	}




}
