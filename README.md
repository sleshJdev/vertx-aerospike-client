# Vertx Aerospike Client

* Provides future based implementation of async aerospike client (AsyncAerospikeClient -> VertxAerospikeClient)
* Extends the aerospike's event loops to be aware about vert'x context event loop and use whenever it's possible


### Installation

#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>dev.slesh</groupId>
        <artifactId>vertx-aerospike-client</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.vertx</groupId>
        <artifactId>vertx-core</artifactId>
        <version>4.3.8</version>
    </dependency>
    <dependency>
        <groupId>com.aerospike</groupId>
        <artifactId>aerospike-client</artifactId>
        <version>6.0.0</version>
    </dependency>
</dependencies>
```

#### Gradle

```kotlin
repositories {
    mavenCentral()
}

implementation("dev.slesh:vertx-aerospike-client:1.0.0")
implementation("io.vertx:vertx-core:4.3.8")
implementation("com.aerospike:aerospike-client:6.0.0")
```

The library doesn't bring transitive dependencies like `vert.x-core` or `aeropspike-client` so you have to install them explicitly.

### Code examples


#### Using old-fashion callback based client
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


#### Using future based client
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
