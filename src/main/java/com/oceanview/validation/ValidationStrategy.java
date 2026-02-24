package com.oceanview.validation;

import java.util.Map;

public interface ValidationStrategy<T> {
    //Validate a DTO and return a field -> error message.
    Map<String, String> validate(T dto);
}
