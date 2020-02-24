package gov.nih.nlm.mor.unii;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;

import org.json.JSONObject;
import org.json.JSONTokener;

public class UniiCoverage {
	
	private ArrayList<Unii> uniis = new ArrayList<Unii>();
	private TreeMap<String, Unii> rx2Unii = new TreeMap<String, Unii>();
	private PrintWriter pw = null;
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UniiCoverage coverage = new UniiCoverage();
		coverage.config();
		coverage.run();
		coverage.cleanup();
	}



	private void run() {
		// TODO Auto-generated method stub
		
	}



	private void cleanup() {
		// TODO Auto-generated method stub
		
	}

	private void config() {
		try {
			pw = new PrintWriter(new File("./coverage-report.txt"));
		} catch(Exception e) {
			System.out.println("Unable to create the print writer.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static JSONObject getresult(String URLtoRead) throws IOException {
		URL url;
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		
		String line;
		String result="";
		url= new URL(URLtoRead);
	
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			reader= new BufferedReader(new InputStreamReader(connection.getInputStream()));	
			while ( (line = reader.readLine()) != null ) {
				result += line;
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if( reader != null ) {
				reader.close();
			}
			if( connection != null ) {
				connection.disconnect();
			}
		}
		
		JSONTokener jsonTokener = new JSONTokener(result);
        
        Object ob = jsonTokener.nextValue();

        final JSONObject rv;
        if (ob == JSONObject.NULL)
            rv = null;
        else
            rv = (JSONObject) ob; 		
		
		return rv;
	}			

}
