package gov.nih.nlm.uts;


public class SearchResult {
	
	private String ui;
	private String name;
	private String uri;
	private String rootSource;
	
	//getters
    public String getUi() {
		
		return this.ui;
	}
	
	public String getName() {
		
		return this.name;
	}
	
	public String getUri() {
		
		return this.uri;
	}
	
	public String getRootSource() {
		
		return this.rootSource;
	}

	//setters
    @SuppressWarnings("unused")
	private void setUi(String ui) {
		
		this.ui = ui;
	}
	
    @SuppressWarnings("unused")
	private void setName(String name) {
		
		this.name = name;
	}
	
    @SuppressWarnings("unused")
	private void setUri(String uri) {
		
		this.uri = uri;
	}
	
    @SuppressWarnings("unused")
	private void setRootSource(String rootSource) {
		
		this.rootSource = rootSource;
	}
}
