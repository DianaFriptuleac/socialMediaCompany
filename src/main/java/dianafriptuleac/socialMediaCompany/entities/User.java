package dianafriptuleac.socialMediaCompany.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Set;
import java.util.UUID;

@Entity  // tabella nel DB
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor // Lombok -> costruttore vuoto (necessario per JPA)

@JsonIgnoreProperties({"password", "role", "accountNonLocked", "credentialsNonExpired",
        "accountNonExpired", "authorities", "enabled"})
// Non include questi campi quando l'oggetto viene convertito in JSON
// (es. per sicurezza non mostrare password e campi tecnici di Spring Security)

public class User implements UserDetails {
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    Set<UserDepartmentRole> departmentRoles = new java.util.HashSet<>();
    //UserDetails di Spring Security -> per gestire autenticazione e autorizzazioni
    @Getter
    @Id
    @GeneratedValue  // genera automaticamente un UUID dal sistema
    @Setter(AccessLevel.NONE)    // Non è possibile modificarlo (setter disabilitato da Lombok)
    private UUID id;
    private String name;
    private String surname;
    @Column(unique = true, nullable = false)   // // Indirizzo email univoco e obbligatorio
    private String email;
    private String password;
    private String avatar;
    @Enumerated(EnumType.STRING)   // per enum -> Ruolo utente salvato come stringa nel DB
    private Role role;

    public User(String name, String surname, String email, String password, String avatar, Role role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.role = role != null ? role : Role.USER; // se role è null, assegna USER
    }


    //toString sovrascritto (override) per mostrare i dati dell’utente in formato leggibile
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
    // Metodo richiesto da UserDetails
    // Restituisce una lista di Authority (permessi) basate sul ruolo dell’utente (se role = ADMIN → ["ADMIN"])

    @Override
    public String getUsername() {
        return this.email;
    }
    // Metodo getUsername() richiesto da UserDetails
    // Definisce che per Spring Security, l’email è lo “username” dell’utente.

    // I metodi seguenti (non mostrati ma ereditati da UserDetails) come isAccountNonExpired(), isAccountNonLocked(), ecc.
    // vengono gestiti da Spring Security (di default true) (perché non gli ho sovrascritti).

}
