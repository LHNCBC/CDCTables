package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;

import gov.nih.nlm.mor.db.rxnorm.Term;

public class TermTable {
	private ArrayList<Term> rows = new ArrayList<Term>();
	
	public TermTable() {
		
	}
	
	public void add(Term t) {
		rows.add(t);
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
			if( term.getName().equals(name) && term.getSource().equals(source) ) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public Term getTermByName(String name, String source) {
		Term resultTerm = null;
		for( Term term : rows ) {
			if( term.getName().equals(name) && term.getSource().equals(source) ) {
				resultTerm = term;
				break;
			}
		}
		return resultTerm;
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
		
		for( Term t : rows ) {
			pw.println(t.getId() + "|" + t.getName() + "|" + t.getTty() + 
					"|" + t.getSourceId() + "|" + t.getSource() + "||||||" + t.getDrugConceptId());
			pw.flush();
		}
		
	}

}
