package org.kz.minibank.repository;


import org.kz.minibank.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    /*@Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :id OR t.targetAccount.id = :id ORDER BY t.timestamp DESC")
    List<Transaction> findAllByAccountId(Long id);*/

    List<Transaction> findByTargetAccountIdOrderByTimestampDesc(Long accountId);
    List<Transaction> findBySourceAccountIdOrderByTimestampDesc(Long accountId);

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.targetAccount.id = :accountId")
    Page<Transaction> findAllByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}
