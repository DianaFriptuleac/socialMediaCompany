package dianafriptuleac.socialMediaCompany.repositories;

import dianafriptuleac.socialMediaCompany.entities.User;
import dianafriptuleac.socialMediaCompany.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // In Spring Data JPA, le interfacce repository servono per accedere ai dati nel database.
    // "extends JpaRepository<User, UUID>" significa:
    // - gestisce entità di tipo User
    // - la chiave primaria (ID) di User è di tipo UUID

    //   JpaRepository fornisce metodi pronti (findAll, findById, save, delete, ecc.) senza bisogno di scriverli

    //Metodo custom (findByEmail)
    Optional<User> findByEmail(String email);
    // Spring genera automaticamente la query SQL corrispondente:
    // SELECT * FROM users WHERE email = ?
    // Ritorna un Optional<User>:
    // - Optional -> il caso in cui non venga trovato nessun utente
    // - Se l’utente esiste, contiene l’oggetto User
    // - Se non esiste, contiene "Optional.empty()", evitando i NullPointerException


    User findByRole(Role role);
    // findByRole = (SQL) SELECT * FROM users WHERE role = ?

    //------------------------------------------------------------------------------
    // query JPQL - per cercare utente con serach
    // -- LOWER rende tutto minuscolo per ignorare maiuscole/minuscole.
    //        -- CONCAT aggiunge % prima e dopo la parola cercata.
    //        -- Esempio: "dia" diventa "%dia%" e può trovare "Diana".

    // -- Ordina i risultati prima per nome in ordine alfabetico.
    //        -- Se due utenti hanno lo stesso nome,
    //        -- li ordina anche per cognome.
    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(CONCAT(u.name, ' ', u.surname))
                  LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY u.name ASC, u.surname ASC
            """)
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);
}
