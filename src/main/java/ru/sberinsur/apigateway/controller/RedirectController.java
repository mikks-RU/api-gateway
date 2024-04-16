package ru.sberinsur.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.service.AsyncService;
import ru.sberinsur.apigateway.service.RedirectService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/redirects")
public class RedirectController {

    @Autowired
    private RedirectService redirectService;

    @Autowired
    private AsyncService asyncService;

    @GetMapping
    public ResponseEntity<List<RedirectEndpoint>> getAllRedirects() {
        List<RedirectEndpoint> redirects = redirectService.getAllRedirects();
        return ResponseEntity.ok(redirects);
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<RedirectEndpoint>> addRedirect(@RequestBody RedirectEndpoint redirectEndpoint) {
        return asyncService.addRedirectAsync(redirectEndpoint);
    }

}
