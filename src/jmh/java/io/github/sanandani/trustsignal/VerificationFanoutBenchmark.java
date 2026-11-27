package io.github.sanandani.trustsignal;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class VerificationFanoutBenchmark {
    @Param({"100", "1000"}) public int count;
    @Benchmark public int sequential() { int sum = 0; for (int i=0;i<count;i++) sum += i; return sum; }
    @Benchmark public long virtualThreads() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new java.util.ArrayList<Future<Integer>>();
            for(int i=0;i<count;i++){ int n=i; futures.add(executor.submit(() -> n)); }
            long sum=0; for(var future:futures) sum += future.get(); return sum;
        }
    }
}
