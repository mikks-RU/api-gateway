package ru.sberinsur.apigateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.sberinsur.apigateway.model.RedirectEndpoint;

import java.util.Optional;

@Repository
public interface RedirectEndpointRepository extends JpaRepository<RedirectEndpoint, Long> {

    Optional<RedirectEndpoint> findBySourcePath(String sourcePath);
}
