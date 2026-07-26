package io.github.sps23.spring.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Proves that {@code request} and {@code session} scoped beans resolve a fresh
 * (request) or shared-within-session (session) target through their
 * {@code TARGET_CLASS} scoped proxies.
 *
 * <p>
 * A real {@link AnnotationConfigWebApplicationContext} is used together with
 * {@link RequestContextHolder} and mock servlet request/session objects from
 * {@code spring-test}, exactly as Spring MVC does for every real HTTP request.
 */
class WebScopedBeansTest {

    private AnnotationConfigWebApplicationContext context;

    @BeforeEach
    void startWebContext() {
        // AnnotationConfigWebApplicationContext, backed by a mock ServletContext,
        // registers the "request" and "session" scopes automatically and fully
        // processes @Configuration/@ComponentScan - exactly like a real
        // Spring MVC deployment.
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(ScopesLifecycleConfig.class);
        context.refresh();
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
        context.close();
    }

    @Test
    @DisplayName("each HTTP request resolves its own RequestTrace instance")
    void eachRequestGetsItsOwnRequestTrace() {
        var firstRequestTraceId = withNewRequest(
                () -> context.getBean(RequestTrace.class).traceId());
        var secondRequestTraceId = withNewRequest(
                () -> context.getBean(RequestTrace.class).traceId());

        assertNotEquals(firstRequestTraceId, secondRequestTraceId);
    }

    @Test
    @DisplayName("a session-scoped ShoppingCart is shared across requests in the same session")
    void shoppingCartIsSharedAcrossRequestsInTheSameSession() {
        var session = new MockHttpSession();

        withRequestInSession(session, () -> {
            context.getBean(ShoppingCart.class).addItem("keyboard");
            return null;
        });

        var itemsSeenOnSecondRequest = withRequestInSession(session,
                () -> context.getBean(ShoppingCart.class).items());

        assertEquals(List.of("keyboard"), itemsSeenOnSecondRequest);
    }

    private <T> T withNewRequest(java.util.function.Supplier<T> action) {
        return withRequestInSession(new MockHttpSession(), action);
    }

    private <T> T withRequestInSession(MockHttpSession session,
            java.util.function.Supplier<T> action) {
        var request = new MockHttpServletRequest();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            return action.get();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
