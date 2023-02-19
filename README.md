# Vertx Aerospike Client

* Provides future based implementation of async aerospike client (AsyncAerospikeClient -> VertxAerospikeClient)
* Extends the aerospike's event loops to be aware about vert'x context event loop and use whenever it's possible

### Code examples


Using old-fashion callback based client
```java
class OldFashionDemo {
    @Test
    void test(final Vertx vertx, final VertxTestContext context) {
        final var nettyEventLoops = new NettyEventLoops(vertx.nettyEventLoopGroup());
        final var clientPolicy = new ClientPolicy();
        clientPolicy.eventLoops = nettyEventLoops;
        final var aerospikeClient = new AerospikeClient(
                clientPolicy, aerospike.getHost(), aerospike.getFirstMappedPort());
        final var putFuture = Promise.<Key>promise();
        aerospikeClient.put(null, new WriteListener() {
            @Override
            public void onSuccess(Key key) {
                putFuture.complete(key);
            }

            @Override
            public void onFailure(AerospikeException exception) {
                putFuture.fail(exception);
            }
        }, null, new Key(namespace, set, "user1"), new Bin("age", 10));

        putFuture.future()
                .onSuccess(key -> Assertions.assertEquals("user1", key.userKey.toString()))
                .flatMap(key -> {
                    final var getFuture = Promise.<Record>promise();
                    aerospikeClient.get(null, new RecordListener() {
                        @Override
                        public void onSuccess(Key key, Record record) {
                            getFuture.complete(record);
                        }

                        @Override
                        public void onFailure(AerospikeException exception) {
                            getFuture.fail(exception);
                        }
                    }, null, key);

                    return getFuture.future();
                })
                .onSuccess(record -> context.verify(() -> {
                    Assertions.assertEquals(10L, record.bins.get("age"));
                }))
                .onComplete(context.succeedingThenComplete());
    }
}
```

Pros:
* It works
* No extra dependency

Cons:
* Verbosity
* Code complexity
* Error prone
* Lack of flexibility


Using future based client
```java
class NewApiDemo {
    @Test
    void test(final Vertx vertx, final VertxTestContext context) {
        final var nettyEventLoops = new NettyEventLoops(vertx.nettyEventLoopGroup());
        final var clientPolicy = new ClientPolicy();
        clientPolicy.eventLoops = nettyEventLoops;
        final var aerospikeClient = new AerospikeClient(
                clientPolicy, aerospike.getHost(), aerospike.getFirstMappedPort());
        final var eventLoopSelector = new ContextEventLoop(nettyEventLoops);
        final var promiseSelector = new ContextPromiseSelector();
        final var asyncAerospike = new VertxAerospikeClient(aerospikeClient, eventLoopSelector, promiseSelector);
        asyncAerospike.put(new Key(namespace, set, "user1"), new Bin("age", 10))
                .flatMap(asyncAerospike::get)
                .onSuccess(it -> context.verify(() -> {
                    Assertions.assertEquals("user1", it.key().userKey.toString());
                    Assertions.assertEquals(10L, it.record().bins.get("age"));
                }))
                .onComplete(context.succeedingThenComplete());
    }
}
```

Pros:
* Concise syntax driven by Future API
* Ability to provide different event loop selector
* Ability to reduce data concurrency when binding aerospike to vert.x event loop


Cons:
* Extra dependency
