package com.example.backend.repository;

import com.example.backend.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUserName(String userName);

    Optional<Account> findByUserName(String userName);
}
