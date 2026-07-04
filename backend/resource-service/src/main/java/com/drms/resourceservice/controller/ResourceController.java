package com.drms.resourceservice.controller;

import com.drms.resourceservice.dto.BatchIntakeRequest;
import com.drms.resourceservice.dto.BatchResponse;
import com.drms.resourceservice.dto.BulkIntakeRequest;
import com.drms.resourceservice.dto.DonationHistoryResponse;
import com.drms.resourceservice.dto.ExcessStockView;
import com.drms.resourceservice.dto.ReleaseReservationRequest;
import com.drms.resourceservice.dto.ReservationRequest;
import com.drms.resourceservice.dto.ShortageAnalysisRequest;
import com.drms.resourceservice.dto.ShortageAnalysisResponse;
import com.drms.resourceservice.dto.StockReservationResponse;
import com.drms.resourceservice.dto.TransferConfirmRequest;
import com.drms.resourceservice.service.ResourceInventoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceInventoryService inventoryService;

    public ResourceController(ResourceInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public BatchResponse intake(
            @Valid @RequestBody BatchIntakeRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String donorEmail
    ) {
        return inventoryService.intake(request, donorEmail);
    }

    @PostMapping("/batches/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<BatchResponse> intakeBulk(
            @Valid @RequestBody BulkIntakeRequest request
    ) {
        return inventoryService.intakeBulk(request);
    }

    @GetMapping("/shelters/{shelterId}")
    public List<BatchResponse> getByShelter(@PathVariable Long shelterId) {
        return inventoryService.getByShelter(shelterId);
    }

    @GetMapping("/admin/batches")
    public List<BatchResponse> getAdminBatches() {
        return inventoryService.getAdminBatches();
    }

    @GetMapping("/donations/me")
    public DonationHistoryResponse getDonationHistory(@RequestHeader("X-User-Email") String donorEmail) {
        return inventoryService.getDonationHistory(donorEmail);
    }

    @GetMapping("/excess")
    public List<ExcessStockView> getExcess(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceName,
            @RequestParam(defaultValue = "1") int minimumAvailable
    ) {
        return inventoryService.getExcess(resourceType, resourceName, minimumAvailable);
    }

    @PostMapping("/shortages/analyze")
    public ShortageAnalysisResponse analyzeShortage(@Valid @RequestBody ShortageAnalysisRequest request) {
        return inventoryService.analyzeShortage(request);
    }

    @PostMapping("/reservations")
    public StockReservationResponse reserve(@Valid @RequestBody ReservationRequest request) {
        return inventoryService.reserve(request);
    }

    @PostMapping("/reservations/{id}/release")
    public StockReservationResponse release(@PathVariable Long id, @Valid @RequestBody ReleaseReservationRequest request) {
        return inventoryService.release(id, request.reason());
    }

    @PostMapping("/transfers/confirm")
    public StockReservationResponse confirmTransfer(@Valid @RequestBody TransferConfirmRequest request) {
        return inventoryService.confirmTransfer(request);
    }
}
