package com.cyna.auth_users.users.repositories;

import com.cyna.auth_users.users.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//
//public interface AddressRepository extends JpaRepository<Address, Long> {
//}

public interface AddressRepository extends JpaRepository<Address, Long> {
    // renvoie toutes les adresses d’un utilisateur
    List<Address> findByUserId(Long userId);
}
