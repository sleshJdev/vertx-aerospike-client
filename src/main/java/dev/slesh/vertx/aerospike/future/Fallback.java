package dev.slesh.vertx.aerospike.future;

import io.vertx.core.Promise;

public interface Fallback {
    <T> Promise<T> get();
}
