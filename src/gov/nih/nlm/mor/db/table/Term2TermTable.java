package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;

import gov.nih.nlm.mor.db.rxnorm.TermRelationship;

public class Term2TermTable {

	private ArrayList<TermRelationship> rows = new ArrayList<TermRelationship>();
	
	public Term2TermTable() {

	}
	
	public void add(TermRelationship r) {
		this.rows.add(r);
	}
	
	public boolean hasPair(Integer c1, String rel, Integer c2) {
		boolean result = false;
		for( TermRelationship tRel : rows ) {
			if( tRel.getTermId1().equals(c1) && tRel.getRelationship().equals(rel) && tRel.getTermId2().equals(c2)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public TermRelationship getPair(Integer c1, String rel, Integer c2) {
		TermRelationship resultRel = null;
		for( TermRelationship tRel : rows ) {
			if( tRel.getTermId1().equals(c1) && tRel.getRelationship().equals(rel) && tRel.getTermId2().equals(c2)) {
				resultRel = tRel;
				break;
			}
		}
		return resultRel;		
		
	}
	
	public void print(PrintWriter pw) {
		/*	[DrugTermTermID] [bigint] NOT NULL,
	[DrugTermID1] [bigint] NULL,
	[Relation] [char](50) NULL,
	[DrugTermID2] [bigint] NULL,
	[CreationUserID] [char](4) NULL,
	[CreationDateTime] [smalldatetime] NULL,
	[UpdatedUserID] [char](4) NULL,
	[UpdatedDateTime] [smalldatetime] NULL,
	[IsActive] [bit] NULL,
		 * 
		 */
		for( TermRelationship r : rows ) {
			pw.println(r.getId() + "|" + r.getTermId1() + "|" + r.getRelationship() + 
					"|" + r.getTermId2() + "|||||");
		}
		
	}
	
}
