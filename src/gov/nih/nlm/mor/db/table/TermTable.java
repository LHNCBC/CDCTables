package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalDateTime;            // Import the LocalDateTime class
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class

import gov.nih.nlm.mor.db.rxnorm.Term;
import gov.nih.nlm.mor.util.WordCheck;

public class TermTable {
	private ArrayList<Term> rows = new ArrayList<Term>();
	private WordCheck wordCheck = new WordCheck();
	private ArrayList<String> seenWords = new ArrayList<String>();
	private String brandNameCode = null;
	
	public TermTable() {
		
	}
	
	public void add(Term t) {
		if(t.getTty().equals(brandNameCode) && wordCheck.isWord(t.getName())) {
			t.setIsActive(false);
			if(!seenWords.contains(t.getName()) ) seenWords.add(t.getName());
//			System.out.println(t.getName() + " (BN as a word) will be loaded as a deactivated row.");			
		}
		rows.add(t);		
	}
	

	public void setBnCode(String code) {
		this.brandNameCode = code;
	}

	public ArrayList<String> getSeenWords() {
		return this.seenWords;
	}
	
	public boolean hasTerm(String sourceId, String rel, String source) {
		boolean result = false;
		for( Term term : rows ) {
			if( term.getSourceId().equals(sourceId) && term.getTty().equals(rel) && term.getSource().equals(source) ) {
				result = true;
				break;
			}
		}
		return result;
	}

	public boolean hasTermByName(String name, String source) {
		boolean result = false;
		for( Term term : rows ) {
			if( term.getName().equalsIgnoreCase(name) && term.getSource().equals(source) ) {
				return true;
			}
		}
		return result;
	}
	
	public Term getTermByName(String name, String source) {
		Term resultTerm = null;
		for( Term term : rows ) {
			if( term.getName().equalsIgnoreCase(name) && term.getSource().equals(source) ) {
				return term;
			}
		}
		return resultTerm;
	}
	
	public ArrayList<Term> getTermByName(String name) {
		ArrayList<Term> terms = new ArrayList<Term>();
		for( Term term : rows ) {
			if( term.getName().equalsIgnoreCase(name) ) {
				terms.add(term);
			}
		}
		return terms;
	}
	
	public ArrayList<String> getConceptIdByTermName(String name) {
		ArrayList<String> list = new ArrayList<String>();
		for(Term term : rows) {
			if(term.getName().equalsIgnoreCase(name)) {
				String conceptId = term.getDrugConceptId();
				if(!list.contains(conceptId)) {
					list.add(term.getDrugConceptId());
				}
			}
		}
		return list;
	}	
	
	public Term getTermById(Integer id) {
		for( Term term : rows ) {
			if( term.getId().equals(id) ) {
				return term;
			}
		}
		return null;
	}	
	
	// 17-Mar-2020 added
	public Term getTermByConceptId( String conceptId, String name, String source)
	{
		Term result = null;
		for ( Term term : rows ) {
			if( term.getDrugConceptId().equals(conceptId) && term.getName().equalsIgnoreCase(name)
			  && term.getSource().equalsIgnoreCase(source)) {
				result = term;
				return result;
			}
		}
		return result;
	}
	
	public Term getTerm(String sourceId, String type, String source) {
		Term result = null;
		for( Term term : rows ) {
			if( term.getSourceId().equals(sourceId) && term.getTty().equals(type) && term.getSource().equals(source) ) {
				result = term;
			}
		}
		return result;
	}
	
	public ArrayList<Term> getTermsForSource(String name, String rel, String source) {
		ArrayList<Term> terms = new ArrayList<Term>();
		for( Term term : rows ) {
			if(term.getName().equalsIgnoreCase(name) && term.getTty().equals(rel) && term.getSource().equals(source)) {
				terms.add(term);
			}
		}
		return terms;
	}
	
	public ArrayList<Term> getTermsByType(String name, String type) {
		ArrayList<Term> result = new ArrayList<Term>();
		for( Term term : rows ) {
			if( term.getName().equalsIgnoreCase(name) && term.getTty().equals(type)) {
				result.add(term);
			}
		}
		return result;
	}
	
//	public ArrayList<Term> getTermsByType(Integer id, String name) {
//		ArrayList<Term> result = new ArrayList<Term>();
//		for( Term term : rows ) {
//			if( term.getName().toLowerCase().equals(name.toLowerCase()) && term.getTty().equals(type)) {
//				result.add(term);
//			}
//		}
//		return result;
//	}		
	
	public ArrayList<Term> getTerms() {
		return this.rows;
	}
	
	public void print(PrintWriter pw) {
		/*	[DrugTermID] [bigint] IDENTITY(1,1) NOT NULL,
	[DrugTermName] [varchar](50) NOT NULL,
	[DrugTTYID] [smallint] NOT NULL,
	[DrugExternalID] [varchar](32) NULL,
	[DrugAuthoritativeSourceID] [smallint] NULL,
	[CreationUserID] [char](4) NULL,
	[CreationDate] [smalldatetime] NULL,
	[UpdatedUserID] [char](5) NULL,
	[UpdatedDate] [smalldatetime] NULL,
	[IsActive] [bit] NULL,
	190717: ++[DrugConceptID] [bigint] NULL	
		 * 
		 */
	    LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	    String formattedDate = myDateObj.format(myFormatObj);
		
		for( Term t : rows ) {
			String isActive = "1";
			if(!t.getIsActive()) isActive = "0";
			
			pw.println(t.getId() + "|" + t.getName() + "|" + t.getTty() + 
					"|" + t.getSourceId() + "|" + t.getSource() + 
					"||"+formattedDate+"|||"+ isActive + "|" + t.getDrugConceptId());
			pw.flush();
		}
		
	}


}
