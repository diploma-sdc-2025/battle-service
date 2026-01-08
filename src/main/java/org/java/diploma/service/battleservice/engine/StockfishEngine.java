package org.java.diploma.service.battleservice.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class StockfishEngine {

    private static final Logger log = LoggerFactory.getLogger(StockfishEngine.class);
    private static final String UCI_COMMAND = "uci";
    private static final String ISREADY_COMMAND = "isready";
    private static final String UCINEWGAME_COMMAND = "ucinewgame";
    private static final String POSITION_STARTPOS = "position startpos";
    private static final String POSITION_FEN = "position fen";
    private static final String GO_DEPTH = "go depth";
    private static final String QUIT_COMMAND = "quit";
    private static final String READYOK_RESPONSE = "readyok";
    private static final String INFO_PREFIX = "info";
    private static final String SCORE_CP = "score cp";
    private static final String SCORE_MATE = "score mate";
    private static final String BESTMOVE_PREFIX = "bestmove";
    private static final String PV_PREFIX = "pv";
    private static final String LOG_ENGINE_STARTED = "Stockfish engine started successfully at path: {}";
    private static final String LOG_ENGINE_STOPPED = "Stockfish engine stopped";
    private static final String LOG_ENGINE_ERROR = "Error communicating with Stockfish engine";
    private static final String LOG_EVALUATION_FOUND = "Position evaluation: {} centipawns";
    private static final String ERROR_ENGINE_NOT_READY = "Engine is not ready";
    private static final String LOG_SENDING_COMMAND = "Sending command to engine: {}";
    private static final int MATE_SCORE_BASE = 10000;

    private Process process;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    @Value("${stockfish.path:stockfish}")
    private String stockfishPath;

    @PostConstruct
    public void init() throws IOException {
        startEngine();
    }

    @PreDestroy
    public void cleanup() {
        stopEngine();
    }

    public void startEngine() throws IOException {
        log.info("Starting Stockfish engine at path: {}", stockfishPath);

        process = new ProcessBuilder(stockfishPath).start();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new OutputStreamWriter(process.getOutputStream());

        sendCommand(UCI_COMMAND);
        waitForReady();

        log.info(LOG_ENGINE_STARTED, stockfishPath);
    }

    public void stopEngine() {
        try {
            if (writer != null) {
                sendCommand(QUIT_COMMAND);
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (process != null) {
                process.destroy();
            }
            log.info(LOG_ENGINE_STOPPED);
        } catch (IOException e) {
            log.error(LOG_ENGINE_ERROR, e);
        }
    }

    public PositionEvaluation evaluatePosition(String position, int depth) throws IOException {
        log.debug("Evaluating position with depth: {}", depth);

        if (!isEngineReady()) {
            throw new IllegalStateException(ERROR_ENGINE_NOT_READY);
        }

        sendCommand(UCINEWGAME_COMMAND);
        waitForReady();

        if (position == null || position.isEmpty()) {
            sendCommand(POSITION_STARTPOS);
        } else {
            sendCommand(POSITION_FEN + " " + position);
        }

        sendCommand(GO_DEPTH + " " + depth);

        PositionEvaluation evaluation = readEvaluation();
        log.info(LOG_EVALUATION_FOUND, evaluation.getEvaluationScore());

        return evaluation;
    }

    private void sendCommand(String command) throws IOException {
        log.debug(LOG_SENDING_COMMAND, command);
        writer.write(command + "\n");
        writer.flush();
    }

    private void waitForReady() throws IOException {
        sendCommand(ISREADY_COMMAND);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals(READYOK_RESPONSE)) {
                break;
            }
        }
    }

    private PositionEvaluation readEvaluation() throws IOException {
        int evaluationScore = 0;
        String bestMove = null;
        List<String> principalVariation = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith(INFO_PREFIX)) {
                if (line.contains(SCORE_CP)) {
                    evaluationScore = parseEvaluationScore(line);
                } else if (line.contains(SCORE_MATE)) {
                    evaluationScore = parseMateScore(line);
                }

                if (line.contains(PV_PREFIX)) {
                    principalVariation = parsePrincipalVariation(line);
                }
            }

            if (line.startsWith(BESTMOVE_PREFIX)) {
                bestMove = line.substring(BESTMOVE_PREFIX.length()).trim().split(" ")[0];
                break;
            }
        }

        return new PositionEvaluation(evaluationScore, bestMove, principalVariation);
    }

    private int parseEvaluationScore(String infoLine) {
        try {
            String[] parts = infoLine.split(" ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].equals("cp")) {
                    return Integer.parseInt(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse evaluation from line: {}", infoLine);
        }
        return 0;
    }

    private int parseMateScore(String infoLine) {
        try {
            String[] parts = infoLine.split(" ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].equals("mate")) {
                    int mateIn = Integer.parseInt(parts[i + 1]);
                    return mateIn > 0 ? MATE_SCORE_BASE : -MATE_SCORE_BASE;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse mate score from line: {}", infoLine);
        }
        return 0;
    }

    private List<String> parsePrincipalVariation(String infoLine) {
        List<String> pv = new ArrayList<>();
        try {
            String[] parts = infoLine.split(" ");
            boolean inPv = false;
            for (String part : parts) {
                if (part.equals(PV_PREFIX)) {
                    inPv = true;
                    continue;
                }
                if (inPv && part.length() >= 4 && part.matches("[a-h][1-8][a-h][1-8].*")) {
                    pv.add(part);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse principal variation from line: {}", infoLine);
        }
        return pv;
    }

    private boolean isEngineReady() {
        return process != null && process.isAlive();
    }

    public static class PositionEvaluation {
        private static final String LOG_POSITION_EVAL = "Evaluation: {} centipawns, Best move: {}, PV length: {}";

        private final int evaluationScore;
        private final String bestMove;
        private final List<String> principalVariation;

        public PositionEvaluation(int evaluationScore, String bestMove, List<String> principalVariation) {
            this.evaluationScore = evaluationScore;
            this.bestMove = bestMove;
            this.principalVariation = principalVariation;
        }

        public int getEvaluationScore() {
            return evaluationScore;
        }

        public String getBestMove() {
            return bestMove;
        }

        public List<String> getPrincipalVariation() {
            return principalVariation;
        }

        @Override
        public String toString() {
            return String.format(LOG_POSITION_EVAL, evaluationScore, bestMove, principalVariation.size());
        }
    }
}