package com.example.Payroll.Constants;

public class AuditActions {
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";

    // CRUD for positions
    public static final String CREATE_POSITION = "CREATE_POSITION";
    public static final String UPDATE_POSITION = "UPDATE_POSITION";
    public static final String DELETE_POSITION = "DELETE_POSITION";

    // CRUD for departments
    public static final String CREATE_DEPARTMENT = "CREATE_DEPARTMENT";
    public static final String UPDATE_DEPARTMENT = "UPDATE_DEPARTMENT";
    public static final String DELETE_DEPARTMENT = "DELETE_DEPARTMENT";

    // CRUD for employees
    public static final String CREATE_EMPLOYEE = "CREATE_EMPLOYEE";
    public static final String UPDATE_EMPLOYEE = "UPDATE_EMPLOYEE";
    public static final String DELETE_EMPLOYEE = "DELETE_EMPLOYEE";

    // Payroll actions
    public static final String GENERATE_PAYROLL = "GENERATE_PAYROLL";
    public static final String APPROVE_PAYROLL = "APPROVE_PAYROLL";

    private AuditActions() {
        // Prevent instantiation
    }
}
