/* Robert Wynne, NIH/NLM/LHC
 * 
 * Tracking:
 * 	190712 - first version
 *  191009 - addition of DEA and NFLIS content
 *  191108 - addition of UNIIS
 *  	- fix to multiple BNs for a substance so not just the first is added
 *  	- principal variant fix
 *  191206 - addition of T-codes and hierarchy, from ICD-10, and associate rx substances
 */

package gov.nih.nlm.mor.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.nih.nlm.mor.db.dea.DEASchedule;
import gov.nih.nlm.mor.db.dea.DEASubstance;
import gov.nih.nlm.mor.db.nflis.NFLISCategory;
import gov.nih.nlm.mor.db.nflis.NFLISSubstance;
import gov.nih.nlm.mor.db.rxnorm.Concept;
import gov.nih.nlm.mor.db.rxnorm.ConceptRelationship;
import gov.nih.nlm.mor.db.rxnorm.ConceptType;
import gov.nih.nlm.mor.db.rxnorm.Source;
import gov.nih.nlm.mor.db.rxnorm.Term;
import gov.nih.nlm.mor.db.rxnorm.TermRelationship;
import gov.nih.nlm.mor.db.rxnorm.TermType;
import gov.nih.nlm.mor.db.table.AuthoritativeSourceTable;
import gov.nih.nlm.mor.db.table.Concept2ConceptTable;
import gov.nih.nlm.mor.db.table.ConceptTable;
import gov.nih.nlm.mor.db.table.ConceptTypeTable;
import gov.nih.nlm.mor.db.table.Term2TermTable;
import gov.nih.nlm.mor.db.table.TermTable;
import gov.nih.nlm.mor.db.table.TermTypeTable;
import gov.nih.nlm.mor.icd.IcdConcept;

public class CDCTables {
	
	public AuthoritativeSourceTable authoritativeSourceTable = new AuthoritativeSourceTable();
	public Concept2ConceptTable concept2ConceptTable = new Concept2ConceptTable();
	public ConceptTable conceptTable = new ConceptTable();
	public ConceptTypeTable conceptTypeTable = new ConceptTypeTable();
	public Term2TermTable term2TermTable = new Term2TermTable();
	public TermTable termTable = new TermTable();
	public TermTypeTable termTypeTable = new TermTypeTable();
	
	private PrintWriter authoritativeSourceFile = null;
	private PrintWriter conceptTypeFile = null;
	private PrintWriter termTypeFile = null;
	private PrintWriter termFile = null;
	private PrintWriter conceptFile = null;
	private PrintWriter term2termFile = null;
	private PrintWriter concept2conceptFile = null;
	
	private final String allConceptsUrl = "https://rxnavstage.nlm.nih.gov/REST/allconcepts.json?tty=IN";
	private final String allClassesUrl = "https://rxnavstage.nlm.nih.gov/REST/rxclass/allClasses.json?classTypes=ATC1-4";
	
	private HashMap<String, ArrayList<String>> rxcui2Misspellings = new HashMap<String, ArrayList<String>>();
	private HashMap<String, String> rxcui2ProperSpelling = new HashMap<String, String>();
	
//	private HashMap<String, ArrayList<String>> rxcui2DrugTCodes = new HashMap<String, ArrayList<String>>();
//	private HashMap<String, String> tcode2Description = new HashMap<String, String>();
//	private HashMap<String, ArrayList<String>> tcode2Rxcuis = new HashMap<String, ArrayList<String>>();
	
	private HashMap<IcdConcept, ArrayList<IcdConcept>> icdHierarchy = new HashMap<IcdConcept, ArrayList<IcdConcept>>();
	
	private HashMap<NFLISCategory, ArrayList<NFLISSubstance>> nflisCategory2Substance = new HashMap<NFLISCategory, ArrayList<NFLISSubstance>>();
	private HashMap<NFLISSubstance, ArrayList<NFLISCategory>> nflisSubstance2Category = new HashMap<NFLISSubstance, ArrayList<NFLISCategory>>();

	private HashMap<DEASchedule, ArrayList<DEASubstance>> deaSchedule2Substance = new HashMap<DEASchedule, ArrayList<DEASubstance>>();
	private HashMap<DEASubstance, ArrayList<DEASchedule>> deaSubstance2Schedule = new HashMap<DEASubstance, ArrayList<DEASchedule>>();
	
	private ArrayList<IcdConcept> icdConcepts = new ArrayList<IcdConcept>();
	private ArrayList<IcdConcept> icdConceptsWithCuis = new ArrayList<IcdConcept>();
	
	//I'm guessing this will change if col. 3 starts to vary
	private HashMap<String, ArrayList<String>> mclMap = new HashMap<String, ArrayList<String>>();
	
	private HashMap<String, String> sourceMap = new HashMap<String, String>();
	private HashMap<String, String> termTypeMap = new HashMap<String, String>();
	private HashMap<String, String> classTypeMap = new HashMap<String, String>();
	
	
	private Integer codeGenerator = (int) 0;
	private Integer misspellingCount = (int) 0;
	Integer count = (int) 0;	

	
	public static void main(String[] args) {
		CDCTables tables = new CDCTables();
		long start = System.currentTimeMillis();				
		tables.configure();
		tables.gather();
		tables.serialize();
		tables.cleanup();
		System.out.println("Finished data serialization in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");		
	}
	
