package com.drms.sharingservice.controller;

import com.drms.sharingservice.dto.DonationTraceSummaryResponse;
import com.drms.sharingservice.dto.ShortageRequestResponse;
import com.drms.sharingservice.dto.TransparencyView;
import com.drms.sharingservice.service.SharingTransparencyService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transparency")
public class TransparencyController {

    private final SharingTransparencyService sharingService;

    public TransparencyController(SharingTransparencyService sharingService) {
        this.sharingService = sharingService;
    }

    @GetMapping("/donations/{donationRef}")
    public TransparencyView getTransparency(@PathVariable String donationRef) {
        return sharingService.getTransparency(donationRef);
    }

    @GetMapping("/traces")
    public List<DonationTraceSummaryResponse> getAllTraces() {
        return sharingService.getAllTraces();
    }

    @GetMapping("/shortages")
    public List<ShortageRequestResponse> getAllShortages() {
        return sharingService.listShortages(null);
    }
}
