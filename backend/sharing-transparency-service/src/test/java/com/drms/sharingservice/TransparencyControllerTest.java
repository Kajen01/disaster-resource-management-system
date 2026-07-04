package com.drms.sharingservice;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.drms.sharingservice.controller.TransparencyController;
import com.drms.sharingservice.dto.DonationTraceSummaryResponse;
import com.drms.sharingservice.service.SharingTransparencyService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = TransparencyController.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import="
})
class TransparencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SharingTransparencyService sharingService;

    @Test
    void getAllTracesReturnsGlobalTransparencyList() throws Exception {
        when(sharingService.getAllTraces())
                .thenReturn(List.of(new DonationTraceSummaryResponse(
                        77L,
                        "DON-001",
                        1L,
                        2L,
                        "FOOD",
                        "Dry Rations",
                        60,
                        Instant.parse("2026-05-17T10:15:30Z")
                )));

        mockMvc.perform(get("/api/transparency/traces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transferId").value(77))
                .andExpect(jsonPath("$[0].donationRef").value("DON-001"))
                .andExpect(jsonPath("$[0].destinationShelterId").value(2))
                .andExpect(jsonPath("$[0].resourceName").value("Dry Rations"));

        verify(sharingService).getAllTraces();
    }
}
