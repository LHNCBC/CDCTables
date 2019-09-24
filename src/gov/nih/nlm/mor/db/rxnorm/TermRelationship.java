package gov.nih.nlm.mor.db.rxnorm;

public class TermRelationship {
	Integer id = null;
	Integer termId1 = null;
	String relationship = "";
	Integer termId2 = null;
	
	public TermRelationship() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTermId1() {
		return termId1;
	}

	public void setTermId1(Integer termId1) {
		this.termId1 = termId1;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public Integer getTermId2() {
		return termId2;
	}

	public void setTermId2(Integer termId2) {
		this.termId2 = termId2;
	}	
	
	

}
