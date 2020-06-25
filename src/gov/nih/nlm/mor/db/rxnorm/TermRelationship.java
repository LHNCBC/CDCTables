package gov.nih.nlm.mor.db.rxnorm;

public class TermRelationship {
	Integer index = (int) -1;
	
	Term term1 = new Term();
	Term term2 = new Term();
	String rel = "";
	
	public TermRelationship() {
		
	}
	
	public TermRelationship(Integer id1, String rel, Integer id2) {
		
	}
	
	public TermRelationship(Term t1, String rel, Term t2) {
		term1 = t1;
		this.rel = rel;
		term2 = t2;
	}
	
	public void setId(Integer i) {
		this.index = i;
	}
	
	public Integer getId() {
		return index;
	}

	public Integer getTermId1() {
		return term1.getId();
	}

	public void setTermId1(Integer termId1) {
		term1.setId(termId1);
	}

	public String getRelationship() {
		return rel;
	}

	public void setRelationship(String relationship) {
		this.rel = relationship;
	}

	public Integer getTermId2() {
		return term2.getId();
	}

	public void setTermId2(Integer termId2) {
		term2.setId(termId2);
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rel == null) ? 0 : rel.hashCode());
		result = prime * result + ((term1 == null) ? 0 : term1.hashCode());
		result = prime * result + ((term2 == null) ? 0 : term2.hashCode());
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
		TermRelationship other = (TermRelationship) obj;
		if (rel == null) {
			if (other.rel != null)
				return false;
		} else if (!rel.equals(other.rel))
			return false;
		if (term1 == null) {
			if (other.term1 != null)
				return false;
		} else if (!term1.equals(other.term1))
			return false;
		if (term2 == null) {
			if (other.term2 != null)
				return false;
		} else if (!term2.equals(other.term2))
			return false;
		return true;
	}

}
