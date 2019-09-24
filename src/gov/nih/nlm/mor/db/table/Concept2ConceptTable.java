package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;

import gov.nih.nlm.mor.db.rxnorm.ConceptRelationship;

public class Concept2ConceptTable {
	
	private ArrayList<ConceptRelationship> rows = new ArrayList<ConceptRelationship>();
	
	public Concept2ConceptTable() {
		
	}
	
	public void add(ConceptRelationship r) {
		this.rows.add(r);
	}	
	
	public void print(PrintWriter pw) {
		/*	[DrugConceptConceptID] [bigint] NOT NULL,
	[DrugConceptID1] [bigint] NULL,
	[Relation] [char](50) NULL,
	[DrugConceptID2] [bigint] NULL,
	[CreationUserID] [char](4) NULL,
	[CreationDateTime] [smalldatetime] NULL,
	[UpdatedUserID] [char](4) NULL,
	[UpdatedDateTime] [smalldatetime] NULL,
	[IsActive] [bit] NULL,
		 * 
		 */
		for( ConceptRelationship rel : rows ) {
			pw.println(rel.getId() + "|" + rel.getConceptId1() + "|" + rel.getRelationship() +
					"|" + rel.getConceptId2() + "|||||");
			pw.flush();
		}
		
	}
	
	public boolean containsPair(Integer id1, String rel, Integer id2) {
		boolean exists = false;
		for( ConceptRelationship row : this.rows ) {
			if( row.getConceptId1().equals(id1) && row.getConceptId2().equals(id2) && row.getRelationship().contentEquals(rel) ) {
				exists = true;
				break;
			}
		}
		return exists;
	}
	
}
