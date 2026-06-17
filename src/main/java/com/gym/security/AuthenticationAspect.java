package com.gym.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthenticationAspect {

    @Before("@annotation(com.gym.security.RequiresAuthentication)")
    public void checkAuth() {
        SecurityContext.getCurrentUsername(); // throws if not authenticated
    }

}
