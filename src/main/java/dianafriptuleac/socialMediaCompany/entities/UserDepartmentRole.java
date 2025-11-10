package dianafriptuleac.socialMediaCompany.entities;

import dianafriptuleac.socialMediaCompany.enums.DepartmentRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_department_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDepartmentRole {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_udr_user"))
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_udr_department"))
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private DepartmentRole role;
}
