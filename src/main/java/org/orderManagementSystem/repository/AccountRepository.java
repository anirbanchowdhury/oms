package org.orderManagementSystem.repository;

import org.orderManagementSystem.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository  extends JpaRepository<Account, Long> {
}

