package dev.slesh.vertx.aerospike.client.result;

import com.aerospike.client.Key;
import com.aerospike.client.Record;

public record RecordArrayValue(Key[] keys, Record[] records) {
}
