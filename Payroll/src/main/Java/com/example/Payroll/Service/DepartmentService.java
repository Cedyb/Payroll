package com.example.Payroll.Service;

import com.example.Payroll.Entity.Department;
import com.example.Payroll.Forms.DepartmentsForm;
import org.springframework.data.domain.Page;
import java.util.List;

public interface DepartmentService {

    List<Department> getAllDepartments();
    Page<Department> getDepartmentsPaginated(int page, int size);

    Department createDepartment(DepartmentsForm form);

    Department updateDepartment(DepartmentsForm form);

    void deleteDepartment(Long id);
}
