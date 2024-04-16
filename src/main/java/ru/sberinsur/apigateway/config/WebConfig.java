package ru.sberinsur.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.sberinsur.apigateway.service.RedirectService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RedirectService redirectService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                String sourcePath = request.getRequestURI();
                String targetUrl = redirectService.getTargetUrl(sourcePath);

                if (targetUrl != null) {
                    // Копируем заголовки и тело запроса
                    Map<String, String> headers = new HashMap<>();
                    Enumeration<String> headerNames = request.getHeaderNames();
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        headers.put(headerName, request.getHeader(headerName));
                    }

                    byte[] body = getRequestBody(request).getBytes();

                    // Перенаправляем запрос на целевой URL
                    ResponseEntity<byte[]> targetResponseEntity = redirectService.forwardRequest(targetUrl, request.getMethod(), headers, body);

                    // Устанавливаем статус и тело ответа
                    response.setStatus(targetResponseEntity.getStatusCodeValue());

                    // Копируем заголовки ответа
                    HttpHeaders responseHeaders = targetResponseEntity.getHeaders();
                    response.reset();
                    responseHeaders.forEach((key, value) -> {
                        if (value != null) {
                            value.forEach(v -> {
                                if (v != null && !key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                                    response.addHeader(key, v);
                                }
                            });
                        }
                    });

                    // Записываем тело ответа в OutputStream
                    response.getOutputStream().write(targetResponseEntity.getBody());
                    response.getOutputStream().flush();

                    return false;
                }

                // Возвращаем true, чтобы позволить дальнейшую обработку запроса
                return true;
            }
        });
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading request body", e);
        }
    }
}