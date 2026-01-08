package org.java.diploma.service.battleservice.integration;

import org.java.diploma.service.battleservice.engine.StockfishEngine;
import org.java.diploma.service.battleservice.repository.BattleLogRepository;
import org.java.diploma.service.battleservice.repository.BattleOutcomeRepository;
import org.java.diploma.service.battleservice.repository.PieceInteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BattleControllerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(BattleControllerIntegrationTest.class);

    private static final String ENDPOINT_SIMULATE = "/api/battle/simulate";

    private static final String TEST_JSON_VALID_BATTLE = """
            { 
                "matchId": 1, 
                "roundNumber": 1, 
                "attackerId": 10, 
                "defenderId": 20,
                "fenPosition": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            }
            """;

    private static final String TEST_JSON_WHITE_ADVANTAGE = """
            { 
                "matchId": 2, 
                "roundNumber": 1, 
                "attackerId": 11, 
                "defenderId": 21,
                "fenPosition": "rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
            }
            """;

    private static final String JSON_PATH_BATTLE_ID = "$.battleId";
    private static final String JSON_PATH_WINNER_ID = "$.winnerId";
    private static final String JSON_PATH_DAMAGE_DEALT = "$.damageDealt";
    private static final String JSON_PATH_EVALUATION_SCORE = "$.evaluationScore";

    private static final String LOG_TEST_START = "Starting integration test: {}";
    private static final String LOG_TEST_COMPLETE = "Integration test completed: {}";
    private static final String LOG_COUNTS_BEFORE = "Counts before - Outcomes: {}, Logs: {}, Interactions: {}";
    private static final String LOG_COUNTS_AFTER = "Counts after - Outcomes: {}, Logs: {}, Interactions: {}";
    private static final String LOG_SETUP_MOCKS = "Setting up Stockfish engine mocks";

    private static final int EXPECTED_LOGS_PER_BATTLE = 4;
    private static final int EXPECTED_INTERACTIONS_PER_BATTLE = 1;
    private static final int TEST_EVAL_EQUAL = 0;
    private static final int TEST_EVAL_WHITE_ADVANTAGE = 300;
    private static final String TEST_BEST_MOVE_E4 = "e2e4";
    private static final String TEST_BEST_MOVE_CXF7 = "c4f7";
    private static final String TEST_FEN_WHITE_ADVANTAGE_PARTIAL = "rnbqkb1r/pppp1ppp/5n2";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BattleOutcomeRepository outcomeRepo;

    @Autowired
    BattleLogRepository logRepo;

    @Autowired
    PieceInteractionRepository interactionRepo;

    @MockitoBean
    StockfishEngine stockfishEngine;

    @BeforeEach
    void setupMocks() throws IOException {
        log.info(LOG_SETUP_MOCKS);

        // Mock equal position evaluation (starting position)
        StockfishEngine.PositionEvaluation equalEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_EQUAL,
                TEST_BEST_MOVE_E4,
                Arrays.asList("e2e4", "e7e5")
        );

        // Mock white advantage evaluation
        StockfishEngine.PositionEvaluation whiteAdvantageEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_WHITE_ADVANTAGE,
                TEST_BEST_MOVE_CXF7,
                Arrays.asList("c4f7", "e8f7", "f3e5")
        );

        // Return different evaluations based on position
        when(stockfishEngine.evaluatePosition(anyString(), anyInt())).thenAnswer(invocation -> {
            String position = invocation.getArgument(0);
            if (position != null && position.contains(TEST_FEN_WHITE_ADVANTAGE_PARTIAL)) {
                return whiteAdvantageEval;
            }
            return equalEval;
        });
    }

    @Test
    void simulate_persistsAllEntities() throws Exception {
        log.info(LOG_TEST_START, "simulate_persistsAllEntities");

        long outcomesBefore = outcomeRepo.count();
        long logsBefore = logRepo.count();
        long interactionsBefore = interactionRepo.count();

        log.info(LOG_COUNTS_BEFORE, outcomesBefore, logsBefore, interactionsBefore);

        mockMvc.perform(post(ENDPOINT_SIMULATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEST_JSON_VALID_BATTLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_BATTLE_ID).exists())
                .andExpect(jsonPath(JSON_PATH_WINNER_ID).exists());

        long outcomesAfter = outcomeRepo.count();
        long logsAfter = logRepo.count();
        long interactionsAfter = interactionRepo.count();

        log.info(LOG_COUNTS_AFTER, outcomesAfter, logsAfter, interactionsAfter);

        assertEquals(outcomesBefore + 1, outcomesAfter, "Should persist 1 battle outcome");
        assertEquals(logsBefore + EXPECTED_LOGS_PER_BATTLE, logsAfter, "Should persist 4 battle logs");
        assertEquals(interactionsBefore + EXPECTED_INTERACTIONS_PER_BATTLE, interactionsAfter, "Should persist 1 piece interaction");

        log.info(LOG_TEST_COMPLETE, "simulate_persistsAllEntities");
    }

    @Test
    void simulate_withWhiteAdvantage_persistsCorrectDamage() throws Exception {
        log.info(LOG_TEST_START, "simulate_withWhiteAdvantage_persistsCorrectDamage");

        mockMvc.perform(post(ENDPOINT_SIMULATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TEST_JSON_WHITE_ADVANTAGE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_BATTLE_ID).exists())
                .andExpect(jsonPath(JSON_PATH_DAMAGE_DEALT).isNumber())
                .andExpect(jsonPath(JSON_PATH_EVALUATION_SCORE).value(TEST_EVAL_WHITE_ADVANTAGE));

        log.info(LOG_TEST_COMPLETE, "simulate_withWhiteAdvantage_persistsCorrectDamage");
    }
}