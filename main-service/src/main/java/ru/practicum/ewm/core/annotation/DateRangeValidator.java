package ru.practicum.ewm.core.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateRangeValidator implements ConstraintValidator<DateRange, DateRangeAware> {

    private long minMinutes;
    private String defaultMessage;

    @Override
    public void initialize(DateRange annotation) {
        this.minMinutes = annotation.minMinutes();
        this.defaultMessage = annotation.message();
    }

    @Override
    public boolean isValid(DateRangeAware value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDateTime start = value.getStart();
        LocalDateTime end = value.getEnd();

        if (start == null || end == null) {
            return true;
        }

        if (start.isAfter(end)) {
            return false;
        }

        long actualMinutes = ChronoUnit.MINUTES.between(start, end);
        if (actualMinutes < minMinutes) {
            context.buildConstraintViolationWithTemplate("Интервал должен быть не менее {minMinutes} минут")
                    .addConstraintViolation();
            return false;
        }


        return true;
    }
}