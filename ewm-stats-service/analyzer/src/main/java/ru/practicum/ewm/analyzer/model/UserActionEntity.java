package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(nullable = false)
    private Long timestamp;
}