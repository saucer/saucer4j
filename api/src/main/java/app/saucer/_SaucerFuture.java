package app.saucer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sun.jna.Callback;

class _SaucerFuture<T> implements Future<T> {
    // We MUST keep a reference to all pending Callbacks to prevent use-after-free
    // issues due to GC.
    private static final Set<_SaucerFuture<?>> pendingFutures = Collections.synchronizedSet(new HashSet<>());

    final CompletableFuture<T> completable;
    final Callback mustKeepReferenceTo;

    _SaucerFuture(CompletableFuture<T> completable, Callback mustKeepReferenceTo) {
        this.completable = completable;
        this.mustKeepReferenceTo = mustKeepReferenceTo;

        pendingFutures.add(this);
        this.completable.whenComplete((_unused, _unused2) -> {
            pendingFutures.remove(this);
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.completable.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return this.completable.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.completable.get(timeout, unit);
    }

}
