package org.java.diploma.service.battleservice.controller;

import jakarta.validation.Valid;
import org.java.diploma.service.battleservice.dto.BattleRequest;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.service.BattleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    private static final Logger log = LoggerFactory.getLogger(BattleController.class);
    private static final String SIMULATE_BATTLE_ENDPOINT = "/simulate";
    private static final String LOG_CONTROLLER_INITIALIZED = "BattleController initialized";
    private static final String LOG_SIMULATE_REQUEST = "Received battle simulation request for attacker ID: {} and defender ID: {}";
    private static final String LOG_SIMULATE_SUCCESS = "Battle simulation completed successfully. Winner: {}";
    private static final String LOG_BATTLE_RESULT = "Battle result: {}";

    private final BattleService battle;

    public BattleController(BattleService battle) {
        this.battle = battle;
        log.info(LOG_CONTROLLER_INITIALIZED);
    }

    @PostMapping(SIMULATE_BATTLE_ENDPOINT)
    public BattleResultResponse simulate(@Valid @RequestBody BattleRequest req) {
        log.info(LOG_SIMULATE_REQUEST, req.attackerId(), req.defenderId());

        BattleResultResponse response = battle.simulateBattle(req);

        log.info(LOG_SIMULATE_SUCCESS, response.winnerId());
        log.debug(LOG_BATTLE_RESULT, response);

        return response;
    }
}