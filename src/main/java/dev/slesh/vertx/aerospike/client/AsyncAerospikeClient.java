package dev.slesh.vertx.aerospike.client;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.BatchRead;
import com.aerospike.client.BatchRecord;
import com.aerospike.client.Bin;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Value;
import com.aerospike.client.async.AsyncIndexTask;
import com.aerospike.client.cluster.Node;
import com.aerospike.client.policy.BatchDeletePolicy;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.BatchUDFPolicy;
import com.aerospike.client.policy.BatchWritePolicy;
import com.aerospike.client.policy.InfoPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.IndexCollectionType;
import com.aerospike.client.query.IndexType;
import dev.slesh.vertx.aerospike.client.result.*;
import io.vertx.core.Future;

import java.util.List;
import java.util.Map;

public interface AsyncAerospikeClient {
    IAerospikeClient delegate();

    default Future<Key> put(Key key, Bin... bins) throws AerospikeException {
        return put(null, key, bins);
    }

    Future<Key> put(WritePolicy policy, Key key, Bin... bins) throws AerospikeException;

    default Future<Key> append(Key key, Bin... bins) throws AerospikeException {
        return append(null, key, bins);
    }

    Future<Key> append(WritePolicy policy, Key key, Bin... bins) throws AerospikeException;

    default Future<Key> prepend(Key key, Bin... bins) throws AerospikeException {
        return prepend(null, key, bins);
    }

    Future<Key> prepend(WritePolicy policy, Key key, Bin... bins) throws AerospikeException;

    default Future<Key> add(Key key, Bin... bins) throws AerospikeException {
        return add(null, key, bins);
    }

    Future<Key> add(WritePolicy policy, Key key, Bin... bins) throws AerospikeException;

    default Future<DeleteValue> delete(Key key) throws AerospikeException {
        return delete(null, key);
    }

    Future<DeleteValue> delete(WritePolicy policy, Key key) throws AerospikeException;

    default Future<BatchRecordArrayValue> delete(Key[] keys) throws AerospikeException {
        return delete(null, null, keys);
    }

    Future<BatchRecordArrayValue> delete(BatchPolicy batchPolicy, BatchDeletePolicy deletePolicy, Key[] keys) throws AerospikeException;

    default Future<Key> touch(Key key) throws AerospikeException {
        return touch(null, key);
    }

    Future<Key> touch(WritePolicy policy, Key key) throws AerospikeException;

    default Future<Boolean> exists(Key key) throws AerospikeException {
        return exists(null, key);
    }

    Future<Boolean> exists(Policy policy, Key key) throws AerospikeException;

    default Future<ExistsArrayValue> exists(Key[] keys) throws AerospikeException {
        return exists(null, keys);
    }

    Future<ExistsArrayValue> exists(BatchPolicy policy, Key[] keys) throws AerospikeException;

    default Future<ReadValue> get(Key key) throws AerospikeException {
        return get(null, key);
    }

    Future<ReadValue> get(Policy policy, Key key) throws AerospikeException;

    default Future<ReadValue> get(Key key, String... binNames) throws AerospikeException {
        return get(null, key, binNames);
    }

    Future<ReadValue> get(Policy policy, Key key, String... binNames) throws AerospikeException;

    default Future<ReadValue> getHeader(Key key) throws AerospikeException {
        return getHeader(null, key);
    }

    Future<ReadValue> getHeader(Policy policy, Key key) throws AerospikeException;

    default Future<List<BatchRead>> get(List<BatchRead> records) throws AerospikeException {
        return get(null, records);
    }

    Future<List<BatchRead>> get(BatchPolicy policy, List<BatchRead> records) throws AerospikeException;

    default Future<RecordArrayValue> get(Key[] keys) throws AerospikeException {
        return get(null, keys);
    }

    Future<RecordArrayValue> get(BatchPolicy policy, Key[] keys) throws AerospikeException;

    default Future<RecordArrayValue> get(Key[] keys, String... binNames) throws AerospikeException {
        return get(null, keys, binNames);
    }

    Future<RecordArrayValue> get(BatchPolicy policy, Key[] keys, String... binNames) throws AerospikeException;

    default Future<RecordArrayValue> get(Key[] keys, Operation... ops) throws AerospikeException {
        return get(null, keys, ops);
    }

    Future<RecordArrayValue> get(BatchPolicy policy, Key[] keys, Operation... ops) throws AerospikeException;

    default Future<RecordArrayValue> getHeader(Key[] keys) throws AerospikeException {
        return getHeader(null, keys);
    }

    Future<RecordArrayValue> getHeader(BatchPolicy policy, Key[] keys) throws AerospikeException;

    default Future<ReadValue> operate(Key key, Operation... operations) throws AerospikeException {
        return operate(null, key, operations);
    }

    Future<ReadValue> operate(WritePolicy policy, Key key, Operation... operations) throws AerospikeException;

    default Future<BatchOperateListValue> operate(List<BatchRecord> records) throws AerospikeException {
        return operate(null, records);
    }

    Future<BatchOperateListValue> operate(BatchPolicy policy, List<BatchRecord> records) throws AerospikeException;

    default Future<BatchRecordArrayValue> operate(Key[] keys, Operation... ops) throws AerospikeException {
        return operate(null, null, keys, ops);
    }

    Future<BatchRecordArrayValue> operate(BatchPolicy batchPolicy, BatchWritePolicy writePolicy, Key[] keys, Operation... ops) throws AerospikeException;

    default Future<ExecuteValue> execute(Key key, String packageName, String functionName, Value... args) throws AerospikeException {
        return execute(null, key, packageName, functionName, args);
    }

    Future<ExecuteValue> execute(WritePolicy policy, Key key, String packageName, String functionName, Value... args) throws AerospikeException;

    default Future<BatchRecordArrayValue> execute(BatchUDFPolicy udfPolicy, Key[] keys, String packageName, String functionName, Value... args) throws AerospikeException {
        return execute(null, udfPolicy, keys, packageName, functionName, args);
    }

    Future<BatchRecordArrayValue> execute(BatchPolicy batchPolicy, BatchUDFPolicy udfPolicy, Key[] keys, String packageName, String functionName, Value... args) throws AerospikeException;

    default Future<AsyncIndexTask> createIndex(String namespace, String setName, String indexName, String binName, IndexType indexType, IndexCollectionType indexCollectionType) throws AerospikeException {
        return createIndex(null, namespace, setName, indexName, binName, indexType, indexCollectionType);
    }

    Future<AsyncIndexTask> createIndex(Policy policy, String namespace, String setName, String indexName, String binName, IndexType indexType, IndexCollectionType indexCollectionType) throws AerospikeException;

    default Future<AsyncIndexTask> dropIndex(String namespace, String setName, String indexName) throws AerospikeException {
        return dropIndex(null, namespace, setName, indexName);
    }

    Future<AsyncIndexTask> dropIndex(Policy policy, String namespace, String setName, String indexName) throws AerospikeException;

    default Future<Map<String, String>> info(Node node, String... commands) throws AerospikeException {
        return info(null, node, commands);
    }

    Future<Map<String, String>> info(InfoPolicy policy, Node node, String... commands) throws AerospikeException;
}
