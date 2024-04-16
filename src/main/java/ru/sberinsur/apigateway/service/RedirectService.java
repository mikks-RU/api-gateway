package ru.sberinsur.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.sberinsur.apigateway.model.RedirectEndpoint;
import ru.sberinsur.apigateway.repository.RedirectEndpointRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RedirectService {

    @Autowired
    private RedirectEndpointRepository redirectEndpointRepository;

    public List<RedirectEndpoint> getAllRedirects() {
        return redirectEndpointRepository.findAll();
    }

    public RedirectEndpoint getRedirectById(Long id) {
        return redirectEndpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Redirect not found with id: " + id));
    }

    public RedirectEndpoint addRedirect(RedirectEndpoint redirectEndpoint) {
        return redirectEndpointRepository.save(redirectEndpoint);
    }

    public RedirectEndpoint updateRedirect(Long id, RedirectEndpoint redirectEndpoint) {
        RedirectEndpoint existingRedirect = getRedirectById(id);
        existingRedirect.setSourcePath(redirectEndpoint.getSourcePath());
        existingRedirect.setTargetUrl(redirectEndpoint.getTargetUrl());
        return redirectEndpointRepository.save(existingRedirect);
    }

    public void deleteRedirect(Long id) {
        redirectEndpointRepository.deleteById(id);
    }

    public String getTargetUrl(String sourcePath) {
        Optional<RedirectEndpoint> redirect = redirectEndpointRepository.findBySourcePath(sourcePath);
        return redirect.map(RedirectEndpoint::getTargetUrl).orElse(null);
    }
}
