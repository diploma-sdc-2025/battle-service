package org.java.diploma.service.battleservice.service;

import org.java.diploma.service.battleservice.dto.BattleRequest;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.dto.EvaluatePositionResponse;
import org.java.diploma.service.battleservice.engine.StockfishEngine;
import org.java.diploma.service.battleservice.entity.BattleLog;
import org.java.diploma.service.battleservice.entity.BattleOutcome;
import org.java.diploma.service.battleservice.entity.PieceInteraction;
import org.java.diploma.service.battleservice.repository.BattleLogRepository;
import org.java.diploma.service.battleservice.repository.BattleOutcomeRepository;
import org.java.diploma.service.battleservice.repository.PieceInteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class BattleService {

    private static final Logger log = LoggerFactory.getLogger(BattleService.class);

    private static final String LOG_BATTLE_START = "Starting battle simulation for match: {}, round: {}, attacker: {}, defender: {}";
    private static final String LOG_EVALUATION_RESULT = "Position evaluation: {} centipawns - Advantage: {}";
    private static final String LOG_DAMAGE_CALCULATED = "Damage calculated: {} (from evaluation: {})";
    private static final String LOG_BATTLE_END = "Battle ended. Winner: {}, Damage dealt: {}, Duration: {}ms";
    private static final String LOG_SAVING_OUTCOME = "Saving battle outcome for match ID: {}";
    private static final String LOG_SAVING_LOGS = "Saving battle logs and interactions";
    private static final String ERROR_SIMULATION_FAILED = "Battle simulation failed";

    private static final String EVENT_BATTLE_START = "BATTLE_START";
    private static final String EVENT_EVALUATION = "EVALUATION";
    private static final String EVENT_DAMAGE = "DAMAGE";
    private static final String EVENT_BATTLE_END = "BATTLE_END";
    private static final String INTERACTION_POSITION_EVAL = "POSITION_EVALUATION";

    private static final String ADVANTAGE_WHITE = "WHITE";
    private static final String ADVANTAGE_BLACK = "BLACK";
    private static final String ADVANTAGE_EQUAL = "EQUAL";

    private static final String TARGET_DEFENDER = "defender";
    private static final String TARGET_ATTACKER = "attacker";

    private static final String JSON_FORMAT_START = "{\"attacker\":%d,\"defender\":%d,\"position\":\"%s\"}";
    private static final String JSON_FORMAT_EVAL = "{\"evaluation\":%d,\"advantage\":\"%s\"}";
    private static final String JSON_FORMAT_DAMAGE = "{\"damageDealt\":%d,\"target\":\"%s\"}";
    private static final String JSON_FORMAT_END = "{\"evaluation\":%d,\"damageDealt\":%d}";

    private static final int MAX_DAMAGE = 30;
    private static final int EVALUATION_THRESHOLD = 100;
    private static final int VISUAL_MOVES_LIMIT = 5;
    /** Half-moves (plies): 20 = each side plays 10 moves. */
    private static final int EVAL_PRINCIPAL_VARIATION_LIMIT = 20;

    private final BattleOutcomeRepository outcomes;
    private final BattleLogRepository logs;
    private final PieceInteractionRepository interactions;
    private final StockfishEngine stockfishEngine;

    @Value("${battle.stockfish.depth:15}")
    private int stockfishDepth;

    public BattleService(BattleOutcomeRepository outcomes,
                         BattleLogRepository logs,
                         PieceInteractionRepository interactions,
                         StockfishEngine stockfishEngine) {
        this.outcomes = outcomes;
        this.logs = logs;
        this.interactions = interactions;
        this.stockfishEngine = stockfishEngine;
    }

    /**
     * Static evaluation only (no DB). Centipawns are from White’s perspective (Stockfish UCI).
     */
    public EvaluatePositionResponse evaluateFen(String fen) throws IOException {
        StockfishEngine.FenEvaluationLine built = stockfishEngine.evaluateFenAndBuildGreedyLine(
                fen,
                EVAL_PRINCIPAL_VARIATION_LIMIT,
                stockfishDepth
        );
        int evaluationScore = built.rootCentipawns();
        return new EvaluatePositionResponse(
                evaluationScore,
                determineAdvantage(evaluationScore),
                built.bestMove(),
                built.line()
        );
    }

    @Transactional
    public BattleResultResponse simulateBattle(BattleRequest req) {
        log.info(LOG_BATTLE_START, req.matchId(), req.roundNumber(), req.attackerId(), req.defenderId());

        long startTime = System.currentTimeMillis();

        try {
            StockfishEngine.PositionEvaluation evaluation = stockfishEngine.evaluatePosition(
                    req.fenPosition(),
                    stockfishDepth
            );

            int evaluationScore = evaluation.getEvaluationScore();
            String advantage = determineAdvantage(evaluationScore);

            log.info(LOG_EVALUATION_RESULT, evaluationScore, advantage);

            int damageDealt = calculateDamage(evaluationScore);
            log.info(LOG_DAMAGE_CALCULATED, damageDealt, evaluationScore);

            boolean attackerWinning = evaluationScore > 0;
            int attackerHp = attackerWinning ? MAX_DAMAGE : Math.max(0, MAX_DAMAGE - damageDealt);
            int defenderHp = attackerWinning ? Math.max(0, MAX_DAMAGE - damageDealt) : MAX_DAMAGE;

            Long winnerId = attackerHp > defenderHp ? req.attackerId() : req.defenderId();

            long duration = System.currentTimeMillis() - startTime;
            log.info(LOG_BATTLE_END, winnerId, damageDealt, duration);

            BattleOutcome outcome = saveBattleOutcome(
                    req,
                    winnerId,
                    attackerHp,
                    defenderHp,
                    damageDealt,
                    evaluationScore,
                    (int) duration
            );

            saveBattleLogs(outcome.getId(), req, evaluationScore, damageDealt, advantage);

            List<String> visualMoves = evaluation.getPrincipalVariation().stream()
                    .limit(VISUAL_MOVES_LIMIT)
                    .toList();

            return new BattleResultResponse(
                    outcome.getId(),
                    winnerId,
                    attackerHp,
                    defenderHp,
                    damageDealt,
                    evaluationScore,
                    req.fenPosition(),
                    visualMoves
            );

        } catch (IOException e) {
            log.error(ERROR_SIMULATION_FAILED, e);
            throw new RuntimeException(ERROR_SIMULATION_FAILED, e);
        }
    }

    private int calculateDamage(int evaluationScore) {
        int absEval = Math.abs(evaluationScore);

        if (absEval < EVALUATION_THRESHOLD) {
            return 0;
        }

        /* One HP per full pawn of advantage (100 cp ≈ 1.0 on the eval bar); matches UI pawn readout. */
        int damage = absEval / EVALUATION_THRESHOLD;

        return Math.min(MAX_DAMAGE, damage);
    }

    private String determineAdvantage(int evaluationScore) {
        if (evaluationScore > EVALUATION_THRESHOLD) {
            return ADVANTAGE_WHITE;
        } else if (evaluationScore < -EVALUATION_THRESHOLD) {
            return ADVANTAGE_BLACK;
        }
        return ADVANTAGE_EQUAL;
    }

    private BattleOutcome saveBattleOutcome(BattleRequest req, Long winnerId,
                                            int attackerHp, int defenderHp,
                                            int damageDealt, int evaluationScore,
                                            int duration) {
        log.debug(LOG_SAVING_OUTCOME, req.matchId());

        BattleOutcome outcome = new BattleOutcome();
        outcome.setMatchId(req.matchId());
        outcome.setRoundNumber(req.roundNumber());
        outcome.setAttackerId(req.attackerId());
        outcome.setDefenderId(req.defenderId());
        outcome.setWinnerId(winnerId);
        outcome.setAttackerPiecesRemaining(calculatePiecesRemaining(attackerHp));
        outcome.setDefenderPiecesRemaining(calculatePiecesRemaining(defenderHp));
        outcome.setAttackerDamageDealt(evaluationScore > 0 ? damageDealt : 0);
        outcome.setDefenderDamageDealt(evaluationScore < 0 ? damageDealt : 0);
        outcome.setBattleDurationMs(duration);

        return outcomes.save(outcome);
    }

    private void saveBattleLogs(Integer battleId, BattleRequest req,
                                int evaluationScore, int damageDealt, String advantage) {
        log.debug(LOG_SAVING_LOGS);

        BattleLog startLog = new BattleLog();
        startLog.setBattleId(battleId);
        startLog.setEventType(EVENT_BATTLE_START);
        startLog.setEventData(String.format(
                JSON_FORMAT_START,
                req.attackerId(), req.defenderId(), req.fenPosition()
        ));
        logs.save(startLog);

        BattleLog evalLog = new BattleLog();
        evalLog.setBattleId(battleId);
        evalLog.setEventType(EVENT_EVALUATION);
        evalLog.setEventData(String.format(
                JSON_FORMAT_EVAL,
                evaluationScore, advantage
        ));
        logs.save(evalLog);

        BattleLog damageLog = new BattleLog();
        damageLog.setBattleId(battleId);
        damageLog.setEventType(EVENT_DAMAGE);
        damageLog.setDamageAmount(damageDealt);
        damageLog.setEventData(String.format(
                JSON_FORMAT_DAMAGE,
                damageDealt, evaluationScore > 0 ? TARGET_DEFENDER : TARGET_ATTACKER
        ));
        logs.save(damageLog);

        BattleLog endLog = new BattleLog();
        endLog.setBattleId(battleId);
        endLog.setEventType(EVENT_BATTLE_END);
        endLog.setEventData(String.format(
                JSON_FORMAT_END,
                evaluationScore, damageDealt
        ));
        logs.save(endLog);

        PieceInteraction interaction = new PieceInteraction();
        interaction.setBattleId(battleId);
        interaction.setAttackerPieceId(req.attackerId());
        interaction.setDefenderPieceId(req.defenderId());
        interaction.setInteractionType(INTERACTION_POSITION_EVAL);
        interactions.save(interaction);
    }

    private int calculatePiecesRemaining(int hp) {
        return Math.max(0, (hp + 1) / 2);
    }
}
