package ru.sberinsur.apigateway.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("redirectEndpointCache")
    private Cache<String, RedirectEndpoint> redirectCache;

    @PostConstruct
    public void setupCache() {
        loadRedirects();
    }

    public void loadRedirects() {
        List<RedirectEndpoint> redirectEndpoints = redirectEndpointRepository.findAll();
        redirectEndpoints.forEach(endpoint -> redirectCache.put(endpoint.getSourcePath(), endpoint));
    }

    public RedirectEndpoint getRedirect(String sourcePath) {
        return redirectCache.getIfPresent(sourcePath);
    }

    public void updateRedirect(RedirectEndpoint redirectEndpoint) {
        redirectCache.put(redirectEndpoint.getSourcePath(), redirectEndpoint);
        redirectEndpointRepository.save(redirectEndpoint);
    }

    public void removeRedirect(String sourcePath) {
        redirectCache.invalidate(sourcePath);
        redirectEndpointRepository.deleteBySourcePath(sourcePath);
    }
}
