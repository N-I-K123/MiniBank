package org.kz.minibank.repository;

import org.kz.minibank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUserEmail(String email);
    Account findByAccountNumber(String number);
}
