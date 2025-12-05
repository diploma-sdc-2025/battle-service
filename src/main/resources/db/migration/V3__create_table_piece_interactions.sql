CREATE TABLE piece_interactions (
                                    id SERIAL PRIMARY KEY,
                                    battle_id INT NOT NULL,
                                    attacker_piece_id BIGINT NOT NULL,
                                    defender_piece_id BIGINT NOT NULL,
                                    interaction_type VARCHAR(50) NOT NULL,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_piece_interactions_battle_id
                                        FOREIGN KEY (battle_id) REFERENCES battle_outcomes(id) ON DELETE CASCADE
);

CREATE INDEX idx_piece_interactions_battle_id ON piece_interactions(battle_id);
CREATE INDEX idx_piece_interactions_attacker ON piece_interactions(attacker_piece_id);
CREATE INDEX idx_piece_interactions_defender ON piece_interactions(defender_piece_id);
CREATE INDEX idx_piece_interactions_type ON piece_interactions(interaction_type);
CREATE INDEX idx_piece_interactions_created_at ON piece_interactions(created_at DESC);
CREATE INDEX idx_piece_interactions_pieces ON piece_interactions(attacker_piece_id, defender_piece_id, interaction_type);
