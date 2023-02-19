package dev.slesh.vertx.aerospike.client.result;

import com.aerospike.client.BatchRecord;

import java.util.List;

public record BatchOperateListValue(List<BatchRecord> records, boolean status) {
}
