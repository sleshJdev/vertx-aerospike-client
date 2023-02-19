package dev.slesh.vertx.aerospike.eventloop;

import com.aerospike.client.async.EventLoop;

import java.util.function.Supplier;

@FunctionalInterface
interface Fallback extends Supplier<EventLoop> {
}
