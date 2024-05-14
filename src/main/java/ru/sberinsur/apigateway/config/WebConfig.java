package ru.sberinsur.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
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
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
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
                    String body = getRequestBody(request);

                    if (redirectEndpoint.isLogging()) {
                        logRequest(body, redirectEndpoint.getServiceName(), "Request");
                    }

                    Map<String, String> headers = Collections.list(request.getHeaderNames())
                            .stream()
                            .collect(Collectors.toMap(Function.identity(), request::getHeader));
                    redirectService.forwardRequestAsync(
                                    redirectEndpoint.getTargetUrl(), request.getMethod(), headers, body.getBytes(), redirectEndpoint)
                            .thenAccept(res -> {
                                setResponseDetails(response, res);
                            });

                    // Apply cache control
                    if (!response.containsHeader(HttpHeaders.CACHE_CONTROL)) {
                        CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic();
                        response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControl.getHeaderValue());
                    }

                    return false;
                }

                return true;
            }

            private String getRequestBody(HttpServletRequest request) {
                StringBuilder sb = new StringBuilder();
                try (Scanner scanner = new Scanner(request.getInputStream())) {
                    while (scanner.hasNextLine()) {
                        sb.append(scanner.nextLine());
                    }
                } catch (Exception e) {
                    log.error("Error reading request body", e);
                }
                return sb.toString();
            }

            private void setResponseDetails(HttpServletResponse response, ResponseEntity<byte[]> res) {
                response.setStatus(res.getStatusCodeValue());
                res.getHeaders().forEach((key, value) -> {
                    if (value != null) {
                        value.forEach(v -> {
                            if (!key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                                response.addHeader(key, v);
                            }
                        });
                    }
                });

                try {
                    response.getOutputStream().write(res.getBody());
                } catch (Exception e) {
                    log.error("Failed to write response body", e);
                }
            }

            private void logRequest(String body, String serviceName, String operation) {
                loggingService.sendLog(serviceName, operation, body);
            }
        });
    }
}
