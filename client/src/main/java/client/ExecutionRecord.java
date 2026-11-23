package client;

public class ExecutionRecord {
    private Long id;
    private long requestId;
    private long specialistId;
    private String workDate;
    private double hoursSpent;
    private String description;

    public ExecutionRecord() {}

    public ExecutionRecord(Long id, long requestId, long specialistId, String workDate, double hoursSpent, String description) {
        this.id = id;
        this.requestId = requestId;
        this.specialistId = specialistId;
        this.workDate = workDate;
        this.hoursSpent = hoursSpent;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }

    public long getSpecialistId() { return specialistId; }
    public void setSpecialistId(long specialistId) { this.specialistId = specialistId; }

    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }
    public double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(double hoursSpent) { this.hoursSpent = hoursSpent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
