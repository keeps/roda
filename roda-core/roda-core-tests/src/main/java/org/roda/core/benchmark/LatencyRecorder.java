/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.benchmark;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe recorder of per-operation latencies, used by
 * {@link JobDatabaseBenchmark} to time each model service call.
 *
 * @author RODA Development Team
 */
public class LatencyRecorder {
  private static final double NANOS_PER_MILLI = 1_000_000d;

  private final Map<String, Queue<Long>> samples = new ConcurrentHashMap<>();

  @FunctionalInterface
  public interface ThrowingRunnable {
    void run() throws Exception;
  }

  public <T> T time(String operation, Callable<T> callable) throws Exception {
    long start = System.nanoTime();
    try {
      return callable.call();
    } finally {
      record(operation, System.nanoTime() - start);
    }
  }

  public void time(String operation, ThrowingRunnable runnable) throws Exception {
    long start = System.nanoTime();
    try {
      runnable.run();
    } finally {
      record(operation, System.nanoTime() - start);
    }
  }

  public void record(String operation, long nanos) {
    samples.computeIfAbsent(operation, k -> new ConcurrentLinkedQueue<>()).add(nanos);
  }

  public long totalCount() {
    return samples.values().stream().mapToLong(Queue::size).sum();
  }

  /**
   * @return for each operation (sorted by name): count, total, mean, p50, p95,
   *         p99 and max, in milliseconds
   */
  public Map<String, Map<String, Object>> summarize() {
    Map<String, Map<String, Object>> ret = new TreeMap<>();
    samples.forEach((operation, queue) -> {
      long[] values = queue.stream().mapToLong(Long::longValue).toArray();
      Arrays.sort(values);
      long total = Arrays.stream(values).sum();

      Map<String, Object> stats = new LinkedHashMap<>();
      stats.put("count", values.length);
      stats.put("totalMs", toMillis(total));
      stats.put("meanMs", values.length == 0 ? 0d : toMillis(total / values.length));
      stats.put("p50Ms", toMillis(percentile(values, 50)));
      stats.put("p95Ms", toMillis(percentile(values, 95)));
      stats.put("p99Ms", toMillis(percentile(values, 99)));
      stats.put("maxMs", values.length == 0 ? 0d : toMillis(values[values.length - 1]));
      ret.put(operation, stats);
    });
    return ret;
  }

  private static long percentile(long[] sorted, int percentile) {
    if (sorted.length == 0) {
      return 0;
    }
    int index = (int) Math.ceil(percentile / 100d * sorted.length) - 1;
    return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
  }

  private static double toMillis(long nanos) {
    return Math.round(nanos / NANOS_PER_MILLI * 1000d) / 1000d;
  }
}
