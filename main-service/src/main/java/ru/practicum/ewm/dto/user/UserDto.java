package ru.practicum.ewm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "Не заполнено поле name")
    @Size(min = 2, max = 250, message = "Количество символов в name от 2 до 250")
    private String name;

    @Email(message = "Неверный email")
    @NotBlank(message = "Не заполнено поле email")
    @Size(min = 6, max = 254, message = "Количество символов в email от 6 до 254")
    private String email;
}
