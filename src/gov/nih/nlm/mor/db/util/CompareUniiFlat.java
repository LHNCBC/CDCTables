package gov.nih.nlm.mor.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import gov.nih.nlm.mor.db.dea.DEASchedule;
import gov.nih.nlm.mor.db.dea.DEASubstance;
import gov.nih.nlm.mor.db.nflis.NFLISCategory;
import gov.nih.nlm.mor.db.nflis.NFLISSubstance;

public class CompareUniiFlat {
	public PrintWriter pw = null;
	public String type = "";
	public ArrayList<String> uniiSubstances = new ArrayList<String>();
	public ArrayList<String> sourceSubstances = new ArrayList<String>();
	public ArrayList<DEASubstance> deaSubstances = new ArrayList<DEASubstance>();
	public ArrayList<NFLISSubstance> nflisSubstances = new ArrayList<NFLISSubstance>();
	public ArrayList<String> results = new ArrayList<String>();
	public String result = "";
	public boolean pvMatch = false;
	final public String uniiFileLocation = new String("Q:\\git\\CDCTables\\config\\rawdata\\uniiSubstances.txt");  //or wherever you place your input
	

	public static void main(String[] args) {
		CompareUniiFlat compare = new CompareUniiFlat();
		if( args.length == 3 ) {
			compare.config(args[2]);
			compare.run(args[0], args[1]);
			compare.cleanup();
		}
		else {
			System.exit(-1);
			compare.printHelp();
		}
	}
	
	private void config(String filename) {
		try {
			pw = new PrintWriter(new File(filename));
		} catch(Exception e) {
			System.err.println("Trouble creating the output file " + filename);
			e.printStackTrace();
		}
	}
	
	private void printHelp() {
		System.out.println("Something went wrong. Maybe a menu should be here.");
	}
	
	private void run(String type, String filename) {
		uniiSubstances = readFile(uniiFileLocation, "unii");
		readFile(filename, type);
		switch(type.toLowerCase()) {
		case "rxnorm":
			compareRxNorm(uniiSubstances, true);
			compareRxNorm(readFile(filename, "rxnorm"), false);
			break;
		case "dea":
			compareDea();
			break;
		case "nflis":
			compareNflis();
			break;
		default:
			System.out.println("Unknown source to compare");
			System.exit(-1);
			break;
		}
		print();
	}
	
	private void print() {
		results.forEach(x -> {
			pw.println(x);
			pw.flush();
		});
	}
	
	private void compareRxNorm(ArrayList<String> list, boolean names) {
		String url = "https://rxnav.nlm.nih.gov/REST";
		String uniiParams = "/rxcui.json?idtype=UNII_CODE&id=";
		String nameParams = "";
		if(names) {
			
		}
		else {
			
		}
	}
	
	private void compareDea() {
		uniiSubstances.forEach(a -> {
			result = "";
			pvMatch = false;
			deaSubstances.forEach(y -> {
				if( !pvMatch && y.getName().toLowerCase().equalsIgnoreCase(a) ) {
					pvMatch = true;
					result = !y.getCode().isEmpty() ? result + y.getName() + " (" + y.getCode() + ")|" : result + y.getName() + "|";
				}
				else if( y.getSynonyms(true).contains(a.toLowerCase()) ) {
					result = !y.getCode().isEmpty() ? result + y.getName() + " (" + y.getCode() + ")|" : result + y.getName() + "|";
				}
			});
//			result = a + "\t" + result;
			results.add(result);
		});		
	}
	
	private void compareNflis() {
		uniiSubstances.forEach(a -> {
			result = "";
			pvMatch = false;
			nflisSubstances.forEach(y -> {
				if( !pvMatch && y.getName().toLowerCase().equalsIgnoreCase(a) ) {
					pvMatch = true;
					result = !y.getCode().isEmpty() ? result + y.getName() + " (" + y.getCode() + ")|" : result + y.getName() + "|";
				}
				else if( y.getSynonyms(true).contains(a.toLowerCase()) ) {
					result = !y.getCode().isEmpty() ? result + y.getName() + " (" + y.getCode() + ")|" : result + y.getName() + "|";
				}
			});				
//			result = a + "\t" + result;
			results.add(result);
		});
	}
	
	private void cleanup() {
		pw.close();
	}
	
