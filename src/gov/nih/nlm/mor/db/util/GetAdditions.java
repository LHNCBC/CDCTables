package gov.nih.nlm.mor.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetAdditions {
	
	private HashMap<String, String> atcCode2Name = new HashMap<String, String>();
	
	public final String url = "https://rxnav.nlm.nih.gov/REST";
	public ArrayList<String> records = new ArrayList<String>();
	private final String allConceptsUrl = "https://rxnav.nlm.nih.gov/REST/allconcepts.json?tty=IN";
	private final String allClassesUrl = "https://rxnav.nlm.nih.gov/REST/rxclass/allClasses.json?classTypes=ATC1-4";
	private PrintWriter pw = null;
	
	public static void main(String[] args) {
		GetAdditions additions = new GetAdditions(args[0]);
		additions.gather();
	}
	
	public GetAdditions(String filename) {
		try {
			pw = new PrintWriter(new File(filename));
		} catch(Exception e) {
			System.out.println("Unable to create the additions file.");			
			e.printStackTrace();
		}
		if( pw == null) {
			System.exit(-1);
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
		
		System.out.println("[6] Building T-code hierarchy");
		
		System.out.println("[2] Fetching ATC Classes");
		ArrayList<String> atcCodes = new ArrayList<String>();		
		
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
						
						StringJoiner conceptJoiner = new StringJoiner("|");
						StringJoiner termJoiner = new StringJoiner("|");						
						
						conceptJoiner.add(className);
						conceptJoiner.add("");
						conceptJoiner.add(className);
						conceptJoiner.add(classId);
						conceptJoiner.add("ATC");
						conceptJoiner.add("Class");
						
						atcCodes.add(classId);
					
						addRecord(conceptJoiner.toString());
						atcCode2Name.put(classId, className);
						
						
//						Term term = new Term();
//						term.setId(++codeGenerator);
//						term.setName(className);
//						term.setSourceId(classId);
//						term.setSource(sourceMap.get("ATC"));
//						term.setTty(termTypeMap.get("PV"));
//						
//						Concept concept = new Concept();
//						concept.setConceptId(++codeGenerator);
//						Integer conceptId = codeGenerator;
//						concept.setPreferredTermId(term.getId());						
//						concept.setClassType(classTypeMap.get("Class"));
//						concept.setSource(sourceMap.get("ATC"));
//						concept.setSourceId(classId);
//						
//						term.setDrugConceptId(conceptId);
//						
//						termTable.add(term);
//						conceptTable.add(concept);					
					}
				}
			}
		}
		
		System.out.println("[3] Collecting edges of ATC classes for isa relations");
		//collect edges for each concept
		//https://rxnav.nlm.nih.gov/REST/rxclass/classGraph.json?classId=A&source=ATC1-4
