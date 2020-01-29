package gov.nih.nlm.uts;
//import org.junit.Test;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import gov.nih.nlm.uts.AtomLite;
import gov.nih.nlm.uts.RestTicketClient;
import gov.nih.nlm.uts.SourceAtomClusterLite;

import static com.jayway.restassured.RestAssured.given;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class WalkHierarchyTestCase {

	//String username = System.getProperty("username"); 
	//String password = System.getProperty("password");
	String apiKey = System.getProperty("apikey");
	String id;
	String source;
	//specifying version is not required - if you leave it out the script will default to the latest UMLS publication.
	String version = System.getProperty("version");
	//use either 'parents', 'children', 'ancestors', or 'descendants' here
	//String operation = System.getProperty("operation");
	String operation = "children";
	RestTicketClient ticketClient = new RestTicketClient(apiKey);
	//get a ticket granting ticket for this session.
	String tgt = ticketClient.getTgt();
	public ArrayList<String> children = new ArrayList<String>();
	
	public WalkHierarchyTestCase(String id, String source) throws Exception {
		this.id = id;
		this.source = source;
	    version = System.getProperty("version") == null ? "current": System.getProperty("version");
		//if you do not specify a source vocabulary, the script assumes you're searching for CUI
	    String path = "/rest/content/2016/source/"+source+"/"+id+"/"+operation;
	    SourceAtomClusterLite[] sourceAtomClusters;
//	    AtomLite[] atoms;
	    int page=1;
	    int results = 0;
	    int pageCount;
	    //calls to descendants may produce several hundred or even several thousand results.
	    //so we page through them here.  
	    do {
//		    System.out.println("Page "+page);
//		    System.out.println("***********");
			RestAssured.baseURI = "https://uts-ws.nlm.nih.gov";
	    	Response response =  given()//.log().all()
	                .request().with()
	                //we need a new service ticket for each call since we're requesting multiple pages.
	                	.param("ticket", ticketClient.getST(tgt))
	                	.param("pageNumber",page)
	        	 .expect()
	       		 .statusCode(200)
	        	 .when().get(path);
	        	 //response.then().log().all();;
//	    	System.out.println(response.asString());

	    	String output = response.getBody().asString();
			Configuration config = Configuration.builder().mappingProvider(new JacksonMappingProvider()).build();
			pageCount = JsonPath.using(config).parse(output).read("$.pageCount");

//
//			//the HL7 sources return Atom objects, rather than SourceAtomClusters
//			if(source.equals("HL7V3.0") || source.equals("HL7V2.5")) {
//				
//			   atoms = 	JsonPath.using(config).parse(output).read("$.result",AtomLite[].class);
//			   
//			   for (AtomLite atom:atoms) {
//				   System.out.println("Ui: " + atom.getUi());
//				   System.out.println("Name: "+ atom.getName());
//				   System.out.println("Code: " + atom.getCode());
//				   System.out.println("Parents: "+ atom.getParents());
//				   System.out.println("Children: " + atom.getChildren());
//				   System.out.println("-------");
//				   
//			   }
//			   
//			   results += atoms.length;
//			}
				
			sourceAtomClusters = JsonPath.using(config).parse(output).read("$.result",SourceAtomClusterLite[].class);
		    for(SourceAtomClusterLite sourceAtomCluster : sourceAtomClusters) {
			if( sourceAtomCluster.getUi().contains("T") ) {  //RWW: Added to filter S codes in the branch for T codes only
				children.add(sourceAtomCluster.getUi());
//				System.out.println("Ui: "+sourceAtomCluster.getUi());
//				System.out.println("Name: "+sourceAtomCluster.getName());
//				System.out.println("Number of Atoms: "+sourceAtomCluster.getAtomCount());
//				System.out.println("Concepts: "+sourceAtomCluster.getConcepts());
//				System.out.println("Ancestors: "+sourceAtomCluster.getAncestors());
//				System.out.println("Parents: "+ sourceAtomCluster.getParents());
//				System.out.println("Children: "+sourceAtomCluster.getChildren());
//				System.out.println("Descendants: "+sourceAtomCluster.getDescendants());
//				System.out.println("Highest Ranking Atom: "+sourceAtomCluster.getDefaultPreferredAtom());
//			    System.out.println("-------");
			    }
		    }
		    
		    results += sourceAtomClusters.length;
        
		    page++;
            
            //assertTrue(sourceAtomClusters.length > 0);
		 
		   } while(page <= pageCount );
//	    System.out.println("Found " + results + " results");

	}
	
	public ArrayList<String> getChildren() {
//		System.out.println(id + " -> " + this.children.toString());
		return this.children;
	}
	    	
}
	
