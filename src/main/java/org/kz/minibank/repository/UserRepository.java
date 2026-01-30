package org.kz.minibank.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.kz.minibank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
