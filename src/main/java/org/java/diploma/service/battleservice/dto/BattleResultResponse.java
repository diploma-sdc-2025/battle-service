package org.java.diploma.service.battleservice.dto;

import java.util.List;

public record BattleResultResponse(
        Integer battleId,
        Long winnerId,
        int attackerHpRemaining,
        int defenderHpRemaining,
        int damageDealt,
        int evaluationScore,
        String position,
        List<String> visualMoves
) {}
