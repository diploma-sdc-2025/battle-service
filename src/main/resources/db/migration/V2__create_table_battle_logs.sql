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
                                 FOREIGN KEY (battle_id) REFERENCES battle_outcomes(id) ON DELETE CASCADE,

                             CONSTRAINT chk_battle_logs_event_type CHECK (
                                 event_type IN (
                                                'BATTLE_START', 'BATTLE_END',
                                                'PIECE_MOVED', 'PIECE_DIED'
                                     )
                                 ),
                             CONSTRAINT chk_battle_logs_damage CHECK (damage_amount IS NULL OR damage_amount >= 0),
                             CONSTRAINT chk_battle_logs_hp CHECK (hp_remaining IS NULL OR hp_remaining >= 0),
                             CONSTRAINT chk_battle_logs_position_format CHECK (
                                 (position_from IS NULL OR position_from ~ '^[0-7],[0-7]$') AND
                                 (position_to IS NULL OR position_to ~ '^[0-7],[0-7]$')
                                 )
);

CREATE INDEX idx_battle_logs_battle_id ON battle_logs(battle_id);
CREATE INDEX idx_battle_logs_event_type ON battle_logs(event_type);
CREATE INDEX idx_battle_logs_piece_id ON battle_logs(piece_id);
CREATE INDEX idx_battle_logs_created_at ON battle_logs(battle_id, created_at ASC);

CREATE INDEX idx_battle_logs_event_data ON battle_logs USING GIN (event_data);
