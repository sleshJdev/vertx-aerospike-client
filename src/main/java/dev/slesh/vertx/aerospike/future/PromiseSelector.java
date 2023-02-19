package dev.slesh.vertx.aerospike.future;

import io.vertx.core.Promise;

@FunctionalInterface
public interface PromiseSelector {
    <T> Promise<T> select();
}
