package gov.nih.nlm.uts;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"classType"})

public class SourceAtomClusterLite {

	
	private String ui;
	private String name;
	private boolean obsolete;
	private boolean suppressible;
	private String rootSource;
	private int cVMemberCount;
	private int atomCount;
	private String concepts;
	private String atoms;
	private String parents;
	private String children;
    private String descendants;
    private String ancestors;
	private String relations;
	private String definitions;
	private String attributes;
	private String defaultPreferredAtom;
	private List<HashMap<String,Object>> subsetMemberships;
	private List<HashMap<String,Object>> contentViewMemberships;
	
	
	public String getUi() {
		
		return this.ui;
	}
	
	public String getName() {
		
		return this.name;
	}
	
	public boolean getObsolete() {
		
		return this.obsolete;
	}
	
	public boolean getSuppressible() {
		
		return this.suppressible;
	}
	
	public String getAtoms() {
		
		return this.atoms;
	}
	
	public String getConcepts() {
		
		return this.concepts;
	}

	public String getRootSource() {
		
		return this.rootSource;
	}
	
	public int getAtomCount() {
		
		return this.atomCount;
	}
	
	public int getCVMemberCount() {
		
		return this.cVMemberCount;
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
	
	public String getDefinitions() {
		
		return this.definitions;
	}
	
	public String getRelations() {
		
		return this.relations;
	}
	
	public String getAttributes() {
		
		return this.attributes;
	}
	
	public String getDefaultPreferredAtom() {
		
		return this.defaultPreferredAtom;
	}
	
	@SuppressWarnings("unused")
	private List<HashMap<String,Object>> getSubsetMemberships() {
		
		return this.subsetMemberships;
	}
	
    @SuppressWarnings("unused")
	private List<HashMap<String,Object>> getContentViewMemberships() {
		
		return this.contentViewMemberships;
	}

    @SuppressWarnings("unused")
	private void setAtoms(String atoms) {
		
		this.atoms = atoms;
	}

    @SuppressWarnings("unused")
	private void setAtomCount(int atomCount) {
		
		this.atomCount = atomCount;
	}
	
    @SuppressWarnings("unused")
	private void setcVMemberCount(int cVMemberCount) {
		
		this.cVMemberCount = cVMemberCount;
	}
	
    @SuppressWarnings("unused")
	private void setUi(String ui) {
		
		this.ui = ui;
	}
	
    @SuppressWarnings("unused")
	private void setConcepts(String concepts) {
		
		this.concepts = concepts;
	}
	
    @SuppressWarnings("unused")
	private void setName(String name){
		
		this.name=name;
	}
	
    @SuppressWarnings("unused")
	private void setRootSource(String rootSource) {
		
		this.rootSource = rootSource;
	}
	
    @SuppressWarnings("unused")
	private void setObsolete(boolean obsolete) {
		
		this.obsolete = obsolete;
	}
	
    @SuppressWarnings("unused")
	private void setSuppressible(boolean suppressible) {
		
		this.suppressible = suppressible;
	}
	
    @SuppressWarnings("unused")
	private void setDefinitions (String definitions) {
		
		this.definitions = definitions;
	}
	
    @SuppressWarnings("unused")
	private void setRelations (String relations) {
		
		this.relations = relations;
	}
	
    @SuppressWarnings("unused")
	private void setChildren(String children) {
		
		this.children = children;
	}
	
    @SuppressWarnings("unused")
	private void setParents(String parents) {
		
		this.parents = parents;
	}
	
    @SuppressWarnings("unused")
	private void setAncestors(String ancestors)  {
		
		this.ancestors = ancestors;
	}
	
    @SuppressWarnings("unused")
	private void setAttributes(String attributes) {
		
		this.attributes = attributes;
		
	}
	
    @SuppressWarnings("unused")
	private void setDefaultPreferredAtom(String defaultPreferredAtom) {
		
		this.defaultPreferredAtom = defaultPreferredAtom;
	}
	
    @SuppressWarnings("unused")
	private void setContentViewMemberships(List<HashMap<String,Object>> contentViewMemberships) {
		
		this.contentViewMemberships = contentViewMemberships;
		
	}
	
    @SuppressWarnings("unused")
    private void setSubsetMemberships(List<HashMap<String,Object>> subsetMemberships) {
		
		this.subsetMemberships = subsetMemberships;
		
	}
}
