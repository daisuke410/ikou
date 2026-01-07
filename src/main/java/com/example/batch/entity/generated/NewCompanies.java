package com.example.batch.entity.generated;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "new_companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompanies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, unique = true, length = 20)
    private String companyId;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(length = 100)
    private String representative;

    @Column(name = "industry_category", length = 50)
    private String industryCategory;

    private Integer employees;

    @Column(name = "capital_amount")
    private Long capitalAmount;

    @Column(name = "foundation_date")
    private LocalDate foundationDate;

    @Column(name = "office_address", length = 300)
    private String officeAddress;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "migrated_at")
    private LocalDateTime migratedAt;

}
