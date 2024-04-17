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
        return ResponseEntity.ok(redirectService.getAllRedirects());
    }

    @PutMapping
    public ResponseEntity<String> addRedirect(@RequestBody RedirectEndpoint redirectEndpoint) {
        return redirectService.addRedirect(redirectEndpoint);
    }

    @PatchMapping
    public ResponseEntity<String> updateRedirect(@RequestBody RedirectEndpoint redirectEndpoint) {
        return redirectService.updateRedirect(redirectEndpoint);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteRedirect(@RequestParam String sourcePath) {
        return redirectService.deleteRedirect(sourcePath);
    }

}
