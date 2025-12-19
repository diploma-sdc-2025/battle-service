package org.java.diploma.service.battleservice.service;

import org.java.diploma.service.battleservice.dto.BattleRequest;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.entity.BattleLog;
import org.java.diploma.service.battleservice.entity.BattleOutcome;
import org.java.diploma.service.battleservice.entity.PieceInteraction;
import org.java.diploma.service.battleservice.repository.BattleLogRepository;
import org.java.diploma.service.battleservice.repository.BattleOutcomeRepository;
import org.java.diploma.service.battleservice.repository.PieceInteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BattleServiceTest {

    private BattleOutcomeRepository outcomes;
    private BattleLogRepository logs;
    private PieceInteractionRepository interactions;
    private BattleService service;

    @BeforeEach
    void setup() {
        outcomes = mock(BattleOutcomeRepository.class);
        logs = mock(BattleLogRepository.class);
        interactions = mock(PieceInteractionRepository.class);

        service = new BattleService(outcomes, logs, interactions);
    }

    @Test
    void simulateBattle_persistsOutcomeLogAndInteraction() {
        BattleRequest req = new BattleRequest(1L, 1, 10L, 20L);

        when(outcomes.save(any(BattleOutcome.class))).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(100);
            return o;
        });

        BattleResultResponse response = service.simulateBattle(req);

        assertNotNull(response);
        assertEquals(100, response.battleId());
        assertTrue(
                response.winnerId().equals(10L) || response.winnerId().equals(20L),
                "Winner must be attacker or defender"
        );
        assertTrue(response.attackerRemaining() >= 0);
        assertTrue(response.defenderRemaining() >= 0);

        verify(outcomes, times(1)).save(any(BattleOutcome.class));
        verify(logs, times(1)).save(any(BattleLog.class));
        verify(interactions, times(1)).save(any(PieceInteraction.class));
    }

    @Test
    void simulateBattle_outcomeFieldsAreSetCorrectly() {
        BattleRequest req = new BattleRequest(5L, 3, 11L, 22L);

        when(outcomes.save(any())).thenAnswer(inv -> {
            BattleOutcome o = inv.getArgument(0);
            o.setId(1);
            return o;
        });

        service.simulateBattle(req);

        ArgumentCaptor<BattleOutcome> captor = ArgumentCaptor.forClass(BattleOutcome.class);
        verify(outcomes).save(captor.capture());

        BattleOutcome outcome = captor.getValue();
        assertEquals(5L, outcome.getMatchId());
        assertEquals(3, outcome.getRoundNumber());
        assertEquals(11L, outcome.getAttackerId());
        assertEquals(22L, outcome.getDefenderId());
        assertTrue(outcome.getBattleDurationMs() >= 500);
    }
}
