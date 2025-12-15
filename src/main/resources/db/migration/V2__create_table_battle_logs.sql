CREATE TABLE battle_logs (
                             id SERIAL PRIMARY KEY,
                             battle_id INT NOT NULL,
                             event_type VARCHAR(50) NOT NULL,
                             piece_id BIGINT,
                             position_from VARCHAR(10),
                             position_to VARCHAR(10),
                             damage_amount INT,
                             hp_remaining INT,
                             event_data JSONB,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_battle_logs_battle_id
                                 FOREIGN KEY (battle_id) REFERENCES battle_outcomes(id) ON DELETE CASCADE
);

CREATE INDEX idx_battle_logs_battle_id ON battle_logs(battle_id);
CREATE INDEX idx_battle_logs_event_type ON battle_logs(event_type);

