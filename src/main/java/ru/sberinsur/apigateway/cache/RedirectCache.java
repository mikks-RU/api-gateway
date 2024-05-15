package ru.sberinsur.apigateway.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Cache;
import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.repository.RedirectEndpointRepository;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class RedirectCache {

    @Autowired
    private RedirectEndpointRepository redirectEndpointRepository;

    @Autowired
    @Qualifier("redirectCaffeineCache")
    private Cache<String, RedirectEndpoint> caffeineCache;

    @PostConstruct
    public void setupCache() {
        loadRedirects();
    }

    public void loadRedirects() {
        List<RedirectEndpoint> redirectEndpoints = redirectEndpointRepository.findAll();
        redirectEndpoints.forEach(endpoint -> caffeineCache.put(endpoint.getSourcePath(), endpoint));
    }

    @Cacheable(cacheNames = "redirects", key = "#sourcePath")
    public RedirectEndpoint getRedirect(String sourcePath) {
        return caffeineCache.getIfPresent(sourcePath);
    }
}
