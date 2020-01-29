package gov.nih.nlm.mor.db.nflis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class NFLISSubstance {
	private String code = "";
	private String name = "";
	private ArrayList<String> synonyms = null;
	private ArrayList<String> rxcuis = null;
	
	public NFLISSubstance(String name, String synonyms) {
//		this.code = code;  - codes don't exist, don't make use of the phony ones
		this.name = name;
		//could do fancy parsing, but for now this is a curation issue
		this.synonyms = setSynonyms(synonyms);
		setCuis();
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	public ArrayList<String> getSynonyms(boolean lower) {
		ArrayList<String> list = new ArrayList<String>();
		if(lower) {
			synonyms.forEach(a -> {
				list.add(a.toLowerCase());
			});
			return list;
		}
		else {
			return this.synonyms;
		}
	}

	public ArrayList<String> setSynonyms(String synonymString) {
		ArrayList<String> synonymsList = new ArrayList<String>();
		String[] synonymArr = synonymString.split(";");
		for(int i=0; i < synonymArr.length; i++) {
			String synonym = synonymArr[i].trim();
			if( !synonym.isEmpty() && !synonymsList.contains(synonym) ) {
				synonymsList.add(synonym);
			}
		}
		
		return synonymsList;
	}	

	public ArrayList<String> getRxcuis() {
		return this.rxcuis;
	}

	public void addRxcui(String cui) {
		if( !rxcuis.contains(cui) ) {
			rxcuis.add(cui);
		}
	}
	
	private void setCuis() {
		this.rxcuis = new ArrayList<String>();
		
		String url = "https://rxnav.nlm.nih.gov/REST/rxcui.json?name=";
		String urlParams = "&srclist=rxnorm&allsrc=0&search=0";
		String cuiNameUrl = "";
				
		JSONObject result = null;
		try {
			String encodedString = URLEncoder.encode(this.name, StandardCharsets.UTF_8.toString());
			cuiNameUrl = url + encodedString + urlParams;					
			result = getresult(cuiNameUrl);
		} catch(Exception e) {
			System.out.println(cuiNameUrl);
			e.printStackTrace();
		}
		
		if( result != null ) {
			if( result.has("idGroup")) {
				JSONObject idGroup = result.getJSONObject("idGroup");
				if( idGroup != null ) {
					if( idGroup.has("rxnormId")) {
						JSONArray rxnormId = idGroup.getJSONArray("rxnormId");
						for( int i=0; i < rxnormId.length(); i++) {
							rxcuis.add(rxnormId.getString(i));
						}
					}
				}
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((rxcuis == null) ? 0 : rxcuis.hashCode());
		result = prime * result + ((synonyms == null) ? 0 : synonyms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NFLISSubstance other = (NFLISSubstance) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (rxcuis == null) {
			if (other.rxcuis != null)
				return false;
		} else if (!rxcuis.equals(other.rxcuis))
			return false;
		if (synonyms == null) {
			if (other.synonyms != null)
				return false;
		} else if (!synonyms.equals(other.synonyms))
			return false;
		return true;
	}
}
