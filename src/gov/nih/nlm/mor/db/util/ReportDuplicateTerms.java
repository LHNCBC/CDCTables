package gov.nih.nlm.mor.db.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import gov.nih.nlm.mor.db.rxnorm.Term;

public class ReportDuplicateTerms {
	
	public PrintWriter pw = null;
	public HashMap<String, Term> unii2PrincipalVariant = new HashMap<String, Term>();
	public HashMap<String, ArrayList<Term>> unii2Terms = new HashMap<String, ArrayList<Term>>();
	public HashMap<Term, String> pv2Unii = new HashMap<Term, String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReportDuplicateTerms report = new ReportDuplicateTerms();
		report.config(args[0]);
//		report.run();
//		report.print();
	}
	
	public void config(String filename) {
		readFile(filename);
				
	}
	
	
	
	
	
	private void readFile(String filename) {
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
//					if( line != null && line.contains("|") ) {
					if( line != null ) {
						String[] values = line.split("\\|", -1);
						String termName = values[0];
						String termType = values[1];
						String pv = values[2];
						String source = values[4];
						String classType = values[5];
						if(termType.equalsIgnoreCase("unii")) {
							Term term = new Term();
							term.setName(termName);
							term.setSource(source);
							term.setTty(termType);
				
							unii2PrincipalVariant.put(termName, term);
							pv2Unii.put(term, termName);
							
						}
						else if(!classType.equalsIgnoreCase("class")) {
							Term term = new Term();
							term.setName(termName);
							term.setSource(source);
							term.setTty(termType);
							
							
							
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

}
