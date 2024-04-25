package ru.sberinsur.apigateway.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.repository.RedirectEndpointRepository;

import java.io.IOException;
import java.util.ArrayList;
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

    @Async
    public CompletableFuture<ResponseEntity<byte[]>> forwardRequestAsync(String targetUrl, String method, Map<String, String> headers, byte[] body, RedirectEndpoint endpoint) {

        if (endpoint.isLogging()) {
            loggingService.sendLog(endpoint.getServiceName(), "Request_out", new String(body));
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        HttpMethod httpMethod = HttpMethod.valueOf(method);
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
                return false;  // Always return false to avoid throwing exceptions
            }

            @Override
            public void handleError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
                // Do nothing, as we want to handle all responses without exceptions
            }
        });

        ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, httpMethod, httpEntity, byte[].class);

        if (endpoint.isLogging()) {
            loggingService.sendLog(endpoint.getServiceName(), "Response", new String(response.getBody()));
        }

        return CompletableFuture.completedFuture(response);
    }

    @Transactional
    public ResponseEntity<String> addRedirect(RedirectEndpoint redirectEndpoint) {
        Optional<RedirectEndpoint> existingEndpoint = redirectEndpointRepository.findBySourcePath(redirectEndpoint.getSourcePath());
        if (existingEndpoint.isPresent()) {
            return ResponseEntity.badRequest().body("Redirect already exists");
        } else {
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
