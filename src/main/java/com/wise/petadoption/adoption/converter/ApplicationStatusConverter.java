package com.wise.petadoption.adoption.converter;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatusConverter implements Converter<String, ApplicationStatus> {

    @Override
    public ApplicationStatus convert(String source) {

        if (source == null || source.isBlank()) {
            return null;
        }

        try {
            return ApplicationStatus.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid application status: '" + source + "'. Allowed values: PENDING, APPROVED, REJECTED, CANCELLED"
            );
        }
    }
}
