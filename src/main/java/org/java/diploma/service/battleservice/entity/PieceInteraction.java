package org.java.diploma.service.battleservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "piece_interactions",
        indexes = {
                @Index(name = "idx_piece_interactions_battle_id", columnList = "battle_id"),
                @Index(name = "idx_piece_interactions_attacker", columnList = "attacker_piece_id")
        })
public class PieceInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "battle_id", nullable = false)
    private Integer battleId;

    @Column(name = "attacker_piece_id", nullable = false)
    private Long attackerPieceId;

    @Column(name = "defender_piece_id", nullable = false)
    private Long defenderPieceId;

    @Column(name = "interaction_type", nullable = false)
    private String interactionType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
