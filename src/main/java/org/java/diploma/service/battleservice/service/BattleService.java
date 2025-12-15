package org.java.diploma.service.battleservice.service;

import org.java.diploma.service.battleservice.dto.BattleRequest;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.entity.BattleOutcome;
import org.java.diploma.service.battleservice.entity.BattleLog;
import org.java.diploma.service.battleservice.entity.PieceInteraction;
import org.java.diploma.service.battleservice.repository.BattleLogRepository;
import org.java.diploma.service.battleservice.repository.BattleOutcomeRepository;
import org.java.diploma.service.battleservice.repository.PieceInteractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class BattleService {

    private final BattleOutcomeRepository outcomes;
    private final BattleLogRepository logs;
    private final PieceInteractionRepository interactions;
    private final Random random = new Random();

    public BattleService(BattleOutcomeRepository outcomes,
                         BattleLogRepository logs,
                         PieceInteractionRepository interactions) {
        this.outcomes = outcomes;
        this.logs = logs;
        this.interactions = interactions;
    }

    @Transactional
    public BattleResultResponse simulateBattle(BattleRequest req) {

        int attackerRemaining = random.nextInt(5);
        int defenderRemaining = random.nextInt(5);

        Long winnerId = attackerRemaining > defenderRemaining
                ? req.attackerId()
                : req.defenderId();

        BattleOutcome outcome = new BattleOutcome();
        outcome.setMatchId(req.matchId());
        outcome.setRoundNumber(req.roundNumber());
        outcome.setAttackerId(req.attackerId());
        outcome.setDefenderId(req.defenderId());
        outcome.setWinnerId(winnerId);
        outcome.setAttackerPiecesRemaining(attackerRemaining);
        outcome.setDefenderPiecesRemaining(defenderRemaining);
        outcome.setAttackerDamageDealt(random.nextInt(50));
        outcome.setDefenderDamageDealt(random.nextInt(50));
        outcome.setBattleDurationMs(500 + random.nextInt(500));

        outcome = outcomes.save(outcome);

        BattleLog log = new BattleLog();
        log.setBattleId(outcome.getId());
        log.setEventType("BATTLE_END");
        log.setEventData("{\"result\":\"completed\"}");
        logs.save(log);

        PieceInteraction interaction = new PieceInteraction();
        interaction.setBattleId(outcome.getId());
        interaction.setAttackerPieceId(1L);
        interaction.setDefenderPieceId(2L);
        interaction.setInteractionType("ATTACK");
        interactions.save(interaction);

        return new BattleResultResponse(
                outcome.getId(),
                winnerId,
                attackerRemaining,
                defenderRemaining
        );
    }
}
