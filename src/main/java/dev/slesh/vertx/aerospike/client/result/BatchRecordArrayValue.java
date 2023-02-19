package dev.slesh.vertx.aerospike.client.result;

import com.aerospike.client.BatchRecord;

public record BatchRecordArrayValue(BatchRecord[] records, boolean exists) {
}
