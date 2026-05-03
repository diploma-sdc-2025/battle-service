package org.java.diploma.service.battleservice.engine;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StockfishEngineTest {

    @Test
    void evaluatePositionUsesStartposWhenFenMissing() throws Exception {
        StockfishEngine engine = new StockfishEngine();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(true);

        setField(engine, "process", process);
        setField(engine, "writer", new OutputStreamWriter(out, StandardCharsets.UTF_8));
        setField(engine, "reader", new BufferedReader(new StringReader(
                "readyok\n" +
                "info depth 12 score cp 36 pv e2e4 e7e5\n" +
                "bestmove e2e4\n"
        )));

        StockfishEngine.PositionEvaluation evaluation = engine.evaluatePosition("", 12);

        assertEquals(36, evaluation.getEvaluationScore());
        assertEquals("e2e4", evaluation.getBestMove());
        assertEquals(List.of("e2e4", "e7e5"), evaluation.getPrincipalVariation());
        String sent = out.toString(StandardCharsets.UTF_8);
        assertTrue(sent.contains("position startpos"));
        assertTrue(sent.contains("go depth 12"));
    }

    @Test
    void evaluatePositionUsesFenAndParsesMateScore() throws Exception {
        StockfishEngine engine = new StockfishEngine();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(true);
        String fen = "8/8/8/8/8/8/8/8 w - - 0 1";

        setField(engine, "process", process);
        setField(engine, "writer", new OutputStreamWriter(out, StandardCharsets.UTF_8));
        setField(engine, "reader", new BufferedReader(new StringReader(
                "readyok\n" +
                "info depth 20 score mate 3 pv a2a4\n" +
                "bestmove a2a4\n"
        )));

        StockfishEngine.PositionEvaluation evaluation = engine.evaluatePosition(fen, 20);

        assertEquals(10000, evaluation.getEvaluationScore());
        assertEquals("a2a4", evaluation.getBestMove());
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("position fen " + fen));
    }

    @Test
    void evaluateFenAndBuildGreedyLineCollectsMovesUntilNone() throws Exception {
        StockfishEngine engine = new StockfishEngine();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(true);

        setField(engine, "process", process);
        setField(engine, "writer", new OutputStreamWriter(out, StandardCharsets.UTF_8));
        setField(engine, "reader", new BufferedReader(new StringReader(
                "readyok\ninfo score cp 30 pv e2e4 e7e5\nbestmove e2e4\n" +
                "readyok\ninfo score cp 25 pv e7e5\nbestmove e7e5\n" +
                "readyok\ninfo score cp 12 pv g1f3\nbestmove (none)\n"
        )));

        StockfishEngine.FenEvaluationLine line = engine.evaluateFenAndBuildGreedyLine("", 5, 10);

        assertEquals(30, line.rootCentipawns());
        assertEquals("e2e4", line.bestMove());
        assertEquals(List.of("e2e4", "e7e5"), line.line());
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("position startpos moves e2e4"));
    }

    @Test
    void stopEngineSendsQuitAndClosesResources() throws Exception {
        StockfishEngine engine = new StockfishEngine();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(new StringReader(""));
        Process process = mock(Process.class);

        setField(engine, "writer", writer);
        setField(engine, "reader", reader);
        setField(engine, "process", process);

        engine.stopEngine();

        assertTrue(out.toString(StandardCharsets.UTF_8).contains("quit"));
        verify(process).destroy();
    }

    @Test
    void privateParsingHelpersHandleInvalidInputGracefully() throws Exception {
        StockfishEngine engine = new StockfishEngine();

        Method cpParser = StockfishEngine.class.getDeclaredMethod("parseEvaluationScore", String.class);
        cpParser.setAccessible(true);
        Method mateParser = StockfishEngine.class.getDeclaredMethod("parseMateScore", String.class);
        mateParser.setAccessible(true);
        Method pvParser = StockfishEngine.class.getDeclaredMethod("parsePrincipalVariation", String.class);
        pvParser.setAccessible(true);

        int cp = (int) cpParser.invoke(engine, "info depth 12 score cp not-a-number");
        int mate = (int) mateParser.invoke(engine, "info depth 15 score mate -2");
        @SuppressWarnings("unchecked")
        List<String> pv = (List<String>) pvParser.invoke(engine, "info depth 18 pv e2e4 ??? h7h5");

        assertEquals(0, cp);
        assertEquals(-10000, mate);
        assertEquals(List.of("e2e4", "h7h5"), pv);
    }

    @Test
    void positionEvaluationToStringContainsSummary() {
        StockfishEngine.PositionEvaluation evaluation =
                new StockfishEngine.PositionEvaluation(42, "e2e4", List.of("e2e4", "e7e5"));

        String text = evaluation.toString();

        assertNotNull(text);
        assertFalse(text.isBlank());
        assertTrue(text.contains("Evaluation"));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = StockfishEngine.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
