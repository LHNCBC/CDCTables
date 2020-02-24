package gov.nih.nlm.mor.unii;

import java.util.ArrayList;

public class UniiName {
	private String name = "";
	private ArrayList<String> displayNames = new ArrayList<String>();
	private ArrayList<String> allCodes = new ArrayList<String>();
	private ArrayList<String> uniqueCodes = new ArrayList<String>();
	private String code = "";
	
	public UniiName(String name, String code) {
		this.name = name;
		this.code = code;
		addToAllCodes(code);
	}
	
	public UniiName() {
		
	}
	
	public void addToAllCodes(String c) {
		if( !allCodes.contains(c) ) {
			allCodes.add(c);
		}
		
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getDisplayNames() {
		return displayNames;
	}

	public void setDisplayNames(ArrayList<String> displayNames) {
		this.displayNames = displayNames;
	}
	
	public void addDisplayName(String dn) {
		if( !this.displayNames.contains(dn) ) {
			this.displayNames.add(dn);
		}
	}	

	public ArrayList<String> getAllCodes() {
		return allCodes;
	}

	public void setAllCodes(ArrayList<String> allCodes) {
		this.allCodes = allCodes;
	}

	public ArrayList<String> getUniqueCodes() {
		return uniqueCodes;
	}

	public void setUniqueCodes(ArrayList<String> uniqueCodes) {
		this.uniqueCodes = uniqueCodes;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((displayNames == null) ? 0 : displayNames.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uniqueCodes == null) ? 0 : uniqueCodes.hashCode());
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
		UniiName other = (UniiName) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (displayNames == null) {
			if (other.displayNames != null)
				return false;
		} else if (!displayNames.equals(other.displayNames))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uniqueCodes == null) {
			if (other.uniqueCodes != null)
				return false;
		} else if (!uniqueCodes.equals(other.uniqueCodes))
			return false;
		return true;
	}
}