	private void setDeaMap(String scheduleCode, String schedule, String substance, String code, ArrayList<String> synonyms, String isNarcotic) {
//		DEASchedule deaSchedule = new DEASchedule(schedule);
		DEASubstance deaSubstance = new DEASubstance(code, substance, isNarcotic, synonyms);
		deaSubstances.add(deaSubstance);
		
//		if( this.deaSchedule2Substance.containsKey(deaSchedule)) {
//			ArrayList<DEASubstance> substanceList = this.deaSchedule2Substance.get(deaSchedule);
//			if( !substanceList.contains(deaSubstance) ) {
//				substanceList.add(deaSubstance);
//				deaSchedule2Substance.put(deaSchedule, substanceList);
//			}
//		}
//		else {
//			ArrayList<DEASubstance> substanceList = new ArrayList<DEASubstance>();
//			substanceList.add(deaSubstance);
//			deaSchedule2Substance.put(deaSchedule, substanceList);
//		}
//		
//		if( this.deaSubstance2Schedule.containsKey(deaSubstance)) {
//			ArrayList<DEASchedule> scheduleList = this.deaSubstance2Schedule.get(deaSubstance);
//			if( !scheduleList.contains(deaSchedule) ) {
//				scheduleList.add(deaSchedule);
//				deaSubstance2Schedule.put(deaSubstance, scheduleList);
//			}
//		}
//		else {
//			ArrayList<DEASchedule> scheduleList = new ArrayList<DEASchedule>();
//			scheduleList.add(deaSchedule);
//			deaSubstance2Schedule.put(deaSubstance, scheduleList);
//		}		
	}
	
	private void setNflisMap(String code, String substance, String synonyms, String category, String categoryCode ) { 
//		NFLISCategory nflisCategory = new NFLISCategory(category);
		NFLISSubstance nflisSubstance = new NFLISSubstance(substance, synonyms);
		nflisSubstances.add(nflisSubstance);
		
//		if( !this.nflisCategory2Substance.containsKey(nflisCategory) ) {
//			ArrayList<NFLISSubstance> list = new ArrayList<NFLISSubstance>();
//			list.add(nflisSubstance);
//			this.nflisCategory2Substance.put(nflisCategory, list);
//		}
//		else {
//			ArrayList<NFLISSubstance> list = this.nflisCategory2Substance.get(nflisCategory);
//			if( !list.contains(nflisSubstance) ) {
//				list.add(nflisSubstance);
//				this.nflisCategory2Substance.put(nflisCategory, list);
//			}	
//		}
//		
//		if( !this.nflisSubstance2Category.containsKey(nflisSubstance)) {
//			ArrayList<NFLISCategory> list = new ArrayList<NFLISCategory>();
//			list.add(nflisCategory);
//			this.nflisSubstance2Category.put(nflisSubstance, list);
//		}
//		else {
//			ArrayList<NFLISCategory> list = this.nflisSubstance2Category.get(nflisSubstance);
//			if( !list.contains(nflisCategory) ) {
//				list.add(nflisCategory);
//				this.nflisSubstance2Category.put(nflisSubstance, list);
//			}
//		}
	}	
	
	
	public ArrayList<String> readFile(String filename, String type) {
		ArrayList<String> list = new ArrayList<String>();
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
						switch(type.toLowerCase()) {
						case "unii":
							String substance = "";
							substance = line;
							list.add(substance);
							break;
						case "rxnorm":
							String rxCandidate = "";
							rxCandidate = line;
							list.add(rxCandidate);
							break;
						case "dea":
							if(line.contains("|") ) {
								String[] values = line.split("\\|", -1);							
								String schedule = values[1];
								String substanceDea = values[2];
								String deaCode = values[3];
								String narcotic = values[4]; //not sure what we want to do with this - create a term and term-term rel?
								ArrayList<String> synonymsDea = new ArrayList<String>();
								for(int i=5; i < values.length; i++) {
									String value = values[i];
									if( !value.isEmpty() && !synonymsDea.contains(value)) {
										synonymsDea.add(value);
									}
								}
	
								if( deaCode != null ) {
									setDeaMap("", schedule, substanceDea, deaCode, synonymsDea, narcotic);
								}
							}
							break;
						case "nflis":
							if(line.contains("|") ) {
								String[] values = line.split("\\|", -1);									
								String substanceN = values[1];
								// String categoryCode = values[2];
								String category = values[3];
								String synonyms = values[4];					
								setNflisMap("", substanceN, synonyms, category, "");								
							}
							break;
						default:
							System.out.println("Unknown file type. Check source parameter.");
							System.exit(-1);
							printHelp();
							break;
					}			
				}						
			}
//				pw.println();				
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
		return list;
	}	

}
