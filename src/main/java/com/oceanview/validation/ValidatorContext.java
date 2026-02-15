package com.oceanview.validation;

import java.util.HashMap;
import java.util.Map;

public class ValidatorContext {
    private final Map<Class<?>, ValidationStrategy<?>> strategies = new HashMap<>();

    public <T> void register(Class<T> dtoClass, ValidationStrategy<T> strategy) {
        strategies.put(dtoClass, strategy);
    }

    @SuppressWarnings("unchecked")
    public <T> Map<String, String> validate(T dto, Class<T> dtoClass) {
        ValidationStrategy<T> strategy = (ValidationStrategy<T>) strategies.get(dtoClass);

        if (strategy == null) {
            throw new IllegalStateException("No validation strategy registered for: " + dtoClass.getName());
        }
        return strategy.validate(dto);
    }
}
