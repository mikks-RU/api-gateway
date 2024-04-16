package ru.sberinsur.apigateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.repository.RedirectEndpointRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class RedirectService {

    @Autowired
    private RedirectEndpointRepository redirectEndpointRepository;

    public List<RedirectEndpoint> getAllRedirects() {
        return redirectEndpointRepository.findAll();
    }

    public RedirectEndpoint addRedirect(RedirectEndpoint redirectEndpoint) {
        return redirectEndpointRepository.save(redirectEndpoint);
    }

    public String getTargetUrl(String sourcePath) {
        Optional<RedirectEndpoint> redirect = redirectEndpointRepository.findBySourcePath(sourcePath);
        return redirect.map(RedirectEndpoint::getTargetUrl).orElse(null);
    }

    public ResponseEntity<byte[]> forwardRequest(String targetUrl, String method, Map<String, String> headers, byte[] body) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        HttpMethod httpMethod = HttpMethod.valueOf(method);
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
                return false; // Всегда возвращаем false, чтобы не бросать исключения
            }

            @Override
            public void handleError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
                // Делаем ничего, так как мы хотим обрабатывать все ответы без исключений
            }
        });

        try {
            return restTemplate.exchange(targetUrl, httpMethod, httpEntity, byte[].class);
        } catch (Exception e) {
            // Если произошла любая ошибка, возвращаем ответ с HTTP-статусом 500 и телом ошибки
            return new ResponseEntity<>(e.getMessage().getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}