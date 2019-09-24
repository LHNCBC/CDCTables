package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;

import gov.nih.nlm.mor.db.rxnorm.TermType;

public class TermTypeTable {

	private ArrayList<TermType> rows = new ArrayList<TermType>();
	
	
	
	public TermTypeTable() {
		
	}
	
	public void add(TermType t) {
		rows.add(t);
	}
	
	public void print(PrintWriter pw) {
		/*	[DrugTTYID] [smallint] IDENTITY(1,1) NOT NULL,
	[Abbreviation] [char](4) NOT NULL,
	[Description] [char](50) NULL,
	[CreationUserID] [char](4) NULL,
	[CreationDate] [smalldatetime] NULL,
	[UpdatedUserID] [char](5) NULL,
	[UpdatedDate] [smalldatetime] NULL,
	[IsActive] [bit] NULL,
		 * 
		 */
		for( TermType t : rows ) {
			pw.println(t.getId() + "|" + t.getAbbreviation() + "|" + t.getDescription() + "|" + 
					t.getCreationUserId() + "|" + t.getCreateionDate() + "|" + t.getUpdatedUserId() + "|" + t.getUpdatedDate() + "|" + t.getIsActive() );
			pw.flush();
		}
		
	}

	
	
}
