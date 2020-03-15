package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalDateTime;            // Import the LocalDateTime class
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class

import gov.nih.nlm.mor.db.rxnorm.Term;
import gov.nih.nlm.mor.db.rxnorm.TermRelationship;

public class Term2TermTable {

	private ArrayList<TermRelationship> rows = new ArrayList<TermRelationship>();
//	private Integer c1TermId = (int) -1;
//	private Integer c2TermId = (int) -1;
//	private String rel = "";
	
	public Term2TermTable() {

	}
	
	public void add(TermRelationship r) {
		this.rows.add(r);
	}
	
	public boolean hasPair(String c1, String rel, String c2, String source) {
		if( source == null ) source = "";
		Term t1 = new Term(c1, rel, source);
		Term t2 = new Term(c2, rel, source);
		TermRelationship t2tRel = new TermRelationship(t1, rel, t2);
		if( rows.contains(t2tRel)) return true;
		return false;
	}
	
	public ArrayList<TermRelationship> getPairsForLHS(Integer rhsId, String rel) {
		ArrayList<TermRelationship> rels = new ArrayList<TermRelationship>();
		for(TermRelationship row :  rows) {
			if(row.getTermId2().equals(rhsId) && row.getRelationship().equals(rel)) {
				rels.add(row);
			}
		}
		return rels;
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
	    LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	    String formattedDate = myDateObj.format(myFormatObj);
		
		for( TermRelationship r : rows ) {
			pw.println(r.getId() + "|" + r.getTermId1() + "|" + r.getRelationship() + 
					"|" + r.getTermId2() + "||"+formattedDate +"|||1");
		}
		
	}
	
}
