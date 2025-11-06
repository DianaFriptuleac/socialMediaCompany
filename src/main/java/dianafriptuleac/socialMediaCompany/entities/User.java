package dianafriptuleac.socialMediaCompany.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dianafriptuleac.socialMediaCompany.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"password", "role", "accountNonLocked", "credentialsNonExpired",
        "accountNonExpired", "authorities", "enabled"})
public class User implements UserDetails {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    private String name;
    private String surname;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String avatar;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User(String name, String surname, String email, String password, String avatar, Role role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", avatar='" + avatar + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
