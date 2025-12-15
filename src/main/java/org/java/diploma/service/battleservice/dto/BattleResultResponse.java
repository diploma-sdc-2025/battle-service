package org.java.diploma.service.battleservice.dto;

public record BattleResultResponse(
        Integer battleId,
        Long winnerId,
        int attackerRemaining,
        int defenderRemaining
) {}
