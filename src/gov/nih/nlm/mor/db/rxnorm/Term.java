package gov.nih.nlm.mor.db.rxnorm;

public class Term {
	Integer id = null;
	String name = "";
	String tty = "";
	String sourceId = "";
	String source = "";
	String drugConceptId = "";
	boolean isActive = true;
	
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

	public Term(String name, String tty, String source) {
		this.name = name;
		this.tty = tty;
		this.source = source == null ? "" : source;
	}
	
	public Term() {
		
	}
	
	public void setIsActive(boolean b) {
		this.isActive = b;
	}	
	
	public boolean getIsActive() {
		return this.isActive;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((drugConceptId == null) ? 0 : drugConceptId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isActive ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((sourceId == null) ? 0 : sourceId.hashCode());
		result = prime * result + ((tty == null) ? 0 : tty.hashCode());
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
		Term other = (Term) obj;
		if (drugConceptId == null) {
			if (other.drugConceptId != null)
				return false;
		} else if (!drugConceptId.equals(other.drugConceptId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isActive != other.isActive)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (sourceId == null) {
			if (other.sourceId != null)
				return false;
		} else if (!sourceId.equals(other.sourceId))
			return false;
		if (tty == null) {
			if (other.tty != null)
				return false;
		} else if (!tty.equals(other.tty))
			return false;
		return true;
	}



}
