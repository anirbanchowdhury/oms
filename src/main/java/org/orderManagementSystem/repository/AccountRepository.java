package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository  extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountName(String accountName);
}

