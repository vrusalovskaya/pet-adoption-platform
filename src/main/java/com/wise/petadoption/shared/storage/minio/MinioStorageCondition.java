package com.wise.petadoption.shared.storage.minio;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MinioStorageCondition implements Condition {
    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
        return "minio".equalsIgnoreCase(
                ctx.getEnvironment().getProperty("storage.type"));
    }
}
