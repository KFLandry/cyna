package com.cyna.auth_users.users.repositories;

import com.cyna.auth_users.users.models.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<BankCard, Long> {
}
