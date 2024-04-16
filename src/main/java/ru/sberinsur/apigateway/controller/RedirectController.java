package ru.sberinsur.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.service.RedirectService;

import java.util.List;

@RestController
@RequestMapping("/api/redirects")
public class RedirectController {

    @Autowired
    private RedirectService redirectService;

    @GetMapping
    public ResponseEntity<List<RedirectEndpoint>> getAllRedirects() {
        List<RedirectEndpoint> redirects = redirectService.getAllRedirects();
        return ResponseEntity.ok(redirects);
    }

    @PostMapping
    public ResponseEntity<RedirectEndpoint> addRedirect(@RequestBody RedirectEndpoint redirectEndpoint) {
        RedirectEndpoint newRedirect = redirectService.addRedirect(redirectEndpoint);
        return ResponseEntity.ok(newRedirect);
    }

    // Дополнительные методы для обновления и удаления редиректов могут быть добавлены по аналогии

}
