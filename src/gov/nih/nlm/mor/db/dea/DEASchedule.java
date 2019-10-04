package gov.nih.nlm.mor.db.dea;

public class DEASchedule {

	private String scheduleName = "";
	private String scheduleCode = "";
	
	public DEASchedule(String name) {
		this.scheduleName = name;
//		this.scheduleCode = code; - codes don't exist for schedules, don't make use of the phony ones
	}
	
	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getScheduleCode() {
		return scheduleCode;
	}

	public void setScheduleCode(String scheduleCode) {
		this.scheduleCode = scheduleCode;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scheduleCode == null) ? 0 : scheduleCode.hashCode());
		result = prime * result + ((scheduleName == null) ? 0 : scheduleName.hashCode());
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
		DEASchedule other = (DEASchedule) obj;
		if (scheduleCode == null) {
			if (other.scheduleCode != null)
				return false;
		} else if (!scheduleCode.equals(other.scheduleCode))
			return false;
		if (scheduleName == null) {
			if (other.scheduleName != null)
				return false;
		} else if (!scheduleName.equals(other.scheduleName))
			return false;
		return true;
	}	

}
