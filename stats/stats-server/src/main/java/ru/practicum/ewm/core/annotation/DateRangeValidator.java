package ru.practicum.ewm.core.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateRangeValidator implements ConstraintValidator<DateRange, DateRangeAware> {

    private String defaultMessage;

    @Override
    public void initialize(DateRange annotation) {
        this.defaultMessage = annotation.message();
    }

    @Override
    public boolean isValid(DateRangeAware value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDateTime start = value.getStart();
        LocalDateTime end = value.getEnd();

        if (start != null && end != null && start.isAfter(end)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(defaultMessage).addConstraintViolation();
            return false;
        }

        return true;
    }
}