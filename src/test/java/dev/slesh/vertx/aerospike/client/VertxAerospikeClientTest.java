package dev.slesh.vertx.aerospike.client;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.async.NettyEventLoops;
import dev.slesh.vertx.aerospike.eventloop.ContextEventLoop;
import dev.slesh.vertx.aerospike.future.ContextPromiseSelector;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Testcontainers;


@ExtendWith(VertxExtension.class)
@Testcontainers(disabledWithoutDocker = true)
class VertxAerospikeClientTest extends AerospikeITestSuit {
    @Test
    void name(final Vertx vertx, final VertxTestContext context) {
        final var nettyEventLoops = new NettyEventLoops(vertx.nettyEventLoopGroup());
        final var asyncAerospike = new VertxAerospikeClient(
                createClient(nettyEventLoops),
                new ContextEventLoop(nettyEventLoops),
                new ContextPromiseSelector());
        asyncAerospike.put(new Key(namespace, set, "user1"), new Bin("age", 10))
                .flatMap(asyncAerospike::get)
                .onSuccess(it -> context.verify(() -> {
                    Assertions.assertEquals("user1", it.key().userKey.toString());
                    Assertions.assertEquals(10L, it.record().bins.get("age"));
                }))
                .onComplete(context.succeedingThenComplete());
    }
}
