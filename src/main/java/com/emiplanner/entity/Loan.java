package com.emiplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "loans",
    uniqueConstraints = @UniqueConstraint(
            name = "uk_user_loan",
            columnNames = {"user_id", "loan_name", "provider_name", "start_date"}
    )
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50, name = "loan_name")
    private String loanName;

    @Column(nullable = false, length = 50, name = "provider_name")
    private String providerName;

    @Column(nullable = false, precision = 12, scale = 2, name = "emi_amount")
    private BigDecimal emiAmount;

    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;

    @Column(nullable = false, name = "tenure_months")
    @Min(1)
    private Integer tenureMonths;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        this.endDate = startDate.plusMonths(tenureMonths);
        this.status = LoanStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}