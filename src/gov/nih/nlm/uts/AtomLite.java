package gov.nih.nlm.uts;
import com.fasterxml.jackson.annotation.*;

//ignorable properties are of customizable - this is just an example
@JsonIgnoreProperties({"classType","attributes","definitions","relations","contentViewMemberships"})

public class AtomLite {
	
	private String ui;
	private String name;
	private String termType;
	private String language;
	private boolean suppressible;
	private boolean obsolete;
	private String rootSource;
	private String concept;
	private String code;
	private String sourceConcept;
	private String sourceDescriptor;
	private String parents;
	private String children;
	private String ancestors;
	private String descendants;

	
	public String getUi() {
		
		return this.ui;
	}
	
	public String getName() {
		
		return this.name;
	}
	
	public String getTermType() {
		
		return this.termType;
	}
	
	public String getLanguage() {
		
		return this.language;
	}
	
	public String getConcept() {
		
		return this.concept;
	}
	
	public String getSourceConcept() {
		
		return this.sourceConcept;
	}
	
	public String getSourceDescriptor() {
		
		return this.sourceDescriptor;
	}
	
	
	public String getCode() {
		
		return this.code;
	}
	
	public boolean getObsolete() {
		
		return this.obsolete;
	}
	
    public boolean getSupressible() {
		
		return this.suppressible;
	}
    
    public String getRootSource() {
    	
    	return this.rootSource;
    }
    
    public String getParents() {
    	
    	return this.parents;
    }
    
    public String getChildren() {
    	
    	return this.children;
    }
    
    public String getAncestors() {
    	
    	return this.ancestors;
    }
    
    public String getDescendants() {
    	
    	return this.descendants;
    }
    
    @SuppressWarnings("unused")
    private void setUi(String ui) {
		
		this.ui = ui;
	}
	
    @SuppressWarnings("unused")
	private void setTermType(String termType){
		
		this.termType = termType;
	}
	
    @SuppressWarnings("unused")
	private void setName(String name) {
		
		this.name = name;
	}
	
    @SuppressWarnings("unused")
	private void setLanguage (String language) {
		
		this.language = language;
	}

    @SuppressWarnings("unused")
	private void setObsolete (boolean obsolete) {
		
		this.obsolete = obsolete;
	}
	
    @SuppressWarnings("unused")
	private void setRootSource(String rootSource) {
		
		this.rootSource = rootSource;
	}
	
    @SuppressWarnings("unused")
	private void setSuppressible (boolean suppressible) {
		
		this.suppressible = suppressible;
	}
	
    @SuppressWarnings("unused")
	private void setParents (String parents) {
		
		this.parents = parents;
	}
	
    @SuppressWarnings("unused")
	private void setChildren (String children) {
		
		this.children = children;
	}
	
    public void setAncestors (String ancestors) {
		
		this.ancestors = ancestors;
	}
	
	public void setDescendants (String descendants) {
		
		this.descendants = descendants;
	}

}
