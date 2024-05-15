package ru.sberinsur.apigateway.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberinsur.apigateway.model.RedirectEndpoint;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, RedirectEndpoint> redirectCaffeineCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .build();
    }
}
