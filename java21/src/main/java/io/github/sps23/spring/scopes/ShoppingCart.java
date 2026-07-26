package io.github.sps23.spring.scopes;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * A real {@code session}-scoped Spring bean.
 *
 * <p>
 * Exactly one instance exists per HTTP session, shared by every request that
 * belongs to that session — a natural fit for something like a shopping cart.
 * As with {@link RequestTrace}, a {@code TARGET_CLASS} scoped proxy is injected
 * into longer-lived collaborators so the correct session-bound target is
 * resolved lazily on every call.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart {

    private final List<String> items = new ArrayList<>();

    public void addItem(String item) {
        items.add(item);
    }

    public List<String> items() {
        return List.copyOf(items);
    }
}
