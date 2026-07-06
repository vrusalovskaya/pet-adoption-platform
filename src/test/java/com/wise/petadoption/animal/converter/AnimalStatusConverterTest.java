package com.wise.petadoption.animal.converter;

import com.wise.petadoption.animal.common.AnimalStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnimalStatusConverterTest {

    private final AnimalStatusConverter converter = new AnimalStatusConverter();

    @Test
    void convert_LowerCaseWithWhitespace_ReturnsMatchingStatus() {
        assertThat(converter.convert("  available ")).isEqualTo(AnimalStatus.AVAILABLE);
    }

    @Test
    void convert_UnknownValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> converter.convert("flying"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid animal status");
    }
}
