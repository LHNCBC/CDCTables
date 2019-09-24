package gov.nih.nlm.mor.db.rxnorm;

public class Term {
	Integer id = null;
	String name = "";
	String tty = "";
	String sourceId = "";
	String source = "";
	String drugConceptId = "";	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTty() {
		return tty;
	}

	public void setTty(String tty) {
		this.tty = tty;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public String getDrugConceptId() {
		return drugConceptId;
	}

	public void setDrugConceptId(Integer sourceId) {
		this.drugConceptId = String.valueOf(sourceId);
	}	

	public Term() {
		
	}

}
