package ru.practicum.ewm.core.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FutureByValidator implements ConstraintValidator<FutureBy, LocalDateTime> {

    private long amount;
    private ChronoUnit unit;

    @Override
    public void initialize(FutureBy constraintAnnotation) {
        this.amount = constraintAnnotation.amount();
        this.unit = constraintAnnotation.unit();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minFuture = now.plus(amount, unit);
        return !value.isBefore(minFuture);
    }
}