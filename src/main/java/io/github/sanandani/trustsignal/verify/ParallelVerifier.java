package io.github.sanandani.trustsignal.verify;

import io.github.sanandani.trustsignal.model.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded virtual-thread fan-out with a single deadline measured from invocation. */
public final class ParallelVerifier {
    private final SignalVerifier verifier;
    private final Semaphore permits;
    private final Duration timeout;
    public ParallelVerifier(SignalVerifier verifier, int maxInFlight, Duration timeout) {
        if (maxInFlight < 1 || timeout.isNegative() || timeout.isZero()) throw new IllegalArgumentException();
        this.verifier = verifier; this.permits = new Semaphore(maxInFlight); this.timeout = timeout;
    }
    public List<SignalOutcome> verifyAll(List<SignedClaim> claims) {
        long deadline = System.nanoTime() + timeout.toNanos();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<SignalOutcome>> futures = new ArrayList<>();
            for (SignedClaim claim : claims) {
                futures.add(executor.submit(() -> {
                    if (!permits.tryAcquire()) return new SignalOutcome.Unverifiable(SignalOutcome.Reason.TIMEOUT);
                    try { return verifier.verify(claim); } finally { permits.release(); }
                }));
            }
            List<SignalOutcome> outcomes = new ArrayList<>();
            for (Future<SignalOutcome> future : futures) {
                long remaining = deadline - System.nanoTime();
                try { outcomes.add(remaining <= 0 ? timeout(future) : future.get(remaining, TimeUnit.NANOSECONDS)); }
                catch (TimeoutException e) { future.cancel(true); outcomes.add(new SignalOutcome.Unverifiable(SignalOutcome.Reason.TIMEOUT)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); future.cancel(true); outcomes.add(new SignalOutcome.Unverifiable(SignalOutcome.Reason.TIMEOUT)); }
                catch (ExecutionException e) { outcomes.add(new SignalOutcome.Unverifiable(SignalOutcome.Reason.MISSING)); }
            }
            return List.copyOf(outcomes);
        }
    }
    private static SignalOutcome timeout(Future<?> f) { f.cancel(true); return new SignalOutcome.Unverifiable(SignalOutcome.Reason.TIMEOUT); }
}
