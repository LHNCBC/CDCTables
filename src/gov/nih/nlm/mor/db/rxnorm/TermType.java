package gov.nih.nlm.mor.db.rxnorm;

/*
 * 	[DrugTTYID] [smallint] IDENTITY(1,1) NOT NULL,
	[Abbreviation] [char](4) NOT NULL,
	[Description] [char](50) NULL,
	[CreationUserID] [char](4) NULL,
	[CreationDate] [smalldatetime] NULL,
	[UpdatedUserID] [char](5) NULL,
	[UpdatedDate] [smalldatetime] NULL,
	[IsActive] [bit] NULL,
 */
public class TermType {
	private Integer id = null;
	private String abbreviation = "";
	private String description = "";
	private String creationUserId = "";
	private String createionDate = "";
	private String updatedUserId = "";
	private String updatedDate = "";
	private String isActive = "";
	
	public TermType() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreationUserId() {
		return creationUserId;
	}

	public void setCreationUserId(String creationUserId) {
		this.creationUserId = creationUserId;
	}

	public String getCreateionDate() {
		return createionDate;
	}

	public void setCreateionDate(String createionDate) {
		this.createionDate = createionDate;
	}

	public String getUpdatedUserId() {
		return updatedUserId;
	}

	public void setUpdatedUserId(String updatedUserId) {
		this.updatedUserId = updatedUserId;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	
	

}
