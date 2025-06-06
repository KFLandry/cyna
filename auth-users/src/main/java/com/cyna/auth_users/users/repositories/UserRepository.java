package com.cyna.auth_users.users.repositories;

import com.cyna.auth_users.users.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    User getByIdOrCustomerId(Long id, String customerId);

    List<User> getByFirstnameAndLastname(String firstname, String lastname);

    List<User> findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(String firstname, String lastname);

}