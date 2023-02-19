package dev.slesh.vertx.aerospike.client.result;

import com.aerospike.client.Key;

public record ExistsArrayValue(Key[] keys, boolean[] exists) {
}
