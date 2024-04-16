package ru.sberinsur.apigateway.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.repository.RedirectEndpointRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RedirectCache {

    @Autowired
    private RedirectEndpointRepository redirectEndpointRepository;

    private Map<String, RedirectEndpoint> redirectCache = new HashMap<>();

    @PostConstruct
    public void loadRedirects() {
        List<RedirectEndpoint> redirectEndpoints = redirectEndpointRepository.findAll();
        redirectEndpoints.forEach(endpoint -> redirectCache.put(endpoint.getSourcePath(), endpoint));
    }

    public RedirectEndpoint getRedirect(String sourcePath) {
        return redirectCache.get(sourcePath);
    }

    public void updateRedirect(RedirectEndpoint redirectEndpoint) {
        redirectCache.put(redirectEndpoint.getSourcePath(), redirectEndpoint);
    }

    public void removeRedirect(String sourcePath) {
        redirectCache.remove(sourcePath);
    }
}