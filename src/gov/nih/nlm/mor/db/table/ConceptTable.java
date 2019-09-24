package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.json.JSONObject;

import gov.nih.nlm.mor.db.rxnorm.Concept;

public class ConceptTable {

	private ArrayList<Concept> rows = new ArrayList<Concept>();
	
	public ConceptTable() {
		
	}
	
	public void add(Concept c) {
		rows.add(c);
	}
	
	public boolean hasConcept(String s, String source) {
		boolean exists = false;
		for(Concept c : rows ) {
			if( c.getSourceId().equals(s) && c.getSource().equals(source) ) {
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	public Concept getConcept(String s, String source) {
		Concept concept = null;
		for( Concept c : rows ) {
			if( c.getSourceId().equals(s) && c.getSource().equals(source) ) {
				concept = c;
				break;
			}
		}
		return concept;
	}
	
	public Concept getConceptByTerm(String s, String source) {
		Concept concept = null;
		for( Concept c: rows ) {
			if( c.getPreferredTermId().equals(s) && c.getSource().equals(source) ) {
				concept = c;
				break;
			}
		}
		return concept;
	}
	
	public ArrayList<Concept> getConceptsOfSource(String source) {
		ArrayList<Concept> list = new ArrayList<Concept>();
		for( Concept c : rows ) {
			if( c.getSource().equals(source) ) {
				list.add(c);
			}
		}
		return list;
	}
	
	public void print(PrintWriter pw) {
		/*	[DrugConceptID] [bigint] IDENTITY(1,1) NOT NULL,
	[PreferredTermID] [bigint] NOT NULL,
	[DrugAuthoritativeSourceID] [smallint] NOT NULL,
	[DrugConceptTypeID] [bigint] NULL,
	[DrugSourceConceptID] [varchar](32) NULL,
	[CreationDate] [smalldatetime] NULL,
	[CreationUserID] [char](4) NULL,
	[UpdatedDate] [smalldatetime] NULL,
	[UpdateUserID] [char](4) NULL,
	[IsActive] [bit] NOT NULL,
		 * 
		 */
		
		for( Concept c : rows ) {
			pw.println(c.getConceptId() + "|" + c.getPreferredTermId() + "|" + c.getSource() + "|" +
					c.getClassType() + "|" + c.getSourceId() + "||||||");
			pw.flush();
		}
		
	}	

}
