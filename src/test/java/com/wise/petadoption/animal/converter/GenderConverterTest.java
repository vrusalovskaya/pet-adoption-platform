package com.wise.petadoption.animal.converter;

import com.wise.petadoption.animal.common.Gender;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenderConverterTest {

    private final GenderConverter converter = new GenderConverter();

    @Test
    void convert_LowerCaseWithWhitespace_ReturnsMatchingGender() {
        assertThat(converter.convert(" female ")).isEqualTo(Gender.FEMALE);
    }

    @Test
    void convert_UnknownValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> converter.convert("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid gender");
    }
}
