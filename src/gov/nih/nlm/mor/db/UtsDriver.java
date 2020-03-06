/*
 * The purpose of this class is to build
 * the ICD-10 hierarchy configuration file with
 * the UTS API
 * 
 * In order to run this, you will need to apply
 * for a free UTS account.  Within the profile of your
 * account you'll find an apikey.  This should be passed in as
 * a parameter to the JVM as:
 * 		> java -jar CDCTables*-withdependencies.jar -Dapikey=4f128796-9241-406a-8131-00b698cf3134
 */
package gov.nih.nlm.mor.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import gov.nih.nlm.uts.RetrieveCodeTestCase;
import gov.nih.nlm.uts.WalkHierarchyTestCase;

public class UtsDriver {
	
	PrintWriter pw = null;
	String rootCode = "";
	String source = "";
	String outputPath = "./10-par-chd-rels.txt";
	
	
	public static void main(String args[]) {
		UtsDriver driver = new UtsDriver();
		driver.config();
		driver.run();
		driver.cleanup();
	}
	
	public UtsDriver() {

	}
	
	public void config() {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		rootCode = "S00-T98.9";
		source = "ICD10";
		try {
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath)),StandardCharsets.UTF_8),true);
		} catch(Exception e) {
			System.out.println("Cannot create the output stream.");
			e.printStackTrace();
		}
		
	}
	
	public void run() {
//		System.out.println(getName(this.rootCode));
		long start = System.currentTimeMillis();			
		ArrayList<String> rootList = new ArrayList<String>();
		rootList.add(this.rootCode);
		walkHierarchy(rootList);
//		printParChd(this.rootCode, new ArrayList<String>());
		pw.println("Finished hierarchy retrieval in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
		pw.flush();
	}

	public void printParChd(String parent, ArrayList<String> children) {
		for( String child : children ) {
			String parentName = getName(parent, this.source);			
			String childName = getName(child, this.source);
			pw.println(parent + "|" + parentName + "|" + child + "|" + childName);
			pw.flush();
		}
	}
	
	public void walkHierarchy(ArrayList<String> roots) {
		for( String parent : roots ) {
			ArrayList<String> children = new ArrayList<String>();
			try {
				WalkHierarchyTestCase walk = new WalkHierarchyTestCase(parent, this.source);
				children = walk.getChildren();
				if( !children.isEmpty() ) {
					printParChd(parent, children);
					walkHierarchy(children);
				}
			} catch (Exception e) {
				System.out.println("!!! Unable to walk the hierarchy from T-code " + parent); 
				e.printStackTrace();
			}
		}
	}
	
	public String getName(String code, String source) {
		String name = "";
		try {
			RetrieveCodeTestCase retrieve = new RetrieveCodeTestCase(code, source);
			name = retrieve.getName();
		} catch (Exception e) {
			System.out.println("!!! Unable to find the " + source + " name for code: " + code);
			e.printStackTrace();
		}		
		return name;
	}
	
	public void cleanup() {
		pw.close();
	}
}
