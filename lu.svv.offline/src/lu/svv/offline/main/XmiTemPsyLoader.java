/**
 * Copyright by University of Luxembourg 2020-2021. 
 *   Developed by Chaima Boufaied, chaima.boufaied@uni.lu University of Luxembourg. 
 *   Developed by Claudio Menghi, claudio.menghi@uni.lu University of Luxembourg. 
 *   Developed by Domenico Bianculli, domenico.bianculli@uni.lu University of Luxembourg. 
 *   Developed by Lionel Briand, lionel.briand@uni.lu University of Luxembourg. 
 */


package lu.svv.offline.main;

import org.eclipse.emf.common.util.URI;

import lu.svv.offline.sBTemPsy.SBTemPsyPackage;

public class XmiTemPsyLoader extends XmiResource implements ResourceLoader {

	private static XmiTemPsyLoader instance;
	@Override
	public Object load(String TemPsyFilePath) {
		register(SBTemPsyPackage.eNS_URI, SBTemPsyPackage.eINSTANCE);
		return getContent(URI.createURI(TemPsyFilePath), true);
	}
	
	private XmiTemPsyLoader(){
		super();
	}
	
	public static XmiTemPsyLoader init() {
		if (instance == null) {
			instance = new XmiTemPsyLoader();			
		}
		return instance;
	}

}

