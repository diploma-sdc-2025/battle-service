package org.java.diploma.service.battleservice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChessMoveTest {

    @Test
    void noArgsConstructorAndSettersPopulateFields() {
        ChessMove move = new ChessMove();
        move.setFrom("e2");
        move.setTo("e4");
        move.setPiece("pawn");
        move.setCapture(false);
        move.setEvaluationScore(35);

        assertEquals("e2", move.getFrom());
        assertEquals("e4", move.getTo());
        assertEquals("pawn", move.getPiece());
        assertFalse(move.isCapture());
        assertEquals(35, move.getEvaluationScore());
    }

    @Test
    void allArgsConstructorEqualsHashCodeAndToString() {
        ChessMove left = new ChessMove("d5", "e4", "pawn", true, -50);
        ChessMove right = new ChessMove("d5", "e4", "pawn", true, -50);
        ChessMove other = new ChessMove("g1", "f3", "knight", false, 20);

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, other);
        assertTrue(left.toString().contains("from=d5"));
        assertTrue(left.toString().contains("evaluationScore=-50"));
    }
}
