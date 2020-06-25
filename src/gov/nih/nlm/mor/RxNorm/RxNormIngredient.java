package gov.nih.nlm.mor.RxNorm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class RxNormIngredient implements java.io.Serializable {

	private static final long serialVersionUID = 5772459584322752041L;
	private Integer rxcui = null;
	private String name = null;
	private String synonym = null;
	private String tty = null;
	private String language = null;
	private String suppress = null;
	private String umlscui = null;
	private boolean isPIN = false;
	private boolean isAllergenic = false;
	private Vector<Long> snomedCodes = new Vector<Long>();
	
	public RxNormIngredient(Integer rxcui, String name) {
		this.rxcui = rxcui;
		this.name = name;
	}
	
	public RxNormIngredient(JSONObject b) {
		this.rxcui = new Integer(b.get("rxcui").toString());
		this.name = b.get("name").toString();
		this.synonym = b.get("synonym").toString();
		this.tty = b.get("tty").toString();
		this.language = b.get("language").toString();
		this.suppress = b.get("suppress").toString();
		this.umlscui = b.get("umlscui").toString();
//		JSONObject allRelated = null;
//		JSONObject allProperties = null;
//		JSONObject rxHistory = null;		
//				
//		try {
//			
//			//TODO: Make these configurable
////			allRelated = getresult("https://rxnavdev.nlm.nih.gov/REST/rxcui/" + rxcui + "/allrelated.json");
//			allProperties = getresult("https://rxnavdev.nlm.nih.gov/REST/rxcui/" + rxcui + "/allProperties.json?prop=all");
////			rxHistory = getresult("https://rxnavdev.nlm.nih.gov/REST/rxcuihistory/concept.json?rxcui=" + rxcui );
//		} catch (IOException e) {
//			System.out.println("Unable to finish building SCD for rxcui " + rxcui);
//			e.printStackTrace();
//		}
//		
//		if( allProperties != null ) {
//			if(allProperties.get("propConceptGroup") != null ) {
//				JSONObject propConceptGroup = (JSONObject) allProperties.get("propConceptGroup");
//				JSONArray conceptGroup = (JSONArray) propConceptGroup.get("propConcept");
//				for( int i=0; i < conceptGroup.length(); i++ ) {
//					JSONObject element = (JSONObject) conceptGroup.get(i);
//					if( element.getString("propCategory").equals("CODES") ) {
//						String propName = element.getString("propName");
//						if( propName.equals("SNOMEDCT") ) {
//							String propValue = element.getString("propValue");
//							snomedCodes.add(new Integer(propValue));
//						}
//					}
//				}
//			}
//		}
	}
	
	public static JSONObject getresult(String URLtoRead) throws IOException {
		URL url;
//		HttpsURLConnection connexion;
		HttpURLConnection connexion;		
		BufferedReader reader;
		
		String line;
		String result="";
		url= new URL(URLtoRead);
	
//		connexion= (HttpsURLConnection) url.openConnection();
		connexion = (HttpURLConnection) url.openConnection();		
		connexion.setRequestMethod("GET");
		reader= new BufferedReader(new InputStreamReader(connexion.getInputStream()));	
		while ((line =reader.readLine())!=null) {
			result += line;
			
		}
		
		JSONObject json = new JSONObject(result);
		return json;
	}
	
	public RxNormIngredient() {
		
	}
	
	public void setSnomedCodes() {
		JSONObject allSnomedCodes = null;
		String cuiString = String.valueOf(this.rxcui);
		try {
			allSnomedCodes = getresult("https://rxnav.nlm.nih.gov/REST/rxcui/" + cuiString + "/property.json?propName=SNOMEDCT");			
		}
		catch(Exception e) {
			System.out.println("Unable to fetch snomed codes for rxcui: " + cuiString);
			e.printStackTrace();
		}
		
		if( !allSnomedCodes.isNull("propConceptGroup") ) {
			JSONObject propConceptGroup = (JSONObject) allSnomedCodes.get("propConceptGroup");
			JSONArray propConceptArr = (JSONArray) propConceptGroup.get("propConcept");
			for( int i=0; i < propConceptArr.length(); i++ ) {
				JSONObject conceptValue = (JSONObject) propConceptArr.get(i);
				Long codeToAdd = new Long(conceptValue.get("propValue").toString());
				this.snomedCodes.add(codeToAdd);
			}
		}
	}
	
	public Vector<Long> getSnomedCodes() {
		return this.snomedCodes;
	}	
	
	public void print() {
		System.out.println(" Ingredient(s):");
		System.out.println("\t" + this.getRxcui().toString() + " : " + this.getName());
		for( Long sc : this.snomedCodes ) {
			System.out.println("\t\tSnomed Code => " + sc.toString() );
		}
	}
	
	public void setAllergenic(boolean b) {
		this.isAllergenic = b;
	}
	
	public boolean getIsAllergenic() {
		return this.isAllergenic;
	}
	
	public void setPIN(boolean b) {
		this.isPIN = b;
	}
	
	public boolean getPIN() {
		return this.isPIN;
	}
	
	public Integer getRxcui() {
		return this.rxcui;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSynonym() {
		return this.synonym;
	}
	
	public String getTty() {
		return this.tty;
	}
	
	public String getLanguage() {
		return this.language;
	}
	
	public String getSuppress() {
		return this.suppress;
	}
	
	public String getUmlscui() {
		return this.umlscui;
	}

}
