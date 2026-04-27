package org.java.diploma.service.battleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.dto.EvaluatePositionResponse;
import org.java.diploma.service.battleservice.service.BattleService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BattleController.class)
class BattleControllerValidationTest {

    private static final Logger log = LoggerFactory.getLogger(BattleControllerValidationTest.class);

    private static final String ENDPOINT_SIMULATE = "/api/battle/simulate";
    private static final String ENDPOINT_EVALUATE = "/api/battle/evaluate";
    private static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String JSON_PATH_BATTLE_ID = "$.battleId";
    private static final String JSON_PATH_WINNER_ID = "$.winnerId";
    private static final String JSON_PATH_DAMAGE_DEALT = "$.damageDealt";
    private static final String JSON_PATH_EVAL_SCORE = "$.centipawns";
    private static final String JSON_PATH_ADVANTAGE = "$.advantage";

    private static final String TEST_JSON_MISSING_FIELDS = """
            { "roundNumber": 1, "attackerId": 10 }
            """;
    private static final String TEST_JSON_VALID_REQUEST = """
            { 
                "matchId": 1, 
                "roundNumber": 1, 
                "attackerId": 10, 
                "defenderId": 20,
                "fenPosition": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            }
            """;
    private static final String TEST_JSON_MISSING_FEN = """
            {
                "matchId": 1,
                "roundNumber": 1,
                "attackerId": 10,
                "defenderId": 20
            }
            """;
    private static final String TEST_JSON_EVALUATE = """
            { "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" }
            """;

    private static final Integer TEST_BATTLE_ID = 1;
    private static final Long TEST_WINNER_ID = 10L;
    private static final int TEST_ATTACKER_HP = 30;
    private static final int TEST_DEFENDER_HP = 15;
    private static final int TEST_DAMAGE_DEALT = 15;
    private static final int TEST_EVALUATION_SCORE = 300;
    private static final String TEST_FEN_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final List<String> TEST_VISUAL_MOVES = Arrays.asList("e2e4", "e7e5", "g1f3");

    private static final String LOG_TEST_START = "Starting test: {}";
    private static final String LOG_TEST_COMPLETE = "Test completed: {}";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BattleService battleService;

    @Test
    void simulate_returns400_whenMissingFields() throws Exception {
        log.info(LOG_TEST_START, "simulate_returns400_whenMissingFields");

        mockMvc.perform(post(ENDPOINT_SIMULATE)
                        .contentType(CONTENT_TYPE_JSON)
                        .content(TEST_JSON_MISSING_FIELDS))
                .andExpect(status().isBadRequest());

        log.info(LOG_TEST_COMPLETE, "simulate_returns400_whenMissingFields");
    }

    @Test
    void simulate_returns400_whenMissingFenPosition() throws Exception {
        log.info(LOG_TEST_START, "simulate_returns400_whenMissingFenPosition");

        mockMvc.perform(post(ENDPOINT_SIMULATE)
                        .contentType(CONTENT_TYPE_JSON)
                        .content(TEST_JSON_MISSING_FEN))
                .andExpect(status().isBadRequest());

        log.info(LOG_TEST_COMPLETE, "simulate_returns400_whenMissingFenPosition");
    }

    @Test
    void simulate_returns200_whenValidRequest() throws Exception {
        log.info(LOG_TEST_START, "simulate_returns200_whenValidRequest");

        when(battleService.simulateBattle(any()))
                .thenReturn(new BattleResultResponse(
                        TEST_BATTLE_ID,
                        TEST_WINNER_ID,
                        TEST_ATTACKER_HP,
                        TEST_DEFENDER_HP,
                        TEST_DAMAGE_DEALT,
                        TEST_EVALUATION_SCORE,
                        TEST_FEN_POSITION,
                        TEST_VISUAL_MOVES
                ));

        mockMvc.perform(post(ENDPOINT_SIMULATE)
                        .contentType(CONTENT_TYPE_JSON)
                        .content(TEST_JSON_VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_BATTLE_ID).value(TEST_BATTLE_ID))
                .andExpect(jsonPath(JSON_PATH_WINNER_ID).value(TEST_WINNER_ID))
                .andExpect(jsonPath(JSON_PATH_DAMAGE_DEALT).value(TEST_DAMAGE_DEALT));

        log.info(LOG_TEST_COMPLETE, "simulate_returns200_whenValidRequest");
    }

    @Test
    void evaluate_returns200_whenValidRequest() throws Exception {
        when(battleService.evaluateFen(any()))
                .thenReturn(new EvaluatePositionResponse(45, "WHITE", "e2e4", List.of("e2e4", "e7e5")));

        mockMvc.perform(post(ENDPOINT_EVALUATE)
                        .contentType(CONTENT_TYPE_JSON)
                        .content(TEST_JSON_EVALUATE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_EVAL_SCORE).value(45))
                .andExpect(jsonPath(JSON_PATH_ADVANTAGE).value("WHITE"));
    }

    @Test
    void evaluate_returns503_whenEngineThrowsIOException() throws Exception {
        when(battleService.evaluateFen(any())).thenThrow(new IOException("engine down"));

        mockMvc.perform(post(ENDPOINT_EVALUATE)
                        .contentType(CONTENT_TYPE_JSON)
                        .content(TEST_JSON_EVALUATE))
                .andExpect(status().isServiceUnavailable());
    }
}