package com.example.Payroll.Service.Impl;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.DepartmentsForm;
import com.example.Payroll.Repository.DepartmentRepository;
import com.example.Payroll.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findByIsActiveTrue();
    }

    @Override
    public Department createDepartment(DepartmentsForm form) {
        Department department = new Department();
        department.setName(form.getName());
        department.setDescription(form.getDescription());
        department.setActive(true);
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(DepartmentsForm form) {
        Department department = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        department.setName(form.getName());
        department.setDescription(form.getDescription());
        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.findById(id).ifPresent(dept -> {
            dept.setActive(false);
            departmentRepository.save(dept);
        });
    }
}
