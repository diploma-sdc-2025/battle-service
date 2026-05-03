-- Seed battle-service test data.

INSERT INTO battle_outcomes (
  match_id,
  round_number,
  attacker_id,
  defender_id,
  winner_id,
  attacker_pieces_remaining,
  defender_pieces_remaining,
  attacker_damage_dealt,
  defender_damage_dealt,
  battle_duration_ms,
  created_at
)
VALUES
  (1001, 1, 101, 102, 101, 3, 0, 180, 90, 11200, CURRENT_TIMESTAMP - INTERVAL '15 minutes'),
  (1001, 2, 102, 101, 101, 1, 2, 75, 130, 9800, CURRENT_TIMESTAMP - INTERVAL '10 minutes')
ON CONFLICT DO NOTHING;

INSERT INTO battle_logs (
  battle_id,
  event_type,
  piece_id,
  position_from,
  position_to,
  damage_amount,
  hp_remaining,
  event_data,
  created_at
)
SELECT b.id, 'move', 1, 'e2', 'e4', NULL, NULL, '{"speed":"normal"}'::jsonb, CURRENT_TIMESTAMP - INTERVAL '14 minutes'
FROM battle_outcomes b
WHERE b.match_id = 1001 AND b.round_number = 1
LIMIT 1;

INSERT INTO battle_logs (
  battle_id,
  event_type,
  piece_id,
  position_from,
  position_to,
  damage_amount,
  hp_remaining,
  event_data,
  created_at
)
SELECT b.id, 'attack', 2, 'd5', 'e4', 35, 65, '{"crit":false}'::jsonb, CURRENT_TIMESTAMP - INTERVAL '13 minutes'
FROM battle_outcomes b
WHERE b.match_id = 1001 AND b.round_number = 1
LIMIT 1;

INSERT INTO piece_interactions (
  battle_id,
  attacker_piece_id,
  defender_piece_id,
  interaction_type,
  created_at
)
SELECT b.id, 2, 1, 'capture', CURRENT_TIMESTAMP - INTERVAL '12 minutes'
FROM battle_outcomes b
WHERE b.match_id = 1001 AND b.round_number = 1
LIMIT 1;
