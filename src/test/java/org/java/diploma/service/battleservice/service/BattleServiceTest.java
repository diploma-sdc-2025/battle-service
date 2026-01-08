package org.java.diploma.service.battleservice.service;

import org.java.diploma.service.battleservice.dto.BattleRequest;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.engine.StockfishEngine;
import org.java.diploma.service.battleservice.entity.BattleLog;
import org.java.diploma.service.battleservice.entity.BattleOutcome;
import org.java.diploma.service.battleservice.entity.PieceInteraction;
import org.java.diploma.service.battleservice.repository.BattleLogRepository;
import org.java.diploma.service.battleservice.repository.BattleOutcomeRepository;
import org.java.diploma.service.battleservice.repository.PieceInteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BattleServiceTest {

    private static final Logger log = LoggerFactory.getLogger(BattleServiceTest.class);

    private static final String TEST_FEN_STARTING = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String TEST_FEN_WHITE_ADVANTAGE = "rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4";
    private static final String TEST_FEN_BLACK_ADVANTAGE = "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4";

    private static final Long TEST_MATCH_ID = 1L;
    private static final Integer TEST_ROUND_NUMBER = 1;
    private static final Long TEST_ATTACKER_ID = 10L;
    private static final Long TEST_DEFENDER_ID = 20L;
    private static final Integer TEST_BATTLE_ID = 100;

    private static final int TEST_EVAL_EQUAL = 0;
    private static final int TEST_EVAL_WHITE_ADVANTAGE = 300;
    private static final int TEST_EVAL_BLACK_ADVANTAGE = -300;

    private static final String LOG_TEST_START = "Starting test: {}";
    private static final String LOG_TEST_COMPLETE = "Test completed successfully: {}";

    private BattleOutcomeRepository outcomes;
    private BattleLogRepository logs;
    private PieceInteractionRepository interactions;
    private StockfishEngine stockfishEngine;
    private BattleService service;

    @BeforeEach
    void setup() {
        log.info("Setting up BattleServiceTest");

        outcomes = mock(BattleOutcomeRepository.class);
        logs = mock(BattleLogRepository.class);
        interactions = mock(PieceInteractionRepository.class);
        stockfishEngine = mock(StockfishEngine.class);

        service = new BattleService(outcomes, logs, interactions, stockfishEngine);
    }

    @Test
    void simulateBattle_withEqualPosition_dealsNoDamage() throws IOException {
        log.info(LOG_TEST_START, "simulateBattle_withEqualPosition_dealsNoDamage");

        BattleRequest req = new BattleRequest(
                TEST_MATCH_ID,
                TEST_ROUND_NUMBER,
                TEST_ATTACKER_ID,
                TEST_DEFENDER_ID,
                TEST_FEN_STARTING
        );

        StockfishEngine.PositionEvaluation mockEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_EQUAL,
                "e2e4",
                Arrays.asList("e2e4", "e7e5")
        );

        when(stockfishEngine.evaluatePosition(anyString(), anyInt())).thenReturn(mockEval);
        when(outcomes.save(any(BattleOutcome.class))).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(TEST_BATTLE_ID);
            return o;
        });

        BattleResultResponse response = service.simulateBattle(req);

        assertNotNull(response);
        assertEquals(TEST_BATTLE_ID, response.battleId());
        assertEquals(0, response.damageDealt());
        assertEquals(30, response.attackerHpRemaining());
        assertEquals(30, response.defenderHpRemaining());

        verify(outcomes, times(1)).save(any(BattleOutcome.class));
        verify(logs, times(4)).save(any(BattleLog.class)); // START, EVAL, DAMAGE, END
        verify(interactions, times(1)).save(any(PieceInteraction.class));

        log.info(LOG_TEST_COMPLETE, "simulateBattle_withEqualPosition_dealsNoDamage");
    }

    @Test
    void simulateBattle_withWhiteAdvantage_damagesDefender() throws IOException {
        log.info(LOG_TEST_START, "simulateBattle_withWhiteAdvantage_damagesDefender");

        BattleRequest req = new BattleRequest(
                TEST_MATCH_ID,
                TEST_ROUND_NUMBER,
                TEST_ATTACKER_ID,
                TEST_DEFENDER_ID,
                TEST_FEN_WHITE_ADVANTAGE
        );

        StockfishEngine.PositionEvaluation mockEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_WHITE_ADVANTAGE,
                "c4f7",
                Arrays.asList("c4f7", "e8f7", "f3e5")
        );

        when(stockfishEngine.evaluatePosition(anyString(), anyInt())).thenReturn(mockEval);
        when(outcomes.save(any(BattleOutcome.class))).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(TEST_BATTLE_ID);
            return o;
        });

        BattleResultResponse response = service.simulateBattle(req);

        assertNotNull(response);
        assertEquals(TEST_ATTACKER_ID, response.winnerId());
        assertTrue(response.damageDealt() > 0);
        assertEquals(30, response.attackerHpRemaining());
        assertTrue(response.defenderHpRemaining() < 30);

        log.info(LOG_TEST_COMPLETE, "simulateBattle_withWhiteAdvantage_damagesDefender");
    }

    @Test
    void simulateBattle_withBlackAdvantage_damagesAttacker() throws IOException {
        log.info(LOG_TEST_START, "simulateBattle_withBlackAdvantage_damagesAttacker");

        BattleRequest req = new BattleRequest(
                TEST_MATCH_ID,
                TEST_ROUND_NUMBER,
                TEST_ATTACKER_ID,
                TEST_DEFENDER_ID,
                TEST_FEN_BLACK_ADVANTAGE
        );

        StockfishEngine.PositionEvaluation mockEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_BLACK_ADVANTAGE,
                "c6d4",
                Arrays.asList("c6d4", "f3d4", "e5d4")
        );

        when(stockfishEngine.evaluatePosition(anyString(), anyInt())).thenReturn(mockEval);
        when(outcomes.save(any(BattleOutcome.class))).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(TEST_BATTLE_ID);
            return o;
        });

        BattleResultResponse response = service.simulateBattle(req);

        assertNotNull(response);
        assertEquals(TEST_DEFENDER_ID, response.winnerId());
        assertTrue(response.damageDealt() > 0);
        assertTrue(response.attackerHpRemaining() < 30);
        assertEquals(30, response.defenderHpRemaining());

        log.info(LOG_TEST_COMPLETE, "simulateBattle_withBlackAdvantage_damagesAttacker");
    }

    @Test
    void simulateBattle_outcomeFieldsAreSetCorrectly() throws IOException {
        log.info(LOG_TEST_START, "simulateBattle_outcomeFieldsAreSetCorrectly");

        Long matchId = 5L;
        Integer roundNumber = 3;
        Long attackerId = 11L;
        Long defenderId = 22L;

        BattleRequest req = new BattleRequest(
                matchId,
                roundNumber,
                attackerId,
                defenderId,
                TEST_FEN_STARTING
        );

        StockfishEngine.PositionEvaluation mockEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_EQUAL,
                "e2e4",
                List.of()
        );

        when(stockfishEngine.evaluatePosition(anyString(), anyInt())).thenReturn(mockEval);
        when(outcomes.save(any())).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(1);
            return o;
        });

        service.simulateBattle(req);

        ArgumentCaptor<BattleOutcome> captor = ArgumentCaptor.forClass(BattleOutcome.class);
        verify(outcomes).save(captor.capture());

        BattleOutcome outcome = captor.getValue();
        assertEquals(matchId, outcome.getMatchId());
        assertEquals(roundNumber, outcome.getRoundNumber());
        assertEquals(attackerId, outcome.getAttackerId());
        assertEquals(defenderId, outcome.getDefenderId());
        assertNotNull(outcome.getWinnerId());
        assertTrue(outcome.getBattleDurationMs() >= 0);

        log.info(LOG_TEST_COMPLETE, "simulateBattle_outcomeFieldsAreSetCorrectly");
    }

    @Test
    void simulateBattle_savesAllLogs() throws IOException {
        log.info(LOG_TEST_START, "simulateBattle_savesAllLogs");

        BattleRequest req = new BattleRequest(
                TEST_MATCH_ID,
                TEST_ROUND_NUMBER,
                TEST_ATTACKER_ID,
                TEST_DEFENDER_ID,
                TEST_FEN_STARTING
        );

        StockfishEngine.PositionEvaluation mockEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_WHITE_ADVANTAGE,
                "e2e4",
                Arrays.asList("e2e4", "e7e5", "g1f3")
        );

        when(stockfishEngine.evaluatePosition(anyString(), anyInt())).thenReturn(mockEval);
        when(outcomes.save(any(BattleOutcome.class))).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(TEST_BATTLE_ID);
            return o;
        });

        service.simulateBattle(req);

        ArgumentCaptor<BattleLog> logCaptor = ArgumentCaptor.forClass(BattleLog.class);
        verify(logs, times(4)).save(logCaptor.capture());

        List<BattleLog> savedLogs = logCaptor.getAllValues();
        assertEquals("BATTLE_START", savedLogs.get(0).getEventType());
        assertEquals("EVALUATION", savedLogs.get(1).getEventType());
        assertEquals("DAMAGE", savedLogs.get(2).getEventType());
        assertEquals("BATTLE_END", savedLogs.get(3).getEventType());

        log.info(LOG_TEST_COMPLETE, "simulateBattle_savesAllLogs");
    }

    @Test
    void simulateBattle_returnsVisualMoves() throws IOException {
        log.info(LOG_TEST_START, "simulateBattle_returnsVisualMoves");

        BattleRequest req = new BattleRequest(
                TEST_MATCH_ID,
                TEST_ROUND_NUMBER,
                TEST_ATTACKER_ID,
                TEST_DEFENDER_ID,
                TEST_FEN_STARTING
        );

        List<String> expectedMoves = Arrays.asList("e2e4", "e7e5", "g1f3", "b8c6", "f1c4");
        StockfishEngine.PositionEvaluation mockEval = new StockfishEngine.PositionEvaluation(
                TEST_EVAL_EQUAL,
                "e2e4",
                expectedMoves
        );

        when(stockfishEngine.evaluatePosition(anyString(), anyInt())).thenReturn(mockEval);
        when(outcomes.save(any(BattleOutcome.class))).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(TEST_BATTLE_ID);
            return o;
        });

        BattleResultResponse response = service.simulateBattle(req);

        assertNotNull(response.visualMoves());
        assertEquals(5, response.visualMoves().size());
        assertEquals("e2e4", response.visualMoves().get(0));

        log.info(LOG_TEST_COMPLETE, "simulateBattle_returnsVisualMoves");
    }
}