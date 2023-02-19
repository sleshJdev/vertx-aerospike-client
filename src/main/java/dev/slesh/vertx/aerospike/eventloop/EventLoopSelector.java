package dev.slesh.vertx.aerospike.eventloop;

import com.aerospike.client.async.EventLoop;

public interface EventLoopSelector {
    EventLoop select();
}
