package gov.nih.nlm.mor.unii;

import java.util.ArrayList;
import java.util.HashMap;

public class Unii {
	private String uniiCode = "";
	private ArrayList<UniiName> uniiNames = new ArrayList<UniiName>();
	
	public Unii(String uniiCode, HashMap<String, UniiName> names) {
		this.uniiCode = uniiCode;
		processUniiNames(names);
	}
	
	public Unii() {	
	}
	
	public void processUniiNames(HashMap<String, UniiName> names) {
		for(String uniiCode : names.keySet()) {
			UniiName un = names.get(uniiCode);
			if(!uniiNames.contains(un)) {
				uniiNames.add(un);
			}
		}
	}
	
	public ArrayList<UniiName> getUniiNames() {
		return this.uniiNames;
	}
	
	public void addUniiName(UniiName un) {
		if( !uniiNames.contains(un) ) {
			uniiNames.add(un);
		}
	}

}
