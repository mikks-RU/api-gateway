package ru.sberinsur.apigateway.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedirectEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourcePath; // Например, "api/v1/service"
    private String targetUrl; // Адрес для перенаправления
    private String serviceName; // Название сервиса для логирования
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isLogging; // Нужно ли логирование
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isAuth; // Нужна ли авторизация
    private String authType; // Тип авторизации (например, "Bearer", "Basic", "ApiKey", "Custom")
    private String authValue; // Значение авторизации (например, токен или логин:пароль)
}
