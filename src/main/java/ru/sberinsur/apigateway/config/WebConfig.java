package ru.sberinsur.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.sberinsur.apigateway.cache.RedirectCache;
import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.service.LoggingService;
import ru.sberinsur.apigateway.service.RedirectService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RedirectService redirectService;

    @Autowired
    private RedirectCache redirectCache;

    @Autowired
    private LoggingService loggingService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                String sourcePath = request.getRequestURI();
                RedirectEndpoint redirectEndpoint = redirectCache.getRedirect(sourcePath);

                if (redirectEndpoint != null) {
                    if (redirectEndpoint.isLogging()) {
                        logRequest(request, redirectEndpoint.getServiceName(), "Request_start");
                    }

                    Map<String, String> headers = Collections.list(request.getHeaderNames())
                            .stream()
                            .collect(Collectors.toMap(Function.identity(), request::getHeader));
                    String body = getRequestBody(request);
                    ResponseEntity<byte[]> targetResponse = redirectService.forwardRequest(
                            redirectEndpoint.getTargetUrl(), request.getMethod(), headers, body.getBytes(), redirectEndpoint);

                    if (redirectEndpoint.isLogging()) {
                        loggingService.sendLog( redirectEndpoint.getServiceName(), "Response_finish", new String(Objects.requireNonNull(targetResponse.getBody())));
                    }

                    // Set response details from targetResponse
                    response.setStatus(targetResponse.getStatusCodeValue());
                    targetResponse.getHeaders().forEach((key, value) -> {
                        if (value != null) {
                            value.forEach(v -> {
                                if (v != null && !key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                                    response.addHeader(key, v);
                                }
                            });
                        }
                    });
                    response.getOutputStream().write(targetResponse.getBody());
                    return false;
                }

                return true;
            }

            private String getRequestBody(HttpServletRequest request) {
                String body = (String) request.getAttribute("cachedBody");
                if (body == null) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = request.getReader()) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        body = sb.toString();
                        request.setAttribute("cachedBody", body);
                    } catch (IOException e) {
                        throw new RuntimeException("Error reading request body", e);
                    }
                }
                return body;
            }


            private void logRequest(HttpServletRequest request, String serviceName, String operation) {
                String body = getRequestBody(request);
                loggingService.sendLog(serviceName, operation, body);
            }
        });
    }


}
