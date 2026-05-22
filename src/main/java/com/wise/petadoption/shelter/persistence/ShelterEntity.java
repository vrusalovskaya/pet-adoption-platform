package com.wise.petadoption.shelter.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "catalog", name = "shelters")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShelterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String city;

    private String address;

    @Column(nullable = false)
    private String contactEmail;

    @Column(length = 20)
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
