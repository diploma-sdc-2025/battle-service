package org.java.diploma.service.battleservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "battle_outcomes",
        indexes = {
                @Index(name = "idx_battle_outcomes_match_id", columnList = "match_id"),
                @Index(name = "idx_battle_outcomes_round", columnList = "match_id, round_number")
        })
public class BattleOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "attacker_id", nullable = false)
    private Long attackerId;

    @Column(name = "defender_id", nullable = false)
    private Long defenderId;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "attacker_pieces_remaining", nullable = false)
    private int attackerPiecesRemaining;

    @Column(name = "defender_pieces_remaining", nullable = false)
    private int defenderPiecesRemaining;

    @Column(name = "attacker_damage_dealt", nullable = false)
    private int attackerDamageDealt;

    @Column(name = "defender_damage_dealt", nullable = false)
    private int defenderDamageDealt;

    @Column(name = "battle_duration_ms", nullable = false)
    private int battleDurationMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
