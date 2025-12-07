package com.medilab.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAuthenticatedUserSecurityContextFactory.class)
public @interface WithMockAuthenticatedUser {

    long id() default 1L;

    long labId() default 1L;

    String username() default "testuser";

    String password() default "password";

    String[] roles() default {"Staff"};
}
