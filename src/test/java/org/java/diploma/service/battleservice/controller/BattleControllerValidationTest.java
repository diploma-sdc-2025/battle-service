package org.java.diploma.service.battleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.service.BattleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BattleController.class)
class BattleControllerValidationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BattleService battleService;

    @Test
    void simulate_returns400_whenMissingFields() throws Exception {
        String json = """
            { "roundNumber": 1, "attackerId": 10 }
        """;

        mockMvc.perform(post("/api/battle/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void simulate_returns200_whenValidRequest() throws Exception {
        when(battleService.simulateBattle(any()))
                .thenReturn(new BattleResultResponse(1, 10L, 2, 1));

        String json = """
            { "matchId": 1, "roundNumber": 1, "attackerId": 10, "defenderId": 20 }
        """;

        mockMvc.perform(post("/api/battle/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battleId").value(1));
    }
}
