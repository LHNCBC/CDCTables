package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;

import gov.nih.nlm.mor.db.rxnorm.ConceptType;

public class ConceptTypeTable {

	private ArrayList<ConceptType> rows = new ArrayList<ConceptType>();
	
	
	public ConceptTypeTable() {
		
	}
	
	public void print(PrintWriter pw ) {
		/*	[DrugConceptTypeID] [bigint] NOT NULL,
	[Description] [varchar](50) NULL,
	[CreationDateTime] [smalldatetime] NULL,
	[CreationUserID] [char](4) NULL,
	[UpdatedDateTime] [smalldatetime] NULL,
	[UpdatedUserID] [char](4) NULL,
	[IsActive] [bit] NULL,
		 * 
		 */
		
		for( ConceptType t : rows ) {
			pw.println(t.getId() + "|" + t.getDescription() + "|" + t.getCreationDateTime() + "|" + t.getCreationUserId() +
					"|"+ t.getUpdatedDateTime() + "|" + t.getUpdatedUserId() + "|" + t.getIsActive() );
			pw.flush();
		}
		
	}

	public void add(ConceptType t) {
		rows.add(t);
	}

}
