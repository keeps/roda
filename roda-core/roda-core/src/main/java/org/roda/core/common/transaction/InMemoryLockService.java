package org.roda.core.common.transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Component
public class InMemoryLockService {
  private final Map<String, LockData> locks = new ConcurrentHashMap<>();

  public void acquireLock(String key, String transactionId) {
    LockData lockData = locks.computeIfAbsent(key, k -> new LockData());

    ReentrantLock lock = lockData.getLock();
    lock.lock();
    lockData.incrementAcquisitionCount(transactionId);
  }

  public void releaseLock(String key, String transactionId) {
    LockData lockData = locks.get(key);
    if (lockData != null) {
      ReentrantLock lock = lockData.getLock();

      if (lockData.decrementAcquisitionCount(transactionId)) {
        lock.unlock();
        if (!lock.isLocked()) {
          locks.remove(key, lockData);
        }
      }
    }
  }

  private static class LockData {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, Integer> transactionLocks = new ConcurrentHashMap<>();

    public ReentrantLock getLock() {
      return lock;
    }

    public void incrementAcquisitionCount(String transactionId) {
      transactionLocks.put(transactionId, transactionLocks.getOrDefault(transactionId, 0) + 1);
    }

    public boolean decrementAcquisitionCount(String transactionId) {
      Integer count = transactionLocks.get(transactionId);
      if (count != null && count > 0) {
        transactionLocks.put(transactionId, count - 1);
        return count == 1;
      }
      return false;
    }
  }
}
