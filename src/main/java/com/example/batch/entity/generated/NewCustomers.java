package com.example.batch.entity.generated;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "new_customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCustomers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true, length = 20)
    private String customerId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "full_address", length = 255)
    private String fullAddress;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "migrated_at")
    private LocalDateTime migratedAt;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(length = 10)
    private String gender;

}
