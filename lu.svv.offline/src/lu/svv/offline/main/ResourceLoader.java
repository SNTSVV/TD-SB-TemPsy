/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */

package lu.svv.offline.main;

public interface ResourceLoader {
	public Object load(String filePath);
}
