package com.wise.petadoption.adoption.converter;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationStatusConverterTest {

    private final ApplicationStatusConverter converter = new ApplicationStatusConverter();

    @Test
    void convert_LowerCaseWithWhitespace_ReturnsMatchingStatus() {
        assertThat(converter.convert(" pending ")).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void convert_UnknownValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> converter.convert("archived"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid application status");
    }
}
