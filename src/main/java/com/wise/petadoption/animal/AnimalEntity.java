package com.wise.petadoption.animal;

import com.wise.petadoption.shelter.persistence.ShelterEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "catalog", name = "animals")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnimalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id", nullable = false)
    private ShelterEntity shelterEntity;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    @Column(length = 50)
    private String breed;

    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnimalStatus status;

    @Embedded
    private PhotoMetadata photoMetadata;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
