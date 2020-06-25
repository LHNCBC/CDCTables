package gov.nih.nlm.mor.icd;

import java.util.ArrayList;

import gov.nih.nlm.uts.RetrieveCodeTestCase;
import gov.nih.nlm.uts.WalkHierarchyTestCase;

public class IcdConcept {

	//using CM, not plain 10
	private String sourceAbbreviation = "ICD10";
	private String code = "";
	private String name = "";
	private String displayCode = "";  //death certificates use 5 character length, so we need this for the tables
									  //(remember, the indexes are meaningless so our isa relations will still be valid) 
	private ArrayList<String> associatedRxCuis = new ArrayList<String>();
	private ArrayList<String> children = new ArrayList<String>();

	
	public IcdConcept(String code, ArrayList<String> rxcuis) {
		this.code = code;
		this.associatedRxCuis = rxcuis;
		setDisplayCode();
		setName();
	}
	
	public IcdConcept(String code, String name) {
		this.code = code;
		this.name = name;
		setDisplayCode();
	}
	
	//send back the list of ids to connect
	public ArrayList<String> getChildren() {
		//this isn't going to be a config file
		//we'll use the UTS REST API
		
		//Modified sample code from
		//https://github.com/HHS/uts-rest-api/tree/master/samples/java/src/test/java/uts/rest/samples
		try {
			WalkHierarchyTestCase walk = new WalkHierarchyTestCase(code, sourceAbbreviation);
			children = walk.getChildren();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("!!! Unable to walk the hierarchy from T-code " + code + " (" + name + ")"); 
			e.printStackTrace();
		}
		
		return children;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getAssociatedRxCuis() {
		return associatedRxCuis;
	}

	public void setAssociatedRxCuis(ArrayList<String> associatedRxCuis) {
		this.associatedRxCuis = associatedRxCuis;
	}
	
	public String getDisplayCode() {
		return this.displayCode;
	}	
	
	public void setDisplayCode() {
		if(this.code.contains(".") && !this.code.contains("-")) {
			Integer tcodeDotIndex = this.code.indexOf(".");
			this.displayCode = this.code.substring(0, tcodeDotIndex + 2);
		}
		else {
			this.displayCode = this.code;
		}		
	}
	
	public void setName() {
		try {
			RetrieveCodeTestCase retrieve = new RetrieveCodeTestCase(this.code, this.sourceAbbreviation);
			this.name = retrieve.getName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("!!! Unable to find the name for T-code: " + code);
			e.printStackTrace();
		}
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((associatedRxCuis == null) ? 0 : associatedRxCuis.hashCode());
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((displayCode == null) ? 0 : displayCode.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sourceAbbreviation == null) ? 0 : sourceAbbreviation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IcdConcept other = (IcdConcept) obj;
		if (associatedRxCuis == null) {
			if (other.associatedRxCuis != null)
				return false;
		} else if (!associatedRxCuis.equals(other.associatedRxCuis))
			return false;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (displayCode == null) {
			if (other.displayCode != null)
				return false;
		} else if (!displayCode.equals(other.displayCode))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sourceAbbreviation == null) {
			if (other.sourceAbbreviation != null)
				return false;
		} else if (!sourceAbbreviation.equals(other.sourceAbbreviation))
			return false;
		return true;
	}	
	
}
