package org.java.diploma.service.battleservice.dto;

import java.util.List;

public record EvaluatePositionResponse(
        int centipawns,
        String advantage,
        String bestMove,
        List<String> principalVariation
) {}
