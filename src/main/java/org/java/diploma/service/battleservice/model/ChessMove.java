package org.java.diploma.service.battleservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChessMove {
    private String from;
    private String to;
    private String piece;
    private boolean isCapture;
    private int evaluationScore;
}