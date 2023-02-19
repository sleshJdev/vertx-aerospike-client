package dev.slesh.vertx.aerospike.future;

import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;

import java.util.Objects;

public class ContextPromiseSelector implements PromiseSelector {
    private final Fallback fallback;

    public ContextPromiseSelector() {
        this(Promise::promise);
    }

    public ContextPromiseSelector(final Fallback fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public <T> Promise<T> select() {
        final var context = ContextInternal.current();
        return context != null ? context.promise() : fallback.get();
    }
}
