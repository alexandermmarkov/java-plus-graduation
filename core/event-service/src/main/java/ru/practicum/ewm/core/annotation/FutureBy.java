package ru.practicum.ewm.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureByValidator.class)
public @interface FutureBy {
    String message() default "Значение должно быть в будущем минимум на указанный интервал";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long amount() default 0;

    ChronoUnit unit() default ChronoUnit.SECONDS;
}