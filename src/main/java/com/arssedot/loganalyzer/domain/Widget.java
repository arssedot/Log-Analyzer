package com.arssedot.loganalyzer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "widgets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Widget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private int position;

    /** SMALL=col-md-3, MEDIUM=col-md-4/6, LARGE=col-md-8, FULL=col-md-12 */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String size = "MEDIUM";

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
