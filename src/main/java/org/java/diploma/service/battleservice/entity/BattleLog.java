package org.java.diploma.service.battleservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "battle_logs",
        indexes = {
                @Index(name = "idx_battle_logs_battle_id", columnList = "battle_id"),
                @Index(name = "idx_battle_logs_event_type", columnList = "event_type")
        })
public class BattleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "battle_id", nullable = false)
    private Integer battleId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "piece_id")
    private Long pieceId;

    @Column(name = "position_from")
    private String positionFrom;

    @Column(name = "position_to")
    private String positionTo;

    @Column(name = "damage_amount")
    private Integer damageAmount;

    @Column(name = "hp_remaining")
    private Integer hpRemaining;

    @Column(name = "event_data", columnDefinition = "jsonb")
    private String eventData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
