package dev.slesh.vertx.aerospike.client.result;

import com.aerospike.client.Key;

public record ExecuteValue(Key key, Object obj) {
}
