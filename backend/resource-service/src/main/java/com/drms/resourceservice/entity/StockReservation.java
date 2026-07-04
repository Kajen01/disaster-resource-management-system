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
@Table(name = "stock_reservations")
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long batchId;
    private Long sourceShelterId;
    private Long targetShelterId;

    @Enumerated(EnumType.STRING)
    private ResourceCategory resourceType;

    private String resourceName;
    private String unit;
    private int reservedQuantity;
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Instant createdAt;
    private String releaseReason;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
