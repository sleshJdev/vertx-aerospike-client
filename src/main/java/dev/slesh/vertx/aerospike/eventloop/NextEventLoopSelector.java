package dev.slesh.vertx.aerospike.eventloop;

import com.aerospike.client.async.EventLoop;
import com.aerospike.client.async.EventLoops;

import java.util.Objects;

public class NextEventLoopSelector implements EventLoopSelector {
    private final EventLoops delegate;

    public NextEventLoopSelector(final EventLoops delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public EventLoop select() {
        return delegate.next();
    }
}
