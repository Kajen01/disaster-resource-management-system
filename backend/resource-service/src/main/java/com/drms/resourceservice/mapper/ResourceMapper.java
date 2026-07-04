package com.drms.resourceservice.mapper;

import com.drms.resourceservice.dto.BatchResponse;
import com.drms.resourceservice.dto.ExcessStockView;
import com.drms.resourceservice.dto.StockReservationResponse;
import com.drms.resourceservice.entity.ResourceBatch;
import com.drms.resourceservice.entity.StockReservation;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public BatchResponse toBatchResponse(ResourceBatch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getShelterId(),
                batch.getDonorEmail(),
                batch.getResourceType(),
                batch.getResourceName(),
                batch.getUnit(),
                batch.getQuantityReceived(),
                batch.getQuantityAvailable(),
                batch.getExpiryDate(),
                batch.getReceivedAt(),
                batch.getSourceDonationRef()
        );
    }

    public ExcessStockView toExcessView(ResourceBatch batch) {
        return new ExcessStockView(
                batch.getId(),
                batch.getShelterId(),
                batch.getResourceType(),
                batch.getResourceName(),
                batch.getUnit(),
                batch.getQuantityAvailable(),
                batch.getExpiryDate(),
                batch.getSourceDonationRef()
        );
    }

    public StockReservationResponse toReservationResponse(StockReservation reservation) {
        return new StockReservationResponse(
                reservation.getId(),
                reservation.getBatchId(),
                reservation.getSourceShelterId(),
                reservation.getTargetShelterId(),
                reservation.getResourceType(),
                reservation.getResourceName(),
                reservation.getUnit(),
                reservation.getReservedQuantity(),
                reservation.getReferenceNumber(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
