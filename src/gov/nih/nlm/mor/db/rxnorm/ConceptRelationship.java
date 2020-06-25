package gov.nih.nlm.mor.db.rxnorm;

public class ConceptRelationship {
	Integer id = null;
	Integer conceptId1 = null;
	String relationship = "";
	Integer conceptId2 = null;
	
	public ConceptRelationship() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getConceptId1() {
		return conceptId1;
	}

	public void setConceptId1(Integer conceptId1) {
		this.conceptId1 = conceptId1;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public Integer getConceptId2() {
		return conceptId2;
	}

	public void setConceptId2(Integer conceptId2) {
		this.conceptId2 = conceptId2;
	}
}
