package org.java.diploma.service.battleservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "battle_logs",
        indexes = {
                @Index(name = "idx_battle_logs_battle_id", columnList = "battle_id"),
                @Index(name = "idx_battle_logs_event_type", columnList = "event_type")
        })
public class BattleLog {

    private static final String COLUMN_BATTLE_ID = "battle_id";
    private static final String COLUMN_EVENT_TYPE = "event_type";
    private static final String COLUMN_PIECE_ID = "piece_id";
    private static final String COLUMN_POSITION_FROM = "position_from";
    private static final String COLUMN_POSITION_TO = "position_to";
    private static final String COLUMN_DAMAGE_AMOUNT = "damage_amount";
    private static final String COLUMN_HP_REMAINING = "hp_remaining";
    private static final String COLUMN_EVENT_DATA = "event_data";
    private static final String COLUMN_CREATED_AT = "created_at";

    private static final String JSONB_TYPE = "jsonb";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = COLUMN_BATTLE_ID, nullable = false)
    private Integer battleId;

    @Column(name = COLUMN_EVENT_TYPE, nullable = false)
    private String eventType;

    @Column(name = COLUMN_PIECE_ID)
    private Long pieceId;

    @Column(name = COLUMN_POSITION_FROM)
    private String positionFrom;

    @Column(name = COLUMN_POSITION_TO)
    private String positionTo;

    @Column(name = COLUMN_DAMAGE_AMOUNT)
    private Integer damageAmount;

    @Column(name = COLUMN_HP_REMAINING)
    private Integer hpRemaining;

    @Column(name = COLUMN_EVENT_DATA, columnDefinition = JSONB_TYPE)
    @JdbcTypeCode(SqlTypes.JSON)
    private String eventData;

    @Column(name = COLUMN_CREATED_AT, nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}