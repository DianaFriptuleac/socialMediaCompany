package dianafriptuleac.socialMediaCompany.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dianafriptuleac.socialMediaCompany.enums.DepartmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentType departmentType;

    @Column(length = 260)
    private String description;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<UserDepartmentRole> memberships = new HashSet<>();

    public Department(DepartmentType departmentType, String description) {
        this.departmentType = departmentType;
        this.description = description;
    }
}
