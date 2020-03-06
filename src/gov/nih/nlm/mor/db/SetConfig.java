/*
 * This class is not used. It simply demonstrates how to 
 * retieve cuis by name using the REST API
 */

package gov.nih.nlm.mor.db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import gov.nih.nlm.mor.RxNorm.RxNormIngredient;

@SuppressWarnings("unused")
public class SetConfig {
	
	// final String url = "https://rxnavstage.nlm.nih.gov/REST/rxcui.json?name=";
	final String url = "https://rxnavstage.nlm.nih.gov/REST/rxcui/";
	// final String urlParams = "&srclist=rxnorm&allsrc=0&search=0";
	final String urlParams = "/related.json?tty=PIN+IN";
	
	final String inUrl = "https://rxnavstage.nlm.nih.gov/REST/rxcui/";
	final String inUrlParams = "/related.json?tty=IN+PIN+BN";
	final String propUrl = "https://rxnavstage.nlm.nih.gov/REST/rxcui/";
	final String propUrlParams = "/property.json?propName=UNII_CODE";
	final String uniiUrl = "https://rxnavstage.nlm.nih.gov/REST";	
	final String uniiUrlParams = "/rxcui.json?idtype=UNII_CODE&id=";
	public ArrayList<String[]> substances = new ArrayList<String[]>();
	public ArrayList<String[]> spellings = new ArrayList<String[]>();
	private PrintWriter pw = null;
	
	public static void main(String args[]) {
		SetConfig config = new SetConfig();
		config.run(args[0]);
	}
	
	public void run(String filename) {
		try {
//			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("./unii-coverage-rx.txt")),StandardCharsets.UTF_8),true);
//			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("./rx-mod-nomod-ing-names.txt")),StandardCharsets.UTF_8),true);
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("./rx-unii-coverage.txt")),StandardCharsets.UTF_8),true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.print("Cannot create the printwriter");
			e.printStackTrace();
		}		
		readFile(filename);
	}
	
//		for( String[] substanceCodes : substances ) {
//			
//
//				
//				
//				
//				
//				
//				
//				
//				
//				
//				
//				
//				
//				
//				ArrayList<String> matchingCodes = new ArrayList<String>();
//				ArrayList<RxNormIngredient> ings = new ArrayList<RxNormIngredient>();
//				JSONObject idGroup = result.getJSONObject("idGroup");
//				if( idGroup != null ) {
//					if(idGroup.has("rxnormId") && !idGroup.isNull("rxnormId")) {
//						JSONArray rxnormId = idGroup.getJSONArray("rxnormId");
//						for(int i=0; i < rxnormId.length(); i++) {
//							String value = rxnormId.getString(i);
//							matchingCodes.add(value);
//						}
//					}
//				}
//				ArrayList<String> uniiCodes = new ArrayList<String>();
//				if(!matchingCodes.isEmpty()) {
//					uniiCodes.clear();
//					for(int i=0; i < matchingCodes.size(); i++) {
//						ings = returnIngs(matchingCodes.get(i));
//						if(!ings.isEmpty()) {
//							for(int j=0; j < ings.size(); j++) {
//								pw.print(ings.get(j).getName() + " (" + matchingCodes.get(i) + ")");
//								if(j + 1 < ings.size()) {
//									pw.print("|");
//								}
//								pw.flush();
//							}
//						}
//						JSONObject uniiResult = null;						
//						try {
//							//https://rxnav.nlm.nih.gov/REST/rxcui/3288/property.json?propName=UNII_CODE
//							String rxcui = matchingCodes.get(i);
//							//System.out.println(propUrl + rxcui + propUrlParams);
//							uniiResult = getresult(propUrl + rxcui + propUrlParams );
//						} catch(Exception e) {
//							e.printStackTrace();
//						}						
//						if(uniiResult != null ) {
//							if(uniiResult.has("propConceptGroup") && !uniiResult.isNull("propConceptGroup") ) {
//								JSONObject propConceptGroup = uniiResult.getJSONObject("propConceptGroup");
//								if(propConceptGroup.has("propConcept") && !propConceptGroup.isNull("propConcept")) {
//									JSONArray propConcept = propConceptGroup.getJSONArray("propConcept");
//									for(int k=0; k < propConcept.length(); k++ ) {
//										JSONObject val = propConcept.getJSONObject(k);
//										String uniiCode = val.getString("propValue");
//										uniiCodes.add(uniiCode);
//									}
//								}
//							}
//						}					
//					}	
//					if(!uniiCodes.isEmpty()) {
//						pw.print("\t");
//						for(int m=0; m < uniiCodes.size(); m++) {
//							pw.print(uniiCodes.get(m) + "|");
//							pw.flush();
//						}
//						pw.flush();
//					}
//					else {
//						pw.print("NO CODE");
//						pw.flush();
//					}
//					pw.println();	
//				}
//				else {
//					pw.println();
//				}
//			}
//			else {
//				pw.println();
//			}
			
//		}

		
		
		
		
		

