package com.example.Payroll.Entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@SequenceGenerator(
        name = "department_seq",
        sequenceName = "department_seq",
        initialValue = 1000,
        allocationSize = 1
)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department_seq")
    @Column(name = "department_id")
    private Long departmentId;

    private String name;

    private String description;


    @Column(name = "is_active")
    private boolean isActive = true;

}
