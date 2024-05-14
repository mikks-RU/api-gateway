package ru.sberinsur.apigateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Slf4j
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        log.error("Ошибка при вызове внешнего сервиса: " + response.getStatusText());

        // Не считать ответ ошибочным, чтобы избежать исключений
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.METHOD_NOT_ALLOWED) {
            throw new HttpClientErrorException(statusCode, "Method Not Allowed");
        }
        super.handleError(response);
    }
}
