package com.wise.petadoption.animal.converter;

import com.wise.petadoption.animal.common.Species;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeciesConverterTest {

    private final SpeciesConverter converter = new SpeciesConverter();

    @Test
    void convert_LowerCaseWithWhitespace_ReturnsMatchingSpecies() {
        assertThat(converter.convert(" dog ")).isEqualTo(Species.DOG);
    }

    @Test
    void convert_UnknownValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> converter.convert("dragon"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid species");
    }
}
