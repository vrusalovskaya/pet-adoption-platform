package com.wise.petadoption.security.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256HashCalculatorTest {

    private final Sha256HashCalculator hashCalculator = new Sha256HashCalculator();

    @Test
    void calculate_KnownInput_ReturnsExpectedSha256Hex() {
        String hash = hashCalculator.calculate("abc");

        assertThat(hash)
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
