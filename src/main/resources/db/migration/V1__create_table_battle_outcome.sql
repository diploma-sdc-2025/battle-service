CREATE TABLE battle_outcomes (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    round_number INT NOT NULL,
    attacker_id BIGINT NOT NULL,
    defender_id BIGINT NOT NULL,
    winner_id BIGINT,
    attacker_pieces_remaining INT NOT NULL,
    defender_pieces_remaining INT NOT NULL,
    attacker_damage_dealt INT NOT NULL DEFAULT 0,
    defender_damage_dealt INT NOT NULL DEFAULT 0,
    battle_duration_ms INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_battle_outcomes_round CHECK (round_number > 0),
    CONSTRAINT chk_battle_outcomes_different_players CHECK (attacker_id != defender_id),
    CONSTRAINT chk_battle_outcomes_winner CHECK (
        winner_id IS NULL OR
        winner_id = attacker_id OR
        winner_id = defender_id
    ),
    CONSTRAINT chk_battle_outcomes_attacker_pieces CHECK (attacker_pieces_remaining >= 0),
    CONSTRAINT chk_battle_outcomes_defender_pieces CHECK (defender_pieces_remaining >= 0),
    CONSTRAINT chk_battle_outcomes_attacker_damage CHECK (attacker_damage_dealt >= 0),
    CONSTRAINT chk_battle_outcomes_defender_damage CHECK (defender_damage_dealt >= 0),
    CONSTRAINT chk_battle_outcomes_duration CHECK (battle_duration_ms > 0)
);

CREATE INDEX idx_battle_outcomes_match_id ON battle_outcomes(match_id);
CREATE INDEX idx_battle_outcomes_round ON battle_outcomes(match_id, round_number);
CREATE INDEX idx_battle_outcomes_attacker_id ON battle_outcomes(attacker_id);
CREATE INDEX idx_battle_outcomes_defender_id ON battle_outcomes(defender_id);
CREATE INDEX idx_battle_outcomes_winner_id ON battle_outcomes(winner_id);
CREATE INDEX idx_battle_outcomes_created_at ON battle_outcomes(created_at DESC);

CREATE INDEX idx_battle_outcomes_match_round ON battle_outcomes(match_id, round_number, created_at DESC);
