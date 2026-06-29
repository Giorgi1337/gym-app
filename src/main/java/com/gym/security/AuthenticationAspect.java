package com.gym.security;

import com.gym.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthenticationAspect {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationAspect.class);

    private final AuthenticationService authenticationService;

    public AuthenticationAspect(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Before("@annotation(com.gym.security.RequiresAuthentication)")
    public void checkAuth() {
        HttpServletRequest request = currentRequest();

        String username = request.getHeader("X-Username");
        String password = request.getHeader("X-Password");

        log.debug("Checking authentication for {} {} username={}", request.getMethod(), request.getRequestURI(), username);

        authenticationService.login(username, password);

        log.debug("Authentication check passed for {} {}", request.getMethod(), request.getRequestURI());
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }
}