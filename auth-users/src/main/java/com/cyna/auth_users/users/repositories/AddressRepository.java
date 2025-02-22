package com.cyna.auth_users.users.repositories;

import com.cyna.auth_users.users.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
