package com.drms.resourceservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resource_batches")
public class ResourceBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long shelterId;
    private String donorEmail;

    @Enumerated(EnumType.STRING)
    private ResourceCategory resourceType;

    private String resourceName;
    private String unit;
    private int quantityReceived;
    private int quantityAvailable;
    private LocalDate expiryDate;
    private Instant receivedAt;
    private String sourceDonationRef;

    @PrePersist
    void onCreate() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
    }
}