	private void configure() {
		//We are going to hardcode these filenames to ensure
		//deliverable consistency to CDC
		
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		String authoritativePath = "./authoritative-source.txt";
		String conceptTypePath = "./concept-type.txt";
		String termTypePath = "./term-type.txt";
		String termPath = "./term.txt";
		String conceptPath = "./concept.txt";
		String term2termPath = "./term-term.txt";
		String concept2conceptPath = "./concept-concept.txt";
		
		String cui2MisspellingsPath = "./config/substance-mispellings.txt";
		String icdHierarchy = "./config/10-par-chd-rels.txt";
		String tcode2RxPath = "./config/tcode-map.txt";
		String nflisPath = "./config/nflis-2018-and-2019.txt";
		String deaPath = "./config/dea-2018.txt";
		String mclPath = "./config/MCL-terms";

		System.out.println("[1] Reading configuration files and materializing rxcuis");
		System.out.print("  - from " + cui2MisspellingsPath); 		
		readFile(cui2MisspellingsPath, "spell");
		System.out.println(" ...OK");
		
		System.out.print("  - from " + mclPath);
		readFile(mclPath, "mcl");
		System.out.println(" ...OK");
		
		System.out.print("  - from " + icdHierarchy);
		System.out.print("  - adding ICD-10-CM T-code sub-hierarchy S00-T88");
		readFile(icdHierarchy, "tcode hierarchy");
		System.out.println(" ...OK");	
		
		System.out.print("  - from " + tcode2RxPath);
		System.out.print("  - associating rxcuis to T-codes");
		readFile(tcode2RxPath, "tcode");
		System.out.println(" ...OK");			
		
		System.out.print("  - from " + nflisPath);
		readFile(nflisPath, "nflis");
		System.out.println(" ...OK");
		
		System.out.print("  - from " + deaPath);
		readFile(deaPath, "dea");
		System.out.println(" ...OK");
		
		try {
			authoritativeSourceFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(authoritativePath)),StandardCharsets.UTF_8),true);
			conceptTypeFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(conceptTypePath)),StandardCharsets.UTF_8),true);
			termTypeFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(termTypePath)),StandardCharsets.UTF_8),true); 
			termFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(termPath)),StandardCharsets.UTF_8),true);
			conceptFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(conceptPath)),StandardCharsets.UTF_8),true);
			term2termFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(term2termPath)),StandardCharsets.UTF_8),true);
			concept2conceptFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(concept2conceptPath)),StandardCharsets.UTF_8),true);	
		}
		catch(Exception e) {
			System.err.print("There was an error trying to create one of the table files.");
			e.printStackTrace();
		}
	}
		
	private void readFile(String filename, String type) {
		FileReader file = null;
		BufferedReader buff = null;
		try {
			file = new FileReader(filename);
			buff = new BufferedReader(file);
			boolean eof = false;
			int colIndex = -1;
			while (!eof) {
				String line = buff.readLine();
				if (line == null)
					eof = true;
				else {	
					if( line != null && line.contains("|") ) {
						String[] values = line.split("\\|", -1);

						switch(type)
						{
							case "spell":
								String rxname = values[0];
								String rxcui = values[1];
								String misspell = values[2];
								setMisspellingMap(rxname, rxcui, misspell);
								break;
							case "mcl":
								String variant = values[0];
								String mclSubstance = values[1];
								setMclMap(variant, mclSubstance);
								break;
							case "tcode hierarchy":
								String parentCode = values[0];
								String parentName = values[1];
								String childCode = values[2];
								String childName = values[3];
								setIcdHierarchyMap(parentCode, parentName, childCode, childName);
								break;
							case "tcode":
								String tcode = values[0];
								// String tcodeName = values[1];
								ArrayList<String> cuiList = new ArrayList<String>();
								for(int i=2; i < values.length; i++) {
									cuiList.add(values[i]);
								}
								// setDrugCodesMap(tcode, tcodeName, cuiList);
								setDrugCodesMap(tcode, cuiList);
								break;
							case "nflis":
								// String code = values[0];
								String substance = values[1];
								// String categoryCode = values[2];
								String category = values[3];
								String synonyms = values[4];					
								setNflisMap("", substance, synonyms, category, "");
								break;
							case "dea":
								//String scheduleCode = values[0];
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
								break;	
							default:
								System.err.println("The following config file was unexpected: " + filename);
						}
					}
					else {
						System.err.println("No Property configured for configuration index " + colIndex);
						System.err.println("Exiting");
						System.exit(-1);
					}						
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
	
	private void setIcdHierarchyMap(String parentCode, String parentName, String childCode, String childName) {
		if( parentCode != null && childCode != null && parentName != null && childName != null ) {
			IcdConcept parent = new IcdConcept(parentCode, parentName);
			IcdConcept child = new IcdConcept(childCode, childName);
			if( this.icdHierarchy.containsKey(parent) ) {
				ArrayList<IcdConcept> children = this.icdHierarchy.get(parent);
				if( !children.contains(child) ) {
					children.add(child);
					this.icdHierarchy.put(parent, children);					
				}
			}
			else {
				ArrayList<IcdConcept> children = new ArrayList<IcdConcept>();
				children.add(child);
				this.icdHierarchy.put(parent, children);
			}
			if( !icdConcepts.contains(parent) ) {
				icdConcepts.add(parent);
			}
			if( !icdConcepts.contains(child)) {
				icdConcepts.add(child);
			}
		}
		
	}
	
	private void setNflisMap(String code, String substance, String synonyms, String category, String categoryCode ) { 
		NFLISCategory nflisCategory = new NFLISCategory(category);
		NFLISSubstance nflisSubstance = new NFLISSubstance(substance, synonyms);
		
		if( !this.nflisCategory2Substance.containsKey(nflisCategory) ) {
			ArrayList<NFLISSubstance> list = new ArrayList<NFLISSubstance>();
			list.add(nflisSubstance);
			this.nflisCategory2Substance.put(nflisCategory, list);
		}
		else {
			ArrayList<NFLISSubstance> list = this.nflisCategory2Substance.get(nflisCategory);
			if( !list.contains(nflisSubstance) ) {
				list.add(nflisSubstance);
				this.nflisCategory2Substance.put(nflisCategory, list);
			}	
		}
		
		if( !this.nflisSubstance2Category.containsKey(nflisSubstance)) {
			ArrayList<NFLISCategory> list = new ArrayList<NFLISCategory>();
			list.add(nflisCategory);
			this.nflisSubstance2Category.put(nflisSubstance, list);
		}
		else {
			ArrayList<NFLISCategory> list = this.nflisSubstance2Category.get(nflisSubstance);
			if( !list.contains(nflisCategory) ) {
				list.add(nflisCategory);
				this.nflisSubstance2Category.put(nflisSubstance, list);
			}
		}
	}
	
	private void setMclMap(String variant, String substance) {
		variant = variant.toLowerCase();
		substance = substance.toLowerCase();
		if(mclMap.containsKey(substance)) {
			ArrayList<String> list = mclMap.get(substance);
			list.add(variant);
			mclMap.put(substance, list);
		}
		else {
			ArrayList<String> list = new ArrayList<String>();
			list.add(variant);
			mclMap.put(substance, list);
		}
	}
	
	private void setDeaMap(String scheduleCode, String schedule, String substance, String code, ArrayList<String> synonyms, String isNarcotic) {
		DEASchedule deaSchedule = new DEASchedule(schedule);
		DEASubstance deaSubstance = new DEASubstance(code, substance, isNarcotic, synonyms);		
		
		if( this.deaSchedule2Substance.containsKey(deaSchedule)) {
			ArrayList<DEASubstance> substanceList = this.deaSchedule2Substance.get(deaSchedule);
			if( !substanceList.contains(deaSubstance) ) {
				substanceList.add(deaSubstance);
				deaSchedule2Substance.put(deaSchedule, substanceList);
			}
		}
		else {
			ArrayList<DEASubstance> substanceList = new ArrayList<DEASubstance>();
			substanceList.add(deaSubstance);
			deaSchedule2Substance.put(deaSchedule, substanceList);
		}
		
		if( this.deaSubstance2Schedule.containsKey(deaSubstance)) {
			ArrayList<DEASchedule> scheduleList = this.deaSubstance2Schedule.get(deaSubstance);
			if( !scheduleList.contains(deaSchedule) ) {
				scheduleList.add(deaSchedule);
				deaSubstance2Schedule.put(deaSubstance, scheduleList);
			}
		}
		else {
			ArrayList<DEASchedule> scheduleList = new ArrayList<DEASchedule>();
			scheduleList.add(deaSchedule);
			deaSubstance2Schedule.put(deaSubstance, scheduleList);
		}		
	}
	
	private void setMisspellingMap(String rxname, String rxcui, String misspell) {
		misspellingCount++;
		if( rxcui2Misspellings.containsKey(rxcui) ) {
			ArrayList<String> list = rxcui2Misspellings.get(rxcui);
			if( !list.contains(misspell) ) {
				list.add(misspell);
				rxcui2Misspellings.put(rxcui, list);
			}
		}
		else {
			ArrayList<String> list = new ArrayList<String>();
			list.add(misspell);
			rxcui2Misspellings.put(rxcui, list);
		}
		if( !rxcui2ProperSpelling.containsKey(rxcui) ) {
			rxcui2ProperSpelling.put(rxcui, rxname);
		}
	}
	
	private void setDrugCodesMap(String tcode, ArrayList<String> cuiList ) {
		for( IcdConcept c : this.icdConcepts) {
			if( c.getCode().startsWith(tcode) ) {
				IcdConcept tmp = c;
				tmp.setAssociatedRxCuis(cuiList);
				if( !icdConceptsWithCuis.contains(tmp) ) {
					icdConceptsWithCuis.add(tmp);
				}
			}
		}
	}
	
	private void addMisspellings() {
		
		rxcui2Misspellings.keySet().stream().forEach(x -> {
			ArrayList<String> misList = rxcui2Misspellings.get(x);
			String properName = rxcui2ProperSpelling.get(x);
			Term properSpelling = null;
			Integer properId = null;
			String drugConceptId = null;
			if( termTable.hasTermByName(properName, sourceMap.get("RxNorm")) ) {
				properSpelling = termTable.getTermByName(properName, sourceMap.get("RxNorm"));
				properId = termTable.getTermByName(properName, sourceMap.get("RxNorm")).getId();
				drugConceptId = termTable.getTermByName(properName, sourceMap.get("RxNorm")).getDrugConceptId();				
			}
			else return;
			final String drugConceptIdFin = drugConceptId;
			final Term properSpellingFin = properSpelling;
			final Integer properIdFin = properId;
			if( properId != null ) {
				misList.stream().forEach(y -> {
					Term term = new Term();
					term.setId(++codeGenerator);
					term.setTty(termTypeMap.get("MSP"));
					term.setName(y);
					term.setSource(sourceMap.get("Misspelling"));
					term.setSourceId("");
					
					term.setDrugConceptId(Integer.valueOf(drugConceptIdFin));
					
					termTable.add(term);
					
					Integer misId = codeGenerator;
					
					if( !term2TermTable.hasPair(y, "MSP", properSpellingFin.getName(), null) ) {
						TermRelationship termRel = new TermRelationship();
						termRel.setId(++codeGenerator);
						termRel.setTermId1(misId);
						termRel.setRelationship("MSP");
						termRel.setTermId2(properIdFin);
						
						term2TermTable.add(termRel);
						count++;
					}					
				});
				
			}
		});
	
	}
	
	private void addTCodes() {
		
		
//		for(String tcode : tcode2Description.keySet() ) {
		Integer size = Integer.valueOf(String.valueOf(icdConceptsWithCuis.size()));
		Integer count = Integer.valueOf("0");
		for(IcdConcept icd : icdConceptsWithCuis ) {
			count++;
			if( count % 100 == 0 ) {
				System.out.println("Added " + count + " of " + size + " T-codes");
			}
			String tcodeName = icd.getName();
			String tcode = icd.getCode();
			
			//add a class and term for the tocode
			Term term = new Term();			
			Concept concept = new Concept();
			@SuppressWarnings("unused")
			Integer icdId = null;
			Integer conceptId = null;
			if( !termTable.hasTerm(tcode, "", sourceMap.get("ICD")) &&
				!conceptTable.hasConcept(tcode, sourceMap.get("ICD"))) {
				System.out.println("Creating a ICD10 code for CM(?) " + icd.getCode() + " (" + icd.getName() + ")");  //debug
				term.setId(++codeGenerator);
				term.setName(tcodeName);
				term.setSource(sourceMap.get("ICD"));
				term.setSourceId(tcode);
				term.setTty(termTypeMap.get("PV"));
				concept.setConceptId(++codeGenerator);
				concept.setPreferredTermId(term.getId());
				concept.setClassType(classTypeMap.get("Class"));
				concept.setSource(sourceMap.get("ICD"));
				concept.setSourceId(tcode);
				icdId = conceptId = codeGenerator;
				term.setDrugConceptId(conceptId);
				conceptTable.add(concept);
				termTable.add(term);
			}
			else {
				concept = conceptTable.getConcept(tcode, sourceMap.get("ICD"));
				icdId = concept.getConceptId();
			}
			
			//associate the t-code term to the drug term
			if( !icd.getAssociatedRxCuis().isEmpty() ) {
				relateTCode2Rx(concept, icd.getAssociatedRxCuis() );
			}
			
		}
		
		System.out.println("Done adding T-codes");
	}
	
	private void relateTCode2Rx(Concept c, ArrayList<String> rxcuis) {

		for(String rxcui : rxcuis) {
			
			Concept rxConcept = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
			if( rxConcept != null && c != null ) {
				Integer icdConceptId = c.getConceptId();				
				Integer rxConceptId = rxConcept.getConceptId();
				
				ConceptRelationship conRel = new ConceptRelationship();
				conRel.setId(++codeGenerator);
				conRel.setConceptId1(rxConceptId);
				conRel.setRelationship("memberof");
				conRel.setConceptId2(icdConceptId);
				
				concept2ConceptTable.add(conRel);
			}
		}
		
	
	}
		
	private void addNFLIS() {
		
		for( NFLISCategory category : nflisCategory2Substance.keySet() ) {
			ArrayList<NFLISSubstance> substances = nflisCategory2Substance.get(category);
			Concept categoryConcept = new Concept();
			Term categoryTerm = new Term();
			
			categoryTerm.setId(++codeGenerator);
			categoryTerm.setSource(sourceMap.get("NFLIS"));
			categoryTerm.setSourceId(category.getCategoryCode());
			categoryTerm.setTty("");
			categoryTerm.setName(category.getCategoryName());
			categoryConcept.setConceptId(++codeGenerator);
			categoryConcept.setPreferredTermId(categoryTerm.getId());
			categoryConcept.setSource(sourceMap.get("NFLIS"));
			categoryConcept.setSourceId(category.getCategoryCode());
			categoryConcept.setClassType(classTypeMap.get("Class"));
			categoryTerm.setDrugConceptId(codeGenerator);
			conceptTable.add(categoryConcept);
			termTable.add(categoryTerm);
			
			for( NFLISSubstance substance : substances ) {
				Concept concept = new Concept();
				Term preferredTerm = new Term();
				
				preferredTerm.setId(++codeGenerator);
				preferredTerm.setName(substance.getName());
				preferredTerm.setSourceId(substance.getCode());
				preferredTerm.setSource(sourceMap.get("NFLIS"));
				preferredTerm.setTty(termTypeMap.get("PV"));
				concept.setConceptId(++codeGenerator);
				concept.setPreferredTermId(preferredTerm.getId());
				concept.setSource(sourceMap.get("NFLIS"));
				concept.setSourceId(substance.getCode());
				concept.setClassType(classTypeMap.get("Substance"));
				
				Concept existingConcept = conceptTable.getDrugConceptForName(substance.getName(), termTable);
				
				if( existingConcept == null ) {				
					concept.setConceptId(++codeGenerator);
					concept.setPreferredTermId(preferredTerm.getId());
					concept.setSource(sourceMap.get("NFLIS"));
					concept.setSourceId(substance.getCode());
					concept.setClassType(classTypeMap.get("Substance"));
					conceptTable.add(concept);					
				}
				else {
					//report the concept exists and everything we know about it
					concept = existingConcept;
				}
				
				preferredTerm.setDrugConceptId(concept.getConceptId());
				termTable.add(preferredTerm);
				
				for( String s : substance.getSynonyms(false)) {
					Term synonym = new Term();
					synonym.setId(++codeGenerator);
					synonym.setName(s.trim());
					synonym.setSource(sourceMap.get("NFLIS"));
					synonym.setSourceId("");
					synonym.setTty(termTypeMap.get("SY"));
					synonym.setDrugConceptId(concept.getConceptId());
					termTable.add(synonym);
					
					if( !term2TermTable.hasPair(synonym.getName(), termTypeMap.get("SY"), preferredTerm.getName(), sourceMap.get("NFLIS")) ) {
						TermRelationship termRel = new TermRelationship();
						termRel.setId(++codeGenerator);
						termRel.setTermId1(synonym.getId());
						termRel.setRelationship("SY");
						termRel.setTermId2(preferredTerm.getId());
						
						term2TermTable.add(termRel);
					}						
				}
				
				for( String cui : substance.getRxcuis() ) {
					if( conceptTable.hasConcept(cui, sourceMap.get("RxNorm")) ) {
						Concept rxConcept = conceptTable.getConcept(cui, sourceMap.get("RxNorm"));
						if( rxConcept != null && concept != null ) {
							Integer rxConceptId = rxConcept.getConceptId();
							
							ConceptRelationship conRel = new ConceptRelationship();
							conRel.setId(++codeGenerator);
							conRel.setConceptId1(rxConceptId);
							conRel.setRelationship("memberof");
							conRel.setConceptId2(categoryConcept.getConceptId());
							
							concept2ConceptTable.add(conRel);							
						}
					}
				}
				
			}
		}
	}
	
	private void addDEA() {

		Concept narcConcept = new Concept();
		Term narcTerm = new Term();
		narcTerm.setId(++codeGenerator);
		narcTerm.setSource(sourceMap.get("DEA"));
		narcTerm.setSourceId("NARC1");
		narcTerm.setTty("");
		narcTerm.setName("Narcotic");
		narcConcept.setConceptId(++codeGenerator);
		narcConcept.setPreferredTermId(narcTerm.getId());
		narcConcept.setSource(sourceMap.get("DEA"));
		narcConcept.setSourceId("NARC1");
		narcConcept.setClassType(classTypeMap.get("Class"));
		narcTerm.setDrugConceptId(codeGenerator);
		conceptTable.add(narcConcept);
		termTable.add(narcTerm);
		
		Concept nonNarcConcept = new Concept();
		Term nonNarcTerm = new Term();		
		nonNarcConcept = new Concept();
		nonNarcTerm = new Term();
		nonNarcTerm.setId(++codeGenerator);
		nonNarcTerm.setSource(sourceMap.get("DEA"));
		nonNarcTerm.setSourceId("NARC2");
		nonNarcTerm.setTty("");
		nonNarcTerm.setName("Non-Narcotic");
		nonNarcConcept.setConceptId(++codeGenerator);
		nonNarcConcept.setPreferredTermId(narcTerm.getId());
		nonNarcConcept.setSource(sourceMap.get("DEA"));
		nonNarcConcept.setSourceId("NARC2");
		nonNarcConcept.setClassType(classTypeMap.get("Class"));
		nonNarcTerm.setDrugConceptId(codeGenerator);
		conceptTable.add(nonNarcConcept);
		termTable.add(nonNarcTerm);
		
		for( DEASchedule schedule : deaSchedule2Substance.keySet() ) {
			ArrayList<DEASubstance> substances = deaSchedule2Substance.get(schedule);
			Concept scheduleConcept = new Concept();
			Term scheduleTerm = new Term();
			
			scheduleTerm.setId(++codeGenerator);
			scheduleTerm.setSource(sourceMap.get("DEA"));
			scheduleTerm.setSourceId(schedule.getScheduleCode());
			scheduleTerm.setTty("");
			scheduleTerm.setName(schedule.getScheduleName());
			scheduleConcept.setConceptId(++codeGenerator);
			scheduleConcept.setPreferredTermId(scheduleTerm.getId());
			scheduleConcept.setSource(sourceMap.get("DEA"));
			scheduleConcept.setSourceId(schedule.getScheduleCode());
			scheduleConcept.setClassType(classTypeMap.get("Class"));
			scheduleTerm.setDrugConceptId(codeGenerator);
			conceptTable.add(scheduleConcept);
			termTable.add(scheduleTerm);
			
			for( DEASubstance substance : substances ) {
				Concept concept = new Concept();
				Term preferredTerm = new Term();
				
				preferredTerm.setId(++codeGenerator);
				preferredTerm.setName(substance.getName());
				preferredTerm.setSourceId(substance.getCode());
				preferredTerm.setSource(sourceMap.get("DEA"));
				preferredTerm.setTty(termTypeMap.get("PV"));
				
				Concept existingConcept = conceptTable.getDrugConceptForName(substance.getName(), termTable);
				
				if( existingConcept == null ) {				
					concept.setConceptId(++codeGenerator);
					concept.setPreferredTermId(preferredTerm.getId());
					concept.setSource(sourceMap.get("DEA"));
					concept.setSourceId(substance.getCode());
					concept.setClassType(classTypeMap.get("Substance"));
					conceptTable.add(concept);					
				}
				else {
					//report everything we know about the concept that already exists
					concept = existingConcept;
				}
				
				preferredTerm.setDrugConceptId(concept.getConceptId());
				termTable.add(preferredTerm);
				
				Concept narcoticConcept = substance.isNarcotic() ? narcConcept : nonNarcConcept;  
				if( !concept2ConceptTable.containsPair(concept.getConceptId(), "memberof", narcoticConcept.getConceptId())) {
					Integer substanceId = concept.getConceptId();
					
					ConceptRelationship conRel = new ConceptRelationship();
					conRel.setId(++codeGenerator);
					conRel.setConceptId1(substanceId);
					conRel.setRelationship("memberof");
					conRel.setConceptId2(narcoticConcept.getConceptId());
					
					concept2ConceptTable.add(conRel);						
				}
				
				for( String s : substance.getSynonyms(false)) {
					Term synonym = new Term();
					synonym.setId(++codeGenerator);
					synonym.setName(s.trim());
					synonym.setSource(sourceMap.get("DEA"));
					synonym.setSourceId("");
					synonym.setTty(termTypeMap.get("SY"));
					synonym.setDrugConceptId(concept.getConceptId());
					termTable.add(synonym);
					
					if( !term2TermTable.hasPair(synonym.getName(), termTypeMap.get("SY"), preferredTerm.getName(), sourceMap.get("DEA"))  ) {
						TermRelationship termRel = new TermRelationship();
						termRel.setId(++codeGenerator);
						termRel.setTermId1(synonym.getId());
						termRel.setRelationship("SY");
						termRel.setTermId2(preferredTerm.getId());
						
						term2TermTable.add(termRel);
					}						
				}
				
				for( String cui : substance.getRxcuis() ) {
					if(conceptTable.hasConcept(cui, sourceMap.get("RxNorm")) ) {
						Concept rxConcept = conceptTable.getConcept(cui, sourceMap.get("RxNorm"));
						if( rxConcept != null && concept != null ) {
							Integer rxConceptId = rxConcept.getConceptId();
							
							ConceptRelationship conRel = new ConceptRelationship();
							conRel.setId(++codeGenerator);
							conRel.setConceptId1(rxConceptId);
							conRel.setRelationship("memberof");
							conRel.setConceptId2(scheduleConcept.getConceptId());
							
							concept2ConceptTable.add(conRel);							
						}
					}
				}
				
			}
		}		
	}
	

	
	private void gather() {
		JSONObject allConcepts = null;
		JSONObject allClasses = null;
		
		try {
			allConcepts = getresult(allConceptsUrl);
			allClasses = getresult(allClassesUrl);			
		} catch (IOException e) {
			System.out.println(allConceptsUrl);
			System.out.println(allClassesUrl);
			e.printStackTrace();
		}
		
		setAuthoritativeSourceTable();
		setConceptTypeTable();
		setTermTypeTable();
		
		System.out.println("[6] Building T-code hierarchy");
		buildTCodeHierarchy();				
		
		System.out.println("[2] Fetching ATC Classes");
		if( allClasses != null ) {
			if( !allClasses.isNull("rxclassMinConceptList") ) {
				JSONObject rxclassMinConceptList = (JSONObject) allClasses.get("rxclassMinConceptList");
				if( !rxclassMinConceptList.isNull("rxclassMinConcept") ) {
					JSONArray rxclassMinConceptArray = (JSONArray) rxclassMinConceptList.get("rxclassMinConcept");
					for( int i = 0; i < rxclassMinConceptArray.length(); i++ ) {
						JSONObject atcClass = (JSONObject) rxclassMinConceptArray.get(i);
						
						//Get the class name and id, then proceed to collect all edges
						String classId = atcClass.get("classId").toString();
						String className = atcClass.get("className").toString();
						
						Term term = new Term();
						term.setId(++codeGenerator);
						term.setName(className);
						term.setSourceId(classId);
						term.setSource(sourceMap.get("ATC"));
						term.setTty(termTypeMap.get("PV"));
						
						Concept concept = new Concept();
						concept.setConceptId(++codeGenerator);
						Integer conceptId = codeGenerator;
						concept.setPreferredTermId(term.getId());						
						concept.setClassType(classTypeMap.get("Class"));
						concept.setSource(sourceMap.get("ATC"));
						concept.setSourceId(classId);
						
						term.setDrugConceptId(conceptId);
						
						termTable.add(term);
						conceptTable.add(concept);					
					}
				}
			}
		}
		
		System.out.println("[3] Collecting edges of ATC classes for isa relations");
		//collect edges for each concept
		//https://rxnavstage.nlm.nih.gov/REST/rxclass/classGraph.json?classId=A&source=ATC1-4
		ArrayList<Concept> conceptList = conceptTable.getConceptsOfSource(sourceMap.get("ATC"));
		for( int i=0; i < conceptList.size(); i++ ) {
			Concept concept = conceptList.get(i);

			String graphUrl = "https://rxnavstage.nlm.nih.gov/REST/rxclass/classGraph.json?classId=" + concept.getSourceId() + "&source=ATC1-4";
			JSONObject allEdges = null;
			
			try {
				allEdges = getresult(graphUrl);
			} catch (IOException e) {
				System.out.println(graphUrl);
				e.printStackTrace();
			}
			if( allEdges != null ) {
				if( !allEdges.isNull("rxclassGraph") ) {
					JSONObject rxclassGraph = (JSONObject) allEdges.get("rxclassGraph");
					if( !rxclassGraph.isNull("rxclassEdge") ) {
//						System.out.println(graphUrl);
						JSONArray edgeArray = null;
						if( rxclassGraph.get("rxclassEdge") instanceof JSONArray ) {
							edgeArray = (JSONArray) rxclassGraph.get("rxclassEdge");
						}
						else if(rxclassGraph.get("rxclassEdge") instanceof JSONObject) {
							edgeArray = new JSONArray();
							edgeArray.put((JSONObject) rxclassGraph.get("rxclassEdge"));
						}
						for( int j=0; j < edgeArray.length(); j++ ) {
							JSONObject edge = (JSONObject) edgeArray.get(j);
							String classId1 = edge.get("classId1").toString();
							String classId2 = edge.get("classId2").toString();
							Integer classIndex1 = null;
							Integer classIndex2 = null;
							if( conceptTable.hasConcept(classId1, sourceMap.get("ATC")) ) {
								Concept c = conceptTable.getConcept(classId1, sourceMap.get("ATC"));
								if( c != null) {
									classIndex1 = c.getConceptId();
								}								
							}
							if( conceptTable.hasConcept(classId2, sourceMap.get("ATC")) ) {
								Concept c = conceptTable.getConcept(classId2, sourceMap.get("ATC"));
								if( c != null) {
									classIndex2 = c.getConceptId();
								}
							}
							if( !concept2ConceptTable.containsPair(classIndex1, "isa", classIndex2) && classIndex1 != null && classIndex2 != null) {
								ConceptRelationship conRel = new ConceptRelationship();
								conRel.setId(++codeGenerator);
								conRel.setConceptId1(classIndex1);
								conRel.setRelationship("isa");
								conRel.setConceptId2(classIndex2);
								concept2ConceptTable.add(conRel);
							}
						}
					}
					
				}
			}
		}
		
//		System.out.println(allConceptsUrl);
		System.out.println("[4] Processing RxNorm substances and asserting relations");
		if( allConcepts != null ) {
			JSONObject group = null;
			JSONArray minConceptArray = null;		
			
			group = (JSONObject) allConcepts.get("minConceptGroup");
			minConceptArray = (JSONArray) group.get("minConcept");
			for(int i = 0; i < minConceptArray.length(); i++ ) {
				
				if( i != 0 && i % 500 == 0 ) {
					System.out.println("  Processed " + i + " substances of " + minConceptArray.length());
				}
				
				JSONObject minConcept = (JSONObject) minConceptArray.get(i);
				
				String rxcui = minConcept.get("rxcui").toString();
				String name = minConcept.get("name").toString();
				String type = minConcept.get("tty").toString();	
				
				Concept concept = new Concept();
				Term term = new Term();

				term.setId(++codeGenerator);
				Integer preferredTermId = codeGenerator;				
				term.setName(name);
				term.setTty(termTypeMap.get(type)); //this could be IN instead
				term.setSourceId(rxcui);
				term.setSource(sourceMap.get("RxNorm"));				
				
		
				concept.setConceptId(++codeGenerator);
				Integer conceptId = codeGenerator;
				concept.setSource(sourceMap.get("RxNorm"));
				concept.setSourceId(rxcui);
				concept.setClassType(classTypeMap.get("Substance"));
				concept.setPreferredTermId(preferredTermId);
				
				term.setDrugConceptId(conceptId);
				
				conceptTable.add(concept);
				termTable.add(term);
				
			
				JSONObject allProperties = null;
				JSONObject allRelated = null;
				JSONObject possibleMembers = null;;
				
				try {
					allRelated = getresult("https://rxnavstage.nlm.nih.gov/REST/rxcui/" + rxcui + "/allrelated.json");
					allProperties = getresult("https://rxnavstage.nlm.nih.gov/REST/rxcui/" + rxcui + "/allProperties.json?prop=all");		
					possibleMembers = getresult("https://rxnavstage.nlm.nih.gov/REST/rxclass/class/byRxcui.json?rxcui=" + rxcui + "&relaSource=ATC");				
				} catch(IOException e) {
					System.out.println("https://rxnavstage.nlm.nih.gov/REST/rxcui/" + rxcui + "/allrelated.json");
					System.out.println("https://rxnavstage.nlm.nih.gov/REST/rxcui/" + rxcui + "/allProperties.json?prop=all");
					System.out.println("https://rxnavstage.nlm.nih.gov/REST/rxclass/class/byRxcui.json?rxcui=" + rxcui + "&relaSource=ATC");
					e.printStackTrace();
				}
				
//				System.out.println("https://rxnavstage.nlm.nih.gov/REST/rxcui/" + rxcui + "/allrelated.json");
				if( allRelated != null ) {
					JSONObject allRelatedGroup = (JSONObject) allRelated.get("allRelatedGroup");
					JSONArray conceptGroup = (JSONArray) allRelatedGroup.get("conceptGroup");
					
					for( int j = 0; j < conceptGroup.length(); j++ ) {
						
						JSONObject relatedConcept = (JSONObject) conceptGroup.get(j);
						String relatedType = relatedConcept.get("tty").toString();
						
						if( (relatedType.equals("PIN") || relatedType.equals("BN")) && !relatedConcept.isNull("conceptProperties") ) {
							
								JSONArray relatedProperties = (JSONArray) relatedConcept.get("conceptProperties");
								
								for( int k = 0; k < relatedProperties.length(); k++) {
									JSONObject soleProperty = (JSONObject) relatedProperties.get(k);
									Term relatedTerm = new Term();
									
									String relatedCuiString = soleProperty.get("rxcui").toString();
									String relatedName = soleProperty.get("name").toString();
									
									relatedTerm.setId(++codeGenerator);
									relatedTerm.setName(relatedName);
									relatedTerm.setTty(termTypeMap.get(relatedType));
									relatedTerm.setSourceId(relatedCuiString);
									relatedTerm.setSource(sourceMap.get("RxNorm"));
									
									Concept conceptForTerm = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
									Integer conceptIdForTerm = conceptForTerm.getConceptId();
									
									relatedTerm.setDrugConceptId(conceptIdForTerm);
									
									termTable.add(relatedTerm);
									
									Integer termId = codeGenerator;
									
									TermRelationship termRel = new TermRelationship();
									termRel.setId(++codeGenerator);
									termRel.setTermId1(termId);
									termRel.setTermId2(preferredTermId);
									termRel.setRelationship(relatedType);
									
									term2TermTable.add(termRel);
								}
								
						}
							
					}
						
				}
				
				//what are we looking for here?  Synonyms and UNIIs
//				System.out.println("https://rxnavstage.nlm.nih.gov/REST/rxcui/" + rxcui + "/allProperties.json?prop=all");
				if( allProperties != null ) {
					JSONObject propConceptGroup = (JSONObject) allProperties.get("propConceptGroup");
					JSONArray propConcept = (JSONArray) propConceptGroup.get("propConcept");
					
					for( int j = 0; j < propConcept.length(); j++ ) {
						
						JSONObject prop = (JSONObject) propConcept.get(j);
						String propName = prop.getString("propName");
						if( propName.equals("RxNorm Synonym") ) {
							Term synonym = new Term();
							synonym.setId(++codeGenerator);					
							synonym.setName(prop.get("propValue").toString());
							synonym.setTty(termTypeMap.get("SY"));
							synonym.setSourceId(rxcui);
							synonym.setSource(sourceMap.get("RxNorm"));
							
							Concept conceptForTerm = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
							Integer conceptIdForTerm = conceptForTerm.getConceptId();
							
							synonym.setDrugConceptId(conceptIdForTerm);
							
							termTable.add(synonym);
							
							Integer synonymId = codeGenerator;							
							
							TermRelationship termRel = new TermRelationship();
							termRel.setId(++codeGenerator);
							termRel.setTermId1(synonymId);							
							termRel.setTermId2(preferredTermId);
							termRel.setRelationship("SY");
							
							term2TermTable.add(termRel);
							
						}
// 191107 - OB: Include
						else if( propName.equals("UNII_CODE") ) {
							Term uniiCode = new Term();
							uniiCode.setId(++codeGenerator);
							//uniiCode.setName(prop.getString("propValue").toString());
							uniiCode.setName(name); //OB: use the rx name
							uniiCode.setTty(termTypeMap.get("UNII"));
							uniiCode.setSourceId(prop.getString("propValue").toString());
							uniiCode.setSource(sourceMap.get("FDA"));
							
							Concept conceptForTerm = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
							Integer conceptIdForTerm = conceptForTerm.getConceptId();
							
							uniiCode.setDrugConceptId(conceptIdForTerm);
							
							termTable.add(uniiCode);
							
							Integer uniiId = codeGenerator;
							
							TermRelationship uniiRel = new TermRelationship();  //term-term rel was here all along, i just can't remember these things during meetings
							uniiRel.setId(++codeGenerator);
							uniiRel.setTermId1(uniiId);
							uniiRel.setTermId2(preferredTermId);
							uniiRel.setRelationship("UNII");
							
							term2TermTable.add(uniiRel);
						}							
					}
					
				}
				
//				System.out.println("https://rxnavstage.nlm.nih.gov/REST/rxclass/class/byRxcui.json?rxcui=" + rxcui + "&relaSource=ATC");	
				if( possibleMembers != null ) {
					if( !possibleMembers.isNull("rxclassDrugInfoList") ) {
						JSONObject rxclassdrugInfoList = (JSONObject) possibleMembers.get("rxclassDrugInfoList");
						JSONArray drugInfoList = (JSONArray) rxclassdrugInfoList.get("rxclassDrugInfo");					
						for( int j=0; j < drugInfoList.length(); j++ ) {
							JSONObject rxclassDrugInfo = (JSONObject) drugInfoList.get(j);
							if( !rxclassDrugInfo.isNull("rxclassMinConceptItem") && !rxclassDrugInfo.isNull("minConcept") ) {
								JSONObject minConcept2 = (JSONObject) rxclassDrugInfo.get("minConcept");
								JSONObject rxclassMinConceptItem = (JSONObject) rxclassDrugInfo.get("rxclassMinConceptItem");
								String theMinCui = minConcept2.get("rxcui").toString();
								if( theMinCui.equals(rxcui) ) {
									String classId = rxclassMinConceptItem.get("classId").toString();
									if( conceptTable.hasConcept(rxcui, sourceMap.get("RxNorm")) && conceptTable.hasConcept(classId, sourceMap.get("ATC")) ) {
										Concept c1 = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
										Concept c2 = conceptTable.getConcept(classId, sourceMap.get("ATC"));
										if( !concept2ConceptTable.containsPair(c1.getConceptId(), "memberof", c2.getConceptId()) ) {
											ConceptRelationship conRel = new ConceptRelationship();
											conRel.setId(++codeGenerator);
											conRel.setConceptId1(c1.getConceptId());
											conRel.setRelationship("memberof");
											conRel.setConceptId2(c2.getConceptId());
											
											concept2ConceptTable.add(conRel);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		
		System.out.println("[5] Adding misspellings - This will take quite some time.");
		addMisspellings();
			
		System.out.println("[7] Associating T-codes to substances");
		addTCodes();
		
		System.out.println("[8] Adding NFLIS categories and substances");
		addNFLIS();
		
		System.out.println("[9] Adding DEA schedules and substances");
		addDEA();
		
		System.out.println("[10] Adding MCL variants not in the ACL");
		addMCL();
		
	}
	
	private void addMCL() {
				
		for(String substance : mclMap.keySet()) {
			//does the substance exist in the database?
			//if not, add it as a PV with the MCL source
			ArrayList<Term> pvsInDb = termTable.getTermsByType(substance, termTypeMap.get("PV"));
			ArrayList<Term> insInDb = termTable.getTermsByType(substance, termTypeMap.get("IN"));
			pvsInDb.addAll(insInDb);
			
			if( pvsInDb.isEmpty() ) {
				System.out.println("MCL is adding local PV: " + substance);
				
				Concept concept = new Concept();
				Term preferredTerm = new Term();
				
				preferredTerm.setId(++codeGenerator);
				preferredTerm.setName(substance);
				preferredTerm.setSourceId("");  //we don't have source-codes for the MCL
				preferredTerm.setSource(sourceMap.get("MCL"));
				preferredTerm.setTty(termTypeMap.get("PV"));
				
				concept.setPreferredTermId(preferredTerm.getId());
				concept.setSource(sourceMap.get("MCL"));
				concept.setClassType(classTypeMap.get("Substance"));
				concept.setConceptId(++codeGenerator);
				
				preferredTerm.setDrugConceptId(codeGenerator);
				
				conceptTable.add(concept);
				termTable.add(preferredTerm);
				addVariants(preferredTerm, mclMap.get(substance));
			}
			else {
				for(Term pvTerm : pvsInDb ) {
					ArrayList<String> variants = mclMap.get(substance);		
					Integer termId = pvTerm.getId();
					ArrayList<Term> existingMisspellings = getRelatedTermsForLHS(termId, "MSP");
					for(Term missTerm : existingMisspellings) {
						if(variants.contains(missTerm.getName().toLowerCase()) ) {
							variants.remove(missTerm.getName().toLowerCase());
						}
					}
					if(!variants.isEmpty()) {						
						addVariants(pvTerm, variants);
					}
				}
			}
			//if it does, check all variants
			//add those that do not exist.
//			else {
//				substanceInDb = true;
//			}
//		}
//		
//		if( !substanceInDb ) {
//			//add all variants as... MSPs
//			
		}
	
	}
	
	private void addVariants(Term term, ArrayList<String> variants) {
		for(String variant : variants) {
			Term varTerm = new Term();
			varTerm.setName(variant);
			varTerm.setDrugConceptId(Integer.valueOf(term.getDrugConceptId()));
			varTerm.setId(++codeGenerator);
			varTerm.setSource(sourceMap.get("Misspelling"));
			varTerm.setSourceId("");
			varTerm.setTty(termTypeMap.get("MSP"));
			
			termTable.add(varTerm);
			
			TermRelationship tRel = new TermRelationship();
			tRel.setId(++codeGenerator);
			tRel.setRelationship("MSP");
			tRel.setTermId1(varTerm.getId());
			tRel.setTermId2(term.getId());
			
			System.out.println("MCL is adding to PV " + term.getName() + " (" + term.getSource() + ") the variant: " + variant);
			term2TermTable.add(tRel);
		}
	}
	
	private ArrayList<Term> getRelatedTermsForLHS(Integer termId, String rel) {
		ArrayList<Term> relatedTerms = new ArrayList<Term>();

		//we want the left-hand side
		//( termX MSPof known)
		ArrayList<TermRelationship> rels = term2TermTable.getPairsForLHS(termId, rel);
		for(TermRelationship tRel : rels) {
			Integer lhs = tRel.getTermId1();
			Term term = termTable.getTermById(lhs);
			relatedTerms.add(term);
		}
		
		return relatedTerms;
	}
	
	private void buildTCodeHierarchy() {
		
		for( IcdConcept icd : this.icdHierarchy.keySet() ) {
			IcdConcept parent = icd;
			ArrayList<IcdConcept> children = this.icdHierarchy.get(parent);
			
			Concept hierParent = null;
			Term icdTerm = null;			
			if( !conceptTable.hasConcept(icd.getCode(), sourceMap.get("ICD")) ) {
				icdTerm = new Term();
				icdTerm.setId(++codeGenerator);
				icdTerm.setName(parent.getName());
				icdTerm.setSource(sourceMap.get("ICD"));
				icdTerm.setSourceId(parent.getCode());
				icdTerm.setTty(termTypeMap.get("PV"));

				hierParent = new Concept();
				hierParent.setClassType(classTypeMap.get("Class"));
				hierParent.setPreferredTermId(codeGenerator);				
				hierParent.setConceptId(++codeGenerator);
				hierParent.setSource(sourceMap.get("ICD"));
				hierParent.setSourceId(parent.getCode());
				
				icdTerm.setDrugConceptId(codeGenerator);
				
				conceptTable.add(hierParent);
				termTable.add(icdTerm);								
			}
			else {
				hierParent = conceptTable.getConcept(parent.getCode(), sourceMap.get("ICD"));
				icdTerm = termTable.getTerm(parent.getCode(), termTypeMap.get("PV"), sourceMap.get("ICD"));
			}
			
			for( IcdConcept child : children ) {
				Concept hierChild = null;
				Term icdChildTerm = null;
				if( !conceptTable.hasConcept(child.getCode(), sourceMap.get("ICD")) ) {
					icdChildTerm = new Term();
					icdChildTerm.setId(++codeGenerator);
					icdChildTerm.setName(child.getName());
					icdChildTerm.setSource(sourceMap.get("ICD"));
					icdChildTerm.setSourceId(child.getCode());
					icdChildTerm.setTty(termTypeMap.get("PV"));
					
					hierChild = new Concept();
					hierChild.setClassType(classTypeMap.get("Class"));
					hierChild.setPreferredTermId(codeGenerator);
					hierChild.setConceptId(++codeGenerator);
					hierChild.setSource(sourceMap.get("ICD"));
					hierChild.setSourceId(child.getCode());
					
					icdChildTerm.setDrugConceptId(codeGenerator);
					
					conceptTable.add(hierChild);
					termTable.add(icdChildTerm);
				}
				else {
					hierChild = conceptTable.getConcept(child.getCode(), sourceMap.get("ICD"));
					icdChildTerm = termTable.getTerm(child.getCode(), termTypeMap.get("PV"), sourceMap.get("ICD"));
				}
				
				if( hierParent != null && hierChild != null ) {
					if( !concept2ConceptTable.containsPair(hierChild.getConceptId(), "isa", hierParent.getConceptId())) {
						ConceptRelationship conRel = new ConceptRelationship();
						conRel.setId(++codeGenerator);
						conRel.setConceptId1(hierChild.getConceptId());
						conRel.setRelationship("isa");
						conRel.setConceptId2(hierParent.getConceptId());
						
						concept2ConceptTable.add(conRel);						
					}
				}
			}
		}
	}
	
	private void setAuthoritativeSourceTable() {
		//this can all be made configurable if desired
		Source s1 = new Source();
		Source s2 = new Source();
		Source s3 = new Source();
		Source s4 = new Source();
		Source s5 = new Source();
		Source s6 = new Source();
		Source s7 = new Source();
		Source s8 = new Source();
		
		s1.setId(++codeGenerator);
		s1.setName("RxNorm");
		sourceMap.put("RxNorm", String.valueOf(codeGenerator) );
		
		s2.setId(++codeGenerator);
		s2.setName("ATC");
		sourceMap.put("ATC", String.valueOf(codeGenerator));
		
		s3.setId(++codeGenerator);
		s3.setName("ICD");
		sourceMap.put("ICD", String.valueOf(codeGenerator));

		s4.setId(++codeGenerator);
		s4.setName("Misspelling");
		sourceMap.put("Misspelling", String.valueOf(codeGenerator));
		
		s5.setId(++codeGenerator);
		s5.setName("NFLIS");
		sourceMap.put("NFLIS", String.valueOf(codeGenerator));
		
		s6.setId(++codeGenerator);
		s6.setName("DEA");
		sourceMap.put("DEA", String.valueOf(codeGenerator));
	
		s7.setId(++codeGenerator);
		s7.setName("FDA");
		sourceMap.put("FDA", String.valueOf(codeGenerator));
		
		s8.setId(++codeGenerator);
		s8.setName("MCL");
		sourceMap.put("MCL", String.valueOf(codeGenerator));
		
		authoritativeSourceTable.add(s1);
		authoritativeSourceTable.add(s2);
		authoritativeSourceTable.add(s3);
		authoritativeSourceTable.add(s4);
		authoritativeSourceTable.add(s5);
		authoritativeSourceTable.add(s6);
		authoritativeSourceTable.add(s7);
		authoritativeSourceTable.add(s8);
	}
	
	private void setConceptTypeTable() {
		ConceptType t1 = new ConceptType();
		ConceptType t2 = new ConceptType();
		
		t1.setId(++codeGenerator);
		t1.setDescription("Substance");
		classTypeMap.put("Substance", String.valueOf(codeGenerator));
		
		t2.setId(++codeGenerator);;
		t2.setDescription("Class");
		classTypeMap.put("Class", String.valueOf(codeGenerator));
		
		conceptTypeTable.add(t1);
		conceptTypeTable.add(t2);		
	}
	
	private void setTermTypeTable() {
		TermType t1 = new TermType();
		TermType t2 = new TermType();
		TermType t3 = new TermType();
		TermType t4 = new TermType();
		TermType t5 = new TermType();		
		TermType t6 = new TermType();
		TermType t7 = new TermType();		
		
		t1.setId(++codeGenerator);
		t1.setAbbreviation("IN");
		t1.setDescription("Ingredient");
		termTypeMap.put("IN", String.valueOf(codeGenerator));
		
		t2.setId(++codeGenerator);
		t2.setAbbreviation("PIN");
		t2.setDescription("Precise Ingredient");
		termTypeMap.put("PIN", String.valueOf(codeGenerator));
		
		t3.setId(++codeGenerator);
		t3.setAbbreviation("BN");
		t3.setDescription("Brand Name");
		termTypeMap.put("BN", String.valueOf(codeGenerator));
		
		t4.setId(++codeGenerator);
		t4.setAbbreviation("MSP");
		t4.setDescription("Misspelling");
		termTypeMap.put("MSP", String.valueOf(codeGenerator));		
		
		t5.setId(++codeGenerator);
		t5.setAbbreviation("SY");
		t5.setDescription("Synonym");
		termTypeMap.put("SY", String.valueOf(codeGenerator));
		
		t6.setId(++codeGenerator);
		t6.setAbbreviation("PV");
		t6.setDescription("Principal Variant");
		termTypeMap.put("PV", String.valueOf(codeGenerator));

		t7.setId(++codeGenerator);
		t7.setAbbreviation("UNII");
		t7.setDescription("Unique Ingredient Identifier");
		termTypeMap.put("UNII", String.valueOf(codeGenerator));		
		
		termTypeTable.add(t1);
		termTypeTable.add(t2);
		termTypeTable.add(t3);
		termTypeTable.add(t4);		
		termTypeTable.add(t5);
		termTypeTable.add(t6);
		termTypeTable.add(t7);
		
	}
		
	private void serialize() {
		
		System.out.println("[10] Serializing table files");
		
		this.authoritativeSourceTable.print(this.authoritativeSourceFile);
		this.conceptTypeTable.print(this.conceptTypeFile);
		this.termTypeTable.print(this.termTypeFile);
		this.termTable.print(this.termFile);
		this.conceptTable.print(this.conceptFile);
		this.term2TermTable.print(this.term2termFile);
		this.concept2ConceptTable.print(this.concept2conceptFile);

	}
	
	private void cleanup() {
		
		authoritativeSourceFile.close();
		conceptTypeFile.close();
		termTypeFile.close();
		termFile.close();
		conceptFile.close();
		term2termFile.close();
		concept2conceptFile.close();
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
