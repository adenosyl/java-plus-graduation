package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_similarity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSimilarityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Long timestamp;
}