package io.github.sps23.spring.scopes;

import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * A real {@code request}-scoped Spring bean.
 *
 * <p>
 * Exactly one instance of this bean exists per HTTP request. Because
 * collaborators (controllers, other singletons) are created once and injected
 * with a reference that must survive across many requests, Spring injects a
 * <strong>scoped proxy</strong> instead of a raw instance — that proxy
 * transparently resolves to the correct request-bound target on every method
 * call. {@code ScopedProxyMode.TARGET_CLASS} tells Spring to build that proxy
 * with CGLIB (subclassing), which works whether or not this class implements an
 * interface.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestTrace {

    private final String traceId = UUID.randomUUID().toString();

    public String traceId() {
        return traceId;
    }
}