//		ArrayList<Concept> conceptList = conceptTable.getConceptsOfSource(sourceMap.get("ATC"));
		for( int i=0; i < atcCodes.size(); i++ ) {
			String code = atcCodes.get(i);

			String graphUrl = "https://rxnav.nlm.nih.gov/REST/rxclass/classGraph.json?classId=" + code + "&source=ATC1-4";
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
							
							String className1 = atcCode2Name.get(classId1);
							String className2 = atcCode2Name.get(classId2);
							
							StringJoiner conceptJoiner = new StringJoiner("|");
							
							conceptJoiner.add(className1);
							conceptJoiner.add("isa");
							conceptJoiner.add(className2);
							conceptJoiner.add("");
							conceptJoiner.add("ATC");
							conceptJoiner.add("Class");
							
							addRecord(conceptJoiner.toString());
							
//							if( conceptTable.hasConcept(classId1, sourceMap.get("ATC")) ) {
//								Concept c = conceptTable.getConcept(classId1, sourceMap.get("ATC"));
//								if( c != null) {
//									classIndex1 = c.getConceptId();
//								}								
//							}
//							if( conceptTable.hasConcept(classId2, sourceMap.get("ATC")) ) {
//								Concept c = conceptTable.getConcept(classId2, sourceMap.get("ATC"));
//								if( c != null) {
//									classIndex2 = c.getConceptId();
//								}
//							}
//							if( !concept2ConceptTable.containsPair(classIndex1, "isa", classIndex2) && classIndex1 != null && classIndex2 != null) {
//								ConceptRelationship conRel = new ConceptRelationship();
//								conRel.setId(++codeGenerator);
//								conRel.setConceptId1(classIndex1);
//								conRel.setRelationship("isa");
//								conRel.setConceptId2(classIndex2);
//								concept2ConceptTable.add(conRel);
//							}
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
				
				StringJoiner conceptJoiner = new StringJoiner("|");
				
				conceptJoiner.add(name);
				conceptJoiner.add("PV");
				conceptJoiner.add(name);
				conceptJoiner.add(rxcui);
				conceptJoiner.add("RxNorm");			
				conceptJoiner.add("Substance");
				
				addRecord(conceptJoiner.toString());
				
				
//				Concept concept = new Concept();
//				Term term = new Term();
//
//				term.setId(++codeGenerator);
//				Integer preferredTermId = codeGenerator;				
//				term.setName(name);
//				term.setTty(termTypeMap.get(type)); //this could be IN instead
//				term.setSourceId(rxcui);
//				term.setSource(sourceMap.get("RxNorm"));				
//				
//		
//				concept.setConceptId(++codeGenerator);
//				Integer conceptId = codeGenerator;
//				concept.setSource(sourceMap.get("RxNorm"));
//				concept.setSourceId(rxcui);
//				concept.setClassType(classTypeMap.get("Substance"));
//				concept.setPreferredTermId(preferredTermId);
//				
//				term.setDrugConceptId(conceptId);
//
//				conceptTable.add(concept);
//				termTable.add(term);
				
			
				JSONObject allProperties = null;
				JSONObject allRelated = null;
				JSONObject possibleMembers = null;;
				
				try {
					allRelated = getresult("https://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui + "/allrelated.json");
					allProperties = getresult("https://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui + "/allProperties.json?prop=all");		
					possibleMembers = getresult("https://rxnav.nlm.nih.gov/REST/rxclass/class/byRxcui.json?rxcui=" + rxcui + "&relaSource=ATC");				
				} catch(IOException e) {
					System.out.println("https://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui + "/allrelated.json");
					System.out.println("https://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui + "/allProperties.json?prop=all");
					System.out.println("https://rxnav.nlm.nih.gov/REST/rxclass/class/byRxcui.json?rxcui=" + rxcui + "&relaSource=ATC");
					e.printStackTrace();
				}
				
//				System.out.println("https://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui + "/allrelated.json");
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
									
									StringJoiner termJoiner = new StringJoiner("|");
									
									String relatedCuiString = soleProperty.get("rxcui").toString();
									String relatedName = soleProperty.get("name").toString();
									
									termJoiner.add(relatedName);
									termJoiner.add(relatedType);
									termJoiner.add(name);  //from first for loop
									termJoiner.add(relatedCuiString);
									termJoiner.add("RxNorm");
									termJoiner.add("Substance");
									
									addRecord(termJoiner.toString());
																		
//									
//									relatedTerm.setId(++codeGenerator);
//									relatedTerm.setName(relatedName);
//									relatedTerm.setTty(termTypeMap.get(relatedType));
//									relatedTerm.setSourceId(relatedCuiString);
//									relatedTerm.setSource(sourceMap.get("RxNorm"));
//									
//									Concept conceptForTerm = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
//									Integer conceptIdForTerm = conceptForTerm.getConceptId();
//									
//									relatedTerm.setDrugConceptId(conceptIdForTerm);
//									
//									termTable.add(relatedTerm);
//									
//									Integer termId = codeGenerator;
//									
//									TermRelationship termRel = new TermRelationship();
//									termRel.setId(++codeGenerator);
//									termRel.setTermId1(termId);
//									termRel.setTermId2(preferredTermId);
//									termRel.setRelationship(relatedType);
//									
//									term2TermTable.add(termRel);
								}
								
						}
							
					}
						
				}
				
				//what are we looking for here?  Synonyms and UNIIs
//				System.out.println("https://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui + "/allProperties.json?prop=all");
				if( allProperties != null ) {
					JSONObject propConceptGroup = (JSONObject) allProperties.get("propConceptGroup");
					JSONArray propConcept = (JSONArray) propConceptGroup.get("propConcept");
					
					for( int j = 0; j < propConcept.length(); j++ ) {
						
						JSONObject prop = (JSONObject) propConcept.get(j);
						String propName = prop.getString("propName");
						
						StringJoiner termJoiner = new StringJoiner("|");
						
						if( propName.equals("RxNorm Synonym") ) {			
							
							termJoiner.add(prop.get("propValue").toString());
							termJoiner.add("SY");
							termJoiner.add(name);
							termJoiner.add(rxcui);
							termJoiner.add("RxNorm");
							termJoiner.add("Substance");
							
//							Term synonym = new Term();
//							synonym.setId(++codeGenerator);					
//							synonym.setName(prop.get("propValue").toString());
//							synonym.setTty(termTypeMap.get("SY"));
//							synonym.setSourceId(rxcui);
//							synonym.setSource(sourceMap.get("RxNorm"));
//							
//							Concept conceptForTerm = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
//							Integer conceptIdForTerm = conceptForTerm.getConceptId();
//							
//							synonym.setDrugConceptId(conceptIdForTerm);
//							
//							termTable.add(synonym);
//							
//							Integer synonymId = codeGenerator;							
//							
//							TermRelationship termRel = new TermRelationship();
//							termRel.setId(++codeGenerator);
//							termRel.setTermId1(synonymId);							
//							termRel.setTermId2(preferredTermId);
//							termRel.setRelationship("SY");
//							
//							term2TermTable.add(termRel);
							
						}
// 191107 - OB: Include
						else if( propName.equals("UNII_CODE") ) {
//							Term uniiCode = new Term();
							
							termJoiner.add(prop.get("propValue").toString());
							termJoiner.add("UNII");
							termJoiner.add(name);
							termJoiner.add(rxcui);
							termJoiner.add("RxNorm");
							termJoiner.add("Substance");							
							
							
//							uniiCode.setId(++codeGenerator);
//							//uniiCode.setName(prop.getString("propValue").toString());
//							uniiCode.setName(name); //OB: use the rx name
//							uniiCode.setTty(termTypeMap.get("UNII"));
//							uniiCode.setSourceId(prop.getString("propValue").toString());
//							uniiCode.setSource(sourceMap.get("FDA"));
//							
//							Concept conceptForTerm = conceptTable.getConcept(rxcui, sourceMap.get("RxNorm"));
//							Integer conceptIdForTerm = conceptForTerm.getConceptId();
//							
//							uniiCode.setDrugConceptId(conceptIdForTerm);
//							
//							termTable.add(uniiCode);
//							
//							Integer uniiId = codeGenerator;
//							
//							TermRelationship uniiRel = new TermRelationship();  //term-term rel was here all along, i just can't remember these things during meetings
//							uniiRel.setId(++codeGenerator);
//							uniiRel.setTermId1(uniiId);
//							uniiRel.setTermId2(preferredTermId);
//							uniiRel.setRelationship("UNII");
//							
//							term2TermTable.add(uniiRel);
						}
						
						if(termJoiner.length() != 0) {
							addRecord(termJoiner.toString());
						}
						
					}
				}
				
//				System.out.println("https://rxnav.nlm.nih.gov/REST/rxclass/class/byRxcui.json?rxcui=" + rxcui + "&relaSource=ATC");	
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
									String className = rxclassMinConceptItem.get("className").toString();
									
									StringJoiner memberJoiner = new StringJoiner("|");
									memberJoiner.add(name);
									memberJoiner.add("memberof");
									memberJoiner.add(className);
									memberJoiner.add("");
									memberJoiner.add("ATC");
									memberJoiner.add("Class");									
									
// we may have to assume here for the addition...
//									if( conceptTable.hasConcept(rxcui, sourceMap.get("RxNorm")) && conceptTable.hasConcept(classId, sourceMap.get("ATC")) ) {
//
//										
//										
//										
// this logic is handled during addition										
//										if( !concept2ConceptTable.containsPair(c1.getConceptId(), "memberof", c2.getConceptId()) ) {
//											ConceptRelationship conRel = new ConceptRelationship();
//											conRel.setId(++codeGenerator);
//											conRel.setConceptId1(c1.getConceptId());
//											conRel.setRelationship("memberof");
//											conRel.setConceptId2(c2.getConceptId());
//											
//											concept2ConceptTable.add(conRel);
//										}
//									}
								}
							}
						}
					}
				}
			}
		}
		
		printRecords(records);
		pw.close();
		
	}	
	
	public void addRecord(String s) {
		if(!records.contains(s)) {
			records.add(s);
		}
	}
	
	public void printRecords(ArrayList<String> recs) {
		for(String record : recs) {
			pw.println(record);
			pw.flush();
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
