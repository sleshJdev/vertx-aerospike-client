package dev.slesh.vertx.aerospike.client.result;

import com.aerospike.client.Key;

public record DeleteValue(Key key, boolean existed) {
}
