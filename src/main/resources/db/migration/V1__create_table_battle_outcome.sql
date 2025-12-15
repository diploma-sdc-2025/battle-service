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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_battle_outcomes_match_id ON battle_outcomes(match_id);
CREATE INDEX idx_battle_outcomes_round ON battle_outcomes(match_id, round_number);

