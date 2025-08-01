package com.example.Payroll.Service;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.DepartmentsForm;

import java.util.List;

public interface DepartmentService {

    List<Department> getAllDepartments();

    Department createDepartment(DepartmentsForm form);

    Department updateDepartment(DepartmentsForm form);

    void deleteDepartment(Long id);
}
