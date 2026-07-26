package io.github.sps23.spring.scopes;

/**
 * A plain POJO with <strong>no Spring annotations at all</strong> — this
 * simulates a third-party class you do not own (for example, from an external
 * library) that exposes its own {@code start()}/{@code stop()} lifecycle
 * methods using whatever naming convention its author chose.
 *
 * <p>
 * You cannot annotate this class with {@code @PostConstruct}/
 * {@code @PreDestroy} because you cannot edit its source, and it does not
 * implement Spring's {@code InitializingBean}/{@code DisposableBean}. The fix
 * is to register it as a bean with
 * {@code @Bean(initMethod = "start", destroyMethod = "stop")} — see
 * {@link ScopesLifecycleConfig}.
 */
public class EmbeddedMessageBroker {

    private boolean running;

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