//		Collections.sort(res);
//		for( String r : res ) {
//			System.out.println(r);
//		}
//		for( String[] spelling : spellings ) {
//			if( substance.equalsIgnoreCase("Yellow fever vaccine")) { 
//				System.out.println("HALT");
//			}
//			ArrayList<String> cuis = returnRxCodes(spelling[1]);
// This will print PINs (e.g., anyhdrous terms) if existing
//			for(int i=0; i < cuis.size(); i++) {
//				System.out.print(cuis.get(i));
//				if( i != cuis.size() - 1) {
//					System.out.print("|");
//				}
//			}
//			for( String cui : cuis ) {
//				ArrayList<String> inCuis = returnInCodes(cui);
//				int size = inCuis.size();
//				for(int j=0; j < size; j++) {
//					pw.println(spelling[1] + "|" + inCuis.get(j) + "|" + spelling[0]);
//				}
//			}
//			pw.close();
//		}
//	}
	
	public ArrayList<RxNormIngredient> returnIngs(String cui) {
		ArrayList<RxNormIngredient> ings = new ArrayList<RxNormIngredient>();
		
		JSONObject result = null;
		try {
			String cuiUrl = inUrl + cui + inUrlParams;
			result = getresult(cuiUrl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if( result != null ) {
			if( result.has("relatedGroup")) {
				JSONObject relatedGroup = result.getJSONObject("relatedGroup");
				if( relatedGroup.has("conceptGroup")) {
					JSONArray arr = relatedGroup.getJSONArray("conceptGroup");
					for(int i=0; i < arr.length(); i++) {
						JSONObject val = arr.getJSONObject(i);
//						if( val.getString("tty").equals("IN") || val.getString("tty").equals("PIN") || val.getString("tty").equals("BN") ) {
						if( val.getString("tty").equals("IN") || val.getString("tty").equals("PIN") ) {						
							if( val.has("conceptProperties")) {
								JSONArray conceptProperties = val.getJSONArray("conceptProperties");
								for(int j=0; j < conceptProperties.length(); j++) {
									JSONObject o = conceptProperties.getJSONObject(j);
									RxNormIngredient ing = new RxNormIngredient(o);
									ings.add(ing);
								}
							}
						}
					}
				}
			}
		}
		return ings;
	}
	
//	public ArrayList<String> returnInNames(String cui) {
//		ArrayList<String> names = new ArrayList<String>();
//		
//		JSONObject result = null;
//		try {
//			String cuiUrl = inUrl + cui + inUrlParams;
//			result = getresult(cuiUrl);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		if( result != null ) {
//			if( result.has("relatedGroup")) {
//				JSONObject relatedGroup = result.getJSONObject("relatedGroup");
//				if( relatedGroup.has("conceptGroup")) {
//					JSONArray arr = relatedGroup.getJSONArray("conceptGroup");
//					for(int i=0; i < arr.length(); i++) {
//						JSONObject val = arr.getJSONObject(i);
//						if( val.getString("tty").equals("IN") ) {
//							if( val.has("conceptProperties")) {
//								JSONArray conceptProperties = val.getJSONArray("conceptProperties");
//								for(int j=0; j < conceptProperties.length(); j++) {
//									JSONObject o = conceptProperties.getJSONObject(j);
//									if( o.has("rxcui")) {
//										String inCui = o.getString("rxcui");
//										cuis.add(inCui);
//									}									
//									
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		return names;
//	}	
	
	public ArrayList<String> returnRxCodes(String s) {
		
		ArrayList<String> cuis = new ArrayList<String>();
		
		JSONObject result = null;
		try {
			String encodedString = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
			String cuiNameUrl = url + encodedString + urlParams;					
			result = getresult(cuiNameUrl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if( result != null ) {
			if( result.has("idGroup")) {
				JSONObject idGroup = result.getJSONObject("idGroup");
				if( idGroup != null ) {
					if( idGroup.has("rxnormId")) {
						JSONArray rxnormId = idGroup.getJSONArray("rxnormId");
						for( int i=0; i < rxnormId.length(); i++) {
							cuis.add(rxnormId.getString(i));
						}
					}
				}
			}
		}
		
		return cuis;
	}	
	
	public void readFile(String filename) { 
		FileReader file = null;
		BufferedReader buff = null;
		try {
			file = new FileReader(filename);
			buff = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null)
					eof = true;
				else {
					String[] codes = line.split(" ");
					if(codes != null && codes.length>0) {
						for(int i=0; i<codes.length; i++) {
					JSONObject result = null;
					try {
						//String encodedSubstance = URLEncoder.encode(substance, StandardCharsets.UTF_8.toString());
						//result = getresult(url + encodedSubstance);
						result = getresult(url + codes[i] + urlParams);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					if(result != null ) {
						ArrayList<RxNormIngredient> ings = returnIngs(codes[i]);
						ings.forEach(x-> {
							pw.print(x.getName() + "|");
							pw.flush();
						});
//					String[] pair = line.split("\\|");
//					spellings.add(pair);
					}					
				}						
				}
				}
				pw.println();				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				file.close();
			} catch (Exception e) {
				System.err.println("Error reading the file " + filename);
				e.printStackTrace();
			}
		}						
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

}
