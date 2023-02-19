package dev.slesh.vertx.aerospike.eventloop;

import com.aerospike.client.async.EventLoop;
import com.aerospike.client.async.NettyEventLoops;
import io.vertx.core.impl.ContextInternal;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * It's important component in aerospike-client - vert.x integration.
 * The default aerospike client is not aware of vert.x event loop and run all the tasks
 * on events loops selected by round-robin. This class makes sure that aerospike will
 * schedule next task on the event loop which belongs to thread initiated the aerospike call:
 * <p>
 * 1. {@code aerospike.get(null, listener, null, key, null);}
 * <p>
 * 2. This where {@code ContextEventLoop} comes into play
 * <pre>{@code if (eventLoop == null) {
 *     eventLoop = cluster.eventLoops.next();
 * }}</pre>
 * <p>
 * It will resolve event loop using {@link ContextInternal#current()} and {@link NettyEventLoops#get(int)} and return it
 * allowing to schedule all aerospike tasks related to same envelope request processing to the same event loop.
 * <p>
 * The benefit - we don't need to care about data concurrency.
 */
public class ContextEventLoop implements EventLoopSelector {
    private final NettyEventLoops eventLoops;
    private final Fallback fallback;

    public ContextEventLoop(final NettyEventLoops eventLoops) {
        this(eventLoops, eventLoops::next);
    }

    public ContextEventLoop(final NettyEventLoops eventLoops,
                            final Fallback fallback) {
        this.eventLoops = Objects.requireNonNull(eventLoops, "eventLoops");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public EventLoop select() {
        final ContextInternal ctx = ContextInternal.current();
        final EventLoop eventLoop;
        if (ctx != null
                && ctx.isEventLoopContext()
                && (eventLoop = eventLoops.get(ctx.nettyEventLoop())) != null) {
            return eventLoop;
        }
        return fallback.get();
    }
}
