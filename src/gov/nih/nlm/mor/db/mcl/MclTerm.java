package gov.nih.nlm.mor.db.mcl;

public class MclTerm {
	String variant = "";
	String substance = "";
	boolean isClass = false;
	
	public MclTerm(String variant, String substance, String type) {
		this.variant = variant;
		this.substance = substance;
		if(type.equalsIgnoreCase("class")) {
			this.isClass = true;
		}
	}

	public String getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getSubstance() {
		return substance;
	}

	public void setSubstance(String substance) {
		this.substance = substance;
	}

	public boolean isClass() {
		return isClass;
	}

	public void setClass(boolean isClass) {
		this.isClass = isClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isClass ? 1231 : 1237);
		result = prime * result + ((substance == null) ? 0 : substance.hashCode());
		result = prime * result + ((variant == null) ? 0 : variant.hashCode());
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
		MclTerm other = (MclTerm) obj;
		if (isClass != other.isClass)
			return false;
		if (substance == null) {
			if (other.substance != null)
				return false;
		} else if (!substance.equals(other.substance))
			return false;
		if (variant == null) {
			if (other.variant != null)
				return false;
		} else if (!variant.equals(other.variant))
			return false;
		return true;
	}
	
	
}
