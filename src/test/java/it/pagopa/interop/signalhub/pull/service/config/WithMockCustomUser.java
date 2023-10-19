package it.pagopa.interop.signalhub.pull.service.config;



import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = SecurityTestConfig.WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
}