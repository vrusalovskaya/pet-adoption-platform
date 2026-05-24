package com.wise.petadoption.animal.converter;

import com.wise.petadoption.animal.common.Species;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SpeciesConverter implements Converter<String, Species> {

    @Override
    public Species convert(String source) {

        if (source == null || source.isBlank()) {
            return null;
        }

        try {
            return Species.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid species: '" + source + "'. Allowed values: CAT, DOG, OTHER"
            );
        }
    }
}
