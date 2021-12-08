/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */


package lu.svv.offline.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CommonFunctions {

	/**
	 * get the base name from an input file	
	 */
	protected String getFileBaseName(String fileName) {
	    int index = fileName.lastIndexOf('.');
	    if (index == -1) {
	        return fileName;
	    }
	    else {
	        return fileName.split("/")[2];
	    	}
		}

	protected void deleteDirectory(File file) {
		if (file.isDirectory()) {
            for (File f : file.listFiles()) {
            	f.delete();
            }
            file.delete();
        }
	}
	
	
	/**
	 * remove duplicates from merged traces
	 * @throws IOException
	 */
	protected void removeDuplicates(File file) throws IOException {
	    String input = null;
	    Scanner sc = new Scanner(file);
	    File tempFile = new File(file+ ".tmp");
	    PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
	    Set<String> set = new HashSet<String>();
	    while (sc.hasNextLine()) {
	       input = sc.nextLine();
	       if(set.add(input.split("\t")[0]+"\t"+input.split("\t")[1])) {
	          pw.println(input);
	          pw.flush();
	       }
	    }
	    sc.close();
	    pw.close();
	    file.delete();
	    tempFile.renameTo(file);   
		 }	

	/**
	 * remove duplicates from traces stored under CsvLogs folder
	 * @throws IOException
	 */
	protected void removeDuplicatesFromCsvLogs(File file) throws IOException {
	    String input = null;
	    Scanner sc = new Scanner(file);
	    File tempFile = new File(file+ ".tmp");
	    PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
	    Set<String> set = new HashSet<String>();
	    while (sc.hasNextLine()) {
	       input = sc.nextLine();
	       if(set.add(input.split(",")[0]+","+input.split(",")[1])) {
	          pw.println(input);
	          pw.flush();
	       }
	    }
	    sc.close();
	    pw.close();
	    file.delete();
	    tempFile.renameTo(file);   
	}


}
