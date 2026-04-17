package ru.practicum.ewm.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.core.interfaceValidation.CreateValidation;
import ru.practicum.ewm.core.interfaceValidation.UpdateValidation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateDto {

    @NotNull(message = "Идентификатор события обязателен к заполнению", groups = CreateValidation.class)
    private Long eventId;

    @NotBlank(message = "Не заполнен комментарий", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(max = 2000, message = "Количество символов в комментарии не более 2000",
            groups = {CreateValidation.class, UpdateValidation.class})
    private String text;

    private Boolean deleted = false;

    private Boolean isAdmin = false;
}
