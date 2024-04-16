package ru.sberinsur.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.repository.RedirectEndpointRepository;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {

    @Autowired
    private RedirectEndpointRepository redirectEndpointRepository;

    @Async
    public CompletableFuture<ResponseEntity<RedirectEndpoint>> addRedirectAsync(RedirectEndpoint redirectEndpoint) {
        RedirectEndpoint savedEndpoint = redirectEndpointRepository.save(redirectEndpoint);
        return CompletableFuture.completedFuture(ResponseEntity.ok(savedEndpoint));
    }
}
