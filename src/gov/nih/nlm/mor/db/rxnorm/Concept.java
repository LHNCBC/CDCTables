package gov.nih.nlm.mor.db.rxnorm;

public class Concept {
	Integer conceptId = null;
	Integer preferredTermId = null;
	String source = "";
	String sourceId = "";
	String classType = "";
	
	public Concept() {
		
	}

	public Integer getConceptId() {
		return conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	public Integer getPreferredTermId() {
		return preferredTermId;
	}

	public void setPreferredTermId(Integer preferredTermId) {
		this.preferredTermId = preferredTermId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}
	

}
