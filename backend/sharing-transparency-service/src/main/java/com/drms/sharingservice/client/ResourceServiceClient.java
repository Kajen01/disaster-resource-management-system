package com.drms.sharingservice.client;

import com.drms.sharingservice.dto.ExternalExcessStockView;
import com.drms.sharingservice.dto.ExternalReservationRequest;
import com.drms.sharingservice.dto.ExternalShortageAnalysisRequest;
import com.drms.sharingservice.dto.ExternalShortageAnalysisResponse;
import com.drms.sharingservice.dto.ExternalStockReservationResponse;
import com.drms.sharingservice.dto.ExternalTransferConfirmRequest;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "resource-service")
public interface ResourceServiceClient {

    @PostMapping("/api/resources/shortages/analyze")
    ExternalShortageAnalysisResponse analyzeShortage(@RequestBody ExternalShortageAnalysisRequest request);

    @GetMapping("/api/resources/excess")
    List<ExternalExcessStockView> getExcess(
            @RequestParam("resourceType") String resourceType,
            @RequestParam("resourceName") String resourceName,
            @RequestParam("minimumAvailable") int minimumAvailable
    );

    @PostMapping("/api/resources/reservations")
    ExternalStockReservationResponse reserve(@RequestBody ExternalReservationRequest request);

    @PostMapping("/api/resources/reservations/{id}/release")
    ExternalStockReservationResponse release(@PathVariable("id") Long reservationId, @RequestBody ReleaseReason reason);

    @PostMapping("/api/resources/transfers/confirm")
    ExternalStockReservationResponse confirmTransfer(@RequestBody ExternalTransferConfirmRequest request);

    record ReleaseReason(String reason) {
    }
}
