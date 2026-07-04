package com.drms.resourceservice;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.drms.resourceservice.controller.ResourceController;
import com.drms.resourceservice.dto.BatchResponse;
import com.drms.resourceservice.dto.DonationHistoryResponse;
import com.drms.resourceservice.entity.ResourceCategory;
import com.drms.resourceservice.service.ResourceInventoryService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ResourceController.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import="
})
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResourceInventoryService inventoryService;

    @Test
    void getDonationHistoryReturnsLoggedInDonorHistory() throws Exception {
        when(inventoryService.getDonationHistory("donor@example.com"))
                .thenReturn(new DonationHistoryResponse(
                        "donor@example.com",
                        1,
                        List.of(new BatchResponse(
                                9L,
                                2L,
                                "donor@example.com",
                                ResourceCategory.FOOD,
                                "Dry Rations",
                                "packs",
                                25,
                                25,
                                LocalDate.of(2026, 6, 1),
                                Instant.parse("2026-05-17T10:15:30Z"),
                                "DON-009"
                        ))
                ));

        mockMvc.perform(get("/api/resources/donations/me")
                        .header("X-User-Email", "donor@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.donorEmail").value("donor@example.com"))
                .andExpect(jsonPath("$.totalBatches").value(1))
                .andExpect(jsonPath("$.batches[0].sourceDonationRef").value("DON-009"))
                .andExpect(jsonPath("$.batches[0].resourceType").value("FOOD"));

        verify(inventoryService).getDonationHistory("donor@example.com");
    }
}
