package com.drms.sharingservice.controller;

import com.drms.sharingservice.dto.ShortageRequestDto;
import com.drms.sharingservice.dto.ShortageRequestResponse;
import com.drms.sharingservice.dto.TransferResponse;
import com.drms.sharingservice.service.SharingTransparencyService;
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

import com.drms.sharingservice.dto.AdminTransferRequest;
import com.drms.sharingservice.dto.ExcessNotificationRequest;
import com.drms.sharingservice.dto.BulkExcessNotificationRequest;
import com.drms.sharingservice.dto.ExcessRequestRequest;
import com.drms.sharingservice.entity.ExcessNotification;
import com.drms.sharingservice.entity.ExcessRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/shares")
public class SharingController {

    private final SharingTransparencyService sharingService;

    public SharingController(SharingTransparencyService sharingService) {
        this.sharingService = sharingService;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ShortageRequestResponse createShortage(@Valid @RequestBody ShortageRequestDto request) {
        return sharingService.createShortage(request);
    }

    @GetMapping("/requests")
    public List<ShortageRequestResponse> listShortages(@RequestParam(required = false) Long shelterId) {
        return sharingService.listShortages(shelterId);
    }

    @GetMapping("/requests/{id}")
    public ShortageRequestResponse getShortage(@PathVariable Long id) {
        return sharingService.getShortage(id);
    }

    @PostMapping("/matches")
    public TransferResponse createMatch(@RequestBody MatchRequest request) {
        return sharingService.matchShortage(request.shortageRequestId());
    }

    @PostMapping("/transfers/{id}/dispatch")
    public TransferResponse dispatch(@PathVariable Long id) {
        return sharingService.dispatch(id);
    }

    @PostMapping("/transfers/{id}/receive")
    public TransferResponse receive(@PathVariable Long id) {
        return sharingService.receive(id);
    }

    @PostMapping("/transfers/{id}/cancel")
    public TransferResponse cancel(@PathVariable Long id) {
        return sharingService.cancel(id);
    }

    @GetMapping("/transfers")
    public List<TransferResponse> listTransfers(@RequestParam Long shelterId, @RequestParam(required = false) String direction) {
        return sharingService.listTransfers(shelterId, direction);
    }

    @PostMapping("/transfers/admin-transfer")
    public TransferResponse createAdminTransfer(@Valid @RequestBody AdminTransferRequest request) {
        return sharingService.createAdminTransfer(request);
    }

    @PostMapping("/excess")
    @ResponseStatus(HttpStatus.CREATED)
    public ExcessNotification createExcess(@Valid @RequestBody ExcessNotificationRequest request) {
        return sharingService.createExcess(request);
    }

    @PostMapping("/excess/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ExcessNotification> createBulkExcess(@Valid @RequestBody BulkExcessNotificationRequest request) {
        return sharingService.createBulkExcess(request);
    }

    @GetMapping("/excess")
    public List<ExcessNotification> listExcess(@RequestParam(required = false) Long shelterId) {
        return sharingService.listExcess(shelterId);
    }

    @PostMapping("/excess/{id}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ExcessRequest createExcessRequest(@PathVariable Long id, @Valid @RequestBody ExcessRequestRequest request) {
        return sharingService.createExcessRequest(id, request);
    }

    @GetMapping("/excess/{id}/requests")
    public List<ExcessRequest> listRequestsForNotification(@PathVariable Long id) {
        return sharingService.listRequestsForNotification(id);
    }

    @PatchMapping("/excess/requests/{requestId}/approve")
    public TransferResponse approveExcessRequest(@PathVariable Long requestId) {
        return sharingService.approveExcessRequest(requestId);
    }

    @PatchMapping("/excess/requests/{requestId}/reject")
    public void rejectExcessRequest(@PathVariable Long requestId) {
        sharingService.rejectExcessRequest(requestId);
    }

    public record MatchRequest(Long shortageRequestId) {
    }
}
