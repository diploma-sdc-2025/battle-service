package org.java.diploma.service.battleservice.controller;

import jakarta.validation.Valid;
import org.java.diploma.service.battleservice.dto.BattleRequest;
import org.java.diploma.service.battleservice.dto.BattleResultResponse;
import org.java.diploma.service.battleservice.service.BattleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    private final BattleService battle;

    public BattleController(BattleService battle) {
        this.battle = battle;
    }

    @PostMapping("/simulate")
    public BattleResultResponse simulate(@Valid @RequestBody BattleRequest req) {
        return battle.simulateBattle(req);
    }
}
