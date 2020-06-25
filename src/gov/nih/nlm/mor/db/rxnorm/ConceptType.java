package gov.nih.nlm.mor.db.rxnorm;
import java.time.LocalDateTime;            // Import the LocalDateTime class
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class

/*	[DrugConceptTypeID] [bigint] NOT NULL,
	[Description] [varchar](50) NULL,
	[CreationDateTime] [smalldatetime] NULL,
	[CreationUserID] [char](4) NULL,
	[UpdatedDateTime] [smalldatetime] NULL,
	[UpdatedUserID] [char](4) NULL,
	[IsActive] [bit] NULL,
 * 
 */
public class ConceptType {
	private Integer id = null;
	private String description = "";
	private String creationDateTime = "";
	private String creationUserId = "";
	private String updatedDateTime = "";
	private String updatedUserId = "";
	private String isActive = "1";
	
	public ConceptType() {
	    LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	    String formattedDate = myDateObj.format(myFormatObj);
	    setCreationDateTime(formattedDate);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreationDateTime() {
		return creationDateTime;
	}

	public void setCreationDateTime(String creationDateTime) {
		this.creationDateTime = creationDateTime;
	}

	public String getCreationUserId() {
		return creationUserId;
	}

	public void setCreationUserId(String creationUserId) {
		this.creationUserId = creationUserId;
	}

	public String getUpdatedDateTime() {
		return updatedDateTime;
	}

	public void setUpdatedDateTime(String updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}

	public String getUpdatedUserId() {
		return updatedUserId;
	}

	public void setUpdatedUserId(String updatedUserId) {
		this.updatedUserId = updatedUserId;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	
	

}
