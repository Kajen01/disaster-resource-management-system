package com.drms.resourceservice.repository;

import com.drms.resourceservice.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
}
