package ru.sberinsur.apigateway.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.sberinsur.apigateway.exception.CustomResponseErrorHandler;
import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.repository.RedirectEndpointRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class RedirectService {
    @Autowired
    private LoggingService loggingService;

    @Autowired
    private RedirectEndpointRepository redirectEndpointRepository;

    @Autowired
    @Qualifier("redirectCaffeineCache")
    private Cache<String, RedirectEndpoint> caffeineCache;

    public List<RedirectEndpoint> getAllRedirects() {
        return new ArrayList<>(caffeineCache.asMap().values());
    }

    public CompletableFuture<ResponseEntity<byte[]>> forwardRequestAsync(String targetUrl, String method, Map<String, String> headers, byte[] body, RedirectEndpoint endpoint) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        // Добавление аутентификационного заголовка, если требуется авторизация
        if (endpoint.isAuth() && endpoint.getAuthType() != null && endpoint.getAuthValue() != null) {
            String authHeaderValue = getAuthHeaderValue(endpoint);
            if (authHeaderValue != null) {
                httpHeaders.add(HttpHeaders.AUTHORIZATION, authHeaderValue);
            }
        }

        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, httpHeaders);
        RestTemplate restTemplate = restTemplate();

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, HttpMethod.resolve(method), httpEntity, byte[].class);
            if (endpoint.isLogging()) {
                loggingService.sendLog(endpoint.getServiceName(), "Response", new String(response.getBody()));
            }
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Error forwarding request", e);
            return CompletableFuture.completedFuture(createErrorResponse("Service unavailable", endpoint.getSourcePath(), HttpStatus.SERVICE_UNAVAILABLE));
        }
    }

    private String getAuthHeaderValue(RedirectEndpoint endpoint) {
        switch (endpoint.getAuthType()) {
            case "Bearer":
                return "Bearer " + endpoint.getAuthValue();
            case "Basic":
                return "Basic " + endpoint.getAuthValue();
            default:
                return null;
        }
    }

    private ResponseEntity<byte[]> createErrorResponse(String message, String path, HttpStatus status) {
        LocalDateTime now = LocalDateTime.now();
        String formattedTimestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));

        String errorJson = String.format("{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"path\":\"%s\"}",
                formattedTimestamp, status.value(), message, path);
        byte[] responseBody = errorJson.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        return new ResponseEntity<>(responseBody, headers, status);
    }

    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        return restTemplate;
    }

    @Transactional
    public ResponseEntity<String> addRedirect(RedirectEndpoint redirectEndpoint) {
        Optional<RedirectEndpoint> existingEndpoint = redirectEndpointRepository.findBySourcePath(redirectEndpoint.getSourcePath());
        if (existingEndpoint.isPresent()) {
            return ResponseEntity.badRequest().body("Redirect already exists");
        } else {
            if (redirectEndpoint.isAuth() && "Basic".equals(redirectEndpoint.getAuthType())) {
                redirectEndpoint.setAuthValue(Base64.getEncoder().encodeToString(redirectEndpoint.getAuthValue().getBytes()));
            }
            RedirectEndpoint savedEndpoint = redirectEndpointRepository.save(redirectEndpoint);
            caffeineCache.put(redirectEndpoint.getSourcePath(), savedEndpoint);
            return ResponseEntity.ok("Redirect added successfully");
        }
    }

    @Transactional
    public ResponseEntity<String> updateRedirect(RedirectEndpoint redirectEndpoint) {
        Optional<RedirectEndpoint> existingEndpoint = redirectEndpointRepository.findBySourcePath(redirectEndpoint.getSourcePath());
        if (existingEndpoint.isPresent()) {
            redirectEndpoint.setId(existingEndpoint.get().getId());
            if (redirectEndpoint.isAuth() && "Basic".equals(redirectEndpoint.getAuthType())) {
                redirectEndpoint.setAuthValue(Base64.getEncoder().encodeToString(redirectEndpoint.getAuthValue().getBytes()));
            }
            RedirectEndpoint updatedEndpoint = redirectEndpointRepository.save(redirectEndpoint);
            caffeineCache.put(redirectEndpoint.getSourcePath(), updatedEndpoint);
            return ResponseEntity.ok("Redirect updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Redirect does not exist");
        }
    }

    @Transactional
    public ResponseEntity<String> deleteRedirect(String sourcePath) {
        Optional<RedirectEndpoint> existingEndpoint = redirectEndpointRepository.findBySourcePath(sourcePath);
        if (existingEndpoint.isPresent()) {
            redirectEndpointRepository.deleteBySourcePath(sourcePath);
            caffeineCache.invalidate(sourcePath);
            return ResponseEntity.ok("Redirect deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Redirect does not exist");
        }
    }
}
