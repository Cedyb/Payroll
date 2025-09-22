package com.example.Payroll.Forms;

import java.time.LocalDate;

public class EmployeeForm {
    private Long id;
    private String username;
    private String password;
    private String role;           // existing legacy field
    private String system_role;    // new field for database
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;
    private LocalDate hireDate;
    private Long positionId;
    private Long departmentId;

    // ID
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // Username
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // Password
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Legacy role
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // New system_role
    public String getSystem_role() { return system_role; }
    public void setSystem_role(String system_role) { this.system_role = system_role; }

    // First Name
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    // Last Name
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    // Email
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Address
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // Phone
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Hire Date
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    // Position
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    // Department
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
