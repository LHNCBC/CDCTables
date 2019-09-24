package gov.nih.nlm.mor.db.table;

import java.io.PrintWriter;
import java.util.ArrayList;

import gov.nih.nlm.mor.db.rxnorm.Source;

public class AuthoritativeSourceTable {

	private ArrayList<Source> rows = new ArrayList<Source>();
	
	public AuthoritativeSourceTable() {
		
	}
	
	public void print(PrintWriter pw) {
//		[DrugAuthoritativeSourceID] [smallint] IDENTITY(1,1) NOT NULL,
//		[Name] [varchar](50) NOT NULL,
//		[Description] [varchar](100) NULL,
//		[CreationUserID] [char](4) NULL,
//		[CreationDate] [smalldatetime] NULL,
//		[UpdatedUserID] [char](5) NULL,
//		[UpdatedDate] [smalldatetime] NULL,
//		[IsActive] [bit] NULL,
		
		for( Source s : rows ) {
			pw.println(s.getId() + "|" + s.getName() + "|" + s.getDescription() + "|" + s.getCreationUserId() + "|" + 
			 s.getCreationDate() + "|" + s.getUpdatedUserId() + "|" + s.getUpdatedDate() + "|" + s.getIsActive() );
		}
		
	}
	
	public void add(Source s) {
		rows.add(s);
	}
	

}
