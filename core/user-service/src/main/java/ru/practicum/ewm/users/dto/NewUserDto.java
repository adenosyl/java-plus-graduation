package ru.practicum.ewm.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "email")
@Builder
public class NewUserDto {

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(min = 6, max = 254, message = "Email должен содержать от 6 до 254 символов")
    private String email;

}
