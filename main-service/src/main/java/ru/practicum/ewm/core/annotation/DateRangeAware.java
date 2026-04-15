package ru.practicum.ewm.core.annotation;

import java.time.LocalDateTime;

public interface DateRangeAware {

    LocalDateTime getStart();

    LocalDateTime getEnd();
}
