package org.java.diploma.service.battleservice.dto;

import jakarta.validation.constraints.NotBlank;

public record EvaluatePositionRequest(@NotBlank String fen) {}
