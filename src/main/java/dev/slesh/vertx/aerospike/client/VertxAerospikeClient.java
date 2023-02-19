package dev.slesh.vertx.aerospike.client;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.BatchRead;
import com.aerospike.client.BatchRecord;
import com.aerospike.client.Bin;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.async.AsyncIndexTask;
import com.aerospike.client.cluster.Node;
import com.aerospike.client.listener.BatchListListener;
import com.aerospike.client.listener.BatchOperateListListener;
import com.aerospike.client.listener.BatchRecordArrayListener;
import com.aerospike.client.listener.DeleteListener;
import com.aerospike.client.listener.ExecuteListener;
import com.aerospike.client.listener.ExistsArrayListener;
import com.aerospike.client.listener.ExistsListener;
import com.aerospike.client.listener.IndexListener;
import com.aerospike.client.listener.InfoListener;
import com.aerospike.client.listener.RecordArrayListener;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.BatchDeletePolicy;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.BatchUDFPolicy;
import com.aerospike.client.policy.BatchWritePolicy;
import com.aerospike.client.policy.InfoPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.IndexCollectionType;
import com.aerospike.client.query.IndexType;
import dev.slesh.vertx.aerospike.client.result.BatchOperateListValue;
import dev.slesh.vertx.aerospike.client.result.BatchRecordArrayValue;
import dev.slesh.vertx.aerospike.client.result.DeleteValue;
import dev.slesh.vertx.aerospike.client.result.ExecuteValue;
import dev.slesh.vertx.aerospike.client.result.ExistsArrayValue;
import dev.slesh.vertx.aerospike.client.result.ReadValue;
import dev.slesh.vertx.aerospike.client.result.RecordArrayValue;
import dev.slesh.vertx.aerospike.eventloop.EventLoopSelector;
import dev.slesh.vertx.aerospike.future.PromiseSelector;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VertxAerospikeClient implements AsyncAerospikeClient {
    private final IAerospikeClient delegate;
    private final EventLoopSelector eventLoopSelector;
    private final PromiseSelector promiseFactory;

    public VertxAerospikeClient(final IAerospikeClient delegate,
                                final EventLoopSelector eventLoopSelector,
                                final PromiseSelector promiseSelector) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.eventLoopSelector = Objects.requireNonNull(eventLoopSelector, "eventLoopSelector");
        this.promiseFactory = Objects.requireNonNull(promiseSelector, "promiseFactory");
    }

    @Override
    public IAerospikeClient delegate() {
        return delegate;
    }

    @Override
    public Future<Key> put(WritePolicy policy, Key key, Bin... bins) throws AerospikeException {
        final Promise<Key> future = promiseFactory.select();
        delegate.put(eventLoopSelector.select(), toWriteListener(future), policy, key, bins);
        return future.future();
    }

    @Override
    public Future<Key> append(WritePolicy policy, Key key, Bin... bins) throws AerospikeException {
        final Promise<Key> promise = promiseFactory.select();
        delegate.append(eventLoopSelector.select(), toWriteListener(promise), policy, key, bins);
        return promise.future();
    }

    @Override
    public Future<Key> prepend(WritePolicy policy, Key key, Bin... bins) throws AerospikeException {
        final Promise<Key> promise = promiseFactory.select();
        delegate.prepend(eventLoopSelector.select(), toWriteListener(promise), policy, key, bins);
        return promise.future();
    }

    @Override
    public Future<Key> add(WritePolicy policy, Key key, Bin... bins) throws AerospikeException {
        final Promise<Key> promise = promiseFactory.select();
        delegate.add(eventLoopSelector.select(), toWriteListener(promise), policy, key, bins);
        return promise.future();
    }

    @Override
    public Future<DeleteValue> delete(WritePolicy policy, Key key) throws AerospikeException {
        final Promise<DeleteValue> promise = promiseFactory.select();
        delegate.delete(eventLoopSelector.select(), new DeleteListener() {
            @Override
            public void onSuccess(Key key1, boolean existed) {
                promise.complete(new DeleteValue(key1, existed));
            }

            @Override
            public void onFailure(AerospikeException exception) {
                promise.fail(exception);
            }
        }, policy, key);
        return promise.future();
    }

    @Override
    public Future<BatchRecordArrayValue> delete(BatchPolicy batchPolicy, BatchDeletePolicy deletePolicy, Key[] keys) throws AerospikeException {
        final Promise<BatchRecordArrayValue> promise = promiseFactory.select();
        delegate.delete(eventLoopSelector.select(), toBatchRecordArrayListener(promise), batchPolicy, deletePolicy, keys);
        return promise.future();
    }

    @Override
    public Future<Key> touch(WritePolicy policy, Key key) throws AerospikeException {
        final Promise<Key> promise = promiseFactory.select();
        delegate.touch(eventLoopSelector.select(), toWriteListener(promise), policy, key);
        return promise.future();
    }

    @Override
    public Future<Boolean> exists(Policy policy, Key key) throws AerospikeException {
        final Promise<Boolean> promise = promiseFactory.select();
        delegate.exists(eventLoopSelector.select(), new ExistsListener() {
            @Override
            public void onSuccess(Key key, boolean exists) {
                promise.complete(exists);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                promise.fail(exception);
            }
        }, policy, key);
        return promise.future();
    }

    @Override
    public Future<ExistsArrayValue> exists(BatchPolicy policy, Key[] keys) throws AerospikeException {
        final Promise<ExistsArrayValue> promise = promiseFactory.select();
        delegate.exists(eventLoopSelector.select(), new ExistsArrayListener() {
            @Override
            public void onSuccess(Key[] keys, boolean[] exists) {
                promise.complete(new ExistsArrayValue(keys, exists));
            }

            @Override
            public void onFailure(AerospikeException ae) {
                promise.fail(ae);
            }
        }, policy, keys);
        return promise.future();
    }

    @Override
    public Future<ReadValue> get(Policy policy, Key key) throws AerospikeException {
        final Promise<ReadValue> promise = promiseFactory.select();
        delegate.get(eventLoopSelector.select(), toRecordListener(promise), policy, key);
        return promise.future();
    }

    @Override
    public Future<ReadValue> get(Policy policy, Key key, String... binNames) throws AerospikeException {
        final Promise<ReadValue> promise = promiseFactory.select();
        delegate.get(eventLoopSelector.select(), toRecordListener(promise), policy, key, binNames);
        return promise.future();
    }

    @Override
    public Future<ReadValue> getHeader(Policy policy, Key key) throws AerospikeException {
        final Promise<ReadValue> promise = promiseFactory.select();
        delegate.getHeader(eventLoopSelector.select(), toRecordListener(promise), policy, key);
        return promise.future();
    }

    @Override
    public Future<List<BatchRead>> get(BatchPolicy policy, List<BatchRead> records) throws AerospikeException {
        final Promise<List<BatchRead>> promise = promiseFactory.select();
        delegate.get(eventLoopSelector.select(), new BatchListListener() {
            @Override
            public void onSuccess(List<BatchRead> records) {
                promise.complete(records);
            }

            @Override
            public void onFailure(AerospikeException ae) {
                promise.fail(ae);
            }
        }, policy, records);
        return promise.future();
    }

    @Override
    public Future<RecordArrayValue> get(BatchPolicy policy, Key[] keys) throws AerospikeException {
        final Promise<RecordArrayValue> promise = promiseFactory.select();
        delegate.get(eventLoopSelector.select(), toRecordArrayListener(promise), policy, keys);
        return promise.future();
    }

    @Override
    public Future<RecordArrayValue> get(BatchPolicy policy, Key[] keys, String... binNames) throws AerospikeException {
        final Promise<RecordArrayValue> promise = promiseFactory.select();
        delegate.get(eventLoopSelector.select(), toRecordArrayListener(promise), policy, keys, binNames);
        return promise.future();
    }

    @Override
    public Future<RecordArrayValue> get(BatchPolicy policy, Key[] keys, Operation... ops) throws AerospikeException {
        final Promise<RecordArrayValue> promise = promiseFactory.select();
        delegate.get(eventLoopSelector.select(), toRecordArrayListener(promise), policy, keys, ops);
        return promise.future();
    }

    @Override
    public Future<RecordArrayValue> getHeader(BatchPolicy policy, Key[] keys) throws AerospikeException {
        final Promise<RecordArrayValue> promise = promiseFactory.select();
        delegate.getHeader(eventLoopSelector.select(), toRecordArrayListener(promise), policy, keys);
        return promise.future();
    }

    @Override
    public Future<ReadValue> operate(WritePolicy policy, Key key, Operation... operations) throws AerospikeException {
        final Promise<ReadValue> promise = promiseFactory.select();
        delegate.operate(eventLoopSelector.select(), toRecordListener(promise), policy, key, operations);
        return promise.future();
    }

    @Override
    public Future<BatchOperateListValue> operate(BatchPolicy policy, List<BatchRecord> records) throws AerospikeException {
        final Promise<BatchOperateListValue> promise = promiseFactory.select();
        delegate.operate(eventLoopSelector.select(), new BatchOperateListListener() {
            @Override
            public void onSuccess(List<BatchRecord> records, boolean status) {
                promise.complete(new BatchOperateListValue(records, status));
            }

            @Override
            public void onFailure(AerospikeException ae) {
                promise.fail(ae);
            }
        }, policy, records);
        return promise.future();
    }

    @Override
    public Future<BatchRecordArrayValue> operate(BatchPolicy batchPolicy, BatchWritePolicy writePolicy, Key[] keys, Operation... ops) throws AerospikeException {
        final Promise<BatchRecordArrayValue> promise = promiseFactory.select();
        delegate.operate(eventLoopSelector.select(), toBatchRecordArrayListener(promise), batchPolicy, writePolicy, keys, ops);
        return promise.future();
    }

    @Override
    public Future<ExecuteValue> execute(WritePolicy policy, Key key, String packageName, String functionName, Value... args) throws AerospikeException {
        final Promise<ExecuteValue> promise = promiseFactory.select();
        delegate.execute(eventLoopSelector.select(), new ExecuteListener() {
            @Override
            public void onSuccess(Key key, Object obj) {
                promise.complete(new ExecuteValue(key, obj));
            }

            @Override
            public void onFailure(AerospikeException exception) {
                promise.fail(exception);
            }
        }, policy, key, packageName, functionName, args);
        return promise.future();
    }

    @Override
    public Future<BatchRecordArrayValue> execute(BatchPolicy batchPolicy, BatchUDFPolicy udfPolicy, Key[] keys, String packageName, String functionName, Value... args) throws AerospikeException {
        final Promise<BatchRecordArrayValue> promise = promiseFactory.select();
        delegate.execute(eventLoopSelector.select(), toBatchRecordArrayListener(promise), batchPolicy, udfPolicy, keys, packageName, functionName, args);
        return promise.future();
    }

    @Override
    public Future<AsyncIndexTask> createIndex(Policy policy, String namespace, String setName, String indexName, String binName, IndexType indexType, IndexCollectionType indexCollectionType) throws AerospikeException {
        final Promise<AsyncIndexTask> promise = promiseFactory.select();
        delegate.createIndex(eventLoopSelector.select(), toIndexListener(promise), policy, namespace, setName, indexName, binName, indexType, indexCollectionType);
        return promise.future();
    }

    @Override
    public Future<AsyncIndexTask> dropIndex(Policy policy, String namespace, String setName, String indexName) throws AerospikeException {
        final Promise<AsyncIndexTask> promise = promiseFactory.select();
        delegate.dropIndex(eventLoopSelector.select(), toIndexListener(promise), policy, namespace, setName, indexName);
        return promise.future();
    }

    @Override
    public Future<Map<String, String>> info(InfoPolicy policy, Node node, String... commands) throws AerospikeException {
        final Promise<Map<String, String>> promise = promiseFactory.select();
        delegate.info(eventLoopSelector.select(), new InfoListener() {
            @Override
            public void onSuccess(Map<String, String> map) {
                promise.complete(map);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                promise.fail(exception);
            }
        }, policy, node, commands);
        return promise.future();
    }

    private static WriteListener toWriteListener(final Promise<Key> future) {
        return new WriteListener() {
            @Override
            public void onSuccess(Key key) {
                future.complete(key);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                future.fail(exception);
            }
        };
    }

    private static RecordListener toRecordListener(final Promise<ReadValue> promise) {
        return new RecordListener() {
            @Override
            public void onSuccess(Key key, Record record) {
                promise.complete(new ReadValue(key, record));
            }

            @Override
            public void onFailure(AerospikeException exception) {
                promise.fail(exception);
            }
        };
    }

    private static RecordArrayListener toRecordArrayListener(final Promise<RecordArrayValue> promise) {
        return new RecordArrayListener() {
            @Override
            public void onSuccess(Key[] keys, Record[] records) {
                promise.complete(new RecordArrayValue(keys, records));
            }

            @Override
            public void onFailure(AerospikeException ae) {
                promise.fail(ae);
            }
        };
    }

    private static BatchRecordArrayListener toBatchRecordArrayListener(final Promise<BatchRecordArrayValue> promise) {
        return new BatchRecordArrayListener() {
            @Override
            public void onSuccess(BatchRecord[] records, boolean status) {
                promise.complete(new BatchRecordArrayValue(records, status));
            }

            @Override
            public void onFailure(BatchRecord[] records, AerospikeException ae) {
                promise.fail(ae);
            }
        };
    }

    private static IndexListener toIndexListener(Promise<AsyncIndexTask> promise) {
        return new IndexListener() {
            @Override
            public void onSuccess(AsyncIndexTask indexTask) {
                promise.complete(indexTask);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                promise.fail(exception);
            }
        };
    }
}
