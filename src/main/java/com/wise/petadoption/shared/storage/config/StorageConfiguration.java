package com.wise.petadoption.shared.storage.config;

import com.wise.petadoption.shared.storage.contract.ImageStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

    @Bean
    @ConditionalOnMissingBean(ImageStorage.class)
    public ImageStorage failFast() {
        throw new IllegalStateException(
                "No ImageStorage configured. Set storage.type=mongo|minio");
    }
}
