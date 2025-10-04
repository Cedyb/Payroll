package com.example.Payroll.Forms;

public class PositionsForm {
    private Long positionId;
    private String title;
    private Long departmentId;
    private Double hourlyRate;
    private Boolean active = true;

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Boolean isActive() {return active;}

    public void setActive(Boolean active) {
        this.active = active;
    }
}
