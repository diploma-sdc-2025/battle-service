package org.java.diploma.service.battleservice.dto;


import jakarta.validation.constraints.NotNull;

public record BattleRequest(
        @NotNull Long matchId,
        @NotNull Integer roundNumber,
        @NotNull Long attackerId,
        @NotNull Long defenderId
) {}
