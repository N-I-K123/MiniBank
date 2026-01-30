package org.kz.minibank;

import jakarta.persistence.EntityListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@SpringBootApplication
@EntityListeners(AuditingEntityListener.class)
public class MiniBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniBankApplication.class, args);
    }

}
