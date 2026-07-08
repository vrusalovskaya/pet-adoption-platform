package com.wise.petadoption.shared.storage.mongo;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MongoStorageCondition implements Condition {
    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
        String type = ctx.getEnvironment().getProperty("storage.type", "mongo");
        return "mongo".equalsIgnoreCase(type);
    }
}
