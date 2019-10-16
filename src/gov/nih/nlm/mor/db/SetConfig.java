/*
 * This class is not used. It simply demonstrates how to 
 * retieve cuis by name using the REST API
 */

package gov.nih.nlm.mor.db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class SetConfig {
	
	final String url = "https://rxnav.nlm.nih.gov/REST/rxcui.json?name=";
	final String urlParams = "&srclist=rxnorm&allsrc=0&search=0";
	
	final String inUrl = "https://rxnav.nlm.nih.gov/REST/rxcui/";
	final String inUrlParams = "/related.json?tty=IN";
	public ArrayList<String> substances = new ArrayList<String>();
	
	public static void main(String args[]) {
		SetConfig config = new SetConfig();
		config.run(args[0]);
	}
	
	public void run(String filename) {
		readFile(filename);
		for( String substance : substances ) {
//			if( substance.equalsIgnoreCase("Yellow fever vaccine")) { 
//				System.out.println("HALT");
//			}
			System.out.print(substance + "\t");
			ArrayList<String> cuis = returnRxCodes(substance);
// This will print PINs (e.g., anyhdrous terms) if existing
//			for(int i=0; i < cuis.size(); i++) {
//				System.out.print(cuis.get(i));
//				if( i != cuis.size() - 1) {
//					System.out.print("|");
//				}
//			}
			for( String cui : cuis ) {
				ArrayList<String> inCuis = returnInCodes(cui);
				int size = inCuis.size();
				for(int j=0; j < size; j++) {
					System.out.print(inCuis.get(j) + "|");
				}
			}
			System.out.println();
		}
	}
	
	public ArrayList<String> returnInCodes(String cui) {
		ArrayList<String> codes = new ArrayList<String>();
		
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
						if( val.getString("tty").equals("IN") ) {
							if( val.has("conceptProperties")) {
								JSONArray conceptProperties = val.getJSONArray("conceptProperties");
								for(int j=0; j < conceptProperties.length(); j++) {
									JSONObject o = conceptProperties.getJSONObject(j);
									if( o.has("rxcui")) {
										String inCui = o.getString("rxcui");
										codes.add(inCui);
									}									
									
								}
							}
						}
					}
				}
			}
		}
		return codes;
	}
	
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
					line = line.trim();
					substances.add(line);
				}
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
		HttpsURLConnection connexion;
		BufferedReader reader;
		
		String line;
		String result="";
		url= new URL(URLtoRead);
	
		connexion= (HttpsURLConnection) url.openConnection();
		connexion.setRequestMethod("GET");
		reader= new BufferedReader(new InputStreamReader(connexion.getInputStream()));	
		while ((line =reader.readLine())!=null) {
			result += line;
			
		}
		
		JSONObject json = new JSONObject(result);
		return json;
	}		

}
