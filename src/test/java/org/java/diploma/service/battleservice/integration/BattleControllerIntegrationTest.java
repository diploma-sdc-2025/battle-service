package org.java.diploma.service.battleservice.integration;

import org.java.diploma.service.battleservice.repository.BattleLogRepository;
import org.java.diploma.service.battleservice.repository.BattleOutcomeRepository;
import org.java.diploma.service.battleservice.repository.PieceInteractionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BattleControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired BattleOutcomeRepository outcomeRepo;
    @Autowired BattleLogRepository logRepo;
    @Autowired PieceInteractionRepository interactionRepo;

    @Test
    void simulate_persistsAllEntities() throws Exception {
        long outcomes = outcomeRepo.count();
        long logs = logRepo.count();
        long interactions = interactionRepo.count();

        String json = """
            { "matchId": 1, "roundNumber": 1, "attackerId": 10, "defenderId": 20 }
        """;

        mockMvc.perform(post("/api/battle/simulate")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        assertEquals(outcomes + 1, outcomeRepo.count());
        assertEquals(logs + 1, logRepo.count());
        assertEquals(interactions + 1, interactionRepo.count());
    }
}
