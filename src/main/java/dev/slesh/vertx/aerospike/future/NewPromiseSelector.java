package dev.slesh.vertx.aerospike.future;

import io.vertx.core.Promise;

public class NewPromiseSelector implements PromiseSelector {
    @Override
    public <T> Promise<T> select() {
        return Promise.promise();
    }
}
